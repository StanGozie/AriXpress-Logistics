package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CancelABookingDto {


    private Long id;
    private Boolean cancelOrder;
    private String reasonForOrderCancellation;
}
