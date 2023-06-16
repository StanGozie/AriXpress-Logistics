package com.example.demo.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ChangePasswordDto {

    @NotBlank(message = "New password cannot be blank")
    private String newPassword;
    @NotBlank(message = "Confirm new password cannot be blank")
    private String confirmNewPassword;
}
