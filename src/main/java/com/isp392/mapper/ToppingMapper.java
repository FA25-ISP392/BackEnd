package com.isp392.mapper;

import com.isp392.dto.request.ToppingCreationRequest;
import com.isp392.dto.request.ToppingUpdateRequest;
import com.isp392.dto.response.ToppingResponse;
import com.isp392.entity.Topping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ToppingMapper {
    Topping toTopping(ToppingCreationRequest topping);
    ToppingResponse toToppingResponse(Topping topping);
    List<ToppingResponse> toToppingResponse(List<Topping> toppings);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTopping(@MappingTarget Topping topping, ToppingUpdateRequest request);
}
