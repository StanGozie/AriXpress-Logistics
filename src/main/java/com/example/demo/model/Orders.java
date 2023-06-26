package com.example.demo.model;

import com.example.demo.enums.CustomerType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentInterval;
import com.example.demo.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Orders extends Base{


    private String referenceNumber;
    private String customerFirstName;
    private String customerLastName;
    private String email;
    private Long clientCode;
    private String companyName;
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
