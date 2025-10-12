package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.DishToppingCreationRequest;
import com.isp392.dto.request.DishToppingUpdateRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.service.DishToppingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishtoppings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DishToppingController {

    DishToppingService dishToppingService;

    @PostMapping("/create")
    // @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DishToppingResponse> create(@Valid @RequestBody DishToppingCreationRequest request) {
        DishToppingResponse response = dishToppingService.create(request);
        return ApiResponse.<DishToppingResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<DishToppingResponse>> getAll() {
        List<DishToppingResponse> responses = dishToppingService.getAll();
        return ApiResponse.<List<DishToppingResponse>>builder()
                .result(responses)
                .build();
    }

    @GetMapping("/{dishId}/{toppingId}")
    public ApiResponse<DishToppingResponse> getById(@PathVariable int dishId, @PathVariable int toppingId) {
        DishToppingResponse response = dishToppingService.getById(dishId, toppingId);
        return ApiResponse.<DishToppingResponse>builder()
                .result(response)
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<DishToppingResponse> update(@Valid @RequestBody DishToppingUpdateRequest request) {
        DishToppingResponse response = dishToppingService.update(request);
        return ApiResponse.<DishToppingResponse>builder()
                .result(response)
                .build();
    }

    @DeleteMapping("/{dishId}/{toppingId}")
    public ApiResponse<Void> delete(@PathVariable int dishId, @PathVariable int toppingId) {
        dishToppingService.delete(dishId, toppingId);
        return ApiResponse.<Void>builder()
                .result(null)
                .build();
    }
}