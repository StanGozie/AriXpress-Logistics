package com.example.demo.dto.request;

import com.example.demo.enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderStatusDto {

    private Long id;
    private OrderStatus status;
}
