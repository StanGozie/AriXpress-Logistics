package com.example.demo.dto.request;

import lombok.Data;

@Data
public class ThirdPartySenderDto {

    private String thirdPartyName;
    private String thirdPartyPhoneNumber;
    private String thirdPartyAddress;
    private String deliveryAddress;
    private String receiverName;
    private String receiverPhoneNumber;
    private String itemType;
    private int itemQuantity;

}
