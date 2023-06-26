package com.example.demo.dto.request;

import lombok.Data;

import java.time.LocalDate;
@Data
public class OrdersHistoryDto {

    private LocalDate startDate;
    protected LocalDate endDate;
}
