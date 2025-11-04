package com.isp392.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AIChatRequest {
    // Câu hỏi của người dùng, ví dụ: "tôi muốn ăn gì cay cay"
    private String query;
}
