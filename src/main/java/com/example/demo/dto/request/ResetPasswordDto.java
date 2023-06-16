package com.example.demo.dto.request;

import lombok.Data;

@Data
public class ResetPasswordDto {

    private String confirmationToken;
    private String newPassword;
    private String confirmNewPassword;
}
