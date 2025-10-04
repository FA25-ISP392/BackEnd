package com.isp392.controller;

import com.isp392.dto.request.ApiResponse;
import com.isp392.dto.request.IngredientCreationRequest;
import com.isp392.dto.request.IngredientUpdateRequest;
import com.isp392.dto.response.IngredientResponse;
import com.isp392.service.IngredientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredient")

public class IngredientController {
    @Autowired
    IngredientService ingredientService;

    @GetMapping
    ApiResponse<List<IngredientResponse>> findAll() {
        ApiResponse<List<IngredientResponse>> response = new ApiResponse<>();
        response.setResult(ingredientService.getAllIngredients());
        return response;
    }

    @GetMapping("/{ingredientID}")
    ApiResponse<IngredientResponse> findById(@PathVariable int ingredientID) {
        ApiResponse<IngredientResponse> response = new ApiResponse<>();
        response.setResult(ingredientService.getIngredient(ingredientID));
        return response;
    }

    @PostMapping
    ApiResponse<IngredientResponse> create(@RequestBody @Valid IngredientCreationRequest request) {
        ApiResponse<IngredientResponse> response = new ApiResponse<>();
        response.setResult(ingredientService.createIngredient(request));
        return response;
    }

    @PutMapping("/{ingredientId}")
    ApiResponse<IngredientResponse> updateIngredient(@RequestBody @Valid IngredientUpdateRequest request, @PathVariable int ingredientId) {
        ApiResponse<IngredientResponse> response = new ApiResponse<>();
        response.setResult(ingredientService.updateIngredient(ingredientId, request));
        return response;
    }
    @DeleteMapping("/{ingredientID}")
    ApiResponse<String> deleteIngredient(@PathVariable int ingredientID) {
        ingredientService.deleteIngredient(ingredientID);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult("Delete Successfully!");
        return response;
    }
}
