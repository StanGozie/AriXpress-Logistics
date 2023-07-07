package com.example.demo.service;

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
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Orders;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CustomerService {

    ResponseEntity<ApiResponse> customerSignUp (SignUpDto signUpDto) throws ValidationException;

    ResponseEntity<ApiResponse> customerCompleteRegistration (CompleteRegistrationDto completeRegistrationDto);

    ResponseEntity<ApiResponse> completeBusinessRegistration (CompleteBusinessRegistrationDto completeBusinessRegistrationDto);

    ResponseEntity<String> customerLogin (LoginDto loginDto);

    ResponseEntity<ApiResponse> customerForgotPassword(ForgotPasswordDto forgotPasswordDto);

    ResponseEntity<ApiResponse> customerResetPassword (ResetPasswordDto resetPasswordDto);

    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);

    ResponseEntity<ApiResponse> bookADelivery(DirectDeliveryDto directDeliveryDto);

    ResponseEntity<ApiResponse> thirdPartySender(ThirdPartySenderDto thirdPartySenderDto);

    ResponseEntity<ApiResponse> cancelABooking(String referenceNumber, CancelABookingDto cancelABookingDto);

    ResponseEntity<ApiResponse> confirmDelivery(String referenceNumber);

    ResponseEntity<ApiResponse> giveFeedback(String referenceNumber, GiveFeedbackDto giveFeedbackDto);
    List<Orders> weeklyOrderSummary (WeeklyOrderSummaryDto weeklyOrderSummaryDto);

    ResponseEntity<ApiResponse> viewRiderLocation (ViewRiderLocationDto viewRiderLocationDto);
    ResponseEntity<ApiResponse> updateCustomerDetails (UpdateCustomerDetailsDto updateCustomerDetailsDto);

}
