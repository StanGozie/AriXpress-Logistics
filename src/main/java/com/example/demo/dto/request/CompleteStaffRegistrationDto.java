package com.example.demo.dto.request;

import com.example.demo.enums.Gender;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Date;

@Data
public class CompleteStaffRegistrationDto {

    @NotBlank(message = "Token cannot be null")
    private String token;
    private String dob;
    @NotBlank(message = "Phone Number cannot be null")
    private String phoneNumber;
    @NotBlank(message = "Address cannot be null")
    private String address;
    @NotBlank(message = "Next of kin name cannot be null")
    private String nextOfKinFirstName;
    @NotBlank(message = "Next of kin last name cannot be null")
    private String nextOfKinLastName;
    @NotBlank(message = "Next of kin address cannot be null")
    private String nextOfKinAddress;
    @NotBlank(message = "Next of kin phone number cannot be null")
    private String nextOfKinPhoneNumber;
    @NotBlank(message = "State cannot be null")
    private String stateOfOrigin;
    @NotBlank(message = "Gender cannot be null")
    private Gender gender;
}
