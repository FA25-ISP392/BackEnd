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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable; // ✅ Đảm bảo import đúng
import org.springframework.web.multipart.MultipartFile;

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
    CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public DishResponse getDishById(int dishId) {
        Dish dish = dishRepository.findByIdWithToppings(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));

        LocalDate today = LocalDate.now();

        DailyPlan dishPlan = dailyPlanRepository
                .findByItemIdAndItemTypeAndPlanDate(dish.getDishId(), ItemType.DISH, today)
                .orElse(null);

        // ✅ Thêm kiểm tra null
        List<Topping> optionalToppings = dish.getDishToppings().stream()
                .filter(dt -> dt != null && dt.getTopping() != null) // Lọc các topping null
                .map(dishTopping -> dishTopping.getTopping())
                .toList();

        Map<Integer, DailyPlan> toppingPlansMap = Collections.emptyMap();
        if (!optionalToppings.isEmpty()) {
            List<Integer> toppingIds = optionalToppings.stream().map(Topping::getToppingId).toList();
            toppingPlansMap = dailyPlanRepository
                    .findByPlanDateAndItemTypeAndItemIdIn(today, ItemType.TOPPING, toppingIds)
                    .stream()
                    .collect(Collectors.toMap(DailyPlan::getItemId, plan -> plan));
        }

        DishResponse response = dishMapper.toDishResponse(dish);
        int dishRemainingQuantity = (dishPlan != null && dishPlan.getStatus()) ? dishPlan.getRemainingQuantity() : 0;
        response.setRemainingQuantity(dishRemainingQuantity);

        Map<Integer, DailyPlan> finalToppingPlansMap = toppingPlansMap;
        List<ToppingWithQuantityResponse> toppingResponses = optionalToppings.stream()
                .map(topping -> {
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
    }

    // PHƯƠNG THỨC MỚI DÀNH CHO PHÂN TRANG
    @Transactional(readOnly = true)
    public Page<DishResponse> getAllDishesPaginated(Pageable pageable) {
        Page<Dish> dishPage = dishRepository.findAllWithToppings(pageable);
        List<Dish> dishes = dishPage.getContent();

        if (dishes.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, dishPage.getTotalElements());
        }

        LocalDate today = LocalDate.now();

        // ✅ Thêm kiểm tra null
        List<Integer> dishIds = dishes.stream().map(Dish::getDishId).toList();
        List<Integer> allToppingIds = dishes.stream()
                .filter(dish -> dish.getDishToppings() != null)
                .flatMap(dish -> dish.getDishToppings().stream())
                .filter(dt -> dt != null && dt.getTopping() != null)
                .map(dishTopping -> dishTopping.getTopping().getToppingId())
                .distinct()
                .toList();

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

        final Map<Integer, DailyPlan> finalToppingPlansMap = toppingPlansMap;
        List<DishResponse> dishResponses = dishes.stream().map(dish -> {
            DishResponse response = dishMapper.toDishResponse(dish);

            DailyPlan dishPlan = dishPlansMap.get(dish.getDishId());
            int dishRemaining = (dishPlan != null && dishPlan.getStatus()) ? dishPlan.getRemainingQuantity() : 0;
            response.setRemainingQuantity(dishRemaining);

            // ✅ Thêm kiểm tra null
            List<ToppingWithQuantityResponse> toppingResponses = Collections.emptyList();
            if (dish.getDishToppings() != null) {
                toppingResponses = dish.getDishToppings().stream()
                        .filter(dt -> dt != null && dt.getTopping() != null)
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
            }

            response.setOptionalToppings(toppingResponses);
            return response;
        }).collect(Collectors.toList());

        return new PageImpl<>(dishResponses, pageable, dishPage.getTotalElements());
    }

    // PHIÊN BẢN CŨ (LẤY TẤT CẢ) - ĐÃ ĐƯỢC TỐI ƯU VÀ THÊM NULL CHECK
    @Transactional(readOnly = true)
    public List<DishResponse> getAllDishes() {
        List<Dish> dishes = dishRepository.findAllWithToppings();
        if (dishes.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();

        List<Integer> dishIds = dishes.stream().map(Dish::getDishId).toList();

        // ✅ Thêm kiểm tra null
        List<Integer> allToppingIds = dishes.stream()
                .filter(dish -> dish.getDishToppings() != null)
                .flatMap(dish -> dish.getDishToppings().stream())
                .filter(dt -> dt != null && dt.getTopping() != null)
                .map(dishTopping -> dishTopping.getTopping().getToppingId())
                .distinct()
                .toList();

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

        final Map<Integer, DailyPlan> finalToppingPlansMap = toppingPlansMap;
        return dishes.stream().map(dish -> {
            DishResponse response = dishMapper.toDishResponse(dish);

            DailyPlan dishPlan = dishPlansMap.get(dish.getDishId());
            int dishRemaining = (dishPlan != null && dishPlan.getStatus()) ? dishPlan.getRemainingQuantity() : 0;
            response.setRemainingQuantity(dishRemaining);

            // ✅ Thêm kiểm tra null
            List<ToppingWithQuantityResponse> toppingResponses = Collections.emptyList();
            if (dish.getDishToppings() != null) {
                toppingResponses = dish.getDishToppings().stream()
                        .filter(dt -> dt != null && dt.getTopping() != null)
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
            }

            response.setOptionalToppings(toppingResponses);
            return response;
        }).collect(Collectors.toList());
    }

    public DishResponse createDish(DishCreationRequest request, MultipartFile imageFile) {
        if (dishRepository.existsByDishName(request.getDishName())) {
            throw new AppException(ErrorCode.DISH_EXISTED);
        }

        Dish dish = dishMapper.toDish(request);

        // Upload ảnh lên Cloudinary
        String imageUrl = cloudinaryService.uploadImage(imageFile);
        dish.setPicture(imageUrl);

        dish.setIsAvailable(true);

        Dish saved = dishRepository.save(dish);
        return dishMapper.toDishResponse(saved);
    }

    @Transactional
    public DishResponse updateDish(int dishId, DishUpdateRequest request, MultipartFile imageFile) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));

        if (request.getDishName() != null && !request.getDishName().equals(dish.getDishName())) {
            if (dishRepository.existsByDishName(request.getDishName())) {
                throw new AppException(ErrorCode.DISH_EXISTED);
            }
        }

        dishMapper.updateDish(dish, request);

        if (imageFile != null && !imageFile.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadImage(imageFile);
            dish.setPicture(newImageUrl);
        }

        return dishMapper.toDishResponse(dish);
    }

    public void deleteDish(int dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
        dish.setIsAvailable(false);
        dishRepository.save(dish);
    }
}