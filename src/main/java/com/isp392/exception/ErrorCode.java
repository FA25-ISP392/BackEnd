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
    PHONE_INVALID(1106, "Phone number must be between 9 and 11 digits"),
    USERNAME_INVALID(1103, "Username must be between 3 and 30 characters"),
    PASSWORD_INVALID(1104, "Password must be between 8 and 30 characters"),
    CANNOT_DELETE_SELF(1012, "Cannot delete yourself"),
    LOGIN_REQUIRED(1013, "Login first"),
    SEND_EMAIL_FAILED(1014, "Failed to send email"),
    EMAIL_NOT_VERIFIED(1015, "Email not verified"),
    // Token
    TOKEN_INVALID(1006, "Invalid token"),
    TOKEN_EXPIRED(1008, "Token expired"),
    //Customer Exception
    CUSTOMER_NOT_FOUND(1300, "Customer not found"),
    //Staff
    STAFF_NOT_FOUND(1101, "Staff not found"),


    // 🔹 Topping
    TOPPING_NOT_FOUND(1200, "Topping not found"),
    INGREDIENT_ALREADY_EXISTS(1201, "Topping already exists"),
    INGREDIENT_NAME_NOT_BLANKED(1202, "Topping name cannot be blank"),
    INGREDIENT_NAME_INVALID(1203, "Topping name must be at least 3 characters long"),
    INGREDIENT_CALORIES_REQUIRED(1204, "Calories is required"),
    INGREDIENT_CALORIES_NEGATIVE(1205, "Calories must be greater than or equal to 0"),
    INGREDIENT_QUANTITY_REQUIRED(1206, "Quantity is required"),
    INGREDIENT_QUANTITY_NEGATIVE(1207, "Quantity must be greater than or equal to 0"),
    INGREDIENT_PRICE_REQUIRED(1208, "Price is required"),
    INGREDIENT_PRICE_NEGATIVE(1209, "Price must be greater than or equal to 0"), INGREDIENT_GRAM_REQUIRED(1208, "Gram is required"),
    INGREDIENT_GRAM_NEGATIVE(1209, "Gram must be greater than or equal to 0"),
    ORDER_TOPPING_NOT_FOUND(1210, "Order topping not found"),
    ORDER_TOPPING_ALREADY_EXISTS(1211, "Order topping already exists"),
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
    DISH_CALO_NEGATIVE(2109, "Calo must be greater than or equal to 0"),
    IMAGE_UPLOAD_FAILED(2110, "Image upload failed"),


    //Table
    TABLE_NOT_FOUND(2200, "Table not found"),
    // Orders
    ORDER_NOT_FOUND(3000, "Order notfound"),

    //Order Detail
    ORDER_DETAIL_NOT_FOUND(3001, "Order detail not found"),

    PLAN_NOT_FOUND(4000, "Plan not found"),
    ITEM_NOT_FOUND(4001,"can't find the dish or topping" ), INVALID_REQUEST(4002,"just only oneStaffID can fix it" ),
    PLAN_ALREADY_EXISTS_BATCH(4002,"Already exits" ), PLAN_ALREADY_EXISTS(4003,"already exits" ),

    ACCESS_DENIED(1007, "You do not have permission"), PLAN_ALREADY_APPROVED(4005,"PLAN_ALREADY_APPROVED" ),

    BATCH_PLAN_INCONSISTENT(2205, "All plans in a batch must be for the same staff member.");
    final int code;
    final String message;

}