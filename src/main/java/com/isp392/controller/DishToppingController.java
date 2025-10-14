package com.isp392.controller;

import com.isp392.dto.request.DishToppingCreationRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.service.DishToppingService;
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

    @PostMapping
    public DishToppingResponse create(@RequestBody DishToppingCreationRequest request) {
        return dishToppingService.create(request);
    }

    @GetMapping
    public List<DishToppingResponse> getAll() {
        return dishToppingService.getAll();
    }

    @GetMapping("/{dishId}/{toppingId}")
    public DishToppingResponse getById(@PathVariable int dishId, @PathVariable int toppingId) {
        return dishToppingService.getById(dishId, toppingId);
    }

    @DeleteMapping("/{dishId}/{toppingId}")
    public void delete(@PathVariable int dishId, @PathVariable int toppingId) {
        dishToppingService.delete(dishId, toppingId);
    }
}
