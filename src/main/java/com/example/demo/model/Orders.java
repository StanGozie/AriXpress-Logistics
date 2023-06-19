package com.example.demo.model;

import com.example.demo.enums.CustomerType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentInterval;
import com.example.demo.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Orders extends Base{

    private String customerFirstName;
    private String customerLastName;
    private String email;
    private Long customerId;
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
    private Long riderId;
    private String riderPhoneNumber;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    @Enumerated(EnumType.STRING)
    private CustomerType clientType;
    private double distance;
    private double price;
    @Enumerated(EnumType.STRING)
    private PaymentInterval paymentInterval;
    private String image;
    private String images;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    private String reasonForOrderCancellation;
    private String feedback;

}

//                "phoneNumber": "08032363434",
//                "address": "Prince Ifeanyi Obodoakor Building, Ifite-Awka, Awka",
//                "dob": 29 7, 1992,
//                "state": "Anambra",
//                "gender": "Male"
//
//
//                "firstName": "Chigozie",
//                "lastName": "Enyoghasi",
//                "email": "chigozieenyoghasi@yahoo.com",
//                "password": "12345",
//                "confirmPassword": "12345"
