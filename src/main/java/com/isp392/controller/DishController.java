package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.enums.Category;
import com.isp392.enums.DishType;
import com.isp392.service.DishService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DishController {

    DishService dishService;

//    @PostMapping
//    public ApiResponse<DishResponse> createDish(@Valid @RequestBody DishCreationRequest request) {
//        return ApiResponse.<DishResponse>builder()
//                .result(dishService.createDish(request))
//                .build();
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<DishResponse> createDish(@Valid @RequestPart("dish") DishCreationRequest dish,
                                                @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        return ApiResponse.<DishResponse>builder()
                .result(dishService.createDish(dish, imageFile))
                .build();
    }


    // ✅ SỬA LẠI: Thêm tham số filter (tùy chọn)
    @GetMapping
    public ApiResponse<List<DishResponse>> getAllDishes(
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) DishType type
    ) {
        return ApiResponse.<List<DishResponse>>builder()
                .result(dishService.getAllDishes(category, type))
                .build();
    }

    // ✅ SỬA LẠI: Thêm tham số filter (tùy chọn)
    @GetMapping("/paging")
    public ApiResponse<Page<DishResponse>> getAllDishesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) DishType type
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.<Page<DishResponse>>builder()
                .result(dishService.getAllDishesPaginated(pageable, category, type))
                .build();
    }

    // ⭐ SỬA LẠI: Get dishes containing name
    @GetMapping("/by-name/{dishName}")
    public ApiResponse<List<DishResponse>> getDishesByNameContaining(@PathVariable String dishName) { // Đổi tên và kiểu trả về
        return ApiResponse.<List<DishResponse>>builder()
                .result(dishService.getDishesByNameContaining(dishName)) // Gọi service method mới
                .build();
    }

    // ✅ ĐẶT API /{dishId} XUỐNG DƯỚI
    // Spring sẽ chỉ khớp với URL này nếu nó không phải là "/paging"
    @GetMapping("/{dishId}")
    public ApiResponse<DishResponse> getDishById(@PathVariable int dishId) {
        return ApiResponse.<DishResponse>builder()
                .result(dishService.getDishById(dishId))
                .build();
    }

    @PutMapping(value = "/{dishId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<DishResponse> updateDish(
            @PathVariable int dishId,
            @Valid @RequestPart("dish") DishUpdateRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        return ApiResponse.<DishResponse>builder()
                .result(dishService.updateDish(dishId, request, imageFile))
                .build();
    }

    @DeleteMapping("/{dishId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<String> deleteDish(@PathVariable int dishId) {
        dishService.deleteDish(dishId);
        return ApiResponse.<String>builder()
                .result("Dish deleted successfully (soft delete).")
                .build();
    }
}