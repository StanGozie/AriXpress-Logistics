package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Bike extends Base{

    private String bikeNumber;
    private String make;
    private double price;
    private String images;
    private String riderName;
    private String riderPhoneNumber;
    private String riderAddress;
}
