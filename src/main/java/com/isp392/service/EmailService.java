package com.isp392.service;

import com.isp392.enums.PaymentMethod;
import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import java.text.NumberFormat;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender mailSender;

    @Async("taskExecutor")
    public void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new AppException(ErrorCode.SEND_EMAIL_FAILED);
        }
    }

    @Async("taskExecutor")
    public void sendResetPasswordEmail(String email, String resetLink) {
        String subject = "Yêu cầu đặt lại mật khẩu của bạn";

        String body = """
                <div style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 40px;">
                    <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 10px; 
                                box-shadow: 0 4px 10px rgba(0,0,0,0.1); padding: 30px; text-align: center;">
                
                        <img src="https://cdn-icons-png.flaticon.com/512/2910/2910768.png" alt="Logo" 
                             style="width: 80px; margin-bottom: 20px;">
                
                        <h2 style="color: #333;">Đặt lại mật khẩu của bạn</h2>
                
                        <p style="color: #555; font-size: 15px; line-height: 1.6;">
                            Xin chào,<br><br>
                            Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.<br>
                            Vui lòng nhấn vào nút bên dưới để tiến hành đặt lại mật khẩu.<br>
                            (Liên kết có hiệu lực trong <b>15 phút</b>).
                        </p>
                
                        <a href="%s" 
                           style="display: inline-block; background-color: #007bff; color: #fff; 
                                  padding: 12px 25px; border-radius: 5px; text-decoration: none; 
                                  font-weight: bold; margin: 20px 0;">
                            🔐 Đặt lại mật khẩu
                        </a>
                
                        <p style="color: #777; font-size: 13px;">
                            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.<br>
                            Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!
                        </p>
                
                        <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
                
                        <p style="color: #999; font-size: 12px;">
                            © 2025 Hệ thống Quản lý Nhà hàng | Mọi quyền được bảo lưu.
                        </p>
                    </div>
                </div>
                """.formatted(resetLink);

        send(email, subject, body);
        log.info("Reset password email sent to {}", email);
    }

    @Async("taskExecutor")
    public void sendVerificationEmail(String email, String name, String verifyLink) {
        String subject = "Xác thực tài khoản của bạn";
        String body = String.format("""
                <div style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 40px;">
                    <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 10px; 
                                box-shadow: 0 4px 10px rgba(0,0,0,0.1); padding: 30px; text-align: center;">
                
                        <img src="https://cdn-icons-png.flaticon.com/512/992/992648.png" alt="Logo" 
                             style="width: 80px; margin-bottom: 20px;">
                
                        <h2 style="color: #333;">Chào mừng bạn, %s!</h2>
                
                        <p style="color: #555; font-size: 15px; line-height: 1.6;">
                            Cảm ơn bạn đã đăng ký với chúng tôi.<br>
                            Vui lòng nhấn vào nút bên dưới để hoàn tất việc xác thực tài khoản.<br>
                            (Liên kết có hiệu lực trong <b>24 giờ</b>).
                        </p>
                
                        <a href="%s" 
                           style="display: inline-block; background-color: #007bff; color: #fff; 
                                  padding: 12px 25px; border-radius: 5px; text-decoration: none; 
                                  font-weight: bold; margin: 20px 0;">
                            ✅ Xác thực tài khoản
                        </a>
                
                        <p style="color: #777; font-size: 13px;">
                            Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email.<br>
                            Cảm ơn bạn!
                        </p>
                
                        <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
                
                        <p style="color: #999; font-size: 12px;">
                            © 2025 Hệ thống Quản lý Nhà hàng | Mọi quyền được bảo lưu.
                        </p>
                    </div>
                </div>
                """, name, verifyLink);

        send(email, subject, body);
        log.info("Verification email sent to {}", email);
    }


    @Async("taskExecutor")
    public void sendBookingConfirmationEmail(String toEmail, String customerName, LocalDateTime bookingDateTime, int seatCount, String tableInfo, String status) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
        String formattedDateTime = bookingDateTime.format(formatter);

        String statusMessage;
        String tableDetail;
        String vietnameseStatus; // Biến mới để giữ trạng thái tiếng Việt

        if ("APPROVED".equalsIgnoreCase(status)) {
            vietnameseStatus = "ĐÃ XÁC NHẬN";
            statusMessage = "Chúng tôi vui mừng thông báo lượt đặt bàn của bạn đã được <b>XÁC NHẬN</b>.";
            tableDetail = "<strong>Bàn của bạn:</strong> " + tableInfo;
        } else { // Giả sử các trạng thái khác (ví dụ: PENDING)
            vietnameseStatus = "CHỜ XỬ LÝ";
            statusMessage = "Chúng tôi đã nhận được yêu cầu đặt bàn của bạn và đang <b>CHỜ XỬ LÝ</b>.";
            tableDetail = "<strong>Khu vực mong muốn:</strong> " + (tableInfo != null ? tableInfo : "Không có yêu cầu đặc biệt");
        }

        // Sử dụng trạng thái tiếng Việt cho chủ đề email
        String subject = "Xác nhận đặt bàn của bạn - Trạng thái: " + vietnameseStatus;

        String body = String.format("""
            <div style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 40px;">
                <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 10px; 
                            box-shadow: 0 4px 10px rgba(0,0,0,0.1); padding: 30px; text-align: left;">
            
                    <img src="https://cdn-icons-png.flaticon.com/512/2910/2910768.png" alt="Logo" 
                         style="width: 80px; margin-bottom: 20px; display: block; margin-left: auto; margin-right: auto;">
            
                    <h2 style="color: #333; text-align: center;">Cảm ơn bạn, %s!</h2>
            
                    <p style="color: #555; font-size: 15px; line-height: 1.6;">
                        %s
                    </p>
                    
                    <div style="background-color: #f9f9f9; border-left: 5px solid #007bff; padding: 15px; margin: 20px 0;">
                        <h3 style="color: #333; margin-top: 0;">Chi tiết đặt bàn:</h3>
                        <p style="color: #555; margin: 5px 0;"><strong>Trạng thái:</strong> %s</p>
                        <p style="color: #555; margin: 5px 0;"><strong>Thời gian:</strong> %s</p>
                        <p style="color: #555; margin: 5px 0;"><strong>Số lượng khách:</strong> %d</p>
                        <p style="color: #555; margin: 5px 0;">%s</p>
                    </div>
            
                    <p style="color: #777; font-size: 13px; text-align: center;">
                        Nếu bạn có bất kỳ thay đổi nào, vui lòng liên hệ với chúng tôi qua:<br>
                        Email: <strong>moncuaban@gmail.com</strong> | SĐT: <strong>0123456789</strong><br><br>
                        Cảm ơn bạn đã chọn nhà hàng của chúng tôi!
                    </p>
            
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
            
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2025 Hệ thống Quản lý Nhà hàng | Mọi quyền được bảo lưu.
                    </p>
                </div>
            </div>
            """, customerName, statusMessage, vietnameseStatus, formattedDateTime, seatCount, tableDetail);
        // ^-- Đã cập nhật tham số thứ 3 thành `vietnameseStatus`

        // --- KẾT THÚC THAY ĐỔI ---

        send(toEmail, subject, body);
        log.info("Booking confirmation email sent to {} with status {}", toEmail, status); // Giữ log gốc
    }

    @Async("taskExecutor")
    public void sendBookingReminderEmail(String toEmail, String customerName, LocalDateTime bookingTime, int seatCount, String tableName) {
        String subject = "Nhắc nhở: Lịch đặt bàn của bạn sắp diễn ra";

        // Định dạng lại ngày giờ cho dễ đọc
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
        String formattedDateTime = bookingTime.format(formatter);

        String body = String.format("""
            <div style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 40px;">
                <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 10px; 
                            box-shadow: 0 4px 10px rgba(0,0,0,0.1); padding: 30px; text-align: left;">
            
                    <img src="https://cdn-icons-png.flaticon.com/512/2910/2910768.png" alt="Logo" 
                         style="width: 80px; margin-bottom: 20px; display: block; margin-left: auto; margin-right: auto;">
            
                    <h2 style="color: #333; text-align: center;">Xin chào, %s!</h2>
            
                    <p style="color: #555; font-size: 15px; line-height: 1.6;">
                        Đây là email nhắc nhở về lịch đặt bàn của bạn tại nhà hàng chúng tôi.
                    </p>
                    
                    <div style="background-color: #f9f9f9; border-left: 5px solid #007bff; padding: 15px; margin: 20px 0;">
                        <h3 style="color: #333; margin-top: 0;">Chi tiết đặt bàn (Đã xác nhận):</h3>
                        <p style="color: #555; margin: 5px 0;"><strong>Thời gian:</strong> %s (Còn khoảng 1 tiếng nữa)</p>
                        <p style="color: #555; margin: 5px 0;"><strong>Số lượng khách:</strong> %d</p>
                        <p style="color: #555; margin: 5px 0;"><strong>Bàn của bạn:</strong> %s</p>
                    </div>
            
                    <p style="color: #777; font-size: 13px; text-align: center;">
                        Nếu bạn có bất kỳ thay đổi nào, vui lòng liên hệ với chúng tôi qua:<br>
                        Email: <strong>[Địa chỉ email nhà hàng]</strong> | SĐT: <strong>[Số điện thoại nhà hàng]</strong><br><br>
                        Chúng tôi rất mong được phục vụ bạn!
                    </p>
            
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
            
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2025 Hệ thống Quản lý Nhà hàng | Mọi quyền được bảo lưu.
                    </p>
                </div>
            </div>
            """, customerName, formattedDateTime, seatCount, tableName);

        send(toEmail, subject, body);
        log.info("Booking REMINDER email sent to {}", toEmail);
    }
    @Async("taskExecutor")
    public void sendPaymentSuccessEmail(String toEmail, String customerName, int orderId, double totalAmount, PaymentMethod method, LocalDateTime paidAt) {
        String subject = "Thanh toán thành công cho đơn hàng #" + orderId;

        // Định dạng tiền tệ
        Locale vietnameseLocale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);
        String formattedTotal = currencyFormatter.format(totalAmount);

        // Định dạng ngày giờ
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
        String formattedPaidAt = paidAt.format(formatter);

        // Định dạng phương thức thanh toán
        String paymentMethodString = (method == PaymentMethod.CASH) ? "Tiền mặt" : "Chuyển khoản ngân hàng";

        String body = String.format("""
            <div style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 40px;">
                <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 10px;
                            box-shadow: 0 4px 10px rgba(0,0,0,0.1); padding: 30px; text-align: left;">
            
                    <img src="https://cdn-icons-png.flaticon.com/512/2910/2910768.png" alt="Logo"
                         style="width: 80px; margin-bottom: 20px; display: block; margin-left: auto; margin-right: auto;">
            
                    <h2 style="color: #333; text-align: center;">Thanh toán thành công!</h2>
            
                    <p style="color: #555; font-size: 15px; line-height: 1.6;">
                        Xin chào %s,<br><br>
                        Chúng tôi xác nhận đã nhận thanh toán thành công cho đơn hàng của bạn.
                    </p>
                    
                    <div style="background-color: #f9f9f9; border-left: 5px solid #28a745; padding: 15px; margin: 20px 0;">
                        <h3 style="color: #333; margin-top: 0;">Chi tiết thanh toán:</h3>
                        <p style="color: #555; margin: 5px 0;"><strong>Mã đơn hàng:</strong> #%d</p>
                        <p style="color: #555; margin: 5px 0;"><strong>Tổng tiền:</strong> <span style="font-weight: bold; color: #28a745;">%s</span></p>
                        <p style="color: #555; margin: 5px 0;"><strong>Phương thức:</strong> %s</p>
                        <p style="color: #555; margin: 5px 0;"><strong>Thời gian:</strong> %s</p>
                    </div>
            
                    <p style="color: #777; font-size: 13px; text-align: center;">
                        Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi!<br>
                        Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ bộ phận hỗ trợ.
                    </p>
            
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
            
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2025 Hệ thống Quản lý Nhà hàng | Mọi quyền được bảo lưu.
                    </p>
                </div>
            </div>
            """, customerName, orderId, formattedTotal, paymentMethodString, formattedPaidAt);

        send(toEmail, subject, body);
        log.info("Payment success email sent to {} for order #{}", toEmail, orderId);
    }
}