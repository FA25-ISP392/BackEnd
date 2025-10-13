package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.service.DishService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DishController {

    DishService dishService;

    // ✅ Tạo món mới
    @PostMapping
    public ApiResponse<DishResponse> createDish(@Valid @RequestBody DishCreationRequest request) {
        return ApiResponse.<DishResponse>builder()
                .result(dishService.createDish(request))
                .build();
    }

    // ✅ Lấy danh sách tất cả món ăn kèm topping
    @GetMapping
    public ApiResponse<List<DishResponse>> getAllDishes() {
        return ApiResponse.<List<DishResponse>>builder()
                .result(dishService.getAllDishes())
                .build();
    }

    // ✅ Lấy món theo ID kèm topping
    @GetMapping("/{dishId}")
    public ApiResponse<DishResponse> getDishById(@PathVariable int dishId) {
        return ApiResponse.<DishResponse>builder()
                .result(dishService.getDishById(dishId))
                .build();
    }

    // ✅ Cập nhật món ăn
    @PutMapping("/{dishId}")
    public ApiResponse<DishResponse> updateDish(
            @PathVariable int dishId,
            @RequestBody @Valid DishUpdateRequest request
    ) {
        return ApiResponse.<DishResponse>builder()
                .result(dishService.updateDish(dishId, request))
                .build();
    }

    // ✅ Xóa mềm món ăn
    @DeleteMapping("/{dishId}")
    public ApiResponse<String> deleteDish(@PathVariable int dishId) {
        dishService.deleteDish(dishId);
        return ApiResponse.<String>builder()
                .result("Dish deleted successfully (soft delete).")
                .build();
    }
}
