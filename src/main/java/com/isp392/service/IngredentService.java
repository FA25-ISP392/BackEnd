package com.isp392.service;

import com.isp392.dto.request.IngredentCreationRequest;
import com.isp392.dto.request.IngredentUpdateRequest;
import com.isp392.dto.response.IngredentResponse;
import com.isp392.entity.Ingredent;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.IngredentMapper;
import com.isp392.repository.IngredentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredentService {
    IngredentMapper ingredentMapper;
    IngredentRepository ingredentRepository;

    public IngredentResponse getIngredent(long ingredentId) {
        Ingredent ingredent = ingredentRepository.findById(ingredentId)
                .orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
        return ingredentMapper.toIngredentResponse(ingredent);
    }

    public List<IngredentResponse> getAllIngredents() {
        return ingredentMapper.toIngredentResponse(ingredentRepository.findAll());
    }
    public IngredentResponse createIngredent(IngredentCreationRequest request) {
        if(ingredentRepository.existsByname(request.getName())) {
            throw new AppException(ErrorCode.INGREDIENT_ALREADY_EXISTS);
        }
        Ingredent ingredent = ingredentMapper.toIngredent(request);
        return ingredentMapper.toIngredentResponse(ingredentRepository.save(ingredent));
    }

    public IngredentResponse updateIngredent(long ingredentId, IngredentUpdateRequest request) {
        Ingredent ingredent = ingredentRepository.findById(ingredentId).orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
        ingredentMapper.updateIngredient(ingredent, request);
        return ingredentMapper.toIngredentResponse(ingredentRepository.save(ingredent));
    }

    public void deleteIngredent(long ingredentId) {
        ingredentRepository.deleteById(ingredentId);
    }
}
