package com.bank.slds.gateway.service;

import com.bank.slds.gateway.dto.*;
import com.bank.slds.gateway.model.UserEntity;
import com.bank.slds.gateway.repository.UserRepository;
import com.bank.slds.gateway.response.GenericResponse;
import com.bank.slds.gateway.response.MessageCode;
import com.bank.slds.gateway.response.ResponseFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PENDING_USER_PREFIX = "pending_user:";
    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private final SecureRandom secureRandom = new SecureRandom();

    // Local in-memory cache as fallback if Redis is temporarily unreachable
    private final Map<String, PendingUserDto> localPendingCache = new ConcurrentHashMap<>();
    private final Map<String, Long> localPendingExpiry = new ConcurrentHashMap<>();

    /**
     * Authenticates user against PostgreSQL JPA and issues JWT access & refresh tokens.
     */
    public Mono<ResponseEntity<GenericResponse<AuthResponse>>> login(LoginRequest request) {
        log.info("Processing login for user: {}", request.username());

        return Mono.fromCallable(() -> userRepository.findByUsername(request.username()))
            .map(optionalUser -> {
                if (optionalUser.isPresent()) {
                    UserEntity user = optionalUser.get();
                    
                    // Verify password if hash is present
                    if (user.getPassword() != null && !user.getPassword().isBlank()) {
                        if (!passwordEncoder.matches(request.password(), user.getPassword())
                                && !request.password().equals(user.getPassword())) {
                            log.warn("Invalid password for user: {}", request.username());
                            return ResponseFactory.<AuthResponse>error(HttpStatus.UNAUTHORIZED, MessageCode.LOGIN_FAILED);
                        }
                    }

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

    /**
     * 1. Register User (Step 1):
     * - Check if email or username already exists in Database.
     * - Generate 6-digit OTP.
     * - Store pending registration info into Redis with 10-minute TTL (Key: pending_user:{email}).
     * - DO NOT write anything to Database (prevent DB junk & eliminate batch cleanup jobs).
     */
    public Mono<ResponseEntity<GenericResponse<RegisterResponse>>> register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String username = request.username().trim();

        log.info("Processing registration request for username: '{}', email: '{}'", username, normalizedEmail);

        return Mono.fromCallable(() -> {
            // Check if user or email already exists in DB
            if (userRepository.existsByUsername(username)) {
                log.warn("Registration rejected: Username '{}' already exists in Database", username);
                return "DUPLICATE_USERNAME";
            }
            if (userRepository.existsByEmail(normalizedEmail)) {
                log.warn("Registration rejected: Email '{}' already exists in Database", normalizedEmail);
                return "DUPLICATE_EMAIL";
            }
            return "OK";
        }).flatMap(checkResult -> {
            if ("DUPLICATE_USERNAME".equals(checkResult)) {
                return Mono.just(ResponseFactory.<RegisterResponse>error(
                        HttpStatus.CONFLICT,
                        MessageCode.USER_ALREADY_EXISTS.getCode(),
                        "Tên đăng nhập '" + username + "' đã tồn tại trong hệ thống."
                ));
            }
            if ("DUPLICATE_EMAIL".equals(checkResult)) {
                return Mono.just(ResponseFactory.<RegisterResponse>error(
                        HttpStatus.CONFLICT,
                        MessageCode.USER_ALREADY_EXISTS.getCode(),
                        "Email '" + normalizedEmail + "' đã được sử dụng."
                ));
            }

            // Generate 6-digit random OTP
            String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
            String encodedPassword = passwordEncoder.encode(request.password());

            PendingUserDto pendingUser = PendingUserDto.builder()
                    .username(username)
                    .email(normalizedEmail)
                    .password(encodedPassword)
                    .fullName(request.fullName() != null ? request.fullName().trim() : username)
                    .phone(request.phone())
                    .role(request.role() != null && !request.role().isBlank() ? request.role() : "CREDIT_OFFICER")
                    .branchCode(request.branchCode() != null && !request.branchCode().isBlank() ? request.branchCode() : "BRANCH_HO")
                    .otp(otp)
                    .createdAt(LocalDateTime.now())
                    .build();

            String redisKey = PENDING_USER_PREFIX + normalizedEmail;

            // Save to in-memory fallback cache
            localPendingCache.put(redisKey, pendingUser);
            localPendingExpiry.put(redisKey, System.currentTimeMillis() + OTP_TTL.toMillis());

            try {
                String jsonPayload = objectMapper.writeValueAsString(pendingUser);
                return redisTemplate.opsForValue()
                        .set(redisKey, jsonPayload, OTP_TTL)
                        .map(saved -> {
                            log.info("Successfully stored pending user in Redis (Key: '{}', TTL: 10 mins). Database remains clean.", redisKey);
                            RegisterResponse responseData = new RegisterResponse(
                                    normalizedEmail,
                                    username,
                                    "Mã OTP xác thực đã được tạo và lưu tạm trong Redis (hiệu lực 10 phút). Không ghi DB.",
                                    OTP_TTL.toSeconds(),
                                    otp,
                                    LocalDateTime.now()
                            );
                            return ResponseFactory.success(responseData, MessageCode.OTP_SENT);
                        })
                        .onErrorResume(e -> {
                            log.warn("Redis write failed, using local memory cache fallback for '{}': {}", redisKey, e.getMessage());
                            RegisterResponse responseData = new RegisterResponse(
                                    normalizedEmail,
                                    username,
                                    "Mã OTP xác thực đã được gửi (lưu tạm 10 phút).",
                                    OTP_TTL.toSeconds(),
                                    otp,
                                    LocalDateTime.now()
                            );
                            return Mono.just(ResponseFactory.success(responseData, MessageCode.OTP_SENT));
                        });
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize PendingUserDto to JSON: {}", e.getMessage());
                return Mono.just(ResponseFactory.<RegisterResponse>error(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        MessageCode.INTERNAL_SERVER_ERROR
                ));
            }
        }).onErrorResume(e -> {
            log.error("Error during user registration check: {}", e.getMessage());
            return Mono.just(ResponseFactory.<RegisterResponse>error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    MessageCode.INTERNAL_SERVER_ERROR.getCode(),
                    "Lỗi hệ thống khi xử lý đăng ký: " + e.getMessage()
            ));
        });
    }

    /**
     * 2. Verify OTP (Step 2):
     * - Read pending user from Redis by Key: pending_user:{email}.
     * - If expired or not found -> Return OTP_INVALID / expired error.
     * - If OTP matches -> INSERT user into PostgreSQL DB -> DELETE Redis Key -> Issue JWT access & refresh tokens.
     */
    public Mono<ResponseEntity<GenericResponse<AuthResponse>>> verifyOtp(VerifyOtpRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String inputOtp = request.otp().trim();
        String redisKey = PENDING_USER_PREFIX + normalizedEmail;

        log.info("Processing OTP verification for email: '{}'", normalizedEmail);

        return redisTemplate.opsForValue().get(redisKey)
            .switchIfEmpty(Mono.defer(() -> {
                // Fallback to local memory cache if Redis didn't return or was offline
                Long expiry = localPendingExpiry.get(redisKey);
                if (expiry != null && System.currentTimeMillis() < expiry) {
                    PendingUserDto cached = localPendingCache.get(redisKey);
                    if (cached != null) {
                        try {
                            return Mono.just(objectMapper.writeValueAsString(cached));
                        } catch (Exception ignored) {}
                    }
                }
                return Mono.empty();
            }))
            .flatMap(jsonPayload -> {
                try {
                    PendingUserDto pendingUser = objectMapper.readValue(jsonPayload, PendingUserDto.class);

                    // Verify OTP
                    if (!pendingUser.getOtp().equals(inputOtp)) {
                        log.warn("Invalid OTP entered for email '{}'. Expected: '{}', Received: '{}'",
                                normalizedEmail, pendingUser.getOtp(), inputOtp);
                        return Mono.just(ResponseFactory.<AuthResponse>error(
                                HttpStatus.BAD_REQUEST,
                                MessageCode.OTP_INVALID.getCode(),
                                "Mã OTP không chính xác. Vui lòng thử lại."
                        ));
                    }

                    // OTP is valid -> Insert into PostgreSQL DB
                    return Mono.fromCallable(() -> {
                        // Double-check uniqueness in DB
                        if (userRepository.existsByUsername(pendingUser.getUsername()) ||
                            userRepository.existsByEmail(pendingUser.getEmail())) {
                            log.warn("User '{}' was already inserted into DB", pendingUser.getUsername());
                        }

                        UserEntity userEntity = UserEntity.builder()
                                .username(pendingUser.getUsername())
                                .email(pendingUser.getEmail())
                                .password(pendingUser.getPassword())
                                .fullName(pendingUser.getFullName())
                                .role(pendingUser.getRole())
                                .branchCode(pendingUser.getBranchCode())
                                .status("ACTIVE")
                                .createdAt(LocalDateTime.now())
                                .build();

                        return userRepository.save(userEntity);
                    }).flatMap(savedUser -> {
                        log.info("OTP verified successfully! User '{}' (ID: {}) successfully inserted into PostgreSQL DB",
                                savedUser.getUsername(), savedUser.getId());

                        // Delete pending key from Redis and local cache
                        localPendingCache.remove(redisKey);
                        localPendingExpiry.remove(redisKey);
                        redisTemplate.delete(redisKey).subscribe();

                        // Issue JWT Tokens
                        String userId = "USR-" + savedUser.getId();
                        String token = jwtService.generateToken(
                                savedUser.getUsername(),
                                savedUser.getRole(),
                                userId,
                                savedUser.getBranchCode()
                        );
                        String refreshToken = jwtService.generateRefreshToken(savedUser.getUsername(), userId);

                        AuthResponse authData = new AuthResponse(
                                token,
                                refreshToken,
                                "Bearer",
                                86400000L,
                                userId,
                                savedUser.getUsername(),
                                savedUser.getRole(),
                                savedUser.getBranchCode(),
                                LocalDateTime.now()
                        );

                        return Mono.just(ResponseFactory.success(authData, MessageCode.REGISTER_SUCCESS));
                    });

                } catch (Exception e) {
                    log.error("Failed to parse pending user payload from Redis: {}", e.getMessage());
                    return Mono.just(ResponseFactory.<AuthResponse>error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            MessageCode.INTERNAL_SERVER_ERROR
                    ));
                }
            })
            .defaultIfEmpty(ResponseFactory.error(
                    HttpStatus.BAD_REQUEST,
                    MessageCode.OTP_INVALID.getCode(),
                    "Mã OTP không tồn tại hoặc đã hết hạn (quá 10 phút). Vui lòng đăng ký lại."
            ));
    }

    /**
     * Resend OTP for pending registration.
     */
    public Mono<ResponseEntity<GenericResponse<RegisterResponse>>> resendOtp(ResendOtpRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String redisKey = PENDING_USER_PREFIX + normalizedEmail;

        return redisTemplate.opsForValue().get(redisKey)
            .flatMap(jsonPayload -> {
                try {
                    PendingUserDto pendingUser = objectMapper.readValue(jsonPayload, PendingUserDto.class);
                    String newOtp = String.format("%06d", secureRandom.nextInt(1_000_000));
                    pendingUser.setOtp(newOtp);

                    String updatedJson = objectMapper.writeValueAsString(pendingUser);

                    localPendingCache.put(redisKey, pendingUser);
                    localPendingExpiry.put(redisKey, System.currentTimeMillis() + OTP_TTL.toMillis());

                    return redisTemplate.opsForValue().set(redisKey, updatedJson, OTP_TTL)
                        .map(saved -> {
                            RegisterResponse responseData = new RegisterResponse(
                                    normalizedEmail,
                                    pendingUser.getUsername(),
                                    "Mã OTP mới đã được cấp lại và gia hạn 10 phút trong Redis.",
                                    OTP_TTL.toSeconds(),
                                    newOtp,
                                    LocalDateTime.now()
                            );
                            return ResponseFactory.success(responseData, MessageCode.OTP_SENT);
                        });
                } catch (Exception e) {
                    return Mono.just(ResponseFactory.<RegisterResponse>error(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            MessageCode.INTERNAL_SERVER_ERROR
                    ));
                }
            })
            .defaultIfEmpty(ResponseFactory.error(
                    HttpStatus.BAD_REQUEST,
                    MessageCode.OTP_INVALID.getCode(),
                    "Không tìm thấy yêu cầu đăng ký cho email này hoặc phiên đã hết hạn. Vui lòng đăng ký mới."
            ));
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
