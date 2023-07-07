package com.example.demo.dto.request;

import lombok.Data;

import java.time.LocalDate;
@Data
public class RidersDeliveryCountPerMonthDto {

    private LocalDate startDate;
    private LocalDate endDate;
}
