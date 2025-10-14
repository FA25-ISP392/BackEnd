package com.isp392.mapper;

import com.isp392.dto.request.DishToppingCreationRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.entity.Dish;
import com.isp392.entity.DishTopping;
import com.isp392.entity.Topping;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DishToppingMapper {

    @Mapping(target = "dish", source = "dish")
    @Mapping(target = "topping", source = "topping")
    DishTopping toEntity(DishToppingCreationRequest request, Dish dish, Topping topping);

    @Mapping(target = "toppingId", source = "topping.toppingId")
    @Mapping(target = "toppingName", source = "topping.name") // ✅ đúng
    @Mapping(target = "price", source = "topping.price")
    @Mapping(target = "calories", source = "topping.calories")
    @Mapping(target = "gram", source = "topping.gram")
    DishToppingResponse toResponse(DishTopping entity);

    List<DishToppingResponse> toResponseList(List<DishTopping> entities);

    void updateEntity(@MappingTarget DishTopping entity,
                      DishToppingCreationRequest request,
                      Dish dish,
                      Topping topping);
}

