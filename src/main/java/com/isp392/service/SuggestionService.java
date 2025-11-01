package com.isp392.service;

import com.isp392.dto.request.SuggestionCreationRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.dto.response.MenuSuggestion;
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
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    // ----- CÁC RECORD HELPER (Túi chứa dữ liệu nội bộ) -----
    private record CalorieRange(double minCal, double maxCal, double targetPerMeal) {
    }

    private record DishPool(List<Dish> drinks, List<Dish> salads, List<Dish> mainCourses, List<Dish> desserts) {
    }

    // ✅ TÁCH RA: Record để chứa danh sách ID
    private record ItemIds(List<Integer> dishIds, List<Integer> toppingIds) {
    }

    // ✅ TÁCH RA: Record để chứa Map số lượng
    private record QuantityMaps(Map<Integer, Integer> dishQuantities, Map<Integer, Integer> toppingQuantities) {
    }


    // ===================================================================
    // HÀM CHÍNH (ĐIỀU PHỐI)
    // ===================================================================

    public List<MenuSuggestion> getSuggestionsForCustomer(String username, SuggestionCreationRequest request) {
        // VIỆC 1: Lấy Customer
        Customer customer = customerRepository.findByUsernameForSuggestion(username)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        // VIỆC 2: Tính Calo
        CalorieRange calorieRange = calculateTargetCalories(customer, request);

        // VIỆC 3: Tải "bể" món ăn
        DishPool dishPool = loadDishPool();
        if (dishPool.drinks.isEmpty() || dishPool.salads.isEmpty() ||
                dishPool.mainCourses.isEmpty() || dishPool.desserts.isEmpty()) {
            return Collections.emptyList();
        }

        // VIỆC 4: Trích xuất ID từ "bể" món ăn
        ItemIds itemIds = extractItemIds(dishPool);

        // VIỆC 5: Lấy số lượng tồn kho cho các ID đó
        QuantityMaps quantityMaps = loadInventoryQuantities(itemIds);

        // VIỆC 6: Tìm tổ hợp
        return findMatchingCombinations(dishPool, quantityMaps, calorieRange);
    }

    // ===================================================================
    // CÁC HÀM HELPER (MỖI HÀM 1 VIỆC)
    // ===================================================================

    /**
     * (VIỆC 2) Tính toán BMR, TDEE và khoảng Calo mục tiêu.
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
     * (VIỆC 3) Tải tất cả các món ăn (Dish) từ DB, phân loại và xáo trộn.
     */
    private DishPool loadDishPool() {
        List<Dish> drinks = dishRepository.findAllWithToppings(Category.DRINKS, null);
        List<Dish> salads = dishRepository.findAllWithToppings(Category.SALAD, null);
        List<Dish> mainCourses = new ArrayList<>();
        mainCourses.addAll(dishRepository.findAllWithToppings(Category.PIZZA, null));
        mainCourses.addAll(dishRepository.findAllWithToppings(Category.PASTA, null));
        List<Dish> desserts = dishRepository.findAllWithToppings(Category.DESSERT, null);

        // Xáo trộn danh sách
        Collections.shuffle(drinks);
        Collections.shuffle(salads);
        Collections.shuffle(mainCourses);
        Collections.shuffle(desserts);

        return new DishPool(drinks, salads, mainCourses, desserts);
    }

    /**
     * (VIỆC 4) ✅ HÀM MỚI: Chỉ làm 1 việc: Trích xuất ID từ "bể" món ăn.
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
     * (VIỆC 5) ✅ HÀM MỚI: Chỉ làm 1 việc: Tải số lượng tồn kho.
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
     * (VIỆC 6) Tìm kiếm các tổ hợp món ăn phù hợp.
     */
    private List<MenuSuggestion> findMatchingCombinations(DishPool dishPool,
                                                          QuantityMaps quantityMaps,
                                                          CalorieRange calorieRange) {
        List<MenuSuggestion> suggestions = new ArrayList<>();

        for (Dish drink : dishPool.drinks) {
            for (Dish salad : dishPool.salads) {
                for (Dish main : dishPool.mainCourses) {
                    for (Dish dessert : dishPool.desserts) {

                        double totalCal = drink.getCalo() + salad.getCalo() + main.getCalo() + dessert.getCalo();

                        if (totalCal >= calorieRange.minCal && totalCal <= calorieRange.maxCal) {

                            // Gọi hàm helper (VIỆC 7)
                            DishResponse drinkResponse = buildEnrichedDishResponse(drink, quantityMaps);
                            DishResponse saladResponse = buildEnrichedDishResponse(salad, quantityMaps);
                            DishResponse mainResponse = buildEnrichedDishResponse(main, quantityMaps);
                            DishResponse dessertResponse = buildEnrichedDishResponse(dessert, quantityMaps);

                            // Lọc các món đã hết hàng
                            if (drinkResponse.getRemainingQuantity() <= 0 || saladResponse.getRemainingQuantity() <= 0 ||
                                    mainResponse.getRemainingQuantity() <= 0 || dessertResponse.getRemainingQuantity() <= 0) {
                                continue; // Bỏ qua combo này
                            }

                            MenuSuggestion menu = MenuSuggestion.builder()
                                    .drink(drinkResponse)
                                    .salad(saladResponse)
                                    .mainCourse(mainResponse)
                                    .dessert(dessertResponse)
                                    .totalCalories(totalCal)
                                    .targetCaloriesPerMeal(calorieRange.targetPerMeal)
                                    .build();

                            suggestions.add(menu);

                            if (suggestions.size() >= MAX_SUGGESTIONS) {
                                return suggestions;
                            }
                        }
                    }
                }
            }
        }
        return suggestions;
    }

    /**
     * (VIỆC 7 - Helper) Lắp ráp một DishResponse hoàn chỉnh (kèm topping & số lượng).
     */
    private DishResponse buildEnrichedDishResponse(Dish dish, QuantityMaps quantityMaps) {
        // 1. Dùng mapper để map thông tin cơ bản
        DishResponse response = dishMapper.toDishResponse(dish);

        // 2. Lấy số lượng món (Đọc từ Map)
        int dishRemaining = quantityMaps.dishQuantities().getOrDefault(dish.getDishId(), 0);
        response.setRemainingQuantity(dishRemaining);

        // 3. Lấy danh sách topping (Gọi hàm helper VIỆC 8)
        List<ToppingWithQuantityResponse> toppingResponses = buildEnrichedToppingList(dish, quantityMaps.toppingQuantities());
        response.setOptionalToppings(toppingResponses);

        return response;
    }


    /**
     * (VIỆC 8 - Helper) Xây dựng danh sách topping kèm số lượng.
     */
    private List<ToppingWithQuantityResponse> buildEnrichedToppingList(Dish dish, Map<Integer, Integer> toppingQuantities) {
        if (dish.getDishToppings() == null || dish.getDishToppings().isEmpty()) {
            return Collections.emptyList();
        }

        return dish.getDishToppings().stream()
                .filter(dt -> dt != null && dt.getTopping() != null)
                .map(dishTopping -> {
                    Topping topping = dishTopping.getTopping();

                    // Lấy số lượng topping (Đọc từ Map)
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