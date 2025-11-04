package com.isp392.service;

import com.isp392.dto.response.DishResponse;
import com.isp392.entity.Customer;
import com.isp392.repository.CustomerRepository;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
// ***** SỬA LỖI IMPORT 1: Import 'Content' từ .api *****
import com.google.cloud.vertexai.api.Content;
// ***** SỬA LỖI IMPORT 2: Import 'Part' từ .api *****
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AISuggestionService {

    // 1. Tái sử dụng các service/repository bạn đã có
    private final DishService dishService;
    private final CustomerRepository customerRepository;

    // 2. Lấy thông tin cấu hình AI từ application.yaml
    @Value("${google.gemini.project-id}")
    private String geminiProjectId;

    @Value("${google.gemini.location}")
    private String geminiLocation;

    @Value("${google.gemini.model-name}")
    private String geminiModelName;

    /**
     * Hàm chính để xử lý chat
     */
    public String getChatSuggestion(String userQuery, String customerUsername) {

        // --- PHẦN 1: THU THẬP BỐI CẢNH (CONTEXT) ---
        log.info("AI Chat: Lấy dữ liệu cho người dùng {}", customerUsername);

        Customer customer = customerRepository.findByUsername(customerUsername)
                .orElse(null);

        List<DishResponse> allDishes = dishService.getAllDishes(null, null);

        String dishDataContext = convertDishesToText(allDishes);

        // --- PHẦN 2: TẠO PROMPT CHO AI ---
        String finalPrompt = buildPrompt(userQuery, customer, dishDataContext);
        log.debug("AI Chat: Final Prompt: {}", finalPrompt);

        // --- PHẦN 3: GỌI API CỦA GEMINI (tức là tôi) ---
        log.info("AI Chat: Đang gọi VertexAI Gemini...");
        try (VertexAI vertexAi = new VertexAI(geminiProjectId, geminiLocation)) {
            GenerativeModel model = new GenerativeModel(geminiModelName, vertexAi);

            // ***** SỬA LỖI LOGIC 3: Cách tạo 'Content' và 'Part' *****
            // Cách 1: Tạo Part
            Part textPart = Part.newBuilder()
                    .setText(finalPrompt)
                    .build();

            // Cách 2: Gói Part vào trong Content
            Content content = Content.newBuilder()
                    .addParts(textPart)
                    .build();
            // ******************************************************

            GenerateContentResponse response = model.generateContent(content);

            // Lấy text từ response (phần này vẫn đúng)
            String aiResponse = response.getCandidates(0).getContent().getParts(0).getText();
            log.info("AI Chat: Đã nhận phản hồi từ Gemini.");
            return aiResponse;

        } catch (Exception e) {
            log.error("Lỗi khi gọi VertexAI: {}", e.getMessage(), e);
            throw new RuntimeException("Xin lỗi, tôi đang gặp lỗi khi kết nối đến AI. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * (Helper) Xây dựng prompt hoàn chỉnh cho AI
     */
    private String buildPrompt(String userQuery, Customer customer, String dishDataContext) {
        StringBuilder prompt = new StringBuilder();

        // 1. Thiết lập vai trò (System Prompt)
        prompt.append("Bạn là một chuyên gia tư vấn dinh dưỡng của nhà hàng. ");
        prompt.append("Nhiệm vụ của bạn CHỈ LÀ gợi ý món ăn từ danh sách được cung cấp. ");
        prompt.append("KHÔNG trả lời bất kỳ câu hỏi nào không liên quan đến việc chọn món ăn (ví dụ: không trả lời câu hỏi về thời tiết, lịch sử, toán học...). ");
        prompt.append("Nếu người dùng hỏi ngoài chủ đề, hãy từ chối một cách lịch sự. ");
        prompt.append("Phân tích sở thích của người dùng (ví dụ: 'cay', 'chua', 'ít béo') ");
        prompt.append("dựa trên 'dishName' và 'description' của món ăn. ");
        prompt.append("Phân tích mục tiêu ('tăng cân', 'giảm cân') dựa trên 'calo' và 'type' (ví dụ: BUILD_MUSCLE, FAT_LOSS). ");
        prompt.append("Luôn trả lời bằng tiếng Việt, một cách thân thiện và chuyên nghiệp.\n\n");

        // 2. Cung cấp thông tin khách hàng (nếu có)
        if (customer != null) {
            prompt.append("--- Thông tin khách hàng (dùng để tham khảo) ---\n");
            if(customer.getHeight() != null) prompt.append("Chiều cao: ").append(customer.getHeight()).append(" cm\n");
            if(customer.getWeight() != null) prompt.append("Cân nặng: ").append(customer.getWeight()).append(" kg\n");
            if(customer.getSex() != null) prompt.append("Giới tính: ").append(customer.getSex() ? "Nam" : "Nữ").append("\n\n");
        }

        // 3. Cung cấp dữ liệu món ăn (Lấy từ DB của bạn)
        prompt.append("--- Danh sách món ăn HÔM NAY (chỉ được chọn từ đây) ---\n");
        prompt.append(dishDataContext);
        prompt.append("\n\n");

        // 4. Câu hỏi của người dùng
        prompt.append("--- Câu hỏi của khách hàng ---\n");
        prompt.append(userQuery).append("\n");

        prompt.append("\n--- Câu trả lời tư vấn của bạn (Hãy gợi ý 1-2 món phù hợp nhất) ---\n");

        return prompt.toString();
    }

    /**
     * (Helper) Chuyển List<DishResponse> thành text cho AI đọc
     */
    private String convertDishesToText(List<DishResponse> dishes) {
        // Chỉ gợi ý các món còn hàng (remainingQuantity > 0)
        return dishes.stream()
                .filter(dish -> dish.getRemainingQuantity() > 0)
                .map(dish -> String.format(
                        "- Tên: %s (ID: %d), Mô tả: %s, Calo: %.0f, Giá: %.0f, Mục tiêu: %s, Loại: %s, Số lượng còn lại: %d",
                        dish.getDishName(),
                        dish.getDishId(),
                        dish.getDescription(),
                        dish.getCalo().doubleValue(),
                        dish.getPrice().doubleValue(),
                        dish.getType(),
                        dish.getCategory(),
                        dish.getRemainingQuantity()
                ))
                .collect(Collectors.joining("\n"));
    }
}

