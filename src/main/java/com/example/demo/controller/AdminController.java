package com.example.demo.controller;

import com.example.demo.dto.request.*;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.exceptions.ValidationException;
import com.example.demo.model.Orders;
import com.example.demo.service.AdminService;
import com.lowagie.text.Document;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/sign-up")
    public ResponseEntity<ApiResponse> signUp(@Valid @RequestBody StaffSignUpDto staffSignUpDto) throws ValidationException {
        return adminService.signUp(staffSignUpDto);
    }

    @PostMapping("/admin/complete-registration")
    public ResponseEntity<ApiResponse> completeRegistration(@Valid @RequestBody CompleteStaffRegistrationDto completeStaffRegistrationDto) {
        return adminService.completeRegistration(completeStaffRegistrationDto);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDto loginDto) {
        return adminService.login(loginDto);
    }

    @PostMapping("/admin/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody String email) {
        return adminService.forgotPassword(email);
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        return adminService.resetPassword(resetPasswordDto);
    }

    @PostMapping("/admin/change-password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        return adminService.changePassword(changePasswordDto);
    }

    @PostMapping("/admin/dispatch-order")
    public ResponseEntity<Document> dispatchOrder(@PathVariable Long orderId, HttpServletResponse response, @RequestBody DispatchOrderDto dispatchOrderDto) throws IOException {
        return adminService.dispatchOrder(orderId, response, dispatchOrderDto);
    }

    @PostMapping("/admin/register-bike")
    public ResponseEntity<ApiResponse> registerABike(@RequestBody RegisterBikeDto registerBikeDto) {
        return adminService.registerABike(registerBikeDto);
    }

    @PatchMapping("/admin/make-admin")
    public ApiResponse makeStaffAdmin(@PathVariable String newAdminEmail) {
        return adminService.makeStaffAdmin(newAdminEmail);
    }

    @PatchMapping("/admin/assign-bike")
    public ApiResponse assignBikeToRider(@RequestBody AssignRiderToBikeDto assignRiderToBikeDto) {
        return adminService.assignRiderToBike(assignRiderToBikeDto);
    }

    @GetMapping("/admin/view-order-by-id")
    public Optional<Orders> viewAnOrderById(@PathVariable Long orderId) {
        return adminService.viewAnOrderById(orderId);
    }

    @GetMapping("/admin/view-orders-by-status")
    List<Orders> viewAllOrdersByStatus(@PathVariable OrderStatus orderStatus) {
        return adminService.viewAllOrdersByStatus(orderStatus);
    }

    @GetMapping("/admin/count-trips-per-rider")
    Integer countRidesPerRider(@PathVariable String phoneNumber){
        return adminService.countRidesPerRider(phoneNumber);
    }

}
