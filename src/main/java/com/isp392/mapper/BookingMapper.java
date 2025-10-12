package com.isp392.mapper;

import com.isp392.dto.request.BookingCreationRequest;
import com.isp392.dto.request.BookingUpdateRequest;
import com.isp392.dto.response.BookingResponse;
import com.isp392.entity.Booking;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "table.tableId", source = "tableId")
    Booking toEntity(BookingCreationRequest request);

    // Map khi cập nhật
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "table.tableId", source = "tableId")
    void updateEntity(@MappingTarget Booking booking, BookingUpdateRequest request);

    // Map sang response
    @Mapping(target = "customerName", source = "customer.account.username")
    @Mapping(target = "tableId", source = "table.tableId")
    BookingResponse toResponse(Booking booking);

}
