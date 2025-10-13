package com.isp392.mapper;

import com.isp392.dto.request.DishCreationRequest;
import com.isp392.dto.request.DishUpdateRequest;
import com.isp392.dto.response.DishResponse;
import com.isp392.entity.Dish;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

// ✅ BƯỚC 1: Thêm 'uses = {DishToppingMapper.class}'
// Báo cho DishMapper biết rằng nó có thể sử dụng DishToppingMapper khi cần
@Mapper(componentModel = "spring", uses = {DishToppingMapper.class})
public interface DishMapper {

    Dish toDish(DishCreationRequest request);

    // ✅ BƯỚC 2: Thêm @Mapping tường minh cho dishToppings
    // MapStruct sẽ tự động dùng DishToppingMapper để chuyển đổi List<DishTopping> -> List<DishToppingResponse>
    @Mapping(source = "dishToppings", target = "dishToppings")
    DishResponse toDishResponse(Dish dish);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDish(@MappingTarget Dish dish, DishUpdateRequest request);

    // ✅ BƯỚC 3: Xóa hoàn toàn 2 phương thức default và @AfterMapping ở dưới.
    // Chúng không còn cần thiết nữa.
}