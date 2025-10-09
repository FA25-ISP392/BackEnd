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
    DTO_NOT_FOUND(1005, "DTO class not found"),
    USER_EXISTED(1100, "User already exists"),
    USER_NOT_EXISTED(1102, "User not existed"),
    STAFF_EXISTED(1100, "User already exists"),
    EMAIL_EXISTED(1109, "Email already exists"),
    SIZE_INVALID(1010, "Field size is invalid"),
    EMAIL_INVALID(1011, "Email format is invalid"),
    //Customer Exception
    CUSTOMER_NOT_FOUND(1300, "Customer not found"),
    CUSTOMER_PHONE_INVALID(1302, "Customer phone format is invalid"),

    //Staff
    STAFF_NOT_FOUND(1101, "Staff not found"),
    USERNAME_INVALID(1103, "Username must be between 3 and 30 characters"),
    PASSWORD_INVALID(1104, "Password must be between 8 and 30 characters"),
    STAFF_NAME_INVALID(1105, "Staff name must be between 2 and 30 characters"),
    PHONE_INVALID(1106, "Phone number must be between 9 and 11 digits"),

    // 🔹 Ingredient
    INGREDIENT_NOT_FOUND(1200, "Ingredient not found"),
    INGREDIENT_ALREADY_EXISTS(1201, "Ingredient already exists"),
    INGREDIENT_NAME_NOT_BLANKED(1202, "Ingredient name cannot be blank"),
    INGREDIENT_NAME_INVALID(1203, "Ingredient name must be at least 3 characters long"),
    INGREDIENT_CALORIES_REQUIRED(1204, "Calories is required"),
    INGREDIENT_CALORIES_NEGATIVE(1205, "Calories must be greater than or equal to 0"),
    INGREDIENT_QUANTITY_REQUIRED(1206, "Quantity is required"),
    INGREDIENT_QUANTITY_NEGATIVE(1207, "Quantity must be greater than or equal to 0"),
    INGREDIENT_PRICE_REQUIRED(1208, "Price is required"),
    INGREDIENT_PRICE_NEGATIVE(1209, "Price must be greater than or equal to 0"),
    // Dish
    DISH_EXISTED(2001, "Dish existed"),
    DISH_NOT_FOUND(2002, "Dish not found"),
    DISH_NAME_REQUIRED(2100, "Dish name cannot be blank"),
    DISH_NAME_INVALID(2101, "Dish name must be at least 3 characters long"),
    DISH_DESCRIPTION_NOT_BLANKED(2102, "Description cannot be blank"),
    DISH_PRICE_REQUIRED(2103, "Price is required"),
    DISH_PRICE_NEGATIVE(2104, "Price must be greater than or equal to 0"),
    DISH_CATEGORY_REQUIRED(2105, "Category is required"),
    DISH_STATUS_REQUIRED(2107, "Status is required"),
    DISH_CALO_REQUIRED(2108, "Calo is required"),
    DISH_CALO_NEGATIVE(2109, "Calo must be greater than or equal to 0");

    final int code;
    final String message;
}