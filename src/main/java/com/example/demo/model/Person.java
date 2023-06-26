package com.example.demo.model;

import com.example.demo.enums.Gender;
import com.example.demo.enums.PaymentInterval;
import com.example.demo.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public abstract class Person extends Base{

    private String firstName;
    private String lastName;
    private String companyName;
    private String email;
    private String password;
    private String phoneNumber;
    private String confirmationToken;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String state;
}
