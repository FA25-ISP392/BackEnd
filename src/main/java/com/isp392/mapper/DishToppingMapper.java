package com.isp392.mapper;

import com.isp392.dto.request.DishToppingCreationRequest;
import com.isp392.dto.request.DishToppingUpdateRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.entity.DishTopping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DishToppingMapper {

    // Map từ request → entity
    DishTopping toEntity(DishToppingCreationRequest request);

    // Map từ entity → response (với tên món và tên topping)
    @Mapping(target = "dishName",
            expression = "java(entity.getDish() != null ? entity.getDish().getDishName() : null)")
    @Mapping(target = "toppingName",
            expression = "java(entity.getTopping() != null ? entity.getTopping().getName() : null)")
    DishToppingResponse toResponse(DishTopping entity);

    // Map list entity → list response
    List<DishToppingResponse> toResponseList(List<DishTopping> entities);

    // Update entity từ request (nếu sau này bạn muốn PATCH)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget DishTopping entity, DishToppingUpdateRequest request);
}
