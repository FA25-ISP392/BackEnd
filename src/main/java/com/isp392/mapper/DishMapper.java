package com.isp392.mapper;

import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.entity.Dish;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DishMapper {

    Dish toDish(DishCreationRequest request);

    // ✅ Bỏ qua các trường phức tạp để Service tự xử lý
    @Mapping(target = "remainingQuantity", ignore = true)
    @Mapping(target = "optionalToppings", ignore = true)
    DishResponse toDishResponse(Dish dish);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDish(@MappingTarget Dish dish, DishUpdateRequest request);
}