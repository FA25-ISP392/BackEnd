package com.isp392.repository.projection;

import com.isp392.enums.PaymentMethod;

public interface RevenueByMethodProjection {
    PaymentMethod getMethod();
    Double getTotalRevenue();
    Long getTotalOrders();
}