// src/main/java/com/isp392/service/DishService.java
package com.isp392.service;

import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.entity.Dish;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.DishMapper;
import com.isp392.repository.DishRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DishService {

    DishRepository dishRepository;
    DishMapper dishMapper;

    public Dish createDish(DishCreationRequest request) {
        if (dishRepository.existsByDishName(request.getDishName())) {
            throw new AppException(ErrorCode.DISH_EXISTED);
        }

        Dish dish = dishMapper.toDish(request);
        dish.setIsAvailable(true);
        return dishRepository.save(dish);
    }


    public List<Dish> getDishes() {
        return dishRepository.findAll();
    }

    public DishResponse getDish(int dishId, String usernameFromJwt) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
        // Add access control if needed
        return dishMapper.toDishResponse(dish);
    }

    public DishResponse updateDish(int dishId, DishUpdateRequest request) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
        dishMapper.updateDish(dish, request);
        return dishMapper.toDishResponse(dishRepository.save(dish));
    }

    public void deleteDish(int dishId) {
        dishRepository.deleteById(dishId);
    }



}
