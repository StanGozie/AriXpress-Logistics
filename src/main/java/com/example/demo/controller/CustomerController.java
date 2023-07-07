package com.example.demo.controller;

import com.example.demo.dto.request.CancelABookingDto;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.DirectDeliveryDto;
import com.example.demo.dto.request.GiveFeedbackDto;
import com.example.demo.dto.request.ThirdPartySenderDto;
import com.example.demo.dto.request.UpdateCustomerDetailsDto;
import com.example.demo.dto.request.WeeklyOrderSummaryDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.model.Orders;
import com.example.demo.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/client")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        return customerService.changePassword(changePasswordDto);
    }

    @PostMapping("/new-order")
    public ResponseEntity<ApiResponse> bookADelivery(@Valid @RequestBody DirectDeliveryDto directDeliveryDto) {
        return customerService.bookADelivery(directDeliveryDto);
    }

    @PostMapping("/thirdparty-order")
    ResponseEntity<ApiResponse> thirdPartySender(@Valid @RequestBody ThirdPartySenderDto thirdPartySenderDto){
        return customerService.thirdPartySender(thirdPartySenderDto);
    }

    @PatchMapping("/cancel-order/{referenceNumber}")
    public ResponseEntity<ApiResponse> cancelABooking(@PathVariable String referenceNumber, @Valid @RequestBody CancelABookingDto cancelABookingDto) {
        return customerService.cancelABooking(referenceNumber, cancelABookingDto);
    }

    @GetMapping("/weekly-summary")
    public List<Orders> weeklyOrderSummary(@Valid @RequestBody WeeklyOrderSummaryDto weeklyOrderSummaryDto) {
        return customerService.weeklyOrderSummary(weeklyOrderSummaryDto);
    }

    @PatchMapping("/confirm-receipt/{referenceNumber}")
    public ResponseEntity<ApiResponse> confirmOrderReceipt(@PathVariable String referenceNumber) {
        return customerService.confirmDelivery(referenceNumber);
    }

    @PatchMapping("/feedback/{referenceNumber}")
    public ResponseEntity<ApiResponse> giveFeedback(@PathVariable String referenceNumber, @Valid @RequestBody GiveFeedbackDto giveFeedbackDto) {
        return customerService.giveFeedback(referenceNumber, giveFeedbackDto);
    }

    @PatchMapping("/update-customer-details")
    public ResponseEntity<ApiResponse> updateCustomerDetails(@Valid @RequestBody UpdateCustomerDetailsDto updateCustomerDetailsDto) {
        return customerService.updateCustomerDetails(updateCustomerDetailsDto);
    }


    }
