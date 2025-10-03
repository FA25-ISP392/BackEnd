package com.isp392.controller;

import com.isp392.dto.request.ApiResponse;
import com.isp392.dto.request.IngredentCreationRequest;
import com.isp392.dto.request.IngredentUpdateRequest;
import com.isp392.dto.response.IngredentResponse;
import com.isp392.service.IngredentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ingredent")

public class IngredentController {
    @Autowired
    IngredentService ingredentService;

    @GetMapping
    ApiResponse<List<IngredentResponse>> findAll() {
        ApiResponse<List<IngredentResponse>> response = new ApiResponse<>();
        response.setResult(ingredentService.getAllIngredents());
        return response;
    }

    @GetMapping("/{ingredentID}")
    ApiResponse<IngredentResponse> findById(@PathVariable int ingredentID) {
        ApiResponse<IngredentResponse> response = new ApiResponse<>();
        response.setResult(ingredentService.getIngredent(ingredentID));
        return response;
    }

    @PostMapping
    ApiResponse<IngredentResponse> create(@RequestBody @Valid IngredentCreationRequest request) {
        ApiResponse<IngredentResponse> response = new ApiResponse<>();
        response.setResult(ingredentService.createIngredent(request));
        return response;
    }

    @PutMapping("/{ingredentId}")
    ApiResponse<IngredentResponse> updateIngredent(@RequestBody @Valid IngredentUpdateRequest request, @PathVariable int ingredentId) {
        ApiResponse<IngredentResponse> response = new ApiResponse<>();
        response.setResult(ingredentService.updateIngredent(ingredentId, request));
        return response;
    }
    @DeleteMapping("/{ingredentID}")
    ApiResponse<String> deleteIngredent(@PathVariable int ingredentID) {
        ingredentService.deleteIngredent(ingredentID);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult("Delete Successfully!");
        return response;
    }
}
