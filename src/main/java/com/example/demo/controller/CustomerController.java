package com.example.demo.controller;

import com.example.demo.dto.request.CancelABookingDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.DirectDeliveryDto;
import com.example.demo.dto.request.GiveFeedbackDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.request.ThirdPartySenderDto;
import com.example.demo.dto.request.UpdateOrderStatusDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Orders;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/client")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@RequestBody @Valid SignUpDto signUpDto) throws ValidationException {
        System.out.println("Request body comes here " + signUpDto.toString());
        return customerService.signUp(signUpDto);
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<ApiResponse> completeRegistration(@Valid @RequestBody CompleteRegistrationDto completeRegistrationDto) {
        return customerService.completeRegistration(completeRegistrationDto);
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

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        return customerService.changePassword(changePasswordDto);
    }

    @PostMapping("/new-order")
    public ResponseEntity<ApiResponse> bookADelivery(@Valid @RequestBody DirectDeliveryDto directDeliveryDto) {
        return customerService.bookADelivery(directDeliveryDto);
    }

    @PostMapping("/thirdparty-order")
    ResponseEntity<ApiResponse> thirdPartySender(@Valid @RequestBody ThirdPartySenderDto thirdPartySenderDto){
        return customerService.thirdPartySender(thirdPartySenderDto);
    }

    @PatchMapping("/cancel-order")
    public ResponseEntity<ApiResponse> cancelABooking(@Valid @RequestBody CancelABookingDto cancelABookingDto) {
        return customerService.cancelABooking(cancelABookingDto);
    }

    @PatchMapping("/update-order-status")
    public ResponseEntity<ApiResponse> updateOrderStatus(UpdateOrderStatusDto updateOrderStatusDto) {
        return customerService.updateOrderStatus(updateOrderStatusDto);
    }

    @PatchMapping("/feedback")
    public ResponseEntity<ApiResponse> giveFeedback(GiveFeedbackDto giveFeedbackDto) {
        return customerService.giveFeedback(giveFeedbackDto);
    }


}
