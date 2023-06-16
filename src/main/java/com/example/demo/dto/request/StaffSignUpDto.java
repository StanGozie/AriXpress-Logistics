package com.example.demo.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
@Data
public class StaffSignUpDto {

        @NotBlank(message = "Firstname cannot be blank")
        private String firstName;

        @NotBlank(message = "Lastname cannot be blank")
        private String lastName;

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        private String email;

        @NotBlank(message = "Password cannot be blank")
        private String password;

        @NotBlank(message = "Confirm password cannot be blank")
        private String confirmPassword;

    }
