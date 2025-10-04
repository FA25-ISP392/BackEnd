package com.isp392.controller;

import com.isp392.dto.request.ApiResponse;
import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.entity.Dish;
import com.isp392.service.DishService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "https://isp392-production.up.railway.app")
@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DishController {

    DishService dishService;

    @PostMapping
    ApiResponse<Dish> createDish(@RequestBody @Valid DishCreationRequest request) {
        ApiResponse<Dish> apiResponse = new ApiResponse<>();
        apiResponse.setResult(dishService.createDish(request));
        return apiResponse;
    }

    @GetMapping
    ApiResponse<List<Dish>> getDishes() {
        ApiResponse<List<Dish>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(dishService.getDishes());
        return apiResponse;
    }

    @GetMapping("/{dishId}")
    ApiResponse<DishResponse> getDish(@PathVariable long dishId, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("sub");
        DishResponse dish = dishService.getDish(dishId, username);
        ApiResponse<DishResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(dish);
        return apiResponse;
    }

    @PutMapping("/{dishId}")
    ApiResponse<DishResponse> updateDish(@PathVariable long dishId, @RequestBody DishUpdateRequest request) {
        ApiResponse<DishResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResult(dishService.updateDish(dishId, request));
        return apiResponse;
    }

    @DeleteMapping("/{dishId}")
    ApiResponse<String> deleteDish(@PathVariable long dishId) {
        dishService.deleteDish(dishId);
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setResult("Delete dish successfully");
        return apiResponse;
    }
}
