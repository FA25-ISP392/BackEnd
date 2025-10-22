package com.isp392.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PayOSWebhookData {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("code")
    private String code; // Mã "00" (thành công) của tổng thể webhook

    @JsonProperty("data")
    private PayOSWebhookBody data; // Đây là đối tượng lồng

    @JsonProperty("signature")
    private String signature;

    // Thêm Getters và Setters cho các trường trên
    // (Bấm Alt+Insert -> Getters and Setters)

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public PayOSWebhookBody getData() {
        return data;
    }

    public void setData(PayOSWebhookBody data) {
        this.data = data;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}