package com.example.demo.service;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.RiderStatus;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StaffService {

    ResponseEntity<ApiResponse> staffSignUp (SignUpDto signUpDto);

    ResponseEntity<ApiResponse> staffCompleteRegistration (CompleteRegistrationDto completeRegistrationDto);

    ResponseEntity<ApiResponse> updateStaffInformation (StaffRelevantDetailsDto staffRelevantDetailsDto);

    ResponseEntity<String> staffLogin (LoginDto loginDto);

    ResponseEntity<ApiResponse> staffForgotPassword(ForgotPasswordDto forgotPasswordDto);

    ResponseEntity<ApiResponse> staffResetPassword (ResetPasswordDto resetPasswordDto);

    ResponseEntity<ApiResponse> changePassword (ChangePasswordDto changePasswordDto);

    ResponseEntity<ApiResponse> dispatchOrder(String referenceNumber, HttpServletResponse response, DispatchOrderDto dispatchOrderDto) throws IOException;

    ResponseEntity<ApiResponse> registerABike(RegisterBikeDto registerBikeDto);

    ResponseEntity<ApiResponse> registerARider(RegisterRiderDto registerRiderDto);

    ApiResponse assignBikeToRider(AssignRiderToBikeDto assignRiderToBikeDto);

    Optional<Orders> viewAnOrderByReferenceNumber (String referenceNumber);

    List<Orders> viewAllOrdersByStatus (OrderStatus orderStatus);

    List<Staff> viewAllRidersByStatus (RiderStatus riderStatus);

    Integer countRidesPerRider(Long staffId);

    Optional<Staff> viewStaffDetails(Long staffId);

    ResponseEntity<ApiResponse> deleteStaff (Long staffId);

    ResponseEntity<ApiResponse> createAdmin(Long staffId);

    List<Orders> viewAllOrders();
    List<Orders> clientWeeklyOrderSummary (WeeklyOrderSummaryDto weeklyOrderSummaryDto);

    List<Orders> viewAllOrdersToday ();

    List<Orders> viewAllOrdersInAMonth (OrdersHistoryDto ordersHistoryDto);

    List<Orders> viewAllOrdersInAWeek(OrdersHistoryDto ordersHistoryDto);
    int viewDeliveryCountOfRider (Long riderId, RidersDeliveryCountPerMonthDto ridersDeliveryCountPerMonthDto);
    BigDecimal weeklyBill (Long clientCode, PeriodicBillDto periodicBillDto);
    List<Orders> generatePeriodicOrderDetailsPdf (Long clientCode, HttpServletResponse response, PeriodicBillDto periodicBillDto) throws IOException;

}


