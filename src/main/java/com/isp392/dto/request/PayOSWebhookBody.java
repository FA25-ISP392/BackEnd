package com.isp392.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayOSWebhookBody {
    @JsonProperty("orderCode")
    private long orderCode; // ID đơn hàng của bạn

    @JsonProperty("amount")
    private long amount;

    @JsonProperty("code")
    private String code; // Mã "00" (thành công) của giao dịch

    @JsonProperty("status")
    private String status; // "PAID", "CANCELLED", "FAILED", "EXPIRED"

    @JsonProperty("desc")
    private String description; // Mô tả giao dịch (có thể khác description bạn gửi đi)

    // Thêm các trường khác nếu cần (vd: transactionDateTime, reference,...)

    // Getters and Setters
    public long getOrderCode() { return orderCode; }
    public void setOrderCode(long orderCode) { this.orderCode = orderCode; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
