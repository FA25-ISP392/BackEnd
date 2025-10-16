package com.isp392.service;

import com.isp392.dto.request.DishToppingCreationRequest;
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
import org.springframework.transaction.annotation.Transactional; // Thêm import
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

    @Transactional
    public DishToppingResponse create(DishToppingCreationRequest request) {
        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Dish ID " + request.getDishId() + " does not exist."));
        Topping topping = toppingRepository.findById(request.getToppingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Topping ID " + request.getToppingId() + " does not exist."));

        // ✅ BƯỚC QUAN TRỌNG: TẠO VÀ GÁN EMBEDDED ID
        DishToppingId id = new DishToppingId(request.getDishId(), request.getToppingId());

        // Kiểm tra xem liên kết đã tồn tại chưa
        if(dishToppingRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This dish-topping association already exists.");
        }

        DishTopping entity = dishToppingMapper.toEntity(request, dish, topping);
        entity.setId(id); // Gán ID vào entity

        DishTopping saved = dishToppingRepository.save(entity);
        return dishToppingMapper.toResponse(saved);
    }

    // ✅ BỔ SUNG PHƯƠNG THỨC UPDATE
    @Transactional
    public DishToppingResponse update(int dishId, int toppingId, DishToppingCreationRequest request) {
        DishToppingId id = new DishToppingId(dishId, toppingId);
        DishTopping existingEntity = dishToppingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DishTopping not found"));

        // Lấy lại Dish và Topping để đảm bảo tính toàn vẹn
        Dish dish = dishRepository.findById(request.getDishId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dish ID does not exist."));
        Topping topping = toppingRepository.findById(request.getToppingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topping ID does not exist."));

        // Dùng mapper để cập nhật các trường
        dishToppingMapper.updateEntity(existingEntity, request, dish, topping);

        DishTopping updated = dishToppingRepository.save(existingEntity);
        return dishToppingMapper.toResponse(updated);
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