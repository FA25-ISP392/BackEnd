package com.isp392.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonProperty("cancel")
    private Boolean cancel;

    @JsonProperty("transactionDateTime")
    private String transactionDateTime;



}
