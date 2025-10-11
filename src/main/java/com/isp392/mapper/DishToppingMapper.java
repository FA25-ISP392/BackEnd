package com.isp392.mapper;

import com.isp392.dto.request.DishToppingCreationRequest;
import com.isp392.dto.request.DishToppingUpdateRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.entity.Dish;
import com.isp392.entity.DishTopping;
import com.isp392.entity.Topping;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DishToppingMapper {

    DishTopping toEntity(DishToppingCreationRequest request, @Context Dish dish, @Context Topping topping);

    @Mapping(target = "dishName",
            expression = "java(entity.getDish() != null ? entity.getDish().getDishName() : null)")
    @Mapping(target = "toppingName",
            expression = "java(entity.getTopping() != null ? entity.getTopping().getName() : null)")
    DishToppingResponse toResponse(DishTopping entity);

    List<DishToppingResponse> toResponseList(List<DishTopping> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget DishTopping entity, DishToppingUpdateRequest request,
                      @Context Dish dish, @Context Topping topping);
}
