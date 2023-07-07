package com.example.demo.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AssignRiderToBikeDto {

    @NotBlank
    private String bikeNumber;
    @NotBlank
    private Long staffId;

}
