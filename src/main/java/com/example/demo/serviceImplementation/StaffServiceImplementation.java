package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.configuration.security.CustomUserDetailService;
import com.example.demo.configuration.security.JwtUtils;
import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

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
    public ApiResponse signUp(StaffSignUpDto staffSignUpDto) {

        if (!appUtil.isValidEmail(staffSignUpDto.getEmail()))
            throw new ValidationException("Email is invalid");

        Boolean isUserExist = userRepository.existsByEmail(staffSignUpDto.getEmail());
        if (isUserExist)
            throw new ValidationException("User Already Exists!");

        if(!(staffSignUpDto.getConfirmPassword().equals(staffSignUpDto.getPassword())))
            throw new InputMismatchException("Confirm password and Password do not match!");

        User user = new User();
        user.setFirstName(staffSignUpDto.getFirstName());
        user.setLastName(staffSignUpDto.getLastName());
        user.setEmail(staffSignUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(staffSignUpDto.getPassword()));
        user.setRole(Role.ROLE_STAFF);
        String token = jwtUtils.generateSignUpConfirmationToken(staffSignUpDto.getEmail());
        user.setConfirmationToken(token);
        userRepository.save(user);

        String URL = "http://localhost:8080/api/v1/auth/verify-link/?token=" + token;
        String link = "<h3>Hello "  + staffSignUpDto.getFirstName()  +"<br> Click the link below to activate your account <a href=" + URL + "><br>Activate</a></h3>";

        emailService.sendEmail(staffSignUpDto.getEmail(),"AriXpress: Verify Your Account", link);

        return new ApiResponse<>("Successful", "SignUp Successful. Check your mail to activate your account", null);
    }

    @Override
    public ApiResponse completeRegistration(CompleteClientRegistrationDto completeClientRegistrationDto) {
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
            return new ApiResponse<>("Successful", "Registration completed", null);
        }
        return new ApiResponse<>("Failed", "This user does not exist. Kindly sign up.", null);
    }

    @Override
    public String login(LoginDto loginDto) {
        User users = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User Not Found"));
        if(!users.isActive()){
            throw new ValidationException("User Not Active. Kindly complete your registration.");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        UserDetails user = customUserDetailService.loadUserByUsername(loginDto.getEmail());
        if(user == null) {
            return "Login attempt unsuccessful. Please, try again.";
        }
        return jwtUtils.generateToken(user);

    }

    @Override
    public ApiResponse forgotPassword(String email) {
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
        return new ApiResponse<>("Sent", "Check your email to reset your password", null);

    }

    @Override
    public ApiResponse resetPassword(ResetPasswordDto resetPasswordDto) {
        Optional<User> user = Optional.ofNullable(userRepository.findByConfirmationToken(resetPasswordDto.getConfirmationToken())
                .orElseThrow(() -> new ValidationException("Token is incorrect or User does not exist!")));

        if(!resetPasswordDto.getConfirmNewPassword().equals(passwordEncoder.encode(resetPasswordDto.getNewPassword()))){
            throw new InputMismatchException("Passwords do not match!");
        }
        user.get().setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        userRepository.save(user.get());

        return new ApiResponse<>("Success", "Password reset successful.", null);
    }

    @Override
    public ApiResponse changePassword(ChangePasswordDto changePasswordDto) {
        User user = appUtil.getLoggedInUser();
        if(!(changePasswordDto.getConfirmNewPassword().equals(changePasswordDto.getNewPassword()))){
            throw new InputMismatchException("Confirm password and Password do not match!");
        }
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
        return new ApiResponse<>("Success", "Password change successful", null);
    }

    @Override
    public ApiResponse assignRiderToBike(String email, String bikeNumber) {
        User user = appUtil.getLoggedInUser();
        if(!user.getRole().equals(Role.ROLE_STAFF)){
            throw new UnsupportedOperationException("You are not authorised to perform this operation");
        }
        User user1 = userRepository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException("This user does not exist"));

        if(!user1.getRole().equals(Role.ROLE_RIDER))
            throw new UnsupportedOperationException("You cannot perform this operation on this user");

        Optional<Bike> bike = bikeRepository.findByBikeNumber(bikeNumber);
        if(bike.isEmpty())
            throw new ResourceNotFoundException("There is no bike with this number!");

        bike.get().setRiderName(user1.getFirstName());
        bike.get().setRiderPhoneNumber(user1.getPhoneNumber());
        bike.get().setRiderAddress(user1.getAddress());
        bikeRepository.save(bike.get());

        return new ApiResponse<>("Success", "Successful", bike.get().getRiderName());
    }

    @Override
    public ApiResponse registerABike(RegisterBikeDto registerBikeDto) {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_ADMIN))){
            throw new ValidationException("You are not authorised to perform this operation.");
        }
        Bike bike = new Bike();
        bike.setBikeNumber(registerBikeDto.getBikeNumber());
        bike.setPrice(registerBikeDto.getPrice());
        bike.setMake(registerBikeDto.getMake());
        bike.setImages(registerBikeDto.getImages());
        bikeRepository.save(bike);

        return new ApiResponse<>("Success", "Bike registered", null);
    }

    @Override
    public ApiResponse registerARider(RegisterRiderDto registerRiderDto) {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_ADMIN))){
            throw new ValidationException("You are not authorised to perform this operation.");
        }
        User user1 = new User();
        user1.setRole(Role.ROLE_RIDER);
        user1.setFirstName(registerRiderDto.getFirstName());
        user1.setLastName(registerRiderDto.getLastName());
        user1.setAddress(registerRiderDto.getAddress());
        user1.setPhoneNumber(registerRiderDto.getPhoneNumber());
        user1.setPassword(passwordEncoder.encode(registerRiderDto.getPassword()));
        userRepository.save(user1);

        return null;
    }

    @Override
    public Integer countRidesPerRider(String phoneNumber) {
        User user = appUtil.getLoggedInUser();
        if(!(user.getRole().equals(Role.ROLE_STAFF))){
            throw new ValidationException("You are not authorised to perform this operation.");
        }

        List<Orders> ordersList = new ArrayList<>();
        ordersList.add((Orders) orderRepository.findByRiderPhoneNumber(phoneNumber));
        Integer orderCount = ordersList.size();
        return orderCount;
    }
}
