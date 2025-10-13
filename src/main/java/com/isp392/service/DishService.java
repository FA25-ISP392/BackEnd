package com.isp392.service;

import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.entity.Dish;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.DishMapper;
import com.isp392.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DishService {

    DishRepository dishRepository;
    DishMapper dishMapper;

    // ✅ Tạo món mới
    public DishResponse createDish(DishCreationRequest request) {
        if (dishRepository.existsByDishName(request.getDishName())) {
            throw new AppException(ErrorCode.DISH_EXISTED);
        }
        Dish dish = dishMapper.toDish(request);
        dish.setIsAvailable(true);
        Dish saved = dishRepository.save(dish);
        return dishMapper.toDishResponse(saved);
    }

    // ✅ Cập nhật món
    @Transactional
    public DishResponse updateDish(int dishId, DishUpdateRequest request) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));

        if (request.getDishName() != null && !request.getDishName().equals(dish.getDishName())) {
            if (dishRepository.existsByDishName(request.getDishName())) {
                throw new AppException(ErrorCode.DISH_EXISTED);
            }
        }
        dishMapper.updateDish(dish, request);
        return dishMapper.toDishResponse(dish);
    }

    // ✅ Xóa mềm món
    public void deleteDish(int dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
        dish.setIsAvailable(false);
        dishRepository.save(dish);
    }

    // ✅ Lấy tất cả món còn khả dụng (tránh vòng lặp)
    public List<DishResponse> getAllDishes() {
        List<Dish> dishes = dishRepository.findAllWithToppings();
        return dishes.stream()
                .map(dishMapper::toDishResponse)
                .toList();
    }

    public DishResponse getDishById(int dishId) {
        Dish dish = dishRepository.findByIdWithToppings(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
        return dishMapper.toDishResponse(dish);
    }
}
