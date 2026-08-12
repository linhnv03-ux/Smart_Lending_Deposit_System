package com.bank.slds.gateway.response;

import lombok.Getter;

@Getter
public enum MessageCode {
    SUCCESS("SLDS_MSG_001", "Operation completed successfully"),
    LOGIN_SUCCESS("SLDS_AUTH_001", "User authentication successful"),
    TOKEN_REFRESH_SUCCESS("SLDS_AUTH_002", "Token refreshed successfully"),
    TOKEN_VALID("SLDS_AUTH_003", "Token is active and valid"),
    UNAUTHORIZED("SLDS_ERR_401", "Unauthorized or invalid credentials"),
    FORBIDDEN("SLDS_ERR_403", "Access forbidden"),
    NOT_FOUND("SLDS_ERR_404", "Resource not found"),
    BAD_REQUEST("SLDS_ERR_400", "Bad request or missing parameters"),
    INTERNAL_SERVER_ERROR("SLDS_ERR_500", "Internal authentication server error");

    private final String code;
    private final String message;

    MessageCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
