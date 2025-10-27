package com.isp392.controller;

import com.isp392.dto.request.DishToppingBatchCreationRequest;
import com.isp392.dto.response.ApiResponse; // ✅ Thêm import này
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.service.DishToppingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/dish-topping")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class DishToppingController {

    DishToppingService dishToppingService;

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<List<DishToppingResponse>> createDishToppings(@RequestBody @Valid DishToppingBatchCreationRequest request) {
        return ApiResponse.<List<DishToppingResponse>>builder()
                .result(dishToppingService.createDishToppings(request))
                .build();
    }

    @GetMapping
    public ApiResponse<List<DishToppingResponse>> getAll() {
        return ApiResponse.<List<DishToppingResponse>>builder()
                .result(dishToppingService.getAll())
                .build();
    }

    @GetMapping("/{dishId}/{toppingId}")
    public ApiResponse<DishToppingResponse> getById(@PathVariable int dishId, @PathVariable int toppingId) {
        return ApiResponse.<DishToppingResponse>builder()
                .result(dishToppingService.getById(dishId, toppingId))
                .build();
    }

    @DeleteMapping("/{dishId}/{toppingId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ApiResponse<String> delete(@PathVariable int dishId, @PathVariable int toppingId) {
        dishToppingService.delete(dishId, toppingId);
        return ApiResponse.<String>builder()
                .result("Dish-topping association deleted successfully.")
                .build();
    }
}
