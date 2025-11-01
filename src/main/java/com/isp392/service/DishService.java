package com.isp392.service;

import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.dto.response.ToppingWithQuantityResponse;
import com.isp392.entity.Dish;
import com.isp392.entity.Topping;
import com.isp392.enums.Category;
import com.isp392.enums.DishType;
import com.isp392.enums.ItemType;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.DishMapper;
import com.isp392.repository.DishRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    DailyPlanService dailyPlanService;
    DishMapper dishMapper;
    CloudinaryService cloudinaryService;

    // ----- Record nội bộ để vận chuyển dữ liệu -----
    private record ItemIds(List<Integer> dishIds, List<Integer> toppingIds) {}
    private record QuantityMaps(Map<Integer, Integer> dishQuantities, Map<Integer, Integer> toppingQuantities) {}

    // ===================================================================
    // CÁC HÀM PUBLIC (ĐÃ ĐƯỢC TÁI CẤU TRÚC)
    // ===================================================================

    @Transactional(readOnly = true)
    public DishResponse getDishById(int dishId) {
        // VIỆC 1: Lấy Dish Entity (kèm topping)
        Dish dish = dishRepository.findByIdWithToppings(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));

        // VIỆC 2: Lấy ID của món này và topping của nó
        ItemIds ids = extractItemIds(List.of(dish)); // Tái sử dụng helper

        // VIỆC 3: Lấy số lượng tồn kho
        QuantityMaps quantities = loadQuantities(ids, LocalDate.now());

        // VIỆC 4: Lắp ráp Response
        return buildEnrichedDishResponse(dish, quantities);
    }

    @Transactional(readOnly = true)
    public Page<DishResponse> getAllDishesPaginated(Pageable pageable, Category category, DishType type) {
        // VIỆC 1: Lấy dữ liệu thô (Entity)
        Page<Dish> dishPage = dishRepository.findAllWithToppings(pageable, category, type);
        List<Dish> dishes = dishPage.getContent();

        if (dishes.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, dishPage.getTotalElements());
        }

        // VIỆC 2: Gọi hàm helper tổng để xử lý
        List<DishResponse> dishResponses = mapDishListToResponseListWithDetails(dishes);

        // VIỆC 3: Trả về Page
        return new PageImpl<>(dishResponses, pageable, dishPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<DishResponse> getAllDishes(Category category, DishType type) {
        // VIỆC 1: Lấy dữ liệu thô (Entity)
        List<Dish> dishes = dishRepository.findAllWithToppings(category, type);
        if (dishes.isEmpty()) {
            return Collections.emptyList();
        }
        // VIỆC 2: Gọi hàm helper tổng để xử lý
        return mapDishListToResponseListWithDetails(dishes);
    }

    @Transactional
    public DishResponse createDish(DishCreationRequest request, MultipartFile imageFile) {
        // VIỆC 1: Kiểm tra nghiệp vụ
        if (dishRepository.existsByDishName(request.getDishName())) {
            throw new AppException(ErrorCode.DISH_EXISTED);
        }

        // VIỆC 2: Map cơ bản
        Dish dish = dishMapper.toDish(request);

        // VIỆC 3: Xử lý upload ảnh (gọi helper)
        String imageUrl = uploadImage(imageFile);
        dish.setPicture(imageUrl != null ? imageUrl : "loading"); // Gán ảnh hoặc ảnh default

        // VIỆC 4: Gán giá trị mặc định & Lưu
        dish.setIsAvailable(true);
        Dish saved = dishRepository.save(dish);
        dishRepository.flush(); // Đảm bảo ID được sinh ra ngay

        // VIỆC 5: Trả về response (lúc này chưa có topping hay số lượng)
        return dishMapper.toDishResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<DishResponse> getDishesByNameContaining(String dishName) {
        // VIỆC 1: Lấy dữ liệu thô
        List<Dish> dishes = dishRepository.findByDishNameContainingWithToppings(dishName);
        if (dishes.isEmpty()) {
            return Collections.emptyList();
        }
        // VIỆC 2: Gọi hàm helper tổng
        return mapDishListToResponseListWithDetails(dishes);
    }

    @Transactional
    public DishResponse updateDish(int dishId, DishUpdateRequest request, MultipartFile imageFile) {
        // VIỆC 1: Lấy entity
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));

        // VIỆC 2: Kiểm tra nghiệp vụ (trùng tên)
        if (request.getDishName() != null && !request.getDishName().equals(dish.getDishName())) {
            if (dishRepository.existsByDishName(request.getDishName())) {
                throw new AppException(ErrorCode.DISH_EXISTED);
            }
        }

        // VIỆC 3: Map các trường update
        dishMapper.updateDish(dish, request);

        // VIỆC 4: Xử lý upload ảnh (gọi helper)
        String newImageUrl = uploadImage(imageFile);
        if (newImageUrl != null) {
            dish.setPicture(newImageUrl); // Chỉ cập nhật nếu có ảnh mới
        }

        // VIỆC 5: Lưu và trả về
        Dish updatedDish = dishRepository.save(dish);
        return dishMapper.toDishResponse(updatedDish);
    }

    // (Hàm này đã tuân thủ SRP, giữ nguyên)
    public void deleteDish(int dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new AppException(ErrorCode.DISH_NOT_FOUND));
        dish.setIsAvailable(false);
        dishRepository.save(dish);
    }

    // ===================================================================
    // CÁC HÀM HELPER (ĐÃ ĐƯỢC TÁCH NHỎ)
    // ===================================================================

    /**
     * HÀM HELPER TỔNG:
     * (HELPER CHÍNH) Điều phối việc map 1 list Dish sang List<DishResponse> kèm số lượng.
     */
    private List<DishResponse> mapDishListToResponseListWithDetails(List<Dish> dishes) {
        // VIỆC 1: Trích xuất IDs
        ItemIds ids = extractItemIds(dishes);

        // VIỆC 2: Tải số lượng
        QuantityMaps quantities = loadQuantities(ids, LocalDate.now());

        // VIỆC 3: Lắp ráp (Stream và gọi helper 'buildEnrichedDishResponse')
        return dishes.stream()
                .map(dish -> buildEnrichedDishResponse(dish, quantities))
                .collect(Collectors.toList());
    }

    /**
     * (HELPER 1) Chỉ làm 1 việc: Trích xuất tất cả ID từ list Dish (và topping của chúng).
     */
    private ItemIds extractItemIds(List<Dish> dishes) {
        List<Integer> dishIds = dishes.stream()
                .map(Dish::getDishId)
                .distinct()
                .toList();

        List<Integer> allToppingIds = dishes.stream()
                .filter(dish -> dish.getDishToppings() != null)
                .flatMap(dish -> dish.getDishToppings().stream())
                .filter(dt -> dt != null && dt.getTopping() != null)
                .map(dishTopping -> dishTopping.getTopping().getToppingId())
                .distinct()
                .toList();

        return new ItemIds(dishIds, allToppingIds);
    }

    /**
     * (HELPER 2) Chỉ làm 1 việc: Tải Map số lượng từ DailyPlanService.
     */
    private QuantityMaps loadQuantities(ItemIds ids, LocalDate date) {
        Map<Integer, Integer> dishQuantities = dailyPlanService.getRemainingQuantitiesForItems(
                ItemType.DISH, ids.dishIds, date
        );
        Map<Integer, Integer> toppingQuantities = dailyPlanService.getRemainingQuantitiesForItems(
                ItemType.TOPPING, ids.toppingIds, date
        );
        return new QuantityMaps(dishQuantities, toppingQuantities);
    }

    /**
     * (HELPER 3) Chỉ làm 1 việc: Lắp ráp 1 DishResponse từ 1 Dish và Map số lượng.
     */
    private DishResponse buildEnrichedDishResponse(Dish dish, QuantityMaps quantities) {
        // 3a. Map cơ bản
        DishResponse response = dishMapper.toDishResponse(dish);

        // 3b. Set số lượng món
        int dishRemaining = quantities.dishQuantities().getOrDefault(dish.getDishId(), 0);
        response.setRemainingQuantity(dishRemaining);

        // 3c. Set list topping (gọi helper 4)
        List<ToppingWithQuantityResponse> toppingResponses = buildEnrichedToppingList(
                dish, quantities.toppingQuantities()
        );
        response.setOptionalToppings(toppingResponses);

        return response;
    }

    /**
     * (HELPER 4) Chỉ làm 1 việc: Xây dựng List<ToppingWithQuantityResponse> cho 1 món.
     */
    private List<ToppingWithQuantityResponse> buildEnrichedToppingList(Dish dish, Map<Integer, Integer> toppingQuantities) {
        if (dish.getDishToppings() == null || dish.getDishToppings().isEmpty()) {
            return Collections.emptyList();
        }

        return dish.getDishToppings().stream()
                .filter(dt -> dt != null && dt.getTopping() != null)
                .map(dishTopping -> {
                    Topping topping = dishTopping.getTopping();
                    int remaining = toppingQuantities.getOrDefault(topping.getToppingId(), 0);

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

    /**
     * (HELPER 5) Chỉ làm 1 việc: Upload ảnh (nếu có) và trả về URL.
     */
    private String uploadImage(MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            // Logic upload ảnh nằm gọn ở đây
            return cloudinaryService.uploadImage(imageFile);
        }
        return null; // Trả về null nếu không có ảnh
    }
}