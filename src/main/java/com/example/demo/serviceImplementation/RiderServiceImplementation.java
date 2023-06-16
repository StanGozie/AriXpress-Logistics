package com.example.demo.serviceImplementation;

import com.example.demo.configuration.EmailService;
import com.example.demo.configuration.security.CustomUserDetailService;
import com.example.demo.configuration.security.JwtUtils;
import com.example.demo.dto.request.ChangePasswordDto;
import com.example.demo.dto.request.CompleteStaffRegistrationDto;
import com.example.demo.dto.request.ResetPasswordDto;
import com.example.demo.dto.request.StaffSignUpDto;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.RiderService;
import com.example.demo.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RiderServiceImplementation implements RiderService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AppUtil appUtil;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    @Override
    public ResponseEntity<ApiResponse> riderSignUp(StaffSignUpDto signUpDto) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> completeRiderRegistration(CompleteStaffRegistrationDto completeStaffRegistrationDto) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> riderEmailLogin(String email, String password) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> riderPhoneNumberLogin(String phoneNumber, String password) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> riderLoginWithId(String id) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> forgotPassword(String email) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> resetPassword(ResetPasswordDto resetPasswordDto) {
        return null;
    }

    @Override
    public ResponseEntity<ApiResponse> changePassword(ChangePasswordDto changePasswordDto) {
        return null;
    }
}
