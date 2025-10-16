package com.isp392.mapper;

import com.isp392.dto.response.DishToppingResponse;
import com.isp392.entity.DishTopping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface DishToppingMapper {

    @Mapping(target = "toppingId", source = "topping.toppingId")
    @Mapping(target = "toppingName", source = "topping.name")
    @Mapping(target = "price", source = "topping.price")
    @Mapping(target = "calories", source = "topping.calories")
    @Mapping(target = "gram", source = "topping.gram")
    DishToppingResponse toResponse(DishTopping entity);

    List<DishToppingResponse> toResponseList(List<DishTopping> entities);
}