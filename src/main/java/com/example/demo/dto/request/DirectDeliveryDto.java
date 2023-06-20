package com.example.demo.dto.request;

import com.example.demo.enums.PaymentType;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DirectDeliveryDto {

    private String pickUpAddress;
    @NotBlank(message = "Delivery address cannot be blank")
    private String deliveryAddress;
    @NotBlank(message = "Item type cannot be blank")
    private String itemType;
    @NotBlank(message = "Receiver name cannot be blank")
    private String receiverName;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    @NotBlank(message = "Receiver phone number cannot be blank")
    private String receiverPhoneNumber;
    @NotNull(message = "Quantity cannot be null")
    private int itemQuantity;

}
