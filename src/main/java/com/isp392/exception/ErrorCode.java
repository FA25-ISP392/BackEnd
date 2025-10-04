package com.isp392.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    // 🔹 Common
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"),
    INVALID_ARGUMENT(1000, "Invalid request argument"),
    INVALID_KEY(1001, "Invalid message key"),
    NOT_BLANKED(1002, "Field cannot be empty"),
    UNAUTHENTICATED(1003, "Unauthenticated"),
    STAFF_ACCESS_FORBIDDEN(1004, "Access denied"),

    // 🔹 User / Staff
    STAFF_EXISTED(1100, "Staff already exists"),
    STAFF_NOT_FOUND(1101, "Staff not found"),
    USER_NOT_EXISTED(1102, "User not existed"),
    USERNAME_INVALID(1103, "Username must be between 3 and 30 characters"),
    PASSWORD_INVALID(1104, "Password must be between 8 and 30 characters"),
    STAFF_NAME_INVALID(1105, "Staff name must be between 2 and 30 characters"),
    STAFF_PHONE_INVALID(1106, "Staff phone must be between 9 and 11 digits"),
    STAFF_EMAIL_INVALID(1107, "Staff email is not valid"),
    STAFF_ROLE_INVALID(1108, "Staff role is not valid"),

    // 🔹 Ingredient
    INGREDIENT_NOT_FOUND(1200, "Ingredient not found"),
    INGREDIENT_ALREADY_EXISTS(1201, "Ingredient already exists"),

    // Dish
    DISH_EXISTED(2001, "Dish existed"),
    DISH_NOT_FOUND(2002, "Dish not found");
    final int code;
    final String message;
}
