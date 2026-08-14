package com.bank.slds.gateway.response;

import lombok.Getter;

@Getter
public enum MessageCode {

    SUCCESS("00", "SUCCESS"),
    LOGIN_SUCCESS("AUTH_001", "LOGIN_SUCCESS"),
    LOGIN_FAILED("AUTH_002", "LOGIN_FAILED"),
    TOKEN_VALID("AUTH_003", "TOKEN_VALID"),
    TOKEN_INVALID("AUTH_004", "TOKEN_INVALID"),
    TOKEN_EXPIRED("AUTH_005", "TOKEN_EXPIRED"),
    UNAUTHORIZED("AUTH_006", "UNAUTHORIZED"),
    FORBIDDEN("AUTH_007", "FORBIDDEN"),
    LOGOUT_SUCCESS("AUTH_008", "LOGOUT_SUCCESS"),
    RATE_LIMIT_EXCEEDED("SEC_001", "RATE_LIMIT_EXCEEDED"),
    INTERNAL_SERVER_ERROR("ERR_500", "INTERNAL_SERVER_ERROR");

    private final String code;
    private final String messageKey;

    MessageCode(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }
}
