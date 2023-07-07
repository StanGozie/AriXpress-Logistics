package com.example.demo.model;

import com.example.demo.enums.Gender;
import com.example.demo.enums.PaymentInterval;
import com.example.demo.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Size;

@Getter
@Setter
@MappedSuperclass
public abstract class Person extends Base{

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String confirmationToken;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String address;
    private String state;
}
