package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.RiderStatus;
import com.example.demo.model.Orders;
import com.example.demo.model.User;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface StaffService {

    ResponseEntity<ApiResponse> signUp (SignUpDto signUpDto);

    ResponseEntity<ApiResponse> completeRegistration (CompleteRegistrationDto completeRegistrationDto);

    ResponseEntity<ApiResponse> updateStaffInformation (StaffRelevantDetailsDto staffRelevantDetailsDto);

    ResponseEntity<String> login (LoginDto loginDto);

    ResponseEntity<ApiResponse> forgotPassword(ForgotPasswordDto forgotPasswordDto);

    ResponseEntity<ApiResponse> resetPassword (ResetPasswordDto resetPasswordDto);

    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);

    ResponseEntity<ApiResponse> dispatchOrder(Long clientCode, String referenceNumber, HttpServletResponse response, DispatchOrderDto dispatchOrderDto) throws IOException;

    ResponseEntity<ApiResponse> registerABike(RegisterBikeDto registerBikeDto);

    ResponseEntity<ApiResponse> registerARider(RegisterRiderDto registerRiderDto);

    ApiResponse assignBikeToRider(AssignRiderToBikeDto assignRiderToBikeDto);

    Optional<Orders> viewAnOrderById (Long orderId);

    List<Orders> viewAllOrdersByStatus (OrderStatus orderStatus);

    List<User> viewAllRidersByStatus (RiderStatus riderStatus);

    Integer countRidesPerRider(Long staffId);

    Optional<User> viewStaffDetails(Long staffId);

    ResponseEntity<ApiResponse> deleteStaff (Long staffId);

    ResponseEntity<ApiResponse> createAdmin(Long staffId);

    List<Orders> viewAllOrders();
    List<Orders> clientWeeklyOrderSummary (WeeklyOrderSummaryDto weeklyOrderSummaryDto);

    List<Orders> viewAllOrdersToday ();

    List<Orders> viewAllOrdersInAMonth (OrdersHistoryDto ordersHistoryDto);

    public List<Orders> viewAllOrdersInAWeek(OrdersHistoryDto ordersHistoryDto);


    }


