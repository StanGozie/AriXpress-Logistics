package com.example.demo.model;

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
public class Staff extends Person{

    private String dob;
    @Enumerated(EnumType.STRING)
    private Role role;
    private int ridesCount;
    private Long staffId;
    private boolean isActive;
    private String nextOfKinFirstName;
    private String nextOfKinLastName;
    private String nextOfKinPhoneNumber;
    private String nextOfKinAddress;
    private String stateOfOrigin;
    @Enumerated(EnumType.STRING)
    private RiderStatus riderStatus;

}
