package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.service.StaffService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/staff/sign-up")
    ApiResponse signUp(@RequestBody StaffSignUpDto staffSignUpDto){
        return staffService.signUp(staffSignUpDto);
    }

    @PostMapping("/staff/complete-staff-registration")
    ApiResponse completeRegistration(@Valid @RequestBody CompleteClientRegistrationDto completeClientRegistrationDto){
        return staffService.completeRegistration(completeClientRegistrationDto);
    }

    @PostMapping("/staff/login")
    public String login(@RequestBody @Valid LoginDto loginDto){
        return staffService.login(loginDto);
    }

    @PostMapping("/staff/forgot-password")
    public ApiResponse forgotPassword(@RequestBody @Valid String email) {
    return staffService.forgotPassword(email);
    }

    @PatchMapping("/staff/reset-password")
    public ApiResponse resetPassword(@RequestBody @Valid ResetPasswordDto resetPasswordDto) {
    return staffService.resetPassword(resetPasswordDto);
    }

    @PatchMapping("/staff/change-password")
    public ApiResponse changePassword(@RequestBody @Valid ChangePasswordDto changePasswordDto) {
    return staffService.changePassword(changePasswordDto);
    }

    @PatchMapping("/staff/assign-rider-to-bike")
    public ApiResponse assignRiderToBike(@RequestBody @Valid String email, String bikeNumber) {
    return staffService.assignRiderToBike(email, bikeNumber);
    }

    @PostMapping("/staff/register-bike")
    public ApiResponse registerABike(@RequestBody @Valid RegisterBikeDto registerBikeDto) {
    return staffService.registerABike(registerBikeDto);
    }

    @PostMapping("/register-rider")
    public ApiResponse registerARider(@RequestBody @Valid RegisterRiderDto registerRiderDto) {
    return staffService.registerARider(registerRiderDto);
    }

    @GetMapping("/count-rider-trips")
    public Integer countRidesPerRider(@PathVariable String phoneNumber) {
    return staffService.countRidesPerRider(phoneNumber);
    }

}
