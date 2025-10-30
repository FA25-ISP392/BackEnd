package com.isp392.exception;

import lombok.Getter;
import java.util.Map;

@Getter
public class MultipleFieldsException extends RuntimeException {

    private final Map<String, ErrorCode> errors;
    private final ErrorCode generalErrorCode; // Mã lỗi chung (ví dụ: INVALID_ARGUMENT)

    public MultipleFieldsException(Map<String, ErrorCode> errors, ErrorCode generalErrorCode) {
        super(generalErrorCode.getMessage());
        this.errors = errors;
        this.generalErrorCode = generalErrorCode;
    }
}