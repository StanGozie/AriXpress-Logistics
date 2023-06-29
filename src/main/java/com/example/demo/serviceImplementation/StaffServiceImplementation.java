package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.configuration.security.CustomUserDetailService;
import com.example.demo.configuration.security.JwtUtils;
import com.example.demo.dto.request.AssignRiderToBikeDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.DispatchOrderDto;
import com.example.demo.dto.request.ForgotPasswordDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.OrdersHistoryDto;
import com.example.demo.dto.request.RegisterBikeDto;
import com.example.demo.dto.request.RegisterRiderDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.request.StaffRelevantDetailsDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentType;
import com.example.demo.enums.RiderStatus;
import com.example.demo.enums.Role;
import com.example.demo.exceptions.AccountAlreadyActivatedException;
import com.example.demo.exceptions.ResourceAlreadyExistsException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.RiderUnavailableException;
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
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
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
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

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


    @Override  // tested and is working fine
    public ResponseEntity<ApiResponse> signUp(SignUpDto signUpDto) throws ValidationException {
        if (!appUtil.isValidEmail(signUpDto.getEmail()))
            throw new ValidationException("Email is invalid");

        Boolean isUserExist = userRepository.existsByEmail(signUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

        if(!appUtil.isValidPassword(signUpDto.getPassword()))
            throw new ValidationException("Password MUST be between 6 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character like #,@,%,$");

        if (!(signUpDto.getConfirmPassword().equals(signUpDto.getPassword())))
            throw new InputMismatchException("Confirm Password and Password do not match!");

        Long staffId = appUtil.generateRandomCode();

        User user = new User();
        user.setFirstName(signUpDto.getFirstName());
        user.setLastName(signUpDto.getLastName());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setRole(Role.STAFF);
        if(signUpDto.getEmail().equals("chigoziestanleyenyoghasi@gmail.com") || signUpDto.getEmail().equals("chigozieenyoghasi@yahoo.com")){
            user.setRole(Role.SUPER_ADMIN);}
        user.setStaffId(staffId);
        String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
        user.setConfirmationToken(token);
        userRepository.save(user);

        String URL = "http://localhost:8080/api/v1/auth/verify-link/?token=" + token;
        String link = "<h3>Hello " + signUpDto.getFirstName() + "<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";

        emailService.sendEmail(signUpDto.getEmail(), "AriXpress: Verify Your Account", link);

        return ResponseEntity.ok(new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null));
    }

    @Override  // tested and is working fine
    public ResponseEntity<ApiResponse> updateStaffInformation(StaffRelevantDetailsDto staffRelevantDetailsDto) {

        User user = appUtil.getLoggedInUser();
        if (user.getRole().equals(Role.STAFF) || user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN))

        user.setState(staffRelevantDetailsDto.getStateOfOrigin());
        user.setNextOfKinFirstName(staffRelevantDetailsDto.getNextOfKinFirstName());
        user.setNextOfKinLastName(staffRelevantDetailsDto.getNextOfKinLastName());
        user.setNextOfKinAddress(staffRelevantDetailsDto.getNextOfKinAddress());
        user.setNextOfKinPhoneNumber(staffRelevantDetailsDto.getNextOfKinPhoneNumber());
        user.setStateOfOrigin(staffRelevantDetailsDto.getStateOfOrigin());
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse("Successful", "Staff information update successful", null));
    }

    @Override // tested and is working fine
    public ResponseEntity<ApiResponse> completeRegistration(CompleteRegistrationDto completeRegistrationDto) {
        Optional<User> existingUser = userRepository.findByConfirmationToken(completeRegistrationDto.getToken());

        if (existingUser.isPresent()) {
            if (existingUser.get().isActive()) {
                throw new AccountAlreadyActivatedException("This account is already activated. Pls login");
            }
            existingUser.get().setDob(completeRegistrationDto.getDob());
            existingUser.get().setAddress(completeRegistrationDto.getAddress());
            existingUser.get().setPhoneNumber(completeRegistrationDto.getPhoneNumber());
            existingUser.get().setGender(completeRegistrationDto.getGender());
            existingUser.get().setActive(true);
            userRepository.save(existingUser.get());
            return ResponseEntity.ok(new ApiResponse<>("Successful", "Registration completed", "Your unique staff number is " + existingUser.get().getStaffId()));
        }
        return ResponseEntity.ok(new ApiResponse<>("Failed", "This user does not exist. Kindly sign up.", null));
    }

    @Override // tested and is working fine
    public ResponseEntity<String> login(LoginDto loginDto) {
        User users = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
        if (!users.isActive()) {
            throw new ValidationException("User Not Active. Kindly complete your registration.");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        UserDetails user = customUserDetailService.loadUserByUsername(loginDto.getEmail());
        if (user != null) {
            return ResponseEntity.ok(jwtUtils.generateToken(user));
        }
        return ResponseEntity.status(400).body("Some error occurred");
    }

    @Override // tested and is working fine
    public ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Optional<User> user = userRepository.findByEmail(forgotPasswordDto.getEmail());
        if (user.isEmpty()) {
            throw new UserNotFoundException("User does not exist");
        }
        String token = jwtUtils.resetPasswordToken(forgotPasswordDto.getEmail());
        user.get().setConfirmationToken(token);
        userRepository.save(user.get());

        String URL = "http://localhost:8080/api/v1/auth/reset-password/?token=" + token;
        String link = "<h3>Hello " + "<br> Click the link below to reset your password <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail(forgotPasswordDto.getEmail(), "AriXpress: Reset your password", link);
        return ResponseEntity.ok(new ApiResponse<>("Sent", "Check your email to reset your password", null));
    }

    @Override // tested and is working fine
    public ResponseEntity<ApiResponse> resetPassword(ResetPasswordDto resetPasswordDto) {
        Optional<User> user = Optional.ofNullable(userRepository.findByConfirmationToken(resetPasswordDto.getConfirmationToken())
                .orElseThrow(() -> new ValidationException("Token is incorrect or User does not exist!")));

        if(!appUtil.isValidPassword(resetPasswordDto.getNewPassword()))
            throw new ValidationException("Password MUST be between 6 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character like #,@,%,$");

        if (!(resetPasswordDto.getConfirmNewPassword().equals(resetPasswordDto.getNewPassword()))) {
            throw new InputMismatchException("Passwords do not match!");
        }
        user.get().setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        userRepository.save(user.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password reset successful.", null));
    }

    @Override  // tested and is working fine
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto) {
        User user = appUtil.getLoggedInUser();
        if (!(changePasswordDto.getConfirmNewPassword().equals(changePasswordDto.getNewPassword()))) {
            throw new InputMismatchException("Confirm password and Password do not match!");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password change successful", null));
    }


    @Override  // tested and is working fine
    public ResponseEntity<ApiResponse> dispatchOrder(Long clientCode, String referenceNumber, HttpServletResponse response, DispatchOrderDto dispatchOrderDto) throws IOException {

        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.ADMIN)) || user.getRole().equals(Role.STAFF)) {
            throw new ValidationException("You are not permitted to perform this operation");
        }
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order with the reference number " + referenceNumber + " does not exist")));

        Optional<User> user1 = Optional.ofNullable(userRepository.findByStaffId(dispatchOrderDto.getRiderId())
                .orElseThrow(() -> new UserNotFoundException("Rider does not exist! Check the rider Id")));
        if(!(user1.get().getRiderStatus().equals(RiderStatus.Free)))
            throw new RiderUnavailableException("This rider is currently engaged. Kindly assign another rider to this order.");

        order.get().setRiderPhoneNumber(user1.get().getPhoneNumber());
        order.get().setRiderName(user1.get().getFirstName() + " " + user1.get().getLastName());
        order.get().setRiderId(dispatchOrderDto.getRiderId());
        order.get().setOrderStatus(OrderStatus.INPROGRESS);
        order.get().setPrice(600.00);
        orderRepository.save(order.get());

        user1.get().setRiderStatus(RiderStatus.Engaged);
        userRepository.save(user1.get());

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

        if (order.get().getThirdPartyPickUp().equals(true) && order.get().getPaymentType().equals(PaymentType.BankTransfer)){
            p2.add("Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "ClientId: " + order.get().getClientCode() + '\n' +
                    "Third-party name: " + order.get().getThirdPartyName() + '\n' +
                    "Third-party address: " + order.get().getThirdPartyAddress() + '\n' +
                    "Third-party phone number " + order.get().getThirdPartyPhoneNumber() + '\n' +
                    "Receiver name : " + order.get().getReceiverName() + '\n' +
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
                    "Bank Name: " + "GTB");}
        else if (order.get().getThirdPartyPickUp().equals(true) && order.get().getPaymentType().equals(PaymentType.Cash)) {
            p2.add("Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "ClientId: " + order.get().getClientCode() + '\n' +
                    "Third-party name: " + order.get().getThirdPartyName() + '\n' +
                    "Third-party address: " + order.get().getThirdPartyAddress() + '\n' +
                    "Third-party phone number " + order.get().getThirdPartyPhoneNumber() + '\n' +
                    "Receiver name : " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Date: " + order.get().getCreatedAt() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber());}
        else if (order.get().getThirdPartyPickUp().equals(false) && order.get().getPaymentType().equals(PaymentType.BankTransfer)) {
            p2.add( "Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "ClientId: " + order.get().getClientCode() + '\n' +
                    "Client name: " + order.get().getCustomerFirstName() + " " + order.get().getCustomerLastName() + '\n' +
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
                    "Bank Name: " + "GTB");}
        else {
            p2.add( "Order Date: " + order.get().getCreatedAt() + '\n' +
                    "ReferenceNumber: " + order.get().getReferenceNumber() + '\n' +
                    "ClientId: " + order.get().getClientCode() + '\n' +
                    "Client name: " + order.get().getCustomerFirstName() + " " + order.get().getCustomerLastName() + '\n' +
                    "Pick-up address: " + order.get().getPickUpAddress() + '\n' +
                    "Receiver name: " + order.get().getReceiverName() + '\n' +
                    "Receiver address: " + order.get().getDeliveryAddress() + '\n' +
                    "Receiver phone number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Item type: " + order.get().getItemType() + '\n' +
                    "Item quantity: " + order.get().getItemQuantity() + '\n' +
                    "Rider name: " + order.get().getRiderName() + '\n' +
                    "Rider Phone Number: " + order.get().getReceiverPhoneNumber() + '\n' +
                    "Current Date: " + currentDateTime);
        }
        document.add(p1);
        document.add(p2);
        document.close();

        String URL = "http://localhost:8080/api/v1/auth/order-dispatch/?order=" + document;
        String link = "<h3>Hello " + "<br> Here is a pdf of your order. Kindly download to see the delivery agent details. <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail(order.get().getEmail(), "AriXpress: A new order", link);

        return ResponseEntity.ok(new ApiResponse<>("Success", "Dispatch successful", document));
    }

    @Override // tested and is working fine
    public ResponseEntity<ApiResponse> registerABike(RegisterBikeDto registerBikeDto) {
        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.STAFF))) {
            throw new ValidationException("You are not authorised to perform this operation.");
        }
          Optional<Bike> bike = bikeRepository.findByBikeNumber(registerBikeDto.getBikeNumber());
            if(bike.isPresent())
                throw new ResourceAlreadyExistsException("This bike is already registered!");

        Bike bike1 = new Bike();
        bike1.setBikeNumber(registerBikeDto.getBikeNumber());
        bike1.setPrice(registerBikeDto.getPrice());
        bike1.setMake(registerBikeDto.getMake());
        bike1.setImages(registerBikeDto.getImages());
        bikeRepository.save(bike1);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Bike registered", null));
    }

    @Override  //tested and is working fine
    public ResponseEntity<ApiResponse> registerARider(RegisterRiderDto registerRiderDto) {
        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))) {
            throw new ValidationException("You are not authorised to perform this operation.");
        }
        Long staffNumber = appUtil.generateRandomCode();
        User user1 = new User();
        user1.setRole(Role.RIDER);
        user1.setFirstName(registerRiderDto.getFirstName());
        user1.setLastName(registerRiderDto.getLastName());
        user1.setAddress(registerRiderDto.getAddress());
        user1.setStaffId(staffNumber);
        user1.setPhoneNumber(registerRiderDto.getPhoneNumber());
        user1.setRiderStatus(RiderStatus.Free);
        user1.setActive(true);
        userRepository.save(user1);

        return ResponseEntity.ok(new ApiResponse<>("Success", "Rider registration successful", "Your Rider ID is: " + staffNumber));
    }

    @Override //tested and is working fine
    public ApiResponse assignBikeToRider(AssignRiderToBikeDto assignRiderToBikeDto) {

        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        User user1 = userRepository.findByStaffId(assignRiderToBikeDto.getStaffId())
                .orElseThrow(() -> new UserNotFoundException("This user does not exist"));

        if (!user1.getRole().equals(Role.RIDER))
            throw new UnsupportedOperationException("You cannot perform this operation on this user");

        Optional<Bike> bike = bikeRepository.findByBikeNumber(assignRiderToBikeDto.getBikeNumber());
        if (bike.isEmpty())
            throw new ResourceNotFoundException("There is no bike with this number!");

        bike.get().setRiderName(user1.getFirstName() + " " + user1.getLastName());
        bike.get().setRiderPhoneNumber(user1.getPhoneNumber());
        bike.get().setRiderAddress(user1.getAddress());
        bikeRepository.save(bike.get());

        return new ApiResponse<>("Success", "Successful", "Bike with the number " + assignRiderToBikeDto.getBikeNumber() + " has been assigned to " + bike.get().getRiderName());
    }

    @Override //tested and is working fine
    public Optional<Orders> viewAnOrderById(Long id) {
        User user = appUtil.getLoggedInUser();
        if (!user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))
            throw new ValidationException("You are not authorised to perform this operation.");

        Optional<Orders> order = Optional.ofNullable(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no order with such Id")));
        return order;
    }

    @Override //tested and is working fine
    public List<Orders> viewAllOrdersByStatus(OrderStatus orderStatus) {
        User user = appUtil.getLoggedInUser();
        if (!user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))
            throw new ValidationException("You are not authorised to perform this operation.");
        return new ArrayList<>(orderRepository.findByOrderStatus(orderStatus));
    }

    @Override //tested and is working fine
    public List<User> viewAllRidersByStatus(RiderStatus riderStatus) {
        User user = appUtil.getLoggedInUser();
        if (!user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))
            throw new ValidationException("You are not allowed to perform this operation");

        return new ArrayList<>(userRepository.findByRiderStatus(riderStatus));
    }

    @Override //tested and is working fine
    public Integer countRidesPerRider(Long staffId) {
        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.RIDER) || (user.getRole().equals(Role.SUPER_ADMIN))))) {
            throw new ValidationException("You are not authorised to perform this operation.");
        }
        List<Orders> tripCount = new ArrayList<>(orderRepository.findByRiderId(staffId));
        return tripCount.size();
    }

    @Override //Tested and is working fine. Format the <User> return type to return relevant staff information.
    public Optional<User> viewStaffDetails(Long staffId) {
        User user = appUtil.getLoggedInUser();

        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.SUPER_ADMIN))))
            throw new ValidationException("You are not authorised to perform this operation!");

        Optional<User> user1 = userRepository.findByStaffId(staffId);
        if(user1.get().getRole().equals(Role.SUPER_ADMIN)) {
            throw new ValidationException("You cannot perform this operation on this user!");}
        return user1;
    }

    @Override  //Tested and is working fine
    public ResponseEntity<ApiResponse> deleteStaff(Long staffId) {
        User user = appUtil.getLoggedInUser();
        if (user.getRole().equals(Role.SUPER_ADMIN))
            throw new ValidationException("You are not authorised to perform this operation!");

        Optional<User> user1 = userRepository.findByStaffId(staffId);
        if (user1.isEmpty()) {
            throw new ValidationException("Staff number incorrect!");
        }
        userRepository.delete(user1.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Staff has been deleted!", null));
    }

    @Override //Tested and is working fine
    public ResponseEntity<ApiResponse> createAdmin(Long staffId) {
        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.SUPER_ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }

        Optional<User> user1 = userRepository.findByStaffId(staffId);
        if (user1.isEmpty())
            throw new UserNotFoundException("There is no staff with this Id!");

        if (user1.get().getRole().equals(Role.ADMIN))
            throw new UnsupportedOperationException("This user is already an Admin!");

        if (user1.get().getRole().equals(Role.SUPER_ADMIN))
            throw new UnsupportedOperationException("This user is a Super Admin!");

        if (user1.get().getRole().equals(Role.STAFF))
            user1.get().setRole(Role.ADMIN);
        userRepository.save(user1.get());

        userRepository.save(user1.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "New admin created", null));
    }

    @Override //Tested and is working fine
    public List<Orders> viewAllOrders() {
        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || (user.getRole().equals(Role.ADMIN)))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        return new ArrayList<>(orderRepository.findAll());
    }

    @Override //tested and is working fine
    public List<Orders> clientWeeklyOrderSummary(WeeklyOrderSummaryDto weeklyOrderSummaryDto) {
        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF))))
            throw new ValidationException("You are not authorised to perform this operation");

        return new ArrayList<>(orderRepository.findAllByClientCodeAndCreatedAtBetween(weeklyOrderSummaryDto.getClientCode(), weeklyOrderSummaryDto.getStartDate(), weeklyOrderSummaryDto.getEndDate()));
    }

    @Override //tested and is working fine
    public List<Orders> viewAllOrdersToday() {
    User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || (user.getRole().equals(Role.ADMIN)))) {
            throw new ValidationException("You are not authorised to perform this operation");
    }
       LocalDate today = LocalDate.now();
        return orderRepository.findAllByCreatedAt(today);
    }

    @Override //tested and is working fine
    public List<Orders> viewAllOrdersInAMonth(OrdersHistoryDto ordersHistoryDto) {
        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        return orderRepository.findAllByCreatedAtBetween(ordersHistoryDto.getStartDate(), ordersHistoryDto.getEndDate());
    }

    @Override //tested and is working fine
    public List<Orders> viewAllOrdersInAWeek(OrdersHistoryDto ordersHistoryDto) {
        User user = appUtil.getLoggedInUser();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        return orderRepository.findAllByCreatedAtBetween(ordersHistoryDto.getStartDate(), ordersHistoryDto.getEndDate());
    }
}
