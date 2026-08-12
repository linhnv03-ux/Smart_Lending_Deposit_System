package com.bank.slds.gateway.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseFactory {

    public static <T> ResponseEntity<GenericResponse<T>> success(T data) {
        return ResponseEntity.ok(GenericResponse.<T>builder()
                .success(true)
                .code(MessageCode.SUCCESS.getCode())
                .message(MessageCode.SUCCESS.getMessage())
                .data(data)
                .build());
    }

    public static <T> ResponseEntity<GenericResponse<T>> success(T data, MessageCode messageCode) {
        return ResponseEntity.ok(GenericResponse.<T>builder()
                .success(true)
                .code(messageCode.getCode())
                .message(messageCode.getMessage())
                .data(data)
                .build());
    }

    public static <T> ResponseEntity<GenericResponse<T>> error(HttpStatus status, MessageCode messageCode) {
        return ResponseEntity.status(status).body(GenericResponse.<T>builder()
                .success(false)
                .code(messageCode.getCode())
                .message(messageCode.getMessage())
                .build());
    }

    public static <T> ResponseEntity<GenericResponse<T>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(GenericResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build());
    }
}
