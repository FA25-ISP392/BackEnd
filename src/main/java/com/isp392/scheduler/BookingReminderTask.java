package com.isp392.scheduler;

import com.isp392.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingReminderTask {

    private final BookingService bookingService;

    /**
     * Chạy mỗi 15 phút (vào các phút 0, 15, 30, 45 của mỗi giờ).
     * (cron = "Giây Phút Giờ Ngày Tháng NgàyTrongTuần")
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void sendBookingReminders() {
        log.info("--- [START] Scheduled Task: Sending Booking Reminders ---");
        try {
            bookingService.processAndSendReminders();
        } catch (Exception e) {
            log.error("Error during scheduled booking reminder task: {}", e.getMessage(), e);
        }
        log.info("--- [END] Scheduled Task: Sending Booking Reminders ---");
    }
}