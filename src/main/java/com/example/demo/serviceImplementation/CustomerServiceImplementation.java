package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.configuration.security.CustomUserDetailService;
import com.example.demo.configuration.security.JwtUtils;
import com.example.demo.dto.request.CancelABookingDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteBusinessRegistrationDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.DirectDeliveryDto;
import com.example.demo.dto.request.ForgotPasswordDto;
import com.example.demo.dto.request.GiveFeedbackDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.request.ThirdPartySenderDto;
import com.example.demo.dto.request.UpdateCustomerDetailsDto;
import com.example.demo.dto.request.ViewRiderLocationDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.CustomerType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.RiderStatus;
import com.example.demo.enums.Role;
import com.example.demo.exceptions.AccountAlreadyActivatedException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Customer;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.StaffRepository;
import com.example.demo.service.CustomerService;
import com.example.demo.service.StaffService;
import com.example.demo.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImplementation implements CustomerService {


    private final StaffService staffService;
    private final StaffRepository staffRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final AppUtil appUtil;
    private final OrderRepository orderRepository;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    @Override //tested and working fine
    public ResponseEntity<ApiResponse> customerSignUp(SignUpDto signUpDto) throws ValidationException {

        if (!appUtil.isValidEmail(signUpDto.getEmail()))
            throw new ValidationException("Email is invalid");

        Boolean isUserExist = customerRepository.existsByEmail(signUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

         if((!appUtil.isValidPassword(signUpDto.getPassword())))
            throw new ValidationException("Password MUST be between 8 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character like #,@,%,$");

        if(!(signUpDto.getConfirmPassword().equals(signUpDto.getPassword())))
            throw new InputMismatchException("Confirm password and Password do not match!");

        Boolean registerAsACompany = signUpDto.getRegisterAsACompany();

        if(registerAsACompany.equals(true)){

            Customer user = new Customer();
            user.setFirstName(signUpDto.getFirstName());
            user.setLastName(signUpDto.getLastName());
            user.setEmail(signUpDto.getEmail());
            user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
            user.setRole((Role.CUSTOMER));
            user.setCustomerType(CustomerType.Corporate);
            String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
            user.setConfirmationToken(token);
            customerRepository.save(user);

            String URL = "http://localhost:8080/api/v1/auth/client/corporate/complete-business-registration/?token=" + token;
            String link = "<h3>Hello "  + signUpDto.getFirstName()  +"<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";
            emailService.sendEmail(signUpDto.getEmail(),"AriXpress: Verify Your Account", link);
        }
        else{
            Customer user = new Customer();
            user.setFirstName(signUpDto.getFirstName());
            user.setLastName(signUpDto.getLastName());
            user.setEmail(signUpDto.getEmail());
            user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
            user.setRole((Role.CUSTOMER));
            user.setCustomerType(CustomerType.Individual);
            String token = jwtUtils.generateSignUpConfirmationToken(signUpDto.getEmail());
            user.setConfirmationToken(token);
            customerRepository.save(user);

            String URL = "http://localhost:8080/api/v1/auth/client/complete-registration/?token=" + token;
            String link = "<h3>Hello " + signUpDto.getFirstName() + "<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";
            emailService.sendEmail(signUpDto.getEmail(), "AriXpress: Verify Your Account", link);
            }

        return ResponseEntity.ok(new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null));
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> customerCompleteRegistration(CompleteRegistrationDto completeRegistrationDto) {
        Optional<Customer> existingUser = Optional.ofNullable(customerRepository.findByConfirmationToken(completeRegistrationDto.getToken())
                .orElseThrow(() -> new UserNotFoundException("User does not exist!")));

        Boolean activeUser = existingUser.get().isActive();
        if (activeUser.equals(true)) {
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
            customerRepository.save(existingUser.get());
            return ResponseEntity.ok(new ApiResponse<>("Successful", "Registration completed", "Your client number is "+clientCode));
        }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> completeBusinessRegistration(CompleteBusinessRegistrationDto completeBusinessRegistrationDto) {
        Optional<Customer> existingUser = Optional.ofNullable(customerRepository.findByConfirmationToken(completeBusinessRegistrationDto.getToken())
                .orElseThrow(() -> new UserNotFoundException("User does not exist!")));

        Boolean activeUser = existingUser.get().isActive();
        if (activeUser.equals(true)) {
            throw new AccountAlreadyActivatedException("This account is already activated. Pls login");
        }
        Long clientCode = appUtil.generateRandomCode();
        existingUser.get().setCompanyName(completeBusinessRegistrationDto.getCompanyName());
        existingUser.get().setAddress(completeBusinessRegistrationDto.getAddress());
        existingUser.get().setPaymentType(completeBusinessRegistrationDto.getPaymentType());
        existingUser.get().setPaymentInterval(completeBusinessRegistrationDto.getPaymentInterval());
        existingUser.get().setPhoneNumber(completeBusinessRegistrationDto.getPhoneNumber());
        existingUser.get().setState(completeBusinessRegistrationDto.getState());
        existingUser.get().setClientCode(clientCode);
        existingUser.get().setActive(true);
        customerRepository.save(existingUser.get());

        return ResponseEntity.ok(new ApiResponse("Successful", "Corporate Client Registration Successful.", "You unique code is "+ clientCode ));
    }

    @Override //tested and working fine
    public ResponseEntity<String> customerLogin(LoginDto loginDto) {

        Customer customer = customerRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
        if(!customer.isActive()){
            throw new ValidationException("User Not Active. Kindly complete your registration.");
        }
        if(!passwordEncoder.matches(loginDto.getPassword(),(customer.getPassword())))
            throw new ValidationException("Password is Incorrect!");

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        UserDetails user = customUserDetailService.loadUserByUsername(loginDto.getEmail());
        if(user != null) {
            return ResponseEntity.ok(jwtUtils.generateToken(user));
        }
        return ResponseEntity.status(400).body("Some error occurred");
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> customerForgotPassword(ForgotPasswordDto forgotPasswordDto) {
        Optional<Customer> user = customerRepository.findByEmail(forgotPasswordDto.getEmail());
        if(user.isEmpty()) {
            throw new UserNotFoundException("User does not exist");
        }
        String token = jwtUtils.resetPasswordToken(forgotPasswordDto.getEmail());
        user.get().setConfirmationToken(token);
        customerRepository.save(user.get());

        String URL = "http://localhost:8080/api/v1/auth/client/reset-password/?token=" + token;
        String link = "<h3>Hello " +"<br> Click the link below to reset your password <a href=" + URL + "><br>Reset Password</a></h3>";
        emailService.sendEmail(forgotPasswordDto.getEmail(), "AriXpress: Reset your password", link);
        return ResponseEntity.ok(new ApiResponse<>("Sent", "Check your email to reset your password", null));
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> customerResetPassword(ResetPasswordDto resetPasswordDto) {
        Optional<Customer> user = Optional.ofNullable(customerRepository.findByConfirmationToken(resetPasswordDto.getConfirmationToken())
                .orElseThrow(() -> new ValidationException("Token is incorrect or User does not exist!")));

        if(!appUtil.isValidPassword(resetPasswordDto.getNewPassword()))
            throw new ValidationException("Password MUST be between 6 and 15 characters, and  must contain an UPPERCASE, a lowercase, a number, and a special character like #,@,%,$");

        if(!resetPasswordDto.getConfirmNewPassword().equals(resetPasswordDto.getNewPassword())){
            throw new InputMismatchException("Passwords do not match!");
        }
        user.get().setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        customerRepository.save(user.get());

        return ResponseEntity.ok(new ApiResponse<>("Success", "Password reset successful.", null));
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        if(!(changePasswordDto.getConfirmNewPassword().equals(changePasswordDto.getNewPassword()))){
            throw new InputMismatchException("Confirm password and Password do not match!");
        }
        customer.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Password change successful", null));
    }

    @Override //tested and working fine
    public ResponseEntity<ApiResponse> bookADelivery(DirectDeliveryDto directDeliveryDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        String email = customer.getEmail();
        String id = appUtil.generateSerialNumber("AXL-");
        String adminEmail = "chigozieenyoghasi@yahoo.com";

        if(customer.getCustomerType().equals(CustomerType.Corporate)) {
            Orders orders = new Orders();
            orders.setClientCode(customer.getClientCode());
            orders.setThirdPartyPickUp(false);
            orders.setReferenceNumber(id);
            orders.setCompanyName(customer.getCompanyName());
            orders.setPickUpAddress(customer.getAddress());
            orders.setDeliveryAddress(directDeliveryDto.getDeliveryAddress());
            orders.setReceiverName(directDeliveryDto.getReceiverName());
            orders.setReceiverPhoneNumber(directDeliveryDto.getReceiverPhoneNumber());
            orders.setItemType(directDeliveryDto.getItemType());
            orders.setItemQuantity(directDeliveryDto.getItemQuantity());
            orders.setOrderStatus(OrderStatus.INPROGRESS);
            orders.setEmail(email);
            orders.setPrice(6000.00);
            orderRepository.save(orders);
        }
        Orders orders = new Orders();
        orders.setClientCode(customer.getClientCode());
        orders.setThirdPartyPickUp(false);
        orders.setReferenceNumber(id);
        orders.setCustomerFirstName(customer.getFirstName());
        orders.setCustomerLastName(customer.getLastName());
        orders.setItemType(directDeliveryDto.getItemType());
        orders.setItemQuantity(directDeliveryDto.getItemQuantity());
        orders.setDeliveryAddress(directDeliveryDto.getDeliveryAddress());
        orders.setPickUpAddress(directDeliveryDto.getPickUpAddress());
        orders.setReceiverName(directDeliveryDto.getReceiverName());
        orders.setReceiverPhoneNumber(directDeliveryDto.getReceiverPhoneNumber());
        orders.setItemType(directDeliveryDto.getItemType());
        orders.setPaymentType(directDeliveryDto.getPaymentType());
        orders.setOrderStatus(OrderStatus.PENDING);
        orders.setPrice(6000.00);
        orders.setEmail(email);
        orderRepository.save(orders);

        String url = UriComponentsBuilder.fromUriString("http://localhost:8080/api/v1/auth/dispatch-order/")
                .queryParam("clientCode",orders.getClientCode())
                .queryParam("referenceNumber", orders.getReferenceNumber())
                .build()
                .toUriString();
        String link = "<h3>Hello " +"<br> Click the link to dispatch a new order <a href=" + url + "><br>Dispatch</a></h3>";
        emailService.sendEmail(adminEmail,"AriXpress: A new order", link);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Delivery booked successfully. Details to be sent to you shortly.", null));

    }

    @Override //tested and is working fine
    public ResponseEntity<ApiResponse> thirdPartySender(ThirdPartySenderDto thirdPartySenderDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        String email = customer.getEmail();
        String adminEmail = "chigozieenyoghasi@yahoo.com";
        Long id = customer.getId();
        String oid = appUtil.generateSerialNumber("AXL-");

        Orders order = new Orders();
        order.setThirdPartyPickUp(true);
        order.setClientCode(id);
        order.setReferenceNumber(oid);
        order.setCustomerFirstName(customer.getFirstName());
        order.setCustomerLastName(customer.getLastName());
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

        String url = UriComponentsBuilder.fromUriString("http://localhost:8080/api/v1/auth/dispatch-order/")
                .queryParam("clientCode",order.getClientCode())
                .queryParam("referenceNumber", order.getReferenceNumber())
                .build()
                .toUriString();
        String link = "<h3>Hello " +"<br> Click the link to dispatch a new order <a href=" + url + "><br>Dispatch</a></h3>";
        emailService.sendEmail(adminEmail,"AriXpress: A new order", link);
        return ResponseEntity.ok(new ApiResponse<>("Success", "Delivery booked successfully. Details to be sent to you shortly.", order));

    }
    @Override //Tested and is working fine
    public ResponseEntity<ApiResponse> cancelABooking(String referenceNumber, CancelABookingDto cancelABookingDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long clientCode = customer.getClientCode();
        String adminEmail = "chigozieenyoghasi@yahoo.com";
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

        String url = UriComponentsBuilder.fromUriString("http://localhost:8080/api/v1/auth/view-an-order/")
                .queryParam("referenceNumber", order.get().getReferenceNumber())
                .build()
                .toUriString();
        String link = "<h3>Hello Admin, " +"<br> An order has been cancelled. Click the link below to see details<a href=" + url + "><br>Cancelled Order</a></h3>";
        emailService.sendEmail(adminEmail,"AriXpress: Cancelled Order", link);
        return ResponseEntity.ok(new ApiResponse("Success", "Your booking has been cancelled", null));
    }

    @Override //tested and is working fine
    public ResponseEntity<ApiResponse> confirmDelivery(String referenceNumber) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long clientCode = customer.getClientCode();
        if(!(customer.getRole().equals(Role.CUSTOMER)))
            throw new ValidationException("You cannot perform this action!");
        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order with Id " + referenceNumber + " was not found")));

        order.get().setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.save(order.get());

        Optional<Staff> rider = staffRepository.findByStaffId(order.get().getRiderId());
        rider.get().setRiderStatus(RiderStatus.Free);
        staffRepository.save(rider.get());

        return ResponseEntity.ok(new ApiResponse<>("Success", "Order status updated", null));
    }
    @Override // Tested and is working fine
    public ResponseEntity<ApiResponse> giveFeedback(String referenceNumber, GiveFeedbackDto giveFeedbackDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long clientCode = customer.getClientCode();
        if(!(customer.getRole().equals(Role.CUSTOMER)))
            throw new ValidationException("You cannot perform this action!");

        Optional<Orders> order = Optional.ofNullable(orderRepository.findByClientCodeAndReferenceNumber(clientCode, referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order with number " + referenceNumber + " was not found")));
        order.get().setFeedback(giveFeedbackDto.getFeedback());
        orderRepository.save(order.get());
        return ResponseEntity.ok(new ApiResponse<>("Success", "Feedback Received. Thank you.", null));
    }

    @Override //tested and is working fine
    public List<Orders> weeklyOrderSummary(WeeklyOrderSummaryDto weeklyOrderSummaryDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        Long clientCode = customer.getClientCode();
        if(!(customer.getRole().equals(Role.CUSTOMER)))
            throw new ValidationException("You are not authorised to perform this action");
        if(!(weeklyOrderSummaryDto.getClientCode().equals(clientCode)))
            throw new ValidationException("Client code is wrong!");

        return new ArrayList<>(orderRepository.findAllByClientCodeAndCreatedAtBetween(weeklyOrderSummaryDto.getClientCode(), weeklyOrderSummaryDto.getStartDate(), weeklyOrderSummaryDto.getEndDate()));
    }

    @Override
    public ResponseEntity<ApiResponse> viewRiderLocation(ViewRiderLocationDto viewRiderLocationDto) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> updateCustomerDetails(UpdateCustomerDetailsDto updateCustomerDetailsDto) {
        Customer customer = appUtil.getLoggedInCustomer();
        String email = customer.getEmail();
        customerRepository.findByEmail(email);
        if(customer.isActive())
            customer.setFirstName(updateCustomerDetailsDto.getFirstName());
        customer.setLastName(updateCustomerDetailsDto.getLastName());
        customer.setPhoneNumber(updateCustomerDetailsDto.getPhoneNumber());
        customer.setDob(updateCustomerDetailsDto.getDob());
        customer.setEmail(updateCustomerDetailsDto.getEmail());
        customer.setAddress(updateCustomerDetailsDto.getAddress());
        customerRepository.save(customer);
        return ResponseEntity.ok(new ApiResponse("Suceess", "Your details have been updated!", null));
    }

}
