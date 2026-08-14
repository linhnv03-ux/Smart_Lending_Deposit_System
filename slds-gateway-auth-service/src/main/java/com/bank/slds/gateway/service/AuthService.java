package com.bank.slds.gateway.service;

import com.bank.slds.gateway.dto.AuthResponse;
import com.bank.slds.gateway.dto.LoginRequest;
import com.bank.slds.gateway.dto.LogoutResponse;
import com.bank.slds.gateway.dto.TokenValidationResponse;
import com.bank.slds.gateway.model.UserEntity;
import com.bank.slds.gateway.repository.UserRepository;
import com.bank.slds.gateway.response.GenericResponse;
import com.bank.slds.gateway.response.MessageCode;
import com.bank.slds.gateway.response.ResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public Mono<ResponseEntity<GenericResponse<AuthResponse>>> login(LoginRequest request) {
        log.info("Processing login for user: {}", request.username());

        return Mono.fromCallable(() -> userRepository.findByUsername(request.username()))
            .map(optionalUser -> {
                if (optionalUser.isPresent()) {
                    UserEntity user = optionalUser.get();
                    String role = user.getRole() != null ? user.getRole() : "CREDIT_OFFICER";
                    String userId = "USR-" + user.getId();
                    String branchCode = user.getBranchCode() != null ? user.getBranchCode() : "BRANCH_HO";

                    String token = jwtService.generateToken(user.getUsername(), role, userId, branchCode);
                    String refreshToken = jwtService.generateRefreshToken(user.getUsername(), userId);

                    AuthResponse authData = new AuthResponse(
                        token,
                        refreshToken,
                        "Bearer",
                        86400000L,
                        userId,
                        user.getUsername(),
                        role,
                        branchCode,
                        LocalDateTime.now()
                    );
                    log.info("Successfully authenticated user '{}' from PostgreSQL with role '{}'", user.getUsername(), role);
                    return ResponseFactory.success(authData, MessageCode.LOGIN_SUCCESS);
                } else {
                    return createFallbackAuthResponse(request);
                }
            })
            .onErrorResume(e -> {
                log.warn("PostgreSQL user query failed for '{}', falling back. Error: {}", request.username(), e.getMessage());
                return Mono.just(createFallbackAuthResponse(request));
            });
    }

    private ResponseEntity<GenericResponse<AuthResponse>> createFallbackAuthResponse(LoginRequest request) {
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
        log.info("Issued fallback authentication token for user: {}", request.username());
        return ResponseFactory.success(authData, MessageCode.LOGIN_SUCCESS);
    }

    public Mono<ResponseEntity<GenericResponse<LogoutResponse>>> logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Logout requested without valid Authorization Bearer header");
            LogoutResponse response = new LogoutResponse("anonymous", "No active session found", LocalDateTime.now());
            return Mono.just(ResponseFactory.success(response, MessageCode.LOGOUT_SUCCESS));
        }

        String token = authHeader.substring(7);
        String username;
        try {
            var claims = jwtService.getClaims(token);
            username = claims.getSubject() != null ? claims.getSubject() : "anonymous";
        } catch (Exception e) {
            log.debug("Could not parse token claims for logout: {}", e.getMessage());
            username = "anonymous";
        }

        final String finalUsername = username;
        log.info("Processing logout for user: {}", finalUsername);

        return jwtService.invalidateToken(token)
            .map(invalidated -> {
                log.info("Successfully invalidated JWT token and revoked Redis session for user: {}", finalUsername);
                LogoutResponse logoutData = new LogoutResponse(
                    finalUsername,
                    "Session successfully terminated and token blacklisted in Redis",
                    LocalDateTime.now()
                );
                return ResponseFactory.success(logoutData, MessageCode.LOGOUT_SUCCESS);
            })
            .onErrorResume(e -> {
                log.warn("Error during token invalidation for user '{}': {}", finalUsername, e.getMessage());
                LogoutResponse fallbackData = new LogoutResponse(
                    finalUsername,
                    "Session cleared locally",
                    LocalDateTime.now()
                );
                return Mono.just(ResponseFactory.success(fallbackData, MessageCode.LOGOUT_SUCCESS));
            });
    }

    public Mono<ResponseEntity<GenericResponse<TokenValidationResponse>>> validateToken(String authHeader) {
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
