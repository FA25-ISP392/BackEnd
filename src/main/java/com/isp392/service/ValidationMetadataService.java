package com.isp392.service;

import jakarta.validation.constraints.*;
import org.springframework.stereotype.Service;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class ValidationMetadataService {

    public Map<String, Map<String, Object>> extractValidationRules(Class<?> dtoClass) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();

        for (Field field : dtoClass.getDeclaredFields()) {
            Map<String, Object> rules = new LinkedHashMap<>();

            for (Annotation annotation : field.getAnnotations()) {
                if (annotation instanceof NotBlank || annotation instanceof NotNull) {
                    rules.put("required", true);
                }
                if (annotation instanceof Size size) {
                    rules.put("min", size.min());
                    rules.put("max", size.max());
                }
                if (annotation instanceof Min min) {
                    rules.put("minValue", min.value());
                }
                if (annotation instanceof Max max) {
                    rules.put("maxValue", max.value());
                }
                if (annotation instanceof Email email) {
                    rules.put("pattern", "email");
                }
                if (annotation instanceof Pattern pattern) {
                    rules.put("pattern", pattern.regexp());
                }
            }

            if (!rules.isEmpty()) {
                result.put(field.getName(), rules);
            }
        }

        return result;
    }
}
