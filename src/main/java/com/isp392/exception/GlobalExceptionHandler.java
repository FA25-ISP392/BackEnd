package com.isp392.exception;

import com.isp392.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(Exception exception) {
            log.error("Exception: ", exception);
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }


    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
        // Lấy toàn bộ danh sách lỗi
        List<Map<String, Object>> errors = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> {
                    String enumKey = fieldError.getDefaultMessage();
                    ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

                    try {
                        errorCode = ErrorCode.valueOf(enumKey);
                    } catch (IllegalArgumentException ignored) {
                    }

                    Map<String, Object> error = new HashMap<>();
                    error.put("field", fieldError.getField());
                    error.put("code", errorCode.getCode());
                    error.put("message", errorCode.getMessage());
                    return error;
                })
                .toList();

        // Gói vào ApiResponse
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.INVALID_ARGUMENT.getCode());
        apiResponse.setMessage("Validation failed");
        apiResponse.setResult(errors);

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse> handlingAccessDeniedException(AccessDeniedException exception) {
        // Giả sử bạn đã thêm ACCESS_DENIED vào ErrorCode.java
        // Log lỗi này
        log.warn("Access Denied: {}", exception.getMessage());

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage(exception.getMessage());

        // Lỗi Access Denied phải trả về HTTP 403 (Forbidden)
        return ResponseEntity.badRequest().body(apiResponse);
    }
}
