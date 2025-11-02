package com.isp392.service;

import com.isp392.dto.request.SuggestionCreationRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.dto.response.SuggestionResponse; // 👈 Đổi lại tên DTO cho đúng
import com.isp392.dto.response.ToppingWithQuantityResponse;
import com.isp392.entity.Customer;
import com.isp392.entity.Dish;
import com.isp392.entity.Topping;
import com.isp392.enums.Category;
import com.isp392.enums.ItemType;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.DishMapper;
import com.isp392.repository.CustomerRepository;
import com.isp392.repository.DishRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet; // 👈 Thêm
import java.util.List;
import java.util.Map;
import java.util.Random; // 👈 Thêm
import java.util.Set; // 👈 Thêm
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SuggestionService {

    CustomerRepository customerRepository;
    DishRepository dishRepository;
    DishMapper dishMapper;
    DailyPlanService dailyPlanService;

    private static final double CALORIE_TOLERANCE_PERCENT = 0.15;
    private static final int MAX_SUGGESTIONS = 3;
    // ✅ Giới hạn số lần thử ngẫu nhiên để tránh vòng lặp vô tận
    private static final int MAX_RANDOM_ATTEMPTS = 1000;
    private static final Random random = new Random();

    // ----- CÁC CLASS HELPER (Thay thế Record) -----
    @Data
    @AllArgsConstructor
    private static class CalorieRange {
        double minCal;
        double maxCal;
        double targetPerMeal;
    }

    @Data
    @AllArgsConstructor
    private static class DishPool {
        List<Dish> drinks;
        List<Dish> salads;
        List<Dish> mainCourses;
        List<Dish> desserts;
    }

    @Data
    @AllArgsConstructor
    private static class ItemIds {
        List<Integer> dishIds;
        List<Integer> toppingIds;
    }

    @Data
    @AllArgsConstructor
    private static class QuantityMaps {
        Map<Integer, Integer> dishQuantities;
        Map<Integer, Integer> toppingQuantities;
    }


    // ===================================================================
    // HÀM CHÍNH (ĐIỀU PHỐI)
    // ===================================================================

    public List<SuggestionResponse> getSuggestionsForCustomer(String username, SuggestionCreationRequest request) {
        // VIỆC 1: Lấy Customer
        Customer customer = customerRepository.findByUsernameForSuggestion(username)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        // VIỆC 2: Tính Calo
        CalorieRange calorieRange = calculateTargetCalories(customer, request);

        // VIỆC 3: Tải "bể" món ăn (KHÔNG shuffle)
        DishPool dishPool = loadDishPool(); // 👈 Đổi tên hàm
        if (dishPool.drinks.isEmpty() || dishPool.salads.isEmpty() ||
                dishPool.mainCourses.isEmpty() || dishPool.desserts.isEmpty()) {
            return Collections.emptyList();
        }

        // VIỆC 4: Trích xuất ID
        ItemIds itemIds = extractItemIds(dishPool);

        // VIỆC 5: Lấy số lượng tồn kho
        QuantityMaps quantityMaps = loadInventoryQuantities(itemIds);

        // VIỆC 6: ✅ Tìm tổ hợp NGẪU NHIÊN
        Set<SuggestionResponse> suggestionSet = findRandomCombinations(dishPool, quantityMaps, calorieRange);

        // VIỆC 7: Trả về kết quả (đã được đa dạng hóa)
        return new ArrayList<>(suggestionSet);
    }

    // ===================================================================
    // CÁC HÀM HELPER (MỖI HÀM 1 VIỆC)
    // ===================================================================

    /**
     * (VIỆC 2) Tính Calo mục tiêu (Giữ nguyên)
     */
    private CalorieRange calculateTargetCalories(Customer customer, SuggestionCreationRequest request) {
        Double height = customer.getHeight();
        Double weight = customer.getWeight();
        Boolean sex = customer.getSex();
        Integer age = request.getAge();
        if (height == null || weight == null || sex == null || age == null) {
            throw new AppException(ErrorCode.INCOMPLETE_PROFILE);
        }

        double bmr;
        if (sex) { // true = Nam
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else { // false = Nữ
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        double tdee = bmr * request.getActivityLevel().getMultiplier();
        switch (request.getGoal()) {
            case BUILD_MUSCLE -> tdee += 500;
            case FAT_LOSS -> tdee -= 500;
            case STAY_FIT -> { /* Không điều chỉnh */ }
        }

        double targetCaloriesPerMeal = tdee / customer.getPortion();
        double minCal = targetCaloriesPerMeal * (1 - CALORIE_TOLERANCE_PERCENT);
        double maxCal = targetCaloriesPerMeal * (1 + CALORIE_TOLERANCE_PERCENT);

        return new CalorieRange(minCal, maxCal, targetCaloriesPerMeal);
    }

    /**
     * (VIỆC 3) ✅ SỬA ĐỔI: Chỉ tải "bể" món ăn, KHÔNG shuffle
     */
    private DishPool loadDishPool() {
        List<Dish> drinks = dishRepository.findAllWithToppings(Category.DRINKS, null);
        List<Dish> salads = dishRepository.findAllWithToppings(Category.SALAD, null);
        List<Dish> mainCourses = new ArrayList<>();
        mainCourses.addAll(dishRepository.findAllWithToppings(Category.PIZZA, null));
        mainCourses.addAll(dishRepository.findAllWithToppings(Category.PASTA, null));
        List<Dish> desserts = dishRepository.findAllWithToppings(Category.DESSERT, null);

        return new DishPool(drinks, salads, mainCourses, desserts);
    }

    /**
     * (VIỆC 4) Trích xuất ID (Giữ nguyên)
     */
    private ItemIds extractItemIds(DishPool dishPool) {
        List<Dish> allDishes = new ArrayList<>();
        allDishes.addAll(dishPool.drinks);
        allDishes.addAll(dishPool.salads);
        allDishes.addAll(dishPool.mainCourses);
        allDishes.addAll(dishPool.desserts);

        List<Integer> allDishIds = allDishes.stream()
                .map(Dish::getDishId)
                .distinct()
                .toList();

        List<Integer> allToppingIds = allDishes.stream()
                .filter(dish -> dish.getDishToppings() != null)
                .flatMap(dish -> dish.getDishToppings().stream())
                .filter(dt -> dt != null && dt.getTopping() != null)
                .map(dt -> dt.getTopping().getToppingId())
                .distinct()
                .toList();

        return new ItemIds(allDishIds, allToppingIds);
    }

    /**
     * (VIỆC 5) Tải số lượng tồn kho (Giữ nguyên)
     */
    private QuantityMaps loadInventoryQuantities(ItemIds itemIds) {
        LocalDate today = LocalDate.now();

        Map<Integer, Integer> dishQuantities = dailyPlanService.getRemainingQuantitiesForItems(
                ItemType.DISH, itemIds.dishIds, today
        );

        Map<Integer, Integer> toppingQuantities = dailyPlanService.getRemainingQuantitiesForItems(
                ItemType.TOPPING, itemIds.toppingIds, today
        );

        return new QuantityMaps(dishQuantities, toppingQuantities);
    }


    /**
     * (VIỆC 6) ✅ SỬA ĐỔI: Tìm tổ hợp bằng cách chọn ngẫu nhiên
     */
    private Set<SuggestionResponse> findRandomCombinations(DishPool dishPool,
                                                       QuantityMaps quantityMaps,
                                                       CalorieRange calorieRange) {
        // Dùng Set để tự động chống trùng lặp
        Set<SuggestionResponse> suggestions = new HashSet<>();

        // Lấy kích thước của các danh sách
        int drinksCount = dishPool.drinks.size();
        int saladsCount = dishPool.salads.size();
        int mainCoursesCount = dishPool.mainCourses.size();
        int dessertsCount = dishPool.desserts.size();

        int attempts = 0;
        // Chạy vòng lặp cho đến khi đủ 3 gợi ý, hoặc đã thử quá nhiều lần
        while (suggestions.size() < MAX_SUGGESTIONS && attempts < MAX_RANDOM_ATTEMPTS) {

            // 1. Chọn ngẫu nhiên 1 món từ mỗi loại
            Dish drink = dishPool.drinks.get(random.nextInt(drinksCount));
            Dish salad = dishPool.salads.get(random.nextInt(saladsCount));
            Dish main = dishPool.mainCourses.get(random.nextInt(mainCoursesCount));
            Dish dessert = dishPool.desserts.get(random.nextInt(dessertsCount));

            // Tăng biến đếm số lần thử
            attempts++;

            // 2. Kiểm tra Calo
            double totalCal = drink.getCalo() + salad.getCalo() + main.getCalo() + dessert.getCalo();
            if (totalCal < calorieRange.minCal || totalCal > calorieRange.maxCal) {
                continue; // Bỏ qua nếu Calo không phù hợp
            }

            // 3. Lắp ráp Response (để lấy số lượng)
            DishResponse drinkResponse = buildEnrichedDishResponse(drink, quantityMaps);
            DishResponse saladResponse = buildEnrichedDishResponse(salad, quantityMaps);
            DishResponse mainResponse = buildEnrichedDishResponse(main, quantityMaps);
            DishResponse dessertResponse = buildEnrichedDishResponse(dessert, quantityMaps);

            // 4. Kiểm tra số lượng
            if (drinkResponse.getRemainingQuantity() <= 0 || saladResponse.getRemainingQuantity() <= 0 ||
                    mainResponse.getRemainingQuantity() <= 0 || dessertResponse.getRemainingQuantity() <= 0) {
                continue; // Bỏ qua nếu có món hết hàng
            }

            // 5. Tạo Menu
            SuggestionResponse menu = SuggestionResponse.builder()
                    .drink(drinkResponse)
                    .salad(saladResponse)
                    .mainCourse(mainResponse)
                    .dessert(dessertResponse)
                    .totalCalories(totalCal)
                    .targetCaloriesPerMeal(calorieRange.targetPerMeal)
                    .build();

            // 6. Thêm vào Set (Set sẽ tự bỏ qua nếu đã tồn tại)
            suggestions.add(menu);
        }

        return suggestions; // Trả về Set (chứa 0, 1, 2, hoặc 3 gợi ý)
    }

    /**
     * (VIỆC 7 - Helper) Lắp ráp một DishResponse (Giữ nguyên)
     */
    private DishResponse buildEnrichedDishResponse(Dish dish, QuantityMaps quantityMaps) {
        DishResponse response = dishMapper.toDishResponse(dish);
        int dishRemaining = quantityMaps.getDishQuantities().getOrDefault(dish.getDishId(), 0);
        response.setRemainingQuantity(dishRemaining);

        List<ToppingWithQuantityResponse> toppingResponses = buildEnrichedToppingList(dish, quantityMaps.getToppingQuantities());
        response.setOptionalToppings(toppingResponses);

        return response;
    }


    /**
     * (VIỆC 8 - Helper) Xây dựng danh sách topping (Giữ nguyên)
     */
    private List<ToppingWithQuantityResponse> buildEnrichedToppingList(Dish dish, Map<Integer, Integer> toppingQuantities) {
        if (dish.getDishToppings() == null || dish.getDishToppings().isEmpty()) {
            return Collections.emptyList();
        }

        return dish.getDishToppings().stream()
                .filter(dt -> dt != null && dt.getTopping() != null)
                .map(dishTopping -> {
                    Topping topping = dishTopping.getTopping();
                    int toppingRemaining = toppingQuantities.getOrDefault(topping.getToppingId(), 0);

                    return ToppingWithQuantityResponse.builder()
                            .toppingId(topping.getToppingId())
                            .name(topping.getName())
                            .price(topping.getPrice())
                            .calories(topping.getCalories())
                            .gram(topping.getGram())
                            .remainingQuantity(toppingRemaining)
                            .build();
                })
                .collect(Collectors.toList());
    }
}