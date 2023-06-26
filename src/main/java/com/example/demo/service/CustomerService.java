package com.example.demo.service;

import com.example.demo.dto.request.CancelABookingDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteBusinessRegistrationDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.DirectDeliveryDto;
import com.example.demo.dto.request.GiveFeedbackDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.request.ThirdPartySenderDto;
import com.example.demo.dto.request.UpdateOrderStatusDto;
import com.example.demo.dto.request.ViewRiderLocationDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.model.Orders;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface CustomerService {

    ResponseEntity<ApiResponse> signUp (SignUpDto signUpDto);

    ResponseEntity<ApiResponse> completeRegistration (CompleteRegistrationDto completeRegistrationDto);

    ApiResponse completeBusinessRegistration (CompleteBusinessRegistrationDto completeBusinessRegistrationDto);

    ResponseEntity<String> login (LoginDto loginDto);

    ResponseEntity<ApiResponse> forgotPassword (String email);

    ResponseEntity<ApiResponse> resetPassword (ResetPasswordDto resetPasswordDto);

    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);

    ResponseEntity<ApiResponse> bookADelivery(DirectDeliveryDto directDeliveryDto);

    ResponseEntity<ApiResponse> thirdPartySender(ThirdPartySenderDto thirdPartySenderDto);

    ResponseEntity<ApiResponse> cancelABooking(Long id, CancelABookingDto cancelABookingDto);

    ResponseEntity<ApiResponse> updateOrderStatus(Long id,  OrderStatus orderStatus);

    ResponseEntity<ApiResponse> giveFeedback(Long id, GiveFeedbackDto giveFeedbackDto);
    List<Orders> weeklyOrderSummary (WeeklyOrderSummaryDto weeklyOrderSummaryDto);

    ResponseEntity<ApiResponse> viewRiderLocation (ViewRiderLocationDto viewRiderLocationDto);

}
