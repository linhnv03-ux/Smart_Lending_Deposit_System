package com.bank.slds.gateway.controller;

import com.bank.slds.gateway.constant.ApiPath;
import com.bank.slds.gateway.dto.AuthResponse;
import com.bank.slds.gateway.dto.LoginRequest;
import com.bank.slds.gateway.dto.TokenValidationResponse;
import com.bank.slds.gateway.response.GenericResponse;
import com.bank.slds.gateway.response.MessageCode;
import com.bank.slds.gateway.response.ResponseFactory;
import com.bank.slds.gateway.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping(ApiPath.Auth.BASE)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtService jwtService;

    @PostMapping(ApiPath.Auth.LOGIN)
    public Mono<ResponseEntity<GenericResponse<AuthResponse>>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Authentication request received for user: {}", request.username());

        String role = request.role() != null ? request.role() : "CREDIT_OFFICER";
        String userId = "USR-" + Math.abs(request.username().hashCode());
        String branchCode = "BRANCH_HO";

        String token = jwtService.generateToken(request.username(), role, userId, branchCode);
        String refreshToken = jwtService.generateRefreshToken(request.username(), userId);

        AuthResponse authData = new AuthResponse(
            token,
            refreshToken,
            "Bearer",
            86400000L,
            userId,
            request.username(),
            role,
            branchCode,
            LocalDateTime.now()
        );

        return Mono.just(ResponseFactory.success(authData, MessageCode.LOGIN_SUCCESS));
    }

    @GetMapping(ApiPath.Auth.VALIDATE)
    public Mono<ResponseEntity<GenericResponse<TokenValidationResponse>>> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.just(ResponseFactory.error(HttpStatus.UNAUTHORIZED, MessageCode.UNAUTHORIZED));
        }

        String token = authHeader.substring(7);
        return jwtService.validateToken(token)
            .map(isValid -> {
                if (isValid) {
                    var claims = jwtService.getClaims(token);
                    TokenValidationResponse validationData = new TokenValidationResponse(
                        true,
                        claims.getSubject(),
                        (String) claims.get("role"),
                        (String) claims.get("userId"),
                        "Token is valid and verified via RSA Certificate / Redis Session"
                    );
                    return ResponseFactory.success(validationData, MessageCode.TOKEN_VALID);
                } else {
                    return ResponseFactory.error(HttpStatus.UNAUTHORIZED, MessageCode.UNAUTHORIZED);
                }
            });
    }
}
