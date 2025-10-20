package com.isp392.mapper;

import com.isp392.dto.request.PaymentCreationRequest;
import com.isp392.dto.response.PaymentResponse;
import com.isp392.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(target = "orderId", source = "order.orderId")
    PaymentResponse toPaymentResponse(Payment payment);

    @Mapping(target = "order", ignore = true)
    Payment toPayment(PaymentCreationRequest request);
}
