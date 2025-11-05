package com.isp392.dto.response;

import com.isp392.dto.request.ChatMessage;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIChatResponse {
    // Câu trả lời mới nhất của AI
    private String aiText;

    // Toàn bộ lịch sử hội thoại đã cập nhật
    // (Bao gồm tin nhắn cũ + tin nhắn mới của user + tin nhắn mới của AI)
    private List<ChatMessage> updatedHistory;
}