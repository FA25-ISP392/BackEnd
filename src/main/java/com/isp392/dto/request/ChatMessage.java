package com.isp392.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    // Vai trò: "user" (người dùng) hoặc "model" (AI)
    private String role;

    // Nội dung tin nhắn
    private String text;
}