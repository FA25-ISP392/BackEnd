package com.isp392.service;

import com.isp392.dto.request.DishToppingBatchCreationRequest;
import com.isp392.dto.response.DishToppingResponse;
import com.isp392.entity.Dish;
import com.isp392.entity.DishTopping;
import com.isp392.entity.DishToppingId;
import com.isp392.entity.Topping;
import com.isp392.mapper.DishToppingMapper;
import com.isp392.repository.DishRepository;
import com.isp392.repository.DishToppingRepository;
import com.isp392.repository.ToppingRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class DishToppingService {

    DishToppingRepository dishToppingRepository;
    DishRepository dishRepository;
    ToppingRepository toppingRepository;
    DishToppingMapper dishToppingMapper;

    @Transactional
    public List<DishToppingResponse> createDishToppings(DishToppingBatchCreationRequest request) {
        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dish ID " + request.getDishId() + " does not exist."));

        List<Topping> toppings = toppingRepository.findAllById(request.getToppingIds());
        if (toppings.size() != request.getToppingIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more Topping IDs are invalid.");
        }

        List<DishTopping> newAssociations = new ArrayList<>();

        for (Topping topping : toppings) {
            DishToppingId id = new DishToppingId(dish.getDishId(), topping.getToppingId());

            if (!dishToppingRepository.existsById(id)) {
                DishTopping newAssociation = new DishTopping();
                newAssociation.setId(id);
                newAssociation.setDish(dish);
                newAssociation.setTopping(topping);
                newAssociations.add(newAssociation);
            }
        }

        List<DishTopping> savedAssociations = dishToppingRepository.saveAll(newAssociations);
        return dishToppingMapper.toResponseList(savedAssociations);
    }

    public List<DishToppingResponse> getAll() {
        return dishToppingMapper.toResponseList(dishToppingRepository.findAll());
    }

    public DishToppingResponse getById(int dishId, int toppingId) {
        DishTopping entity = dishToppingRepository.findById(new DishToppingId(dishId, toppingId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DishTopping not found"));
        return dishToppingMapper.toResponse(entity);
    }

    public void delete(int dishId, int toppingId) {
        DishToppingId id = new DishToppingId(dishId, toppingId);
        if (!dishToppingRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DishTopping not found");
        }
        dishToppingRepository.deleteById(id);
    }
}