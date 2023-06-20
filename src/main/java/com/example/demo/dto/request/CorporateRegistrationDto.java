package com.example.demo.dto.request;

import com.example.demo.enums.PaymentInterval;
import com.example.demo.enums.PaymentType;
import lombok.Data;

@Data
public class CorporateRegistrationDto {

    private String companyName;
    private String email;
    private String passWord;
    private String phoneNumber;
    private String address;
    private PaymentInterval paymentInterval;
    private PaymentType paymentType;
}
