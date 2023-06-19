package com.example.demo.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class StaffRelevantDetailsDto {

    private String nextOfKinFirstName;
    @NotBlank(message = "Next of kin last name cannot be null")
    private String nextOfKinLastName;
    @NotBlank(message = "Next of kin address cannot be null")
    private String nextOfKinAddress;
    @NotBlank(message = "Next of kin phone number cannot be null")
    private String nextOfKinPhoneNumber;
    @NotBlank(message = "State cannot be null")
    private String stateOfOrigin;

}
