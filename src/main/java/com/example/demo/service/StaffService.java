package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.model.Orders;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StaffService {

    ApiResponse signUp(StaffSignUpDto staffSignUpDto);

    ApiResponse completeRegistration (CompleteClientRegistrationDto completeClientRegistrationDto);

    String login (LoginDto loginDto);

    ApiResponse forgotPassword (String email);

    ApiResponse resetPassword (ResetPasswordDto resetPasswordDto);

    ApiResponse changePassword (ChangePasswordDto changePasswordDto);

    ApiResponse assignRiderToBike(String email, String bikeNumber);

    ApiResponse registerABike(RegisterBikeDto registerBikeDto);

    ApiResponse registerARider(RegisterRiderDto registerRiderDto);

    Integer countRidesPerRider (String phoneNumber);

}
