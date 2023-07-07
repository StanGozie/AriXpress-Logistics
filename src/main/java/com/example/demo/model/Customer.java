package com.example.demo.model;

import com.example.demo.enums.CustomerType;
import com.example.demo.enums.PaymentInterval;
import com.example.demo.enums.PaymentType;
import com.example.demo.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Customer extends Person{

    private boolean isActive;
    private String dob;
    private Long clientCode;
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;
    private String companyName;
    @Enumerated(EnumType.STRING)
    private PaymentInterval paymentInterval;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    @Enumerated(EnumType.STRING)
    private Role role;
}
