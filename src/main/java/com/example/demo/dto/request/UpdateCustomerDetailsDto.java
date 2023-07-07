package com.example.demo.dto.request;

import lombok.Data;

@Data
public class UpdateCustomerDetailsDto {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String dob;
    private String email;

}
