package com.example.demo.controller;

import com.example.demo.dto.request.CompleteBusinessRegistrationDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.ForgotPasswordDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.request.StaffRelevantDetailsDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.service.CustomerService;
import com.example.demo.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/")
public class AuthController {

    private final CustomerService customerService;

    private final StaffService staffService;

    @PostMapping("/client/sign-up")
    public ResponseEntity<ApiResponse> customerSignUp(@RequestBody @Valid SignUpDto signUpDto) throws ValidationException {
        System.out.println("Request body comes here " + signUpDto.toString());
        return customerService.customerSignUp(signUpDto);
    }

    @PostMapping("/client/complete-registration")
    public ResponseEntity<ApiResponse> customerCompleteRegistration(@Valid @RequestBody CompleteRegistrationDto completeRegistrationDto) {
        return customerService.customerCompleteRegistration(completeRegistrationDto);
    }

    @PostMapping("/client/corporate/complete-business-registration")
    public ResponseEntity<ApiResponse> completeBusinessRegistration(@Valid @RequestBody CompleteBusinessRegistrationDto completeBusinessRegistrationDto) {
        return customerService.completeBusinessRegistration(completeBusinessRegistrationDto);
    }

    @PostMapping("/client/login")
    public ResponseEntity<String> customerLogin(@Valid @RequestBody LoginDto loginDto) {
        return customerService.customerLogin(loginDto);
    }

    @PostMapping("/client/forgot-password")
    public ResponseEntity<ApiResponse> customerForgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto){
        return customerService.customerForgotPassword(forgotPasswordDto);
    }

    @PostMapping("/client/reset-password")
    public ResponseEntity<ApiResponse> customerResetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        return customerService.customerResetPassword(resetPasswordDto);
    }

    @PostMapping("/staff/sign-up")
    public ResponseEntity<ApiResponse> staffSignUp(@Valid @RequestBody SignUpDto signUpDto) {
        return staffService.staffSignUp(signUpDto);
    }

    @PostMapping("/staff/complete-registration")
    public ResponseEntity<ApiResponse> completeRegistration(@Valid @RequestBody CompleteRegistrationDto completeRegistrationDto) {
        return staffService.staffCompleteRegistration(completeRegistrationDto);
    }

    @PostMapping("/staff/login")
    public ResponseEntity<String> staffLogin(@Valid @RequestBody LoginDto loginDto) {
        return staffService.staffLogin(loginDto);
    }

    @PostMapping("/staff/forgot-password")
    public ResponseEntity<ApiResponse> staffForgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {
        return staffService.staffForgotPassword(forgotPasswordDto);
    }

    @PostMapping("/staff/reset-password")
    public ResponseEntity<ApiResponse> staffResetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        return staffService.staffResetPassword(resetPasswordDto);
    }


}