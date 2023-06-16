package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.configuration.security.CustomUserDetailService;
import com.example.demo.configuration.security.JwtUtils;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.CustomerType;
import com.example.demo.enums.OrderStatus;
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

import java.util.InputMismatchException;
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


    @Override
    public ResponseEntity<ApiResponse> signUp(CustomerSignUpDto customerSignUpDto) throws ValidationException {

        if (!appUtil.isValidEmail(customerSignUpDto.getEmail()))
            throw new ValidationException("Email is invalid");

        Boolean isUserExist = userRepository.existsByEmail(customerSignUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

        if(!(customerSignUpDto.getConfirmPassword().equals(customerSignUpDto.getPassword())))
            throw new InputMismatchException("Confirm password and Password do not match!");

        User user = new User();
        user.setFirstName(customerSignUpDto.getFirstName());
        user.setLastName(customerSignUpDto.getLastName());
        user.setEmail(customerSignUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(customerSignUpDto.getPassword()));
        user.setRole((Role.ROLE_CUSTOMER));
        String token = jwtUtils.generateSignUpConfirmationToken(customerSignUpDto.getEmail());
        user.setConfirmationToken(token);
        userRepository.save(user);

        String URL = "http://localhost:8080/api/v1/auth/complete-registration/?token=" + token;
        String link = "<h3>Hello "  + customerSignUpDto.getFirstName()  +"<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";

        emailService.sendEmail(customerSignUpDto.getEmail(),"AriXpress: Verify Your Account", link);

        return ResponseEntity.ok(new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null));
    }

    @Override
    public ResponseEntity<ApiResponse> completeRegistration(CompleteClientRegistrationDto completeClientRegistrationDto) {
        Optional<User> existingUser = userRepository.findByConfirmationToken(completeClientRegistrationDto.getToken());

        if (existingUser.isPresent()) {
            if (existingUser.get().isActive()) {
                throw new AccountAlreadyActivatedException("This account is already activated. Pls login");
            }
            existingUser.get().setDob(completeClientRegistrationDto.getDob());
            existingUser.get().setAddress(completeClientRegistrationDto.getAddress());
            existingUser.get().setPhoneNumber(completeClientRegistrationDto.getPhoneNumber());
            existingUser.get().setState(completeClientRegistrationDto.getState());
            existingUser.get().setGender(completeClientRegistrationDto.getGender());
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
        if(!passwordEncoder.matches(loginDto.getPassword(),(users.getPassword())))
            throw new ValidationException("Password is Incorrect!");

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
    public ResponseEntity<ApiResponse> bookADelivery(DirectDeliveryDto directDeliveryDto) {
        User user = appUtil.getLoggedInUser();
        String email = user.getEmail();

        Orders orders = new Orders();
        orders.setCustomerId(user.getId());
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
        orderRepository.save(orders);

        String URL = "http://localhost:8080/api/v1/auth/new-order/?order=" + orders.getId();
        String link = "<h3>Hello " +"<br> Click the link below dispatch the new order <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail("chigozieenyoghasi@yahoo.com","AriXpress: A new order", link);

        return ResponseEntity.ok(new ApiResponse<>("Success", "Delivery booked successfully. Details to be sent to you shortly.", null));

    }

    @Override
    public ResponseEntity<ApiResponse> thirdPartySender(ThirdPartySenderDto thirdPartySenderDto) {
        User user = appUtil.getLoggedInUser();
        String email = user.getEmail();
        Long id = user.getId();

        Orders order = new Orders();
        order.setThirdPartyPickUp(true);
        order.setCustomerId(id);
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
        order.setPrice(6000.00);
        order.setDistance(23.4);
        orderRepository.save(order);

        String URL = "http://localhost:8080/api/v1/auth/new-order/?order=" + order.getId();
        String link = "<h3>Hello " +"<br> Click the link below dispatch the new order <a href=" + URL + "><br>Activate</a></h3>";
        emailService.sendEmail("chigozieenyoghasi@yahoo.com","AriXpress: A new order", link);

        return ResponseEntity.ok(new ApiResponse<>("Success", "Delivery booked successfully. Details to be sent to you shortly.", null));

    }
    @Override
    public ResponseEntity<ApiResponse> cancelABooking(CancelABookingDto cancelABookingDto) {
        User user = appUtil.getLoggedInUser();
        String email = user.getEmail();
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByEmailAndId(email, cancelABookingDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("There is no order with the Id " + cancelABookingDto.getId())));

        order.get().setOrderStatus(OrderStatus.CANCELLED);
        order.get().setReasonForOrderCancellation(cancelABookingDto.getReasonForOrderCancellation());
        orderRepository.save(order.get());

        String URL = "http://localhost:8080/api/v1/auth/cancelled-order/?order=" + order.get().getId();
        String link = "<h3>Hello " +"<br> This order has been cancelled.<a href=" + URL + "><br>Activate</a></h3>" + '\n'+
                "Reason for cancellation:  " + order.get().getReasonForOrderCancellation();
        emailService.sendEmail("chigozieenyoghasi@yahoo.com","AriXpress: Cancelled Order", link);

        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> updateOrderStatus(UpdateOrderStatusDto updateOrderStatusDto) {
        User user = appUtil.getLoggedInUser();
        String email = user.getEmail();
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByEmailAndId(email, updateOrderStatusDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order with Id " + updateOrderStatusDto.getId() + " was not found")));
        order.get().setOrderStatus(updateOrderStatusDto.getStatus());
        orderRepository.save(order.get());

        return ResponseEntity.ok(new ApiResponse<>("Success", "Order status updated", null));
    }

    @Override
    public ResponseEntity<ApiResponse> giveFeedback(GiveFeedbackDto giveFeedbackDto) {
        User user = appUtil.getLoggedInUser();
        String email = user.getEmail();
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByEmailAndId(email, giveFeedbackDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order with Id " + giveFeedbackDto.getId() + " was not found")));
        order.get().setFeedback(giveFeedbackDto.getFeedback());
        orderRepository.save(order.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Feedback Received. Thank you.", null));
    }

    @Override
    public ResponseEntity<ApiResponse> viewRiderLocation(ViewRiderLocationDto viewRiderLocationDto) {
        return null;
    }

}
