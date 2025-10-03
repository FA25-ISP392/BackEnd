package com.isp392.mapper;

import com.isp392.dto.request.IngredentCreationRequest;
import com.isp392.dto.request.IngredentUpdateRequest;
import com.isp392.dto.response.IngredentResponse;
import com.isp392.entity.Ingredent;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IngredentMapper {
    Ingredent toIngredent(IngredentCreationRequest ingredent);
    IngredentResponse toIngredentResponse(Ingredent ingredent);
    List<IngredentResponse> toIngredentResponse(List<Ingredent> ingredents);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateIngredient(@MappingTarget Ingredent ingredent, IngredentUpdateRequest request);
}
