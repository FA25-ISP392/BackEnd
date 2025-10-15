package com.isp392.service;

import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.dto.response.ToppingWithQuantityResponse;
import com.isp392.entity.DailyPlan;
import com.isp392.entity.Dish;
import com.isp392.entity.Topping;
import com.isp392.enums.ItemType;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.DishMapper;
import com.isp392.repository.DailyPlanRepository;
import com.isp392.repository.DishRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DishService {

    DishRepository dishRepository;
    DailyPlanRepository dailyPlanRepository;
    DishMapper dishMapper;

    // ... các phương thức create, update, delete giữ nguyên ...

    @Transactional(readOnly = true)
    public DishResponse getDishById(int dishId) {
        Dish dish = dishRepository.findByIdWithToppings(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));

        LocalDate today = LocalDate.now();

        // Lấy kế hoạch của món ăn
        DailyPlan dishPlan = dailyPlanRepository
                .findByItemIdAndItemTypeAndPlanDate(dish.getDishId(), ItemType.DISH, today)
                .orElse(null);

        // Thu thập ID topping tùy chọn
        List<Topping> optionalToppings = dish.getDishToppings().stream()
                .map(dishTopping -> dishTopping.getTopping())
                .toList();

        // Lấy kế hoạch của tất cả topping tùy chọn
        Map<Integer, DailyPlan> toppingPlansMap = Collections.emptyMap();
        if (!optionalToppings.isEmpty()) {
            List<Integer> toppingIds = optionalToppings.stream().map(Topping::getToppingId).toList();
            toppingPlansMap = dailyPlanRepository
                    .findByPlanDateAndItemTypeAndItemIdIn(today, ItemType.TOPPING, toppingIds)
                    .stream()
                    .collect(Collectors.toMap(DailyPlan::getItemId, plan -> plan));
        }

        DishResponse response = dishMapper.toDishResponse(dish);

        // ✅ SỬA LẠI: Thêm điều kiện kiểm tra 'status'
        int dishRemainingQuantity = (dishPlan != null && dishPlan.getStatus()) ? dishPlan.getRemainingQuantity() : 0;
        response.setRemainingQuantity(dishRemainingQuantity);

        Map<Integer, DailyPlan> finalToppingPlansMap = toppingPlansMap;
        List<ToppingWithQuantityResponse> toppingResponses = optionalToppings.stream()
                .map(topping -> {
                    DailyPlan toppingPlan = finalToppingPlansMap.get(topping.getToppingId());

                    // ✅ SỬA LẠI: Thêm điều kiện kiểm tra 'status' cho topping
                    int remaining = (toppingPlan != null && toppingPlan.getStatus()) ? toppingPlan.getRemainingQuantity() : 0;

                    return ToppingWithQuantityResponse.builder()
                            .toppingId(topping.getToppingId())
                            .name(topping.getName())
                            .price(topping.getPrice())
                            .calories(topping.getCalories())
                            .gram(topping.getGram())
                            .remainingQuantity(remaining)
                            .build();
                })
                .collect(Collectors.toList());

        response.setOptionalToppings(toppingResponses);

        return response;
    }

    @Transactional(readOnly = true)
    public List<DishResponse> getAllDishes() {
        // Bước 1: Lấy tất cả Dish và Topping liên quan trong 1 query
        List<Dish> dishes = dishRepository.findAllWithToppings();
        if (dishes.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();

        // Bước 2: Thu thập tất cả ID cần thiết
        List<Integer> dishIds = dishes.stream().map(Dish::getDishId).toList();
        List<Integer> allToppingIds = dishes.stream()
                .flatMap(dish -> dish.getDishToppings().stream())
                .map(dishTopping -> dishTopping.getTopping().getToppingId())
                .distinct()
                .toList();

        // Bước 3: Lấy tất cả kế hoạch của Dish và Topping trong 2 query hàng loạt
        Map<Integer, DailyPlan> dishPlansMap = dailyPlanRepository
                .findByPlanDateAndItemTypeAndItemIdIn(today, ItemType.DISH, dishIds)
                .stream()
                .collect(Collectors.toMap(DailyPlan::getItemId, plan -> plan));

        Map<Integer, DailyPlan> toppingPlansMap = Collections.emptyMap();
        if (!allToppingIds.isEmpty()) {
            toppingPlansMap = dailyPlanRepository
                    .findByPlanDateAndItemTypeAndItemIdIn(today, ItemType.TOPPING, allToppingIds)
                    .stream()
                    .collect(Collectors.toMap(DailyPlan::getItemId, plan -> plan));
        }

        // Bước 4: Lắp ráp dữ liệu
        final Map<Integer, DailyPlan> finalToppingPlansMap = toppingPlansMap;
        return dishes.stream().map(dish -> {
            DishResponse response = dishMapper.toDishResponse(dish);

            // Gán số lượng cho món ăn
            DailyPlan dishPlan = dishPlansMap.get(dish.getDishId());
            int dishRemaining = (dishPlan != null && dishPlan.getStatus()) ? dishPlan.getRemainingQuantity() : 0;
            response.setRemainingQuantity(dishRemaining);

            // Gán số lượng cho từng topping tùy chọn
            List<ToppingWithQuantityResponse> toppingResponses = dish.getDishToppings().stream()
                    .map(dishTopping -> {
                        Topping topping = dishTopping.getTopping();
                        DailyPlan toppingPlan = finalToppingPlansMap.get(topping.getToppingId());
                        int remaining = (toppingPlan != null && toppingPlan.getStatus()) ? toppingPlan.getRemainingQuantity() : 0;

                        return ToppingWithQuantityResponse.builder()
                                .toppingId(topping.getToppingId())
                                .name(topping.getName())
                                .price(topping.getPrice())
                                .calories(topping.getCalories())
                                .gram(topping.getGram())
                                .remainingQuantity(remaining)
                                .build();
                    })
                    .collect(Collectors.toList());

            response.setOptionalToppings(toppingResponses);
            return response;
        }).collect(Collectors.toList());
    }


    // Các phương thức còn lại (create, update, delete) không thay đổi
    public DishResponse createDish(DishCreationRequest request) {
        if (dishRepository.existsByDishName(request.getDishName())) {
            throw new AppException(ErrorCode.DISH_EXISTED);
        }
        Dish dish = dishMapper.toDish(request);
        dish.setIsAvailable(true);
        Dish saved = dishRepository.save(dish);
        return dishMapper.toDishResponse(saved);
    }

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

    public void deleteDish(int dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
        dish.setIsAvailable(false);
        dishRepository.save(dish);
    }
}