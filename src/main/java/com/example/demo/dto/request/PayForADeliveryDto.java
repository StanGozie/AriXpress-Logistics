package com.example.demo.dto.request;

import com.example.demo.enums.PaymentType;
import lombok.Data;

@Data
public class PayForADeliveryDto {

    private Long orderId;
    private PaymentType paymentType;
}
