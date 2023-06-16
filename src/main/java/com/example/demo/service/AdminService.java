package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.model.Orders;
import com.lowagie.text.Document;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AdminService {

    ResponseEntity<ApiResponse> signUp (StaffSignUpDto staffSignUpDto);

    ResponseEntity<ApiResponse> completeRegistration (CompleteStaffRegistrationDto completeStaffRegistrationDto);

    ResponseEntity<String> login (LoginDto loginDto);

    ResponseEntity<ApiResponse> forgotPassword (String email);

    ResponseEntity<ApiResponse> resetPassword (ResetPasswordDto resetPasswordDto);

    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);

    ResponseEntity<Document>  dispatchOrder(Long orderId, HttpServletResponse response, DispatchOrderDto dispatchOrderDto) throws IOException;

    ResponseEntity<ApiResponse> registerABike(RegisterBikeDto registerBikeDto);

    ApiResponse makeStaffAdmin (String newAdminEmail);

    ApiResponse assignRiderToBike(AssignRiderToBikeDto assignRiderToBikeDto);

    Optional<Orders> viewAnOrderById (Long orderId);

    List<Orders> viewAllOrdersByStatus (OrderStatus orderStatus);

    Integer countRidesPerRider(String phoneNumber);

    ResponseEntity<ApiResponse> viewAllOrdersToday (Date date);

    ResponseEntity<ApiResponse> viewAllOrdersByMonth (String month);

    ResponseEntity<ApiResponse> viewAllOrdersByWeek();

}
