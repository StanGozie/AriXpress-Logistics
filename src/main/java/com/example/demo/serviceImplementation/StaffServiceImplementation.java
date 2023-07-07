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
import com.example.demo.dto.request.PeriodicBillDto;
import com.example.demo.dto.request.RegisterBikeDto;
import com.example.demo.dto.request.RegisterRiderDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.RidersDeliveryCountPerMonthDto;
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
import com.example.demo.exceptions.UnsupportedOperationException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Bike;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import com.example.demo.repository.BikeRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.service.StaffService;
import com.example.demo.utils.AppUtil;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.hibernate.criterion.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StaffServiceImplementation implements StaffService {

    private final StaffRepository staffRepository;
    private final EmailService emailService;
    private final BikeRepository bikeRepository;
    private final AppUtil appUtil;
    private final OrderRepository orderRepository;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    @Override  // tested and is working fine
    public ResponseEntity<ApiResponse> staffSignUp(SignUpDto signUpDto) throws ValidationException {
        if (!appUtil.isValidEmail(signUpDto.getEmail()))
            throw new ValidationException("Email is invalid");

        Boolean isUserExist = staffRepository.existsByEmail(signUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

        if((!appUtil.isValidPassword(signUpDto.getPassword())))
            throw new ValidationException("Password MUST be between 8 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character like #,@,%,$");

        if (!(signUpDto.getConfirmPassword().equals(signUpDto.getPassword())))
            throw new InputMismatchException("Confirm Password and Password do not match!");

        Long staffId = appUtil.generateRandomCode();

        Staff staff = new Staff();
        staff.setFirstName(signUpDto.getFirstName());
        staff.setLastName(signUpDto.getLastName());
        staff.setEmail(signUpDto.getEmail());
        staff.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        staff.setRole(Role.STAFF);
        if(signUpDto.getEmail().equals("chigoziestanleyenyoghasi@gmail.com") || signUpDto.getEmail().equals("chigozieenyoghasi@yahoo.com")){
            staff.setRole(Role.SUPER_ADMIN);}
        staff.setStaffId(staffId);
        String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
        staff.setConfirmationToken(token);
        staffRepository.save(staff);

        String URL = "http://localhost:8080/api/v1/auth//complete-registration/?token=" + token;
        String link = "<h3>Hello " + signUpDto.getFirstName() + "<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";

        emailService.sendEmail(signUpDto.getEmail(), "AriXpress: Verify Your Account", link);

        return ResponseEntity.ok(new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null));
    }

    @Override  // tested and is working fine
    public ResponseEntity<ApiResponse> updateStaffInformation(StaffRelevantDetailsDto staffRelevantDetailsDto) {

        Staff user = appUtil.getLoggedInStaff();

        if (user.getRole().equals(Role.STAFF) || user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN))

        user.setState(staffRelevantDetailsDto.getStateOfOrigin());
        user.setNextOfKinFirstName(staffRelevantDetailsDto.getNextOfKinFirstName());
        user.setNextOfKinLastName(staffRelevantDetailsDto.getNextOfKinLastName());
        user.setNextOfKinAddress(staffRelevantDetailsDto.getNextOfKinAddress());
        user.setNextOfKinPhoneNumber(staffRelevantDetailsDto.getNextOfKinPhoneNumber());
        user.setStateOfOrigin(staffRelevantDetailsDto.getStateOfOrigin());
        staffRepository.save(user);
        return ResponseEntity.ok(new ApiResponse("Successful", "Staff information update successful", null));
    }

    @Override // tested and is working fine
    public ResponseEntity<ApiResponse> staffCompleteRegistration(CompleteRegistrationDto completeRegistrationDto) {
        Optional<Staff> existingUser = staffRepository.findByConfirmationToken(completeRegistrationDto.getToken());

        if (existingUser.isPresent()) {
            if (existingUser.get().isActive()) {
                throw new AccountAlreadyActivatedException("This account is already activated. Pls login");
            }
            existingUser.get().setDob(completeRegistrationDto.getDob());
            existingUser.get().setAddress(completeRegistrationDto.getAddress());
            existingUser.get().setPhoneNumber(completeRegistrationDto.getPhoneNumber());
            existingUser.get().setGender(completeRegistrationDto.getGender());
            existingUser.get().setActive(true);
            staffRepository.save(existingUser.get());
            return ResponseEntity.ok(new ApiResponse<>("Successful", "Registration completed", "Your unique staff number is " + existingUser.get().getStaffId()));
        }
        return ResponseEntity.ok(new ApiResponse<>("Failed", "This user does not exist. Kindly sign up.", null));
    }

    @Override // tested and is working fine
    public ResponseEntity<String> staffLogin(LoginDto loginDto) {
        Staff staff = staffRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
        if (!staff.isActive()) {
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
    public ResponseEntity<ApiResponse> staffForgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Optional<Staff> user = staffRepository.findByEmail(forgotPasswordDto.getEmail());
        if (user.isEmpty()) {
            throw new UserNotFoundException("User does not exist");
        }
        String token = jwtUtils.resetPasswordToken(forgotPasswordDto.getEmail());
        user.get().setConfirmationToken(token);
        staffRepository.save(user.get());

        String URL = "http://localhost:8080/api/v1/auth/staff/reset-password/?token=" + token;
        String link = "<h3>Hello " + "<br> Click the link below to reset your password <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail(forgotPasswordDto.getEmail(), "AriXpress: Reset your password", link);
        return ResponseEntity.ok(new ApiResponse<>("Sent", "Check your email to reset your password", null));
    }

    @Override // tested and is working fine
    public ResponseEntity<ApiResponse> staffResetPassword(ResetPasswordDto resetPasswordDto) {
        Optional<Staff> user = Optional.ofNullable(staffRepository.findByConfirmationToken(resetPasswordDto.getConfirmationToken())
                .orElseThrow(() -> new ValidationException("Token is incorrect or User does not exist!")));

        if(!appUtil.isValidPassword(resetPasswordDto.getNewPassword()))
            throw new ValidationException("Password MUST be between 6 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character like #,@,%,$");

        if (!(resetPasswordDto.getConfirmNewPassword().equals(resetPasswordDto.getNewPassword()))) {
            throw new InputMismatchException("Passwords do not match!");
        }
        user.get().setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        staffRepository.save(user.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password reset successful.", null));
    }

    @Override  // tested and is working fine
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if (!(changePasswordDto.getConfirmNewPassword().equals(changePasswordDto.getNewPassword()))) {
            throw new InputMismatchException("Confirm password and Password do not match!");
        }
        staff.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        staffRepository.save(staff);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password change successful", null));
    }


    @Override  // tested and is working fine
    public ResponseEntity<ApiResponse> dispatchOrder(String referenceNumber, HttpServletResponse response, DispatchOrderDto dispatchOrderDto) throws IOException {

        Staff staff = appUtil.getLoggedInStaff();
        if (!(staff.getRole().equals(Role.ADMIN)) || staff.getRole().equals(Role.STAFF)) {
            throw new ValidationException("You are not permitted to perform this operation");
        }
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByReferenceNumber(referenceNumber))
                .orElseThrow(() -> new ResourceNotFoundException("Order with the reference number " + referenceNumber + " does not exist"));

        if(order.get().getOrderStatus().equals(OrderStatus.INPROGRESS))
            throw new UnsupportedOperationException("This Order has been dispatched!");

        if(order.get().getOrderStatus().equals(OrderStatus.COMPLETED))
            throw new UnsupportedOperationException("This Order has been completed");

        if(order.get().getOrderStatus().equals(OrderStatus.CANCELLED))
            throw new UnsupportedOperationException("This order has been cancelled by the customer");

        Optional<Staff> staff1 = Optional.ofNullable(staffRepository.findByStaffId(dispatchOrderDto.getRiderId())
                .orElseThrow(() -> new UserNotFoundException("Rider does not exist! Check the rider Id")));
        if(!(staff1.get().getRiderStatus().equals(RiderStatus.Free)))
            throw new RiderUnavailableException("This rider is currently engaged. Kindly assign another rider to this order.");

        Boolean reAssignRider; // implement this for double reassigning of a rider to a new order

        Optional<Bike> bike = Optional.ofNullable(bikeRepository.findByStaffId(staff1.get().getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Bike not found!")));

        order.get().setBikeNumber(bike.get().getBikeNumber());
        order.get().setRiderPhoneNumber(staff1.get().getPhoneNumber());
        order.get().setRiderName(staff1.get().getFirstName() + " " + staff1.get().getLastName());
        order.get().setRiderId(dispatchOrderDto.getRiderId());
        order.get().setOrderStatus(OrderStatus.INPROGRESS);
        order.get().setPrice(120);
        orderRepository.save(order.get());


        int x = staff1.get().getRidesCount();
        staff1.get().setRidesCount(x + 1);
        staff1.get().setRiderStatus(RiderStatus.Engaged);
        staffRepository.save(staff1.get());

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
                    "Bike number: " + order.get().getBikeNumber() + '\n' +
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
                    "Bike number: " + order.get().getBikeNumber() + '\n' +
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
                    "Bike number: " + order.get().getBikeNumber() + '\n' +
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
                    "Bike number: " + order.get().getBikeNumber() + '\n' +
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
        Staff user = appUtil.getLoggedInStaff();
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
        Staff staff = appUtil.getLoggedInStaff();
        if (!(staff.getRole().equals(Role.ADMIN) || (staff.getRole().equals(Role.STAFF)))) {
            throw new ValidationException("You are not authorised to perform this operation.");
        }
        Long staffNumber = appUtil.generateRandomCode();
        Staff staff1 = new Staff();
        staff1.setRole(Role.RIDER);
        staff1.setFirstName(registerRiderDto.getFirstName());
        staff1.setLastName(registerRiderDto.getLastName());
        staff1.setAddress(registerRiderDto.getAddress());
        staff1.setStaffId(staffNumber);
        staff1.setPhoneNumber(registerRiderDto.getPhoneNumber());
        staff1.setRiderStatus(RiderStatus.Free);
        staff1.setRidesCount(0);
        staff1.setActive(true);
        staffRepository.save(staff1);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Rider registration successful", "Your Rider ID is: " + staffNumber));
    }

    @Override //tested and is working fine
    public ApiResponse assignBikeToRider(AssignRiderToBikeDto assignRiderToBikeDto) {

        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        Staff user1 = staffRepository.findByStaffId(assignRiderToBikeDto.getStaffId())
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
    public Optional<Orders> viewAnOrderByReferenceNumber(String referenceNumber) {
        Staff user = appUtil.getLoggedInStaff();
        if (!user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))
            throw new ValidationException("You are not authorised to perform this operation.");

        Optional<Orders> order = Optional.ofNullable(orderRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("There is no order with such Id")));
        return order;
    }

    @Override //tested and is working fine
    public List<Orders> viewAllOrdersByStatus(OrderStatus orderStatus) {
        Staff user = appUtil.getLoggedInStaff();
        if (!user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))
            throw new ValidationException("You are not authorised to perform this operation.");
        return new ArrayList<>(orderRepository.findByOrderStatus(orderStatus));
    }

    @Override //tested and is working fine
    public List<Staff> viewAllRidersByStatus(RiderStatus riderStatus) {
        Staff user = appUtil.getLoggedInStaff();
        if (!user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF)))
            throw new ValidationException("You are not allowed to perform this operation");

        return new ArrayList<>(staffRepository.findByRiderStatus(riderStatus));
    }

    @Override //tested and is working fine
    public Integer countRidesPerRider(Long staffId) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.RIDER) || (user.getRole().equals(Role.SUPER_ADMIN))))) {
            throw new ValidationException("You are not authorised to perform this operation.");
        }
        List<Orders> tripCount = new ArrayList<>(orderRepository.findByRiderId(staffId));
        return tripCount.size();
    }

    @Override //Tested and is working fine. Format the <User> return type to return relevant staff information.
    public Optional<Staff> viewStaffDetails(Long staffId) {
        Staff user = appUtil.getLoggedInStaff();

        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.SUPER_ADMIN))))
            throw new ValidationException("You are not authorised to perform this operation!");

        Optional<Staff> user1 = staffRepository.findByStaffId(staffId);
        if(user1.get().getRole().equals(Role.SUPER_ADMIN)) {
            throw new ValidationException("You cannot perform this operation on this user!");}
        return user1;
    }

    @Override  //Tested and is working fine
    public ResponseEntity<ApiResponse> deleteStaff(Long staffId) {
        Staff user = appUtil.getLoggedInStaff();
        if (user.getRole().equals(Role.SUPER_ADMIN))
            throw new ValidationException("You are not authorised to perform this operation!");

        Optional<Staff> user1 = staffRepository.findByStaffId(staffId);
        if (user1.isEmpty()) {
            throw new ValidationException("Staff number incorrect!");
        }
        staffRepository.delete(user1.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Staff has been deleted!", null));
    }

    @Override //Tested and is working fine
    public ResponseEntity<ApiResponse> createAdmin(Long staffId) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }

        Optional<Staff> user1 = staffRepository.findByStaffId(staffId);
        if (user1.isEmpty())
            throw new UserNotFoundException("There is no staff with this Id!");

        if (user1.get().getRole().equals(Role.ADMIN))
            throw new UnsupportedOperationException("This user is already an Admin!");

        if (user1.get().getRole().equals(Role.SUPER_ADMIN))
            throw new UnsupportedOperationException("This user is a Super Admin!");

        if (user1.get().getRole().equals(Role.STAFF))
            user1.get().setRole(Role.ADMIN);
        staffRepository.save(user1.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "New admin created", null));
    }

    @Override //Tested and is working fine
    public List<Orders> viewAllOrders() {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || (user.getRole().equals(Role.ADMIN)))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        return new ArrayList<>(orderRepository.findAll());
    }

    @Override //tested and is working fine
    public List<Orders> clientWeeklyOrderSummary(WeeklyOrderSummaryDto weeklyOrderSummaryDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.ADMIN) || (user.getRole().equals(Role.STAFF))))
            throw new ValidationException("You are not authorised to perform this operation");

        return new ArrayList<>(orderRepository.findAllByClientCodeAndCreatedAtBetween(weeklyOrderSummaryDto.getClientCode(),
                weeklyOrderSummaryDto.getStartDate(), weeklyOrderSummaryDto.getEndDate()));
    }

    @Override //tested and is working fine
    public List<Orders> viewAllOrdersToday() {
    Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || (user.getRole().equals(Role.ADMIN)))) {
            throw new ValidationException("You are not authorised to perform this operation");
    }
       LocalDate today = LocalDate.now();
        return orderRepository.findAllByCreatedAt(today);
    }

    @Override //tested and is working fine
    public List<Orders> viewAllOrdersInAMonth(OrdersHistoryDto ordersHistoryDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        return orderRepository.findAllByCreatedAtBetween(ordersHistoryDto.getStartDate(), ordersHistoryDto.getEndDate());
    }

    @Override //tested and is working fine
    public List<Orders> viewAllOrdersInAWeek(OrdersHistoryDto ordersHistoryDto) {
        Staff user = appUtil.getLoggedInStaff();
        if (!(user.getRole().equals(Role.SUPER_ADMIN) || user.getRole().equals(Role.ADMIN))) {
            throw new ValidationException("You are not authorised to perform this operation");
        }
        return orderRepository.findAllByCreatedAtBetween(ordersHistoryDto.getStartDate(), ordersHistoryDto.getEndDate());
    }

    @Override// this method is to count the deliveries of a rider over a time frame (could be weekly, daily, or monthly). tested and it's working fine
    public int viewDeliveryCountOfRider(Long riderId, RidersDeliveryCountPerMonthDto ridersDeliveryCountPerMonthDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if(!staff.getRole().equals(Role.ADMIN))
            throw new ValidationException("You are not permitted to perform this operation");
        List<Orders> ridesList = new ArrayList<>(orderRepository.findAllByRiderIdAndCreatedAtBetween(riderId,
                ridersDeliveryCountPerMonthDto.getStartDate(), ridersDeliveryCountPerMonthDto.getEndDate()));
        return ridesList.size();
    }

    @Override
    public BigDecimal weeklyBill(Long clientCode, PeriodicBillDto periodicBillDto) {
        Staff staff = appUtil.getLoggedInStaff();
        if (!staff.getRole().equals(Role.ADMIN))
            throw new ValidationException("You are not permitted to perform this operation");

        return orderRepository.findSumOfOrderPrices(clientCode, periodicBillDto.getStartDate(), periodicBillDto.getEndDate());
    }

    public List<Orders> generatePeriodicOrderDetailsPdf (Long clientCode, HttpServletResponse response, PeriodicBillDto periodicBillDto) throws IOException{
        Staff staff = appUtil.getLoggedInStaff();

        List<Orders> order = orderRepository.findOrderDetails(clientCode, periodicBillDto.getStartDate(), periodicBillDto.getEndDate());

        BigDecimal totalBill = orderRepository.findSumOfOrderPrices(clientCode, periodicBillDto.getStartDate(), periodicBillDto.getEndDate());

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy  hh:mm:ss");
        String currentDateTime = dateFormat.format(new Date());

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font font = FontFactory.getFont(FontFactory.COURIER_BOLD);
        font.setColor(Color.BLACK);
        font.setSize(15);
        Paragraph p1 = new Paragraph("Total Orders Invoice", font);
        p1.setAlignment(Paragraph.ALIGN_CENTER);

        Paragraph p2 = new Paragraph();
        p2.setAlignment(Paragraph.ALIGN_LEFT);
        p2.setFont(FontFactory.getFont(FontFactory.COURIER, 10, Color.black));
        p2.setMultipliedLeading(2);

        Font font2 = FontFactory.getFont(FontFactory.COURIER_BOLD);
        font2.setColor(Color.RED);
        font2.setSize(15);
        Paragraph p3 = new Paragraph("Grand Total: " + totalBill, font2);
        p3.setAlignment(Paragraph.ALIGN_CENTER);

        for (Orders oneOrder : order) {
            p2.add( "Order Date: " + oneOrder.getCreatedAt() + '\n' +
                    "ReferenceNumber: " + oneOrder.getReferenceNumber() + '\n' +
                    "ClientId: " + oneOrder.getClientCode() + '\n' +
                    "Client name: " + oneOrder.getCustomerFirstName() + '\n' +
                    "Pick-up address: " + oneOrder.getPickUpAddress() + '\n' +
                    "Item: " + oneOrder.getItemType() + '\n' +
                    "Receiver address: " + oneOrder.getDeliveryAddress() + '\n' +
                    "Price: " + oneOrder.getPrice() + '\n' +
                    "Order Status: " + oneOrder.getOrderStatus() + '\n' + " " + '\n');}

        document.add(p1);
        document.add(p2);
        document.add(p3);
        document.close();
        return order;
    }



}
