package com.example.demo.service;

import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteStaffRegistrationDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.StaffSignUpDto;
import com.example.demo.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface RiderService {

    ResponseEntity<ApiResponse> riderSignUp (StaffSignUpDto signUpDto);

    ResponseEntity<ApiResponse> completeRiderRegistration(CompleteStaffRegistrationDto completeStaffRegistrationDto);

    ResponseEntity<ApiResponse> riderEmailLogin (String email, String password);

    ResponseEntity<ApiResponse> riderPhoneNumberLogin (String phoneNumber, String password);

    ResponseEntity<ApiResponse> riderLoginWithId (String id);

    ResponseEntity<ApiResponse> forgotPassword (String email);

    ResponseEntity<ApiResponse> resetPassword (ResetPasswordDto resetPasswordDto);

    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);

}
