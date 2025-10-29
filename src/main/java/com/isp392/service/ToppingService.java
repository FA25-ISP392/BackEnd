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

import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ToppingService {
    ToppingMapper toppingMapper;
    ToppingRepository toppingRepository;

    public ToppingResponse getTopping(int toppingId) {
        Topping topping = toppingRepository.findById(toppingId)
                .orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));
        return toppingMapper.toToppingResponse(topping);
    }

    public List<ToppingResponse> getAllToppings() {
        return toppingMapper.toToppingResponse(toppingRepository.findAll());
    }

    // ⭐ MỚI: Lấy tất cả topping có phân trang
    public Page<ToppingResponse> getAllToppingsPaginated(Pageable pageable) {
        Page<Topping> toppingPage = toppingRepository.findAll(pageable);
        // Sử dụng map của đối tượng Page để chuyển đổi các Topping thành ToppingResponse
        return toppingPage.map(toppingMapper::toToppingResponse);
    }

    public List<ToppingResponse> getToppingsByNameContaining(String name) { // Đổi tên và kiểu trả về
        List<Topping> toppings = toppingRepository.findByNameContaining(name); // Gọi repository method mới
        // Sử dụng mapper để chuyển đổi List<Topping> sang List<ToppingResponse>
        // (Giả sử ToppingMapper có phương thức toToppingResponse(List<Topping>))
        // Nếu không có, bạn cần thêm phương thức đó vào ToppingMapper hoặc map thủ công ở đây
        if (toppings.isEmpty()) {
            return Collections.emptyList(); // Trả về list rỗng nếu không tìm thấy
        }
        return toppingMapper.toToppingResponse(toppings); // Map list entity sang list DTO
    }

    public ToppingResponse createTopping(ToppingCreationRequest request) {
        if(toppingRepository.existsByname(request.getName())) {
            throw new AppException(ErrorCode.INGREDIENT_ALREADY_EXISTS);
        }
        Topping topping = toppingMapper.toTopping(request);
        return toppingMapper.toToppingResponse(toppingRepository.save(topping));
    }

    public ToppingResponse updateTopping(int toppingId, ToppingUpdateRequest request) {
        Topping topping = toppingRepository.findById(toppingId).orElseThrow(() -> new AppException(ErrorCode.TOPPING_NOT_FOUND));
        toppingMapper.updateTopping(topping, request);
        return toppingMapper.toToppingResponse(toppingRepository.save(topping));
    }

    public void deleteTopping(int toppingId) {
        toppingRepository.deleteById(toppingId);
    }
}
