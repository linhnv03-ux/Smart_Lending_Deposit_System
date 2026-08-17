package com.bank.slds.gateway.controller;

import com.bank.slds.gateway.constant.ApiPath;
import com.bank.slds.gateway.dto.*;
import com.bank.slds.gateway.response.GenericResponse;
import com.bank.slds.gateway.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(ApiPath.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiPath.Auth.LOGIN)
    public Mono<ResponseEntity<GenericResponse<AuthResponse>>> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping(ApiPath.Auth.REGISTER)
    public Mono<ResponseEntity<GenericResponse<RegisterResponse>>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping(ApiPath.Auth.VERIFY_OTP)
    public Mono<ResponseEntity<GenericResponse<AuthResponse>>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping(ApiPath.Auth.RESEND_OTP)
    public Mono<ResponseEntity<GenericResponse<RegisterResponse>>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return authService.resendOtp(request);
    }

    @PostMapping(ApiPath.Auth.LOGOUT)
    public Mono<ResponseEntity<GenericResponse<LogoutResponse>>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return authService.logout(authHeader);
    }

    @GetMapping(ApiPath.Auth.VALIDATE)
    public Mono<ResponseEntity<GenericResponse<TokenValidationResponse>>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        return authService.validateToken(authHeader);
    }
}
