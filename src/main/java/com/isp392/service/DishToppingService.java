package com.isp392.service;

import com.isp392.dto.request.DishToppingCreationRequest;
import com.isp392.dto.request.DishToppingUpdateRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.entity.DishTopping;
import com.isp392.entity.DishToppingId;
import com.isp392.mapper.DishToppingMapper;
import com.isp392.repository.DishRepository;
import com.isp392.repository.DishToppingRepository;
import com.isp392.repository.ToppingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishToppingService {

    private final DishToppingRepository dishToppingRepository;
    private final DishRepository dishRepository;          // ✅ thêm dòng này
    private final ToppingRepository toppingRepository;    // ✅ thêm dòng này
    private final DishToppingMapper dishToppingMapper;

    // 🟢 CREATE
    public DishToppingResponse create(DishToppingCreationRequest request) {
        // 🔍 Kiểm tra Dish có tồn tại không
        if (!toppingRepository.existsById((long)request.getDishId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dish ID " + request.getDishId() + " does not exist.");
        }

        // 🔍 Kiểm tra Topping có tồn tại không
        if (!toppingRepository.existsById((long)request.getToppingId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Topping ID " + request.getToppingId() + " does not exist.");
        }

        // ✅ Map & save
        DishTopping entity = dishToppingMapper.toEntity(request);
        DishTopping saved = dishToppingRepository.save(entity);
        return dishToppingMapper.toResponse(saved);
    }

    // 🟢 READ ALL
    public List<DishToppingResponse> getAll() {
        return dishToppingMapper.toResponseList(dishToppingRepository.findAll());
    }

    // 🟢 READ ONE
    public DishToppingResponse getById(int dishId, int toppingId) {
        DishTopping entity = dishToppingRepository.findById(new DishToppingId(dishId, toppingId))
                .orElseThrow(() -> new EntityNotFoundException("DishTopping not found"));
        return dishToppingMapper.toResponse(entity);
    }

    // 🟡 UPDATE
    public DishToppingResponse update(DishToppingUpdateRequest request) {
        // 🔍 Kiểm tra Dish có tồn tại không
        if (!dishRepository.existsById(request.getDishId())) {
            throw new RuntimeException("Dish ID " + request.getDishId() + " does not exist.");
        }

        // 🔍 Kiểm tra Topping có tồn tại không
        if (!toppingRepository.existsById((long)request.getToppingId())) {
            throw new RuntimeException("Topping ID " + request.getToppingId() + " does not exist.");
        }

        // 🔑 Tạo composite key
        DishToppingId id = new DishToppingId(request.getDishId(), request.getToppingId());
        DishTopping entity = dishToppingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DishTopping not found"));

        // Cập nhật entity và save
        dishToppingMapper.updateEntity(entity, request);
        DishTopping updated = dishToppingRepository.save(entity);

        return dishToppingMapper.toResponse(updated);
    }

    // 🔴 DELETE
    public void delete(int dishId, int toppingId) {
        DishToppingId id = new DishToppingId(dishId, toppingId);
        if (!dishToppingRepository.existsById(id)) {
            throw new EntityNotFoundException("DishTopping not found");
        }
        dishToppingRepository.deleteById(id);
    }
}
