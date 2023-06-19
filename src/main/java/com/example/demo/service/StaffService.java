package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.model.Orders;
import com.example.demo.model.User;
import com.lowagie.text.Document;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface StaffService {

    ResponseEntity<ApiResponse> signUp (SignUpDto signUpDto);

    ResponseEntity<ApiResponse> completeRegistration (CompleteRegistrationDto completeRegistrationDto);

    ResponseEntity<ApiResponse> updateStaffInformation (StaffRelevantDetailsDto staffRelevantDetailsDto);

    ResponseEntity<String> login (LoginDto loginDto);

    ResponseEntity<ApiResponse> forgotPassword (String email);

    ResponseEntity<ApiResponse> resetPassword (ResetPasswordDto resetPasswordDto);

    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);

    ResponseEntity<Document>  dispatchOrder(Long orderId, HttpServletResponse response, DispatchOrderDto dispatchOrderDto) throws IOException;

    ResponseEntity<ApiResponse> registerABike(RegisterBikeDto registerBikeDto);

    public ApiResponse registerARider(RegisterRiderDto registerRiderDto);

    ApiResponse changeRoleToStaff (String email);

    ApiResponse assignRiderToBike(AssignRiderToBikeDto assignRiderToBikeDto);

    Optional<Orders> viewAnOrderById (Long orderId);

    List<Orders> viewAllOrdersByStatus (OrderStatus orderStatus);

    Integer countRidesPerRider(Long staffId);

    Optional<User> viewStaffDetails(Long staffId);

    ResponseEntity<ApiResponse> deleteStaff (Long staffId);

    ResponseEntity<ApiResponse> createAdmin(Long staffId);

    List<Orders> viewAllOrders();

    ResponseEntity<ApiResponse> viewAllOrdersToday (Date date);

    ResponseEntity<ApiResponse> viewAllOrdersByMonth (String month);

    ResponseEntity<ApiResponse> viewAllOrdersByWeek();

}


