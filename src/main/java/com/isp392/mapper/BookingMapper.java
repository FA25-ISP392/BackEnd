package com.isp392.mapper;

import com.isp392.dto.request.BookingCreationRequest;
import com.isp392.dto.request.BookingUpdateRequest;
import com.isp392.dto.response.BookingResponse;
import com.isp392.entity.Booking;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "bookingId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "table", ignore = true)
    @Mapping(target = "managerNote", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking toEntity(BookingCreationRequest request);

    @Mapping(source = "table.tableId", target = "tableId")
    @Mapping(source = "table.tableName", target = "tableName")
    @Mapping(source = "customer.account.fullName", target = "customerName")
    @Mapping(source = "customer.account.phone", target = "customerPhone")
    BookingResponse toResponse(Booking booking);
    // Map khi cập nhật
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "table.tableId", source = "tableId")
    void updateEntity(@MappingTarget Booking booking, BookingUpdateRequest request);

}
