package com.bank.slds.gateway.controller;

import com.bank.slds.gateway.constant.ApiPath;
import com.bank.slds.gateway.dto.AuthResponse;
import com.bank.slds.gateway.dto.LoginRequest;
import com.bank.slds.gateway.dto.LogoutResponse;
import com.bank.slds.gateway.dto.TokenValidationResponse;
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
