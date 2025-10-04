package com.isp392.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    KEY_INVALID(1001, "Invalid message key"),
    STAFF_EXISTED(1002, "User existed"),
    USER_NOT_EXISTED(1003,"User not existed"),
    USERNAME_INVALID(1004,"Username must be at least 3 characters"),
    PASSWORD_INVALID(1005,"Password must be at least 8 characters"),
    UNAUTHENTICATED(1006, "Unauthenticated "),
    STAFF_ACCESS_FORBIDDEN(1007, "Access denied"),
    STAFF_NOT_FOUND(1008, "Staff not found"),

    //Ingredient Exception
    INGREDIENT_NOT_FOUND(1010,  "Ingredient not found"),
    INGREDIENT_ALREADY_EXISTS(1011, "Ingredient already exists");

    int code;
    String message;

}
