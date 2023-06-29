package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.configuration.security.CustomUserDetailService;
import com.example.demo.configuration.security.JwtUtils;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.CustomerType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.RiderStatus;
import com.example.demo.enums.Role;
import com.example.demo.exceptions.AccountAlreadyActivatedException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Orders;
import com.example.demo.model.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CustomerService;
import com.example.demo.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImplementation implements CustomerService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AppUtil appUtil;
    private final OrderRepository orderRepository;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    @Override //tested and working fine
    public ResponseEntity<ApiResponse> signUp(SignUpDto signUpDto) throws ValidationException {

        if (!appUtil.isValidEmail(signUpDto.getEmail()))
            throw new ValidationException("Email is invalid");

        Boolean isUserExist = userRepository.existsByEmail(signUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

         if(!appUtil.isValidPassword(signUpDto.getPassword()))
            throw new ValidationException("Password MUST be between 6 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character like #,@,%,$");

        if(!(signUpDto.getConfirmPassword().equals(signUpDto.getPassword())))
            throw new InputMismatchException("Confirm password and Password do not match!");

        Boolean registerAsACompany = signUpDto.getRegisterAsACompany();

        if(registerAsACompany.equals(true)){

            User user = new User();
            user.setFirstName(signUpDto.getFirstName());
            user.setLastName(signUpDto.getLastName());
            user.setEmail(signUpDto.getEmail());
            user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
            user.setRole((Role.CUSTOMER));
            user.setCustomerType(CustomerType.Corporate);
            String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
            user.setConfirmationToken(token);
            userRepository.save(user);

            String URL = "http://localhost:8080/api/v1/auth/complete-business-registration/?token=" + token;
            String link = "<h3>Hello "  + signUpDto.getFirstName()  +"<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";

            emailService.sendEmail(signUpDto.getEmail(),"AriXpress: Verify Your Account", link);
        }

        User user = new User();
        user.setFirstName(signUpDto.getFirstName());
        user.setLastName(signUpDto.getLastName());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        user.setRole((Role.CUSTOMER));
        user.setCustomerType(CustomerType.Individual);
        String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
        user.setConfirmationToken(token);
        userRepository.save(user);

        String URL = "http://localhost:8080/api/v1/auth/complete-registration/?token=" + token;
        String link = "<h3>Hello "  + signUpDto.getFirstName()  +"<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";

        emailService.sendEmail(signUpDto.getEmail(),"AriXpress: Verify Your Account", link);

        return ResponseEntity.ok(new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null));
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> completeRegistration(CompleteRegistrationDto completeRegistrationDto) {
        Optional<User> existingUser = userRepository.findByConfirmationToken(completeRegistrationDto.getToken());

        if (existingUser.isPresent()) {
            if (existingUser.get().isActive()) {
                throw new AccountAlreadyActivatedException("This account is already activated. Pls login");
            }
            Long clientCode = appUtil.generateRandomCode();
            existingUser.get().setDob(completeRegistrationDto.getDob());
            existingUser.get().setAddress(completeRegistrationDto.getAddress());
            existingUser.get().setPhoneNumber(completeRegistrationDto.getPhoneNumber());
            existingUser.get().setState(completeRegistrationDto.getState());
            existingUser.get().setGender(completeRegistrationDto.getGender());
            existingUser.get().setClientCode(clientCode);
            existingUser.get().setActive(true);
            userRepository.save(existingUser.get());
            return ResponseEntity.ok(new ApiResponse<>("Successful", "Registration completed", "Your client number is "+clientCode));
        }
        return ResponseEntity.ok(new ApiResponse<>("Failed", "This user does not exist. Kindly sign up.", null));
    }

    @Override //tested and working fine
    public ApiResponse completeBusinessRegistration(CompleteBusinessRegistrationDto completeBusinessRegistrationDto) {
        Optional<User> existingUser = userRepository.findByConfirmationToken(completeBusinessRegistrationDto.getToken());

        Long clientCode = appUtil.generateRandomCode();



        if(existingUser.isPresent() && existingUser.get().isActive()){
                throw new AccountAlreadyActivatedException("This account has been activated. Please login");
            }
        existingUser.get().setCompanyName(completeBusinessRegistrationDto.getCompanyName());
        existingUser.get().setAddress(completeBusinessRegistrationDto.getAddress());
        existingUser.get().setPaymentType(completeBusinessRegistrationDto.getPaymentType());
        existingUser.get().setPaymentInterval(completeBusinessRegistrationDto.getPaymentInterval());
        existingUser.get().setPhoneNumber(completeBusinessRegistrationDto.getPhoneNumber());
        existingUser.get().setState(completeBusinessRegistrationDto.getState());
        existingUser.get().setClientCode(clientCode);
        existingUser.get().setActive(true);
        userRepository.save(existingUser.get());

        return ApiResponse.builder()
                .status("Successful")
                .message("Corporate Client Registration Successful.")
                .data("You unique code is "+ clientCode)
                .build();
    }

    @Override //tested and working fine
    public ResponseEntity<String> login(LoginDto loginDto) {

        User users = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
        if(!users.isActive()){
            throw new ValidationException("User Not Active. Kindly complete your registration.");
        }
        if(!passwordEncoder.matches(loginDto.getPassword(),(users.getPassword())))
            throw new ValidationException("Password is Incorrect!");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        UserDetails user = customUserDetailService.loadUserByUsername(loginDto.getEmail());
        if(user != null) {
            return ResponseEntity.ok(jwtUtils.generateToken(user));
        }
        return ResponseEntity.status(400).body("Some error occurred");
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Optional<User> user = userRepository.findByEmail(forgotPasswordDto.getEmail());
        if(user.isEmpty()) {
            throw new UserNotFoundException("User does not exist");
        }
        String token = jwtUtils.resetPasswordToken(forgotPasswordDto.getEmail());
        user.get().setConfirmationToken(token);
        userRepository.save(user.get());

        String URL = "http://localhost:8080/api/v1/auth/reset-password/?token=" + token;
        String link = "<h3>Hello " +"<br> Click the link below to reset your password <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail(forgotPasswordDto.getEmail(), "AriXpress: Reset your password", link);
        return ResponseEntity.ok(new ApiResponse<>("Sent", "Check your email to reset your password", null));
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> resetPassword(ResetPasswordDto resetPasswordDto) {
        Optional<User> user = Optional.ofNullable(userRepository.findByConfirmationToken(resetPasswordDto.getConfirmationToken())
                .orElseThrow(() -> new ValidationException("Token is incorrect or User does not exist!")));

        if(!appUtil.isValidPassword(resetPasswordDto.getNewPassword()))
            throw new ValidationException("Password MUST be between 6 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character like #,@,%,$");

        if(!resetPasswordDto.getConfirmNewPassword().equals(resetPasswordDto.getNewPassword())){
            throw new InputMismatchException("Passwords do not match!");
        }
        user.get().setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        userRepository.save(user.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password reset successful.", null));
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto) {
        User user = appUtil.getLoggedInUser();
        if(!(changePasswordDto.getConfirmNewPassword().equals(changePasswordDto.getNewPassword()))){
            throw new InputMismatchException("Confirm password and Password do not match!");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password change successful", null));
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> bookADelivery(DirectDeliveryDto directDeliveryDto) {
        User user = appUtil.getLoggedInUser();
        String email = user.getEmail();
        String id = appUtil.generateSerialNumber("AXL-");

        if(user.getCustomerType().equals(CustomerType.Corporate)) {
            Orders orders = new Orders();
            orders.setClientCode(user.getClientCode());
            orders.setThirdPartyPickUp(false);
            orders.setReferenceNumber(id);
            orders.setCompanyName(user.getCompanyName());
            orders.setPickUpAddress(user.getAddress());
            orders.setDeliveryAddress(directDeliveryDto.getDeliveryAddress());
            orders.setReceiverName(directDeliveryDto.getReceiverName());
            orders.setReceiverPhoneNumber(directDeliveryDto.getReceiverPhoneNumber());
            orders.setItemType(directDeliveryDto.getItemType());
            orders.setItemQuantity(directDeliveryDto.getItemQuantity());
            orders.setOrderStatus(OrderStatus.PENDING);
            orders.setEmail(email);
        }
        Orders orders = new Orders();
        orders.setClientCode(user.getClientCode());
        orders.setThirdPartyPickUp(false);
        orders.setReferenceNumber(id);
        orders.setCustomerFirstName(user.getFirstName());
        orders.setCustomerLastName(user.getLastName());
        orders.setItemType(directDeliveryDto.getItemType());
        orders.setItemQuantity(directDeliveryDto.getItemQuantity());
        orders.setDeliveryAddress(directDeliveryDto.getDeliveryAddress());
        orders.setPickUpAddress(directDeliveryDto.getPickUpAddress());
        orders.setReceiverName(directDeliveryDto.getReceiverName());
        orders.setReceiverPhoneNumber(directDeliveryDto.getReceiverPhoneNumber());
        orders.setItemType(directDeliveryDto.getItemType());
        orders.setPaymentType(directDeliveryDto.getPaymentType());
        orders.setOrderStatus(OrderStatus.PENDING);
        orders.setEmail(email);
        orderRepository.save(orders);

        String URL = "http://localhost:8080/api/v1/auth/new-order/?order=" + orders.getId();
        String link = "<h3>Hello " +"<br> Click the link below dispatch the new order <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail(email,"AriXpress: A new order", link);

        return ResponseEntity.ok(new ApiResponse<>("Success", "Delivery booked successfully. Details to be sent to you shortly.", null));

    }

    @Override //tested and is working fine
    public ResponseEntity<ApiResponse> thirdPartySender(ThirdPartySenderDto thirdPartySenderDto) {
        User user = appUtil.getLoggedInUser();
        String email = user.getEmail();
        Long id = user.getId();
        String oid = appUtil.generateSerialNumber("AXL-");

        Orders order = new Orders();
        order.setThirdPartyPickUp(true);
        order.setClientCode(id);
        order.setReferenceNumber(oid);
        order.setCustomerFirstName(user.getFirstName());
        order.setCustomerLastName(user.getLastName());
        order.setThirdPartyName(thirdPartySenderDto.getThirdPartyName());
        order.setThirdPartyAddress(thirdPartySenderDto.getThirdPartyAddress());
        order.setThirdPartyPhoneNumber(thirdPartySenderDto.getThirdPartyPhoneNumber());
        order.setClientType(CustomerType.Individual);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setItemQuantity(thirdPartySenderDto.getItemQuantity());
        order.setItemType(thirdPartySenderDto.getItemType());
        order.setReceiverName(thirdPartySenderDto.getReceiverName());
        order.setReceiverPhoneNumber(thirdPartySenderDto.getReceiverPhoneNumber());
        order.setDeliveryAddress(thirdPartySenderDto.getDeliveryAddress());
        order.setPaymentType(thirdPartySenderDto.getPaymentType());
        order.setEmail(email);
        order.setPrice(6000.00);
        order.setDistance(23.4);
        orderRepository.save(order);

        String URL = "http://localhost:8080/api/v1/auth/new-order/?order=" + order.getId();
        String link = "<h3>Hello " +"<br> Click the link below dispatch the new order <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail("chigozieenyoghasi@yahoo.com","AriXpress: A new order", link);

        return ResponseEntity.ok(new ApiResponse<>("Success", "Delivery booked successfully. Details to be sent to you shortly.", null));

    }
    @Override //Tested and is working fine
    public ResponseEntity<ApiResponse> cancelABooking(String referenceNumber, CancelABookingDto cancelABookingDto) {
        User user = appUtil.getLoggedInUser();
        Long clientCode = user.getClientCode();
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("You have no order with the reference number " + referenceNumber)));

        if(order.get().getOrderStatus().equals(OrderStatus.INPROGRESS)){
            return ResponseEntity.ok(new ApiResponse("Forbidden", "A rider has been dispatched to the address already.", null));
        }
        if(order.get().getOrderStatus().equals(OrderStatus.COMPLETED)){
            return ResponseEntity.ok(new ApiResponse<>("Failed", "This order has been completed!.", null));
        }

        order.get().setOrderStatus(OrderStatus.CANCELLED);
        order.get().setReasonForOrderCancellation(cancelABookingDto.getReasonForOrderCancellation());
        orderRepository.save(order.get());

        String URL = "http://localhost:8080/api/v1/auth/cancelled-order/?order=" + order.get().getId();
        String link = "<h3>Hello " +"<br> This order has been cancelled.<a href=" + URL + "><br>Activate</a></h3>" + '\n'+
                "Reason for cancellation:  " + order.get().getReasonForOrderCancellation();
        emailService.sendEmail("chigozieenyoghasi@yahoo.com","AriXpress: Cancelled Order", link);
        return ResponseEntity.ok(new ApiResponse("Success", "Order has been cancelled", null));
    }

    @Override //tested and is working fine
    public ResponseEntity<ApiResponse> confirmDelivery(String referenceNumber) {
        User user = appUtil.getLoggedInUser();
        Long clientCode = user.getClientCode();
        if(!(user.getRole().equals(Role.CUSTOMER)))
            throw new ValidationException("You cannot perform this action!");
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order with Id " + referenceNumber + " was not found")));
        order.get().setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.save(order.get());

        Optional<User> user1 = userRepository.findByStaffId(order.get().getRiderId());
        user1.get().setRiderStatus(RiderStatus.Free);
        userRepository.save(user1.get());

        return ResponseEntity.ok(new ApiResponse<>("Success", "Order status updated", null));
    }
    @Override // Tested and is working fine
    public ResponseEntity<ApiResponse> giveFeedback(String referenceNumber, GiveFeedbackDto giveFeedbackDto) {
        User user = appUtil.getLoggedInUser();
        Long clientCode = user.getClientCode();
        if(!(user.getRole().equals(Role.CUSTOMER)))
            throw new ValidationException("You cannot perform this action!");

        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order with number " + referenceNumber + " was not found")));
        order.get().setFeedback(giveFeedbackDto.getFeedback());
        orderRepository.save(order.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Feedback Received. Thank you.", null));
    }

    @Override //tested and is working fine
    public List<Orders> weeklyOrderSummary(WeeklyOrderSummaryDto weeklyOrderSummaryDto) {
        User user = appUtil.getLoggedInUser();
        Long clientCode = user.getClientCode();
        if(!(user.getRole().equals(Role.CUSTOMER)))
            throw new ValidationException("You are not authorised to perform this action");
        if(!(weeklyOrderSummaryDto.getClientCode().equals(clientCode)))
            throw new ValidationException("Client code is wrong!");

        return new ArrayList<>(orderRepository.findAllByClientCodeAndCreatedAtBetween(weeklyOrderSummaryDto.getClientCode(), weeklyOrderSummaryDto.getStartDate(), weeklyOrderSummaryDto.getEndDate()));
    }

    @Override
    public ResponseEntity<ApiResponse> viewRiderLocation(ViewRiderLocationDto viewRiderLocationDto) {
        return null;
    }

}
