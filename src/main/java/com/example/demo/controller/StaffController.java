package com.example.demo.controller;

import com.example.demo.dto.request.AssignRiderToBikeDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.DispatchOrderDto;
import com.example.demo.dto.request.ForgotPasswordDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.OrdersHistoryDto;
import com.example.demo.dto.request.PeriodicBillDto;
import com.example.demo.dto.request.RegisterBikeDto;
import com.example.demo.dto.request.RegisterRiderDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.RidersDeliveryCountPerMonthDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.request.StaffRelevantDetailsDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.RiderStatus;
import com.example.demo.model.Orders;
import com.example.demo.model.Staff;
import com.example.demo.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth/staff")
public class StaffController {

    private final StaffService staffService;

    @PostMapping("/update-staff-info")
    public ResponseEntity<ApiResponse> updateStaffInformation(@Valid @RequestBody StaffRelevantDetailsDto staffRelevantDetailsDto) {
        return staffService.updateStaffInformation(staffRelevantDetailsDto);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        return staffService.changePassword(changePasswordDto);
    }

    @PostMapping("/dispatch-order/{referenceNumber}")
    public ResponseEntity<ApiResponse> dispatchOrder(@PathVariable String referenceNumber, HttpServletResponse response, @Valid @RequestBody DispatchOrderDto dispatchOrderDto) throws IOException {
        return staffService.dispatchOrder(referenceNumber, response, dispatchOrderDto);
    }

    @PostMapping("/register-bike")
    public ResponseEntity<ApiResponse> registerABike(@Valid @RequestBody RegisterBikeDto registerBikeDto) {
        return staffService.registerABike(registerBikeDto);
    }

    @PatchMapping("/assign-bike")
    public ApiResponse assignBikeToRider(@RequestBody AssignRiderToBikeDto assignRiderToBikeDto) {
        return staffService.assignBikeToRider(assignRiderToBikeDto);
    }

    @GetMapping("/view-riders-by-status/{riderStatus}")
    public List<Staff> viewAllRidersByStatus(@PathVariable RiderStatus riderStatus) {
        return staffService.viewAllRidersByStatus(riderStatus);
    }

    @GetMapping("/view-an-order/{referenceNumber}")
    Optional<Orders> viewAnOrderByReferenceNumber(@PathVariable String referenceNumber) {
        return staffService.viewAnOrderByReferenceNumber(referenceNumber);
    }

    @GetMapping("/view-orders-by-status/{orderStatus}")
    List<Orders> viewAllOrdersByStatus(@PathVariable OrderStatus orderStatus) {
        return staffService.viewAllOrdersByStatus(orderStatus);
    }

    @GetMapping("/count-trips/{staffId}")
    Integer countRidesPerRider(@PathVariable Long staffId) {
        return staffService.countRidesPerRider(staffId);
    }

    @GetMapping("/view-all-orders")
    public List<Orders> viewAllOrders() {
        return staffService.viewAllOrders();
    }

    @PostMapping("/create-admin/{staffId}")
    public ResponseEntity<ApiResponse> createAdmin(@PathVariable Long staffId) {
        return staffService.createAdmin(staffId);
    }

    @DeleteMapping("/delete-staff/{staffId}")
    public ResponseEntity<ApiResponse> deleteStaff(@PathVariable Long staffId) {
        return staffService.deleteStaff(staffId);
    }

    @GetMapping("/view-staff-details/{staffId}")
    public Optional<Staff> viewStaffDetails(@PathVariable Long staffId) {
        return staffService.viewStaffDetails(staffId);
    }

    @PostMapping("/register-rider")
    public ResponseEntity<ApiResponse> registerARider(@Valid @RequestBody RegisterRiderDto registerRiderDto) {
        return staffService.registerARider(registerRiderDto);
    }

    @GetMapping("/view-client-weekly-orders")
    public List<Orders> clientWeeklyOrderSummary(@Valid @RequestBody WeeklyOrderSummaryDto weeklyOrderSummaryDto) throws Exception {
        return staffService.clientWeeklyOrderSummary(weeklyOrderSummaryDto);
    }

    @GetMapping("/daily-orders")
    public List<Orders> viewAllOrdersToday() {
        return staffService.viewAllOrdersToday();
    }

    @GetMapping("/weekly-orders")
    public List<Orders> viewAllOrdersInAWeek(@Valid @RequestBody OrdersHistoryDto ordersHistoryDto) {
        return staffService.viewAllOrdersInAWeek(ordersHistoryDto);
    }

    @GetMapping("/monthly-orders")
    public List<Orders> viewAllOrdersInAMonth(@Valid @RequestBody OrdersHistoryDto ordersHistoryDto) {
        return staffService.viewAllOrdersInAMonth(ordersHistoryDto);
    }

    @GetMapping("/riders-deliveries-count/{riderId}")
    public int viewDeliveryCountOfRider(@PathVariable Long riderId, @Valid @RequestBody RidersDeliveryCountPerMonthDto ridersDeliveryCountPerMonthDto) {
        return staffService.viewDeliveryCountOfRider(riderId,ridersDeliveryCountPerMonthDto);
    }

    @GetMapping("/clients-bill/{clientCode}")
    public BigDecimal weeklyBill(@PathVariable Long clientCode, @Valid @RequestBody PeriodicBillDto periodicBillDto) {
        return staffService.weeklyBill(clientCode, periodicBillDto);
    }

    @GetMapping("/clients-orders-invoice/{clientCode}")
    public List<Orders> generatePeriodicOrderDetailsPdf (@PathVariable Long clientCode, HttpServletResponse response, @Valid @RequestBody PeriodicBillDto periodicBillDto) throws IOException {
        return staffService.generatePeriodicOrderDetailsPdf(clientCode, response,periodicBillDto);
    }

    }