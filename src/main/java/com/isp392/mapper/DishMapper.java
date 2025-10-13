package com.isp392.mapper;

import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.entity.Dish;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DishMapper {

    Dish toDish(DishCreationRequest request);

    DishResponse toDishResponse(Dish dish);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDish(@MappingTarget Dish dish, DishUpdateRequest request);

    default List<DishToppingResponse> mapToppings(Dish dish) {
        if (dish.getDishToppings() == null) return List.of();
        return dish.getDishToppings().stream()
                .map(dt -> new DishToppingResponse(
                        dt.getTopping().getToppingId(),
                        dt.getTopping().getName(),
                        BigDecimal.valueOf(dt.getTopping().getPrice()),
                        dt.getTopping().getCalories(),
                        dt.getTopping().getGram()
                ))
                .collect(Collectors.toList());
    }

    @AfterMapping
    default void linkToppings(@MappingTarget DishResponse response, Dish dish) {
        response.setDishToppings(mapToppings(dish));
    }
}
