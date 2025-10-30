package com.isp392.service;

import com.isp392.exception.AppException;
import com.isp392.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

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

}
