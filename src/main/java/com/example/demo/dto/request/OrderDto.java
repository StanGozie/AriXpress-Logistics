package com.example.demo.dto.request;

import com.example.demo.enums.CustomerType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentInterval;
import lombok.Data;

@Data
public class OrderDto {

    private String pickUpAddress;
    private String deliveryAddress;
    private String receiverName;
    private String receiverPhoneNumber;
    private Boolean thirdPartyPickUp;
    private String thirdPartyName;
    private String thirdPartyPhoneNumber;
    private String thirdPartyAddress;
    private String itemType;
    private int itemQuantity;
    private String riderName;
    private String riderPhoneNumber;
    private OrderStatus orderStatus;
    private CustomerType clientType;
    private double distance;
    private double price;
    private PaymentInterval paymentInterval;
    private String image;
}
