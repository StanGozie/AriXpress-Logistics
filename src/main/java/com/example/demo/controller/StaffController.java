package com.example.demo.controller;

import com.example.demo.dto.request.AssignRiderToBikeDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteRegistrationDto;
import com.example.demo.dto.request.DispatchOrderDto;
import com.example.demo.dto.request.LoginDto;
import com.example.demo.dto.request.RegisterBikeDto;
import com.example.demo.dto.request.RegisterRiderDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.SignUpDto;
import com.example.demo.dto.request.StaffRelevantDetailsDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.model.Orders;
import com.example.demo.model.User;
import com.example.demo.service.StaffService;
import com.lowagie.text.Document;
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
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class StaffController {

    private final StaffService staffService;

//    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse> signUp(@Valid @RequestBody SignUpDto signUpDto) {
//        logger.info("====> Request body comes here {}", signUpDto);
        System.out.println("Request body comes here " + signUpDto.toString());
        return staffService.signUp(signUpDto);
    }
    @PostMapping("/update-staff-info")
    public ResponseEntity<ApiResponse> updateStaffInformation(StaffRelevantDetailsDto staffRelevantDetailsDto) {
    return staffService.updateStaffInformation(staffRelevantDetailsDto);
    }

        @PostMapping("/complete-registration")
    public ResponseEntity<ApiResponse> completeRegistration(@Valid @RequestBody CompleteRegistrationDto completeRegistrationDto) {
        return staffService.completeRegistration(completeRegistrationDto);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDto loginDto) {
        return staffService.login(loginDto);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody String email) {
        return staffService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        return staffService.resetPassword(resetPasswordDto);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
        return staffService.changePassword(changePasswordDto);
    }

    @PostMapping("/dispatch-order")
    public ResponseEntity<Document> dispatchOrder(@PathVariable Long orderId, HttpServletResponse response, @RequestBody DispatchOrderDto dispatchOrderDto) throws IOException {
        return staffService.dispatchOrder(orderId, response, dispatchOrderDto);
    }

    @PostMapping("/register-bike")
    public ResponseEntity<ApiResponse> registerABike(@RequestBody RegisterBikeDto registerBikeDto) {
        return staffService.registerABike(registerBikeDto);
    }

    @PatchMapping("/make-staff")
    public ApiResponse changeRoleToStaff(@PathVariable String email) {
        return staffService.changeRoleToStaff(email);
    }

    @PatchMapping("/assign-bike")
    public ApiResponse assignBikeToRider(@RequestBody AssignRiderToBikeDto assignRiderToBikeDto) {
        return staffService.assignRiderToBike(assignRiderToBikeDto);
    }

    @GetMapping("/view-order-by-id")
    public Optional<Orders> viewAnOrderById(@PathVariable Long orderId) {
        return staffService.viewAnOrderById(orderId);
    }

    @GetMapping("/view-orders-by-status")
    List<Orders> viewAllOrdersByStatus(@PathVariable OrderStatus orderStatus) {
        return staffService.viewAllOrdersByStatus(orderStatus);
    }

    @GetMapping("/count-trips")
    Integer countRidesPerRider(@PathVariable Long staffId){
        return staffService.countRidesPerRider(staffId);
    }

    @GetMapping("/view-all-orders")
    public List<Orders> viewAllOrders() {
        return staffService.viewAllOrders();
    }

    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponse> createAdmin(@PathVariable Long staffId) {
        return staffService.createAdmin(staffId);
    }

    @DeleteMapping("/delete-staff")
    public ResponseEntity<ApiResponse> deleteStaff(@PathVariable Long staffId) {
        return staffService.deleteStaff(staffId);
    }

    @GetMapping("/view-staff-details")
    public Optional<User> viewStaffDetails(@PathVariable Long staffId) {
        return staffService.viewStaffDetails(staffId);
    }

    @PostMapping("/register-rider")
    public ApiResponse registerARider(@Valid @RequestBody RegisterRiderDto registerRiderDto) {
        return staffService.registerARider(registerRiderDto);
    }

    @GetMapping("/view-client-weekly-orders")
    public List<Optional<Orders>> clientWeeklyOrderSummary(@PathVariable Long clientCode, @Valid @RequestBody WeeklyOrderSummaryDto weeklyOrderSummaryDto) throws Exception {
        return staffService.clientWeeklyOrderSummary(clientCode, weeklyOrderSummaryDto);
    }

}
