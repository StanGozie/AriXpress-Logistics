package com.example.demo.dto.request;

import lombok.Data;

@Data
public class RegisterBikeDto {

    private String bikeNumber;
    private String make;
    private double price;
    private String images;

}
