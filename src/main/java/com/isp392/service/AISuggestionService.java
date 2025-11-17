package com.isp392.service;

import com.isp392.dto.request.AIChatRequest;
import com.isp392.dto.request.ChatMessage;
import com.isp392.dto.response.AIChatResponse;
import com.isp392.dto.response.DishResponse;
import com.isp392.entity.Customer;
import com.isp392.repository.CustomerRepository;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AISuggestionService {

    private final DishService dishService;
    private final CustomerRepository customerRepository;

    @Value("${google.gemini.project-id}")
    private String geminiProjectId;

    @Value("${google.gemini.location}")
    private String geminiLocation;

    @Value("${google.gemini.model-name}")
    private String geminiModelName;

    /**
     * Hàm chính đã được sửa đổi để xử lý hội thoại (chat)
     */
    public AIChatResponse getChatSuggestion(AIChatRequest request, String customerUsername) {

        // --- PHẦN 1: TẠO DANH SÁCH HỘI THOẠI (HISTORY LIST) ---
        List<Content> historyContents = new ArrayList<>();

        // 1a. (QUAN TRỌNG) Luôn bắt đầu bằng RÀNG BUỘC HỆ THỐNG (System Prompt)
        // Ràng buộc này PHẢI ĐƯỢC GỬI MỖI LẦN GỌI, không chỉ lần đầu tiên.
        log.info("AI Chat: Đang xây dựng bối cảnh cho {}", customerUsername);
        String systemPrompt = buildSystemPrompt(customerUsername);

        // "Priming" - Gửi ràng buộc hệ thống cho AI (giả lập vai trò user)
        historyContents.add(createVertexContent("user", systemPrompt));
        // "Priming" - Gửi một câu trả lời mẫu của AI để nó nhận vai
        historyContents.add(createVertexContent("model", "Đã hiểu. Tôi là trợ lý tư vấn món ăn. Tôi đã sẵn sàng, mời bạn đặt câu hỏi."));

        // 1b. Tải lại lịch sử chat CŨ (nếu có)
        // Phần này sẽ tải các câu hỏi/trả lời thực tế của người dùng
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            log.info("AI Chat: Đang tải {} tin nhắn lịch sử.", request.getHistory().size());
            // Bỏ qua tin nhắn mồi "Đã hiểu..." nếu nó vô tình bị gửi lên từ frontend
            for (ChatMessage msg : request.getHistory()) {
                historyContents.add(createVertexContent(msg.getRole(), msg.getText()));
            }
        } else {
            log.info("AI Chat: Đây là tin nhắn đầu tiên.");
        }

        // 1c. Thêm câu hỏi MỚI của người dùng vào cuối danh sách
        historyContents.add(createVertexContent("user", request.getQuery()));

        // --- PHẦN 2: GỌI API CỦA GEMINI ---
        log.info("AI Chat: Đang gọi VertexAI Gemini...");
        try (VertexAI vertexAi = new VertexAI(geminiProjectId, geminiLocation)) {
            GenerativeModel model = new GenerativeModel(geminiModelName, vertexAi);

            // Gửi TOÀN BỘ danh sách hội thoại
            GenerateContentResponse response = model.generateContent(historyContents);

            String aiResponseText = response.getCandidates(0).getContent().getParts(0).getText();
            log.info("AI Chat: Đã nhận phản hồi từ Gemini.");

            // --- PHẦN 3: ĐÓNG GÓI RESPONSE ---

            // 3a. Tạo danh sách lịch sử mới để trả về cho frontend
            List<ChatMessage> updatedHistory = new ArrayList<>();
            if (request.getHistory() != null) {
                updatedHistory.addAll(request.getHistory()); // Thêm lịch sử cũ
            }
            updatedHistory.add(new ChatMessage("user", request.getQuery())); // Thêm câu hỏi mới
            updatedHistory.add(new ChatMessage("model", aiResponseText)); // Thêm câu trả lời mới

            // 3b. Xây dựng đối tượng AIChatResponse
            return AIChatResponse.builder()
                    .aiText(aiResponseText)
                    .updatedHistory(updatedHistory)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi khi gọi VertexAI: {}", e.getMessage(), e);
            throw new RuntimeException("Xin lỗi, tôi đang gặp lỗi khi kết nối đến AI. Vui lòng thử lại sau.", e);
        }
    }

    /**
     * (Helper) Tạo một đối tượng Content của VertexAI từ role và text
     */
    private Content createVertexContent(String role, String text) {
        Part textPart = Part.newBuilder()
                .setText(text)
                .build();
        return Content.newBuilder()
                .setRole(role) // "user" hoặc "model"
                .addParts(textPart)
                .build();
    }

    /**
     * (Helper) Xây dựng RÀNG BUỘC HỆ THỐNG (System Prompt)
     * (Đã loại bỏ userQuery)
     */
    private String buildSystemPrompt(String customerUsername) {
        // --- THU THẬP BỐI CẢNH (CONTEXT) ---
        Customer customer = customerRepository.findByUsername(customerUsername).orElse(null);
        List<DishResponse> allDishes = dishService.getAllDishes(null, null);
        String dishDataContext = convertDishesToText(allDishes);

        // --- XÂY DỰNG PROMPT ---
        StringBuilder prompt = new StringBuilder();

        // 1. Thiết lập vai trò (Giữ nguyên các ràng buộc của bạn)
        prompt.append("Bạn là một chuyên gia tư vấn dinh dưỡng của nhà hàng. ");
        prompt.append("Nhiệm vụ của bạn TUYỆT ĐỐI CHỈ LÀ gợi ý món ăn từ danh sách được cung cấp.");
        prompt.append("KHÔNG trả lời bất kỳ câu hỏi nào không liên quan đến việc chọn món ăn (ví dụ: không trả lời câu hỏi về thời tiết, lịch sử, toán học...). Những câu hỏi không liên quan đến món ăn hoặc topping, bạn sẽ trả lời chúng tôi chỉ phục vụ trong phạm vi dữ liệu của nhà hàng ");
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

        // 3. Cung cấp dữ liệu món ăn
        prompt.append("--- Danh sách món ăn HÔM NAY (chỉ được chọn từ đây) ---\n");
        prompt.append(dishDataContext);
        prompt.append("\n\n");

        // 4. KHÔNG CÒN câu hỏi của user ở đây
        // prompt.append("--- Câu hỏi của khách hàng ---\n");
        // prompt.append(userQuery).append("\n");

        prompt.append("\n--- BẮT ĐẦU HỘI THOẠI ---\n");

        return prompt.toString();
    }

    /**
     * (Helper) Chuyển List<DishResponse> thành text cho AI đọc (Giữ nguyên)
     */
    private String convertDishesToText(List<DishResponse> dishes) {
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
