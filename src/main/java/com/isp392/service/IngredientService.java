package com.isp392.service;

import com.isp392.dto.request.IngredientCreationRequest;
import com.isp392.dto.request.IngredientUpdateRequest;
import com.isp392.dto.response.IngredientResponse;
import com.isp392.entity.Ingredient;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.IngredentMapper;
import com.isp392.repository.IngredientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredientService {
    IngredentMapper ingredentMapper;
    IngredientRepository ingredientRepository;

    public IngredientResponse getIngredient(long ingredentId) {
        Ingredient ingredent = ingredientRepository.findById(ingredentId)
                .orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
        return ingredentMapper.toIngredentResponse(ingredent);
    }

    public List<IngredientResponse> getAllIngredients() {
        return ingredentMapper.toIngredentResponse(ingredientRepository.findAll());
    }
    public IngredientResponse createIngredient(IngredientCreationRequest request) {
        if(ingredientRepository.existsByname(request.getName())) {
            throw new AppException(ErrorCode.INGREDIENT_ALREADY_EXISTS);
        }
        Ingredient ingredient = ingredentMapper.toIngredent(request);
        return ingredentMapper.toIngredentResponse(ingredientRepository.save(ingredient));
    }

    public IngredientResponse updateIngredient(long ingredentId, IngredientUpdateRequest request) {
        Ingredient ingredient = ingredientRepository.findById(ingredentId).orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
        ingredentMapper.updateIngredient(ingredient, request);
        return ingredentMapper.toIngredentResponse(ingredientRepository.save(ingredient));
    }

    public void deleteIngredient(long ingredentId) {
        ingredientRepository.deleteById(ingredentId);
    }
}
