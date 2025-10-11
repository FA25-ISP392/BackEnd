package com.isp392.controller;

import com.isp392.dto.request.DishToppingCreationRequest;
import com.isp392.dto.request.DishToppingUpdateRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.service.DishToppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dishtoppings")
@RequiredArgsConstructor
public class DishToppingController {

    private final DishToppingService dishToppingService;


    @GetMapping
    public ResponseEntity<List<DishToppingResponse>> getAll() {
        List<DishToppingResponse> responses = dishToppingService.getAll();
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/{dishId}/{toppingId}")
    public ResponseEntity<DishToppingResponse> getById(
            @PathVariable int dishId,
            @PathVariable int toppingId) {

        DishToppingResponse response = dishToppingService.getById(dishId, toppingId);
        return ResponseEntity.ok(response);
    }


    @PostMapping
    public ResponseEntity<DishToppingResponse> create(
            @Valid @RequestBody DishToppingCreationRequest request) {

        DishToppingResponse response = dishToppingService.create(request);
        return ResponseEntity.ok(response);
    }


    @PutMapping
    public ResponseEntity<DishToppingResponse> update(
            @Valid @RequestBody DishToppingUpdateRequest request) {

        DishToppingResponse response = dishToppingService.update(request);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{dishId}/{toppingId}")
    public ResponseEntity<Void> delete(
            @PathVariable int dishId,
            @PathVariable int toppingId) {

        dishToppingService.delete(dishId, toppingId);
        return ResponseEntity.noContent().build();
    }
}