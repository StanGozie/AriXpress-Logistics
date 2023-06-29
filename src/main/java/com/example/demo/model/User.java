package com.example.demo.model;

import com.example.demo.enums.CustomerType;
import com.example.demo.enums.PaymentInterval;
import com.example.demo.enums.PaymentType;
import com.example.demo.enums.RiderStatus;
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
public class User extends Person{

    private boolean isActive;
    private String nextOfKinFirstName;
    private String nextOfKinLastName;
    private String address;
    private Long staffId;
    private String nextOfKinPhoneNumber;
    private String nextOfKinAddress;
    private String stateOfOrigin;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private CustomerType customerType;
    @Enumerated(EnumType.STRING)
    private PaymentInterval paymentInterval;
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    private Long clientCode;
    private String dob;
    @Enumerated(EnumType.STRING)
    private RiderStatus riderStatus;

}
