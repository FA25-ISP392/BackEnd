package com.isp392.mapper;

import com.isp392.dto.request.PaymentCreationRequest;
import com.isp392.dto.response.PaymentResponse;
import com.isp392.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toPaymentResponse(PaymentCreationRequest payment);
    Payment toPayment(PaymentCreationRequest payment);
}
