package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface CustomerService {

    ResponseEntity<ApiResponse> signUp (CustomerSignUpDto customerSignUpDto);

    ResponseEntity<ApiResponse> completeRegistration (CompleteClientRegistrationDto completeClientRegistrationDto);

    ResponseEntity<String> login (LoginDto loginDto);

    ResponseEntity<ApiResponse> forgotPassword (String email);

    ResponseEntity<ApiResponse> resetPassword (ResetPasswordDto resetPasswordDto);

    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);

    ResponseEntity<ApiResponse> bookADelivery(DirectDeliveryDto directDeliveryDto);

    ResponseEntity<ApiResponse> thirdPartySender(ThirdPartySenderDto thirdPartySenderDto);

    ResponseEntity<ApiResponse> cancelABooking(CancelABookingDto cancelABookingDto);

    ResponseEntity<ApiResponse> updateOrderStatus(UpdateOrderStatusDto updateOrderStatusDto);

    ResponseEntity<ApiResponse> giveFeedback(GiveFeedbackDto giveFeedbackDto);

    ResponseEntity<ApiResponse> viewRiderLocation (ViewRiderLocationDto viewRiderLocationDto);

}
