package com.isp392.service;

import com.isp392.dto.request.SuggestionRequest;
import com.isp392.dto.response.MenuSuggestion;
import com.isp392.entity.Customer;
import com.isp392.entity.Dish;
import com.isp392.enums.Category;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.DishMapper;
import com.isp392.repository.CustomerRepository;
import com.isp392.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SuggestionService {

    CustomerRepository customerRepository;
    DishRepository dishRepository;
    DishMapper dishMapper;

    // Định nghĩa một khoảng chênh lệch calo cho phép (ví dụ: 15%)
    private static final double CALORIE_TOLERANCE_PERCENT = 0.15;
    // Số lượng thực đơn tối đa trả về
    private static final int MAX_SUGGESTIONS = 3;

    public List<MenuSuggestion> getSuggestionsForCustomer(String username, SuggestionRequest request) {
        // 1. Lấy thông tin Customer
        Customer customer = customerRepository.findByUsernameForSuggestion(username)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        // 2. Lấy các thông tin cần thiết
        Double height = customer.getHeight();
        Double weight = customer.getWeight();
        Boolean sex = customer.getSex(); // Giả sử: true = Nam, false = Nữ
        LocalDate dob = customer.getAccount().getDob();

        if (height == null || weight == null || sex == null || dob == null) {
            // Yêu cầu người dùng cập nhật thông tin
            throw new AppException(ErrorCode.INCOMPLETE_PROFILE);
        }

        int age = Period.between(dob, LocalDate.now()).getYears();

        // 3. Tính BMR (Mifflin-St Jeor)
        double bmr;
        if (sex) { // true = Nam
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else { // false = Nữ
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        // 4. Tính TDEE
        double tdee = bmr * request.getActivityLevel().getMultiplier();

        // 5. Tính Calo mục tiêu mỗi bữa
        double targetCaloriesPerMeal = tdee / request.getNumberOfMeals();
        double minCal = targetCaloriesPerMeal * (1 - CALORIE_TOLERANCE_PERCENT);
        double maxCal = targetCaloriesPerMeal * (1 + CALORIE_TOLERANCE_PERCENT);

        // 6. Lấy danh sách món ăn theo loại (chỉ lấy món available=true)
        List<Dish> drinks = dishRepository.findAllWithToppings(Category.DRINKS, null);
        List<Dish> salads = dishRepository.findAllWithToppings(Category.SALAD, null);
        List<Dish> mainCourses = new ArrayList<>();
        mainCourses.addAll(dishRepository.findAllWithToppings(Category.PIZZA, null));
        mainCourses.addAll(dishRepository.findAllWithToppings(Category.PASTA, null));
        List<Dish> desserts = dishRepository.findAllWithToppings(Category.DESSERT, null);

        // Xáo trộn danh sách để mỗi lần gợi ý có thể khác nhau
        Collections.shuffle(drinks);
        Collections.shuffle(salads);
        Collections.shuffle(mainCourses);
        Collections.shuffle(desserts);

        // 7. Tìm kiếm tổ hợp (thuật toán Brute-force 4-nested-loop)
        List<MenuSuggestion> suggestions = new ArrayList<>();

        // Nếu 1 trong 4 danh sách rỗng, không thể tạo thực đơn 4 món
        if (drinks.isEmpty() || salads.isEmpty() || mainCourses.isEmpty() || desserts.isEmpty()) {
            // Bạn có thể thêm logic tìm 3 món, 2 món ở đây nếu muốn
            // Tạm thời trả về rỗng nếu không đủ 4 loại
            return suggestions;
        }

        for (Dish drink : drinks) {
            for (Dish salad : salads) {
                for (Dish main : mainCourses) {
                    for (Dish dessert : desserts) {

                        // Dish.calo là Double
                        double totalCal = drink.getCalo() + salad.getCalo() + main.getCalo() + dessert.getCalo();

                        if (totalCal >= minCal && totalCal <= maxCal) {
                            // Map Dish entity sang DishResponse
                            // Mapper này chỉ map thông tin cơ bản, không cần số lượng tồn kho
                            MenuSuggestion menu = MenuSuggestion.builder()
                                    .drink(dishMapper.toDishResponse(drink))
                                    .salad(dishMapper.toDishResponse(salad))
                                    .mainCourse(dishMapper.toDishResponse(main))
                                    .dessert(dishMapper.toDishResponse(dessert))
                                    .totalCalories(totalCal)
                                    .targetCaloriesPerMeal(targetCaloriesPerMeal)
                                    .build();

                            suggestions.add(menu);

                            if (suggestions.size() >= MAX_SUGGESTIONS) {
                                return suggestions; // Đã đủ 3 gợi ý
                            }
                        }
                    }
                }
            }
        }

        return suggestions; // Trả về 0, 1, hoặc 2 gợi ý nếu không tìm đủ 3
    }
}