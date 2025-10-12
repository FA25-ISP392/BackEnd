package com.isp392.service;

import com.isp392.dto.request.ToppingCreationRequest;
import com.isp392.dto.request.ToppingUpdateRequest;
import com.isp392.dto.response.ToppingResponse;
import com.isp392.entity.Topping;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import com.isp392.mapper.ToppingMapper;
import com.isp392.repository.ToppingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ToppingService {
    ToppingMapper toppingMapper;
    ToppingRepository toppingRepository;

    public ToppingResponse getTopping(long toppingId) {
        Topping topping = toppingRepository.findById(toppingId)
                .orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
        return toppingMapper.toToppingResponse(topping);
    }

    public List<ToppingResponse> getAllToppings() {
        return toppingMapper.toToppingResponse(toppingRepository.findAll());
    }
    public ToppingResponse createTopping(ToppingCreationRequest request) {
        if(toppingRepository.existsByname(request.getName())) {
            throw new AppException(ErrorCode.INGREDIENT_ALREADY_EXISTS);
        }
        Topping topping = toppingMapper.toTopping(request);
        return toppingMapper.toToppingResponse(toppingRepository.save(topping));
    }

    public ToppingResponse updateTopping(long toppingId, ToppingUpdateRequest request) {
        Topping topping = toppingRepository.findById(toppingId).orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
        toppingMapper.updateTopping(topping, request);
        return toppingMapper.toToppingResponse(toppingRepository.save(topping));
    }

    public void deleteTopping(long toppingId) {
        toppingRepository.deleteById(toppingId);
    }
}
