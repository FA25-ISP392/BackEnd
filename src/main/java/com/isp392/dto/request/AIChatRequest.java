package com.isp392.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class AIChatRequest {
    // Câu hỏi MỚI của người dùng
    private String query;

    // Lịch sử của các tin nhắn TRƯỚC ĐÓ
    private List<ChatMessage> history;
}