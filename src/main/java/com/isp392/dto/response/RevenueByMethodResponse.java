package com.isp392.dto.response;

import com.isp392.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueByMethodResponse {
    private PaymentMethod method;
    private double totalRevenue;
    private long totalOrders;
}