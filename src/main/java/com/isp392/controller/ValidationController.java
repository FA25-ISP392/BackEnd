package com.isp392.controller;

import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.service.ValidationMetadataService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/validation-rules")
public class ValidationController {

    private final ValidationMetadataService validationService;

    public ValidationController(ValidationMetadataService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/{dtoName}")
    public Map<String, Map<String, Object>> getValidationRules(@PathVariable String dtoName) {
        try {
            String className = "com.isp392.dto.request." +
                    toPascalCase(dtoName) + "Request";

            Class<?> dtoClass = Class.forName(className);
            return validationService.extractValidationRules(dtoClass);
        } catch (ClassNotFoundException e) {
            // Ném AppException thay vì IllegalArgumentException
            throw new AppException(ErrorCode.DTO_NOT_FOUND);
        }
    }

    private String toPascalCase(String input) {
        String[] parts = input.split("-");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1));
        }
        return sb.toString();
    }
}
