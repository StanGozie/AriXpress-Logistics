package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CancelABookingDto {

    private Boolean cancelOrder;
    private String reasonForOrderCancellation;
}
