package com.bank.slds.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingUserDto {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phone;
    private String role;
    private String branchCode;
    private String otp;
    private LocalDateTime createdAt;
}
