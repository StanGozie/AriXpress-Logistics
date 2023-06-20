package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.configuration.security.CustomUserDetailService;
import com.example.demo.configuration.security.JwtUtils;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.CustomerType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentType;
import com.example.demo.enums.Role;
import com.example.demo.exceptions.AccountAlreadyActivatedException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Bike;
import com.example.demo.model.Orders;
import com.example.demo.model.User;
import com.example.demo.repository.BikeRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StaffService;
import com.example.demo.utils.AppUtil;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffServiceImplementation implements StaffService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final BikeRepository bikeRepository;
    private final AppUtil appUtil;
    private final OrderRepository orderRepository;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    @Override
    public ResponseEntity<ApiResponse> signUp(SignUpDto signUpDto) throws ValidationException {
        if (!appUtil.isValidEmail(signUpDto.getEmail()))
            throw new ValidationException("Email is invalid");

        Boolean isUserExist = userRepository.existsByEmail(signUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

        if(!(signUpDto.getConfirmPassword().equals(signUpDto.getPassword())))
            throw new InputMismatchException("Confirm Password and Password do not match!");

        User user = new User();
        user.setFirstName(signUpDto.getFirstName());
        user.setLastName(signUpDto.getLastName());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setRole((Role.ROLE_ADMIN));
        String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
        user.setConfirmationToken(token);
        userRepository.save(user);

        String URL = "http://localhost:8080/api/v1/auth/verify-link/?token=" + token;
        String link = "<h3>Hello "  + signUpDto.getFirstName()  +"<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";

        emailService.sendEmail(signUpDto.getEmail(),"AriXpress: Verify Your Account", link);

        return ResponseEntity.ok(new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null));
    }

    @Override
    public ResponseEntity<ApiResponse> updateStaffInformation(StaffRelevantDetailsDto staffRelevantDetailsDto) {

        User user = appUtil.getLoggedInUser();
        if(user.getRole().equals(Role.ROLE_STAFF) || user.getRole().equals(Role.ROLE_SUPER_ADMIN) || user.getRole().equals(Role.ROLE_RIDER))

        user.setState(staffRelevantDetailsDto.getStateOfOrigin());
        user.setNextOfKinFirstName(staffRelevantDetailsDto.getNextOfKinFirstName());
        user.setNextOfKinLastName(staffRelevantDetailsDto.getNextOfKinLastName());
        user.setNextOfKinAddress(staffRelevantDetailsDto.getNextOfKinAddress());
        user.setNextOfKinPhoneNumber(staffRelevantDetailsDto.getNextOfKinPhoneNumber());
        userRepository.save(user);
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> completeRegistration(CompleteRegistrationDto completeRegistrationDto) {
        Optional<User> existingUser = userRepository.findByConfirmationToken(completeRegistrationDto.getToken());
        if (existingUser.isPresent()) {
            if (existingUser.get().isActive()) {
                throw new AccountAlreadyActivatedException("This account is already activated. Pls login");
            }
            existingUser.get().setDob(completeRegistrationDto.getDob());
            existingUser.get().setAddress(completeRegistrationDto.getAddress());
            existingUser.get().setPhoneNumber(completeRegistrationDto.getPhoneNumber());
            existingUser.get().setActive(true);
            userRepository.save(existingUser.get());
            return ResponseEntity.ok(new ApiResponse<>("Successful", "Registration completed", null));
        }
        return ResponseEntity.ok(new ApiResponse<>("Failed", "This user does not exist. Kindly sign up.", null));
    }

    @Override
    public ResponseEntity<String> login(LoginDto loginDto) {
        User users = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
        if(!users.isActive()){
            throw new ValidationException("User Not Active. Kindly complete your registration.");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        UserDetails user = customUserDetailService.loadUserByUsername(loginDto.getEmail());
        if(user != null) {
            return ResponseEntity.ok(jwtUtils.generateToken(user));
        }
        return ResponseEntity.status(400).body("Some error occurred");
    }

    @Override
    public ResponseEntity<ApiResponse> forgotPassword(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()) {
            throw new UserNotFoundException("User does not exist");
        }
        String token = jwtUtils.resetPasswordToken(email);
        User user1 = new User();
        user1.setConfirmationToken(token);
        userRepository.save(user1);

        String URL = "http://localhost:8080/api/v1/auth/reset-password/?token=" + token;
        String link = "<h3>Hello " +"<br> Click the link below to reset your password <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail(email,"AriXpress: Reset your password", link);
        return ResponseEntity.ok(new ApiResponse<>("Sent", "Check your email to reset your password", null));
    }


    @Override
    public ResponseEntity<ApiResponse> resetPassword(ResetPasswordDto resetPasswordDto) {
        Optional<User> user = Optional.ofNullable(userRepository.findByConfirmationToken(resetPasswordDto.getConfirmationToken())
                .orElseThrow(() -> new ValidationException("Token is incorrect or User does not exist!")));

        if(!resetPasswordDto.getConfirmNewPassword().equals(passwordEncoder.encode(resetPasswordDto.getNewPassword()))){
            throw new InputMismatchException("Passwords do not match!");
        }
        user.get().setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        userRepository.save(user.get());

        return ResponseEntity.ok(new ApiResponse<>("Success", "Password reset successful.", null));
    }

    @Override
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto) {
        User user = appUtil.getLoggedInUser();
        if(!(changePasswordDto.getConfirmNewPassword().equals(changePasswordDto.getNewPassword()))){
            throw new InputMismatchException("Confirm password and Password do not match!");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password change successful", null));
    }


    @Override
    public ResponseEntity<Document> dispatchOrder(Long id, HttpServletResponse response, DispatchOrderDto dispatchOrderDto) throws IOException {

        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_ADMIN)) || user.getRole().equals(Role.ROLE_STAFF)){
            throw new ValidationException("You are not permitted to perform this operation");
        }
        Optional<Orders> order = Optional.ofNullable(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order with the id " + id + " does not exist")));

        order.get().setRiderPhoneNumber(dispatchOrderDto.getRiderPhoneNumber());
        order.get().setRiderName(dispatchOrderDto.getRiderName());
        order.get().setRiderId(dispatchOrderDto.getRiderId());
        order.get().setOrderStatus(OrderStatus.INPROGRESS);
        order.get().setPrice(Double.parseDouble("# "+600.00));
        orderRepository.save(order.get());

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss");
        String currentDateTime = dateFormat.format(new Date());

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font font = FontFactory.getFont(FontFactory.COURIER_BOLD);
        font.setColor(Color.BLACK);
        font.setSize(25);
        Paragraph p1 = new Paragraph("New Order Details Receipt", font);
        p1.setAlignment(Paragraph.ALIGN_CENTER);

        Paragraph p2 = new Paragraph();
        p2.setAlignment(Paragraph.ALIGN_LEFT);
        p2.setFont(FontFactory.getFont(FontFactory.COURIER, 16, Color.black));
        p2.setMultipliedLeading(2);

        if(order.get().getPaymentType().equals(PaymentType.BankTransfer)) {

            p2.add("Client name: " + order.get().getCustomerFirstName() + order.get().getCustomerLastName() + '\n' +
                    "Pick-up address: " + order.get().getPickUpAddress() + '\n' +
                    "Receiver name: " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Date: " + order.get().getCreatedAt() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Current Date: " + currentDateTime + '\n' +
                    "Account Name: " + "AriXpress Delivery Nigeria Limited " + '\n' +
                    "Account Number: " + "0044232307 " + '\n' +
                    "Bank Name: " + "GTB");
        } else {
            p2.add("Client name: " + order.get().getCustomerFirstName() + order.get().getCustomerLastName() + '\n' +
                    "Pick-up address: " + order.get().getPickUpAddress() + '\n' +
                    "Receiver name: " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Date: " + order.get().getCreatedAt() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Current Date: " + currentDateTime);
        }
        document.add(p1);
        document.add(p2);
        document.close();

        String URL = "http://localhost:8080/api/v1/auth/order-dispatch/?order=" + document;
        String link = "<h3>Hello " +"<br> Here is a pdf of your order. Kindly download to see the delivery agent details. <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail(order.get().getEmail(),"AriXpress: A new order", link);

        return ResponseEntity.ok(document);
    }

    @Override
    public ResponseEntity<ApiResponse> registerABike(RegisterBikeDto registerBikeDto) {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_ADMIN) || user.getRole().equals(Role.ROLE_STAFF))){
            throw new ValidationException("You are not authorised to perform this operation.");
        }
        Optional<Bike> bike = bikeRepository.findByBikeNumber(registerBikeDto.getBikeNumber());
        if(bike.isEmpty()) {

            Bike bike1 = new Bike();
            bike1.setBikeNumber(registerBikeDto.getBikeNumber());
            bike1.setPrice(registerBikeDto.getPrice());
            bike1.setMake(registerBikeDto.getMake());
            bike1.setImages(registerBikeDto.getImages());
            bike1.setRiderName(registerBikeDto.getRiderName());
            bikeRepository.save(bike1);
            return ResponseEntity.ok(new ApiResponse<>("Success", "Bike registered", null));
        }
        else throw new ResourceNotFoundException("There is no bike with this Id");
    }

    @Override
    public ApiResponse registerARider(RegisterRiderDto registerRiderDto) {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_ADMIN)) || user.getRole().equals(Role.ROLE_STAFF)){
            throw new ValidationException("You are not authorised to perform this operation.");
        }

        Long staffNumber = appUtil.generateRandomCode();
        User user1 = new User();
        user1.setRole(Role.ROLE_RIDER);
        user1.setFirstName(registerRiderDto.getFirstName());
        user1.setLastName(registerRiderDto.getLastName());
        user1.setAddress(registerRiderDto.getAddress());
        user1.setStaffId(Long.valueOf("RD-"+ staffNumber));
        user1.setPhoneNumber(registerRiderDto.getPhoneNumber());
        user1.setPassword(passwordEncoder.encode(registerRiderDto.getPassword()));
        userRepository.save(user1);

        return new ApiResponse<>("Success", "Rider registration successful", "Your Rider ID is: "+ user1.getStaffId());
    }

    @Override
    public ApiResponse changeRoleToStaff(String email) {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_ADMIN))){
            throw new ValidationException("You are not authorised to perform this operation");
        }

        Optional<User> user1 = userRepository.findByEmail(email);
        if(user1.isEmpty())
            throw new UserNotFoundException("There is no user with this email!");

        if(user1.get().getRole().equals(Role.ROLE_STAFF))
            throw new UnsupportedOperationException("This user is already a staff!");

        if(user1.get().getRole().equals(Role.ROLE_SUPER_ADMIN))
            throw new UnsupportedOperationException("This user is a Super Admin!");

        Long staffNumber = appUtil.generateRandomCode();

        if(user1.get().getRole().equals(Role.ROLE_CUSTOMER))
            user1.get().setRole(Role.ROLE_STAFF);
            user1.get().setStaffId(Long.valueOf("AXP-"+staffNumber));
            userRepository.save(user1.get());

        userRepository.save(user1.get());
        return new ApiResponse<>("Success", "New admin created", "Your staff number is: "+ user1.get().getStaffId());
    }

    @Override
    public ApiResponse assignRiderToBike(AssignRiderToBikeDto assignRiderToBikeDto) {

        User user = appUtil.getLoggedInUser();
        if (!user.getRole().equals(Role.ROLE_ADMIN) || user.getRole().equals(Role.ROLE_STAFF)) {
            throw new UnsupportedOperationException("You are not authorised to perform this operation");
        }
        User user1 = userRepository.findByEmail(assignRiderToBikeDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("This user does not exist"));

        if (!user1.getRole().equals(Role.ROLE_RIDER))
            throw new UnsupportedOperationException("You cannot perform this operation on this user");

        Optional<Bike> bike = bikeRepository.findByBikeNumber(assignRiderToBikeDto.getBikeNumber());
        if (bike.isEmpty())
            throw new ResourceNotFoundException("There is no bike with this number!");

        bike.get().setRiderName(user1.getFirstName() + user1.getLastName());
        bike.get().setRiderPhoneNumber(user1.getPhoneNumber());
        bike.get().setRiderAddress(user1.getAddress());
        bikeRepository.save(bike.get());

        return new ApiResponse<>("Success", "Successful", "Bike with the number " + assignRiderToBikeDto.getBikeNumber() + " has been assigned to " + bike.get().getRiderName());
    }

    @Override
    public Optional<Orders> viewAnOrderById(Long id) {
        User user = appUtil.getLoggedInUser();
        if(!user.getRole().equals(Role.ROLE_ADMIN) || user.getRole().equals(Role.ROLE_RIDER))
            throw new UnsupportedOperationException("You are not authorised to perform this operation.");

        Optional<Orders> order = Optional.ofNullable(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no order with such Id")));
        return order;
    }

    @Override
    public List<Orders> viewAllOrdersByStatus(OrderStatus orderStatus) {
        User user = appUtil.getLoggedInUser();
        if(!user.getRole().equals(Role.ROLE_ADMIN) || user.getRole().equals(Role.ROLE_STAFF))
            throw new UnsupportedOperationException("You are not authorised to perform this operation.");

        List<Orders> ordersList = new ArrayList<>();
        ordersList.add((Orders) orderRepository.findByOrderStatus(orderStatus));

        return ordersList;
    }

    @Override
    public Integer countRidesPerRider(Long staffId) {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_ADMIN) || user.getRole().equals(Role.ROLE_RIDER) || user.getRole().equals(Role.ROLE_SUPER_ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation.");
        }
        List<Orders> ordersList = new ArrayList<>();
        ordersList.add((Orders) orderRepository.findByRiderId(staffId));
        Integer x = ordersList.size();
        return x;
    }

    @Override
    public Optional<User> viewStaffDetails(Long staffId) {
        User user = appUtil.getLoggedInUser();

        if(!(user.getRole().equals(Role.ROLE_ADMIN) || user.getRole().equals(Role.ROLE_SUPER_ADMIN)))
            throw new ValidationException("You are not authorised to perform this operation!");

        Optional<User> user1 = userRepository.findByStaffId(staffId);
        if(user1.isPresent()) {
            return user1;
        }else {
            throw new ValidationException("Staff number incorrect!");
        }
    }

    @Override
    public ResponseEntity<ApiResponse> deleteStaff(Long staffId) {
        User user = appUtil.getLoggedInUser();
        if (user.getRole().equals(Role.ROLE_SUPER_ADMIN))
            throw new ValidationException("You are not authorised to perform this operation!");

        Optional<User> user1 = userRepository.findByStaffId(staffId);
        if (user1.isEmpty()) {
            throw new ValidationException("Staff number incorrect!");
        }
        userRepository.delete(user1.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Staff has deleted!", null));
    }
    @Override
    public ResponseEntity<ApiResponse> createAdmin(Long staffId) {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_SUPER_ADMIN))){
            throw new ValidationException("You are not authorised to perform this operation");
        }

        Optional<User> user1 = userRepository.findByStaffId(staffId);
        if(user1.isEmpty())
            throw new UserNotFoundException("There is no staff with this Id!");

        if(user1.get().getRole().equals(Role.ROLE_ADMIN))
            throw new UnsupportedOperationException("This user is already an Admin!");

        if(user1.get().getRole().equals(Role.ROLE_SUPER_ADMIN))
            throw new UnsupportedOperationException("This user is a Super Admin!");

        if(user1.get().getRole().equals(Role.ROLE_STAFF))
            user1.get().setRole(Role.ROLE_ADMIN);
        userRepository.save(user1.get());

        userRepository.save(user1.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "New admin created", null));
    }

    @Override
    public List<Orders> viewAllOrders() {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_SUPER_ADMIN) || user.getRole().equals(Role.ROLE_ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        List<Orders> ordersList = new ArrayList<>();
                ordersList.add((Orders) orderRepository.findAll());
        return ordersList;
    }

    @Override
    public List<Optional<Orders>> clientWeeklyOrderSummary(Long clientId, WeeklyOrderSummaryDto weeklyOrderSummaryDto) throws Exception {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_ADMIN) || user.getRole().equals(Role.ROLE_STAFF)))
            throw new ValidationException("You are not authorised to perform this operation");

        List<Optional<Orders>> ordersList = new ArrayList<>();
        Optional<Orders> orders1 = orderRepository.findByClientCode(clientId);
        if(orders1.isPresent()){
            if(orders1.get().getCreatedAt().isEqual(weeklyOrderSummaryDto.getStartDate())||
                    orders1.get().getCreatedAt().isAfter(weeklyOrderSummaryDto.getStartDate()) ||
                    orders1.get().getCreatedAt().isBefore(weeklyOrderSummaryDto.getEndDate()) ||
                    orders1.get().getCreatedAt().isEqual(weeklyOrderSummaryDto.getEndDate()))
                ordersList.add(orders1);
        } else {
            throw new Exception("Some error occurred");
        }
        return ordersList;
    }

    @Override
    public ResponseEntity<ApiResponse> viewAllOrdersToday(Date date) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> viewAllOrdersByMonth(String month) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> viewAllOrdersByWeek() {
        return null;
    }



}
