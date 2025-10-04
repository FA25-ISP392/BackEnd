package com.isp392.mapper;

import com.isp392.dto.request.IngredientCreationRequest;
import com.isp392.dto.request.IngredientUpdateRequest;
import com.isp392.dto.response.IngredientResponse;
import com.isp392.entity.Ingredient;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredentMapper {
    Ingredient toIngredent(IngredientCreationRequest ingredent);
    IngredientResponse toIngredentResponse(Ingredient ingredient);
    List<IngredientResponse> toIngredentResponse(List<Ingredient> ingredients);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateIngredient(@MappingTarget Ingredient ingredient, IngredientUpdateRequest request);
}
