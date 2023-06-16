package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@RequestBody @Valid CustomerSignUpDto customerSignUpDto) throws ValidationException {
        return customerService.signUp(customerSignUpDto);
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<ApiResponse> completeRegistration(@Valid @RequestBody CompleteClientRegistrationDto completeClientRegistrationDto) {
        return customerService.completeRegistration(completeClientRegistrationDto);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDto loginDto) {
        return customerService.login(loginDto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody String email) {
        return customerService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        return customerService.resetPassword(resetPasswordDto);
    }

    @PostMapping("/customer/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        return customerService.changePassword(changePasswordDto);
    }

    @PostMapping("/customer/new-order")
    public ResponseEntity<ApiResponse> bookADelivery(@Valid @RequestBody DirectDeliveryDto directDeliveryDto) {
        return customerService.bookADelivery(directDeliveryDto);
    }

    @PostMapping("/customer/thirdparty-order")
    ResponseEntity<ApiResponse> thirdPartySender(@Valid @RequestBody ThirdPartySenderDto thirdPartySenderDto){
        return customerService.thirdPartySender(thirdPartySenderDto);
    }

    @PatchMapping("/customer/cancel-order")
    public ResponseEntity<ApiResponse> cancelABooking(@Valid @RequestBody CancelABookingDto cancelABookingDto) {
        return customerService.cancelABooking(cancelABookingDto);
    }

    @PatchMapping("/customer/update-order-status")
    public ResponseEntity<ApiResponse> updateOrderStatus(UpdateOrderStatusDto updateOrderStatusDto) {
        return customerService.updateOrderStatus(updateOrderStatusDto);
    }

    @PatchMapping("/customer/feedback")
    public ResponseEntity<ApiResponse> giveFeedback(GiveFeedbackDto giveFeedbackDto) {
        return customerService.giveFeedback(giveFeedbackDto);
    }


}
