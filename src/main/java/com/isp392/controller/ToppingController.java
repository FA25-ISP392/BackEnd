package com.isp392.controller;

import com.isp392.dto.response.ApiResponse;
import com.isp392.dto.request.ToppingCreationRequest;
import com.isp392.dto.request.ToppingUpdateRequest;
import com.isp392.dto.response.ToppingResponse;
import com.isp392.service.ToppingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topping")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ToppingController {

    ToppingService toppingService;

    @PostMapping
    ApiResponse<ToppingResponse> create(@RequestBody @Valid ToppingCreationRequest request) {
        ApiResponse<ToppingResponse> response = new ApiResponse<>();
        response.setResult(toppingService.createTopping(request));
        return response;
    }

    @GetMapping
    ApiResponse<List<ToppingResponse>> findAll() {
        ApiResponse<List<ToppingResponse>> response = new ApiResponse<>();
        response.setResult(toppingService.getAllToppings());
        return response;
    }

    @GetMapping("/{toppingID}")
    ApiResponse<ToppingResponse> findById(@PathVariable int toppingID) {
        ApiResponse<ToppingResponse> response = new ApiResponse<>();
        response.setResult(toppingService.getTopping(toppingID));
        return response;
    }

    @PutMapping("/{toppingId}")
    ApiResponse<ToppingResponse> updateTopping(@RequestBody @Valid ToppingUpdateRequest request, @PathVariable int toppingId) {
        ApiResponse<ToppingResponse> response = new ApiResponse<>();
        response.setResult(toppingService.updateTopping(toppingId, request));
        return response;
    }

    @DeleteMapping("/{toppingID}")
    ApiResponse<String> deleteTopping(@PathVariable int toppingID) {
        toppingService.deleteTopping(toppingID);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult("Delete Successfully!");
        return response;
    }

}
