package com.isp392.controller;

import com.isp392.dto.request.DishToppingBatchCreationRequest;
import com.isp392.dto.response.ApiResponse; // ✅ Thêm import này
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.service.DishToppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dish-topping")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class DishToppingController {

    DishToppingService dishToppingService;

    // ✅ Đã bọc response trong ApiResponse
    @PostMapping
    public ApiResponse<List<DishToppingResponse>> createDishToppings(@RequestBody @Valid DishToppingBatchCreationRequest request) {
        return ApiResponse.<List<DishToppingResponse>>builder()
                .result(dishToppingService.createDishToppings(request))
                .build();
    }

    // ✅ Đã bọc response trong ApiResponse
    @GetMapping
    public ApiResponse<List<DishToppingResponse>> getAll() {
        return ApiResponse.<List<DishToppingResponse>>builder()
                .result(dishToppingService.getAll())
                .build();
    }

    // ✅ Đã bọc response trong ApiResponse
    @GetMapping("/{dishId}/{toppingId}")
    public ApiResponse<DishToppingResponse> getById(@PathVariable int dishId, @PathVariable int toppingId) {
        return ApiResponse.<DishToppingResponse>builder()
                .result(dishToppingService.getById(dishId, toppingId))
                .build();
    }

    // ✅ Đã bọc response trong ApiResponse và trả về message thành công
    @DeleteMapping("/{dishId}/{toppingId}")
    public ApiResponse<String> delete(@PathVariable int dishId, @PathVariable int toppingId) {
        dishToppingService.delete(dishId, toppingId);
        return ApiResponse.<String>builder()
                .result("Dish-topping association deleted successfully.")
                .build();
    }
}