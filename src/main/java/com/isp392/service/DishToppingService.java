package com.isp392.service;

import com.isp392.dto.request.DishToppingCreationRequest;
import com.isp392.dto.request.DishToppingUpdateRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.entity.Dish;
import com.isp392.entity.DishTopping;
import com.isp392.entity.DishToppingId;
import com.isp392.entity.Topping;
import com.isp392.mapper.DishToppingMapper;
import com.isp392.repository.DishRepository;
import com.isp392.repository.DishToppingRepository;
import com.isp392.repository.ToppingRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class DishToppingService {

    DishToppingRepository dishToppingRepository;
    DishRepository dishRepository;
    ToppingRepository toppingRepository;
    DishToppingMapper dishToppingMapper;

    // CREATE
    public DishToppingResponse create(DishToppingCreationRequest request) {
        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dish ID " + request.getDishId() + " does not exist."));
        Topping topping = toppingRepository.findById(request.getToppingId().longValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Topping ID " + request.getToppingId() + " does not exist."));

        DishTopping entity = dishToppingMapper.toEntity(request, dish, topping);

        // Với @IdClass, set trực tiếp dishId và toppingId
        entity.setDishId(dish.getDishId());
        entity.setToppingId(topping.getId());

        // set liên kết để MapStruct và JPA nhận biết
        entity.setDish(dish);
        entity.setTopping(topping);

        DishTopping saved = dishToppingRepository.save(entity);
        return dishToppingMapper.toResponse(saved);
    }

    // READ ALL
    public List<DishToppingResponse> getAll() {
        return dishToppingMapper.toResponseList(dishToppingRepository.findAll());
    }

    // READ ONE
    public DishToppingResponse getById(int dishId, int toppingId) {
        DishTopping entity = dishToppingRepository.findById(new DishToppingId(dishId, toppingId))
                .orElseThrow(() -> new EntityNotFoundException("DishTopping not found"));
        return dishToppingMapper.toResponse(entity);
    }

    // UPDATE
    public DishToppingResponse update(DishToppingUpdateRequest request) {
        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dish ID " + request.getDishId() + " does not exist."));
        Topping topping = toppingRepository.findById(request.getToppingId().longValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Topping ID " + request.getToppingId() + " does not exist."));

        DishToppingId id = new DishToppingId(request.getDishId(), request.getToppingId());
        DishTopping entity = dishToppingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("DishTopping not found"));

        dishToppingMapper.updateEntity(entity, request, dish, topping);

        // set lại khóa chính (với @IdClass)
        entity.setDishId(dish.getDishId());
        entity.setToppingId(topping.getId());

        entity.setDish(dish);
        entity.setTopping(topping);

        DishTopping updated = dishToppingRepository.save(entity);
        return dishToppingMapper.toResponse(updated);
    }

    // DELETE
    public void delete(int dishId, int toppingId) {
        DishToppingId id = new DishToppingId(dishId, toppingId);
        if (!dishToppingRepository.existsById(id)) {
            throw new EntityNotFoundException("DishTopping not found");
        }
        dishToppingRepository.deleteById(id);
    }
}
