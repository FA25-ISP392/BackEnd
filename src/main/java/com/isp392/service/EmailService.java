package com.isp392.service;

import com.isp392.entity.OrderDetail;
import com.isp392.entity.OrderTopping;
import com.isp392.entity.Orders;
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
        String subject = "Xác nhận đặt bàn của bạn - Trạng thái: " + status;

        // Định dạng lại ngày giờ cho dễ đọc
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
        String formattedDateTime = bookingDateTime.format(formatter);

        String statusMessage;
        String tableDetail;
        String vietnameseStatus;

        if ("APPROVED".equalsIgnoreCase(status)) {
            vietnameseStatus = "ĐÃ XÁC NHẬN";
            statusMessage = "Chúng tôi vui mừng thông báo lượt đặt bàn của bạn đã được <b>XÁC NHẬN</b>.";
            tableDetail = "<strong>Bàn của bạn:</strong> " + tableInfo;
        } else {
            vietnameseStatus = "CHỜ XỬ LÝ";
            statusMessage = "Chúng tôi đã nhận được yêu cầu đặt bàn của bạn và đang <b>CHỜ XỬ LÝ</b>.";
            tableDetail = "<strong>Khu vực mong muốn:</strong> " + (tableInfo != null ? tableInfo : "Không có yêu cầu đặc biệt");
        }

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
                        Email: <strong>moncuaban@gmail.com</strong> | SĐT: <strong>0123456789</strong><br><br>
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
    public void sendPaymentSuccessEmail(String toEmail, String customerName, Orders order, PaymentMethod method, LocalDateTime paidAt) {
        String subject = "Hóa đơn thanh toán cho đơn hàng #" + order.getOrderId();

        // 1. Chuẩn bị các định dạng
        Locale vietnameseLocale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");
        DateTimeFormatter orderDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 2. Tạo bảng chi tiết món ăn (gọi helper đã được thiết kế lại)
        String invoiceTableHtml = generateHtmlInvoiceItemsTable(order, currencyFormatter);

        // 3. Tính tổng tiền
        double totalAmount = order.getOrderDetails().stream()
                .mapToDouble(OrderDetail::getTotalPrice)
                .sum();
        String formattedTotal = currencyFormatter.format(totalAmount);

        // 4. Định dạng các chuỗi
        String formattedOrderDate = order.getOrderDate() != null ? order.getOrderDate().format(orderDateFormatter) : "N/A";
        String formattedPaidAt = paidAt.format(dateFormatter);
        String paymentMethodString = (method == PaymentMethod.CASH) ? "Tiền mặt" : "Chuyển khoản Ngân hàng";

        // 5. Tạo nội dung email
        String body = String.format("""
            <div style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 40px; margin: 0;">
                <div style="max-width: 600px; margin: auto; background-color: #ffffff; border-radius: 10px;
                            box-shadow: 0 4px 10px rgba(0,0,0,0.1); padding: 30px; text-align: left;">
            
                    <img src="https://cdn-icons-png.flaticon.com/512/2910/2910768.png" alt="Logo"
                         style="width: 80px; margin-bottom: 20px; display: block; margin-left: auto; margin-right: auto;">
            
                    <h2 style="color: #333; text-align: center;">Cảm ơn bạn, %s!</h2>
                    <p style="color: #555; font-size: 15px; line-height: 1.6; text-align: center;">
                        Đơn hàng của bạn đã được thanh toán thành công.
                    </p>
                    
                    <table style="width: 100%%; margin-bottom: 20px; font-size: 14px;">
                        <tr>
                            <td style="color: #555;">Mã đơn hàng: <strong>#%d</strong></td>
                            <td style="color: #555; text-align: right;">Đặt hàng vào: <strong>%s</strong></td>
                        </tr>
                    </table>

                    <div style="border: 1px solid #eee; border-radius: 8px; padding: 15px; margin-bottom: 25px;">
                        <h3 style="color: #333; margin-top: 0;">Thanh toán</h3>
                        <p style="color: #555; margin: 5px 0; font-size: 14px;">
                            <strong>Phương thức:</strong> %s
                        </p>
                        <p style="color: #555; margin: 5px 0; font-size: 14px;">
                            <strong>Trạng thái:</strong> <span style="color: #28a745; font-weight: bold;">Đã thanh toán</span> (lúc %s)
                        </p>
                    </div>

                    <h3 style="color: #333; margin-top: 0;">Tóm tắt đơn hàng</h3>
                    %s
                    
                    <table style="width: 100%%; margin-top: 20px; border-top: 2px solid #eee; padding-top: 15px;">
                        <tr>
                            <td style="color: #111; padding: 5px 0; font-size: 1.2em; font-weight: bold;">Tổng cộng:</td>
                            <td style="color: #111; padding: 5px 0; text-align: right; font-size: 1.2em; font-weight: bold;">%s</td>
                        </tr>
                    </table>
            
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
            
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        © 2025 Hệ thống Quản lý Nhà hàng | Mọi quyền được bảo lưu.
                    </p>
                </div>
            </div>
            """, customerName, order.getOrderId(), formattedOrderDate, paymentMethodString, formattedPaidAt, invoiceTableHtml, formattedTotal);

        send(toEmail, subject, body);
        log.info("Payment success email (invoice) sent to {} for order #{}", toEmail, order.getOrderId());
    }



    private String generateHtmlInvoiceItemsTable(Orders order, NumberFormat currencyFormatter) {
        StringBuilder tableBuilder = new StringBuilder();

        // CSS cho bảng (Thêm style cho hình ảnh)
        tableBuilder.append("""
            <style>
                .invoice-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 0;
                    font-size: 14px;
                }
                .invoice-table tr {
                    border-bottom: 1px solid #eee; /* Đường kẻ mờ giữa các món */
                }
                .invoice-table td {
                    padding: 15px 0; /* Tăng khoảng cách */
                    text-align: left;
                    vertical-align: top;
                }
                .invoice-table .item-info {
                    padding-left: 15px;
                }
                .invoice-table .item-image {
                    width: 65px;
                    height: 65px;
                    object-fit: cover;
                    border-radius: 8px;
                    border: 1px solid #eee;
                }
                .invoice-table .item-name {
                    font-size: 1.1em;
                    font-weight: bold;
                    color: #000;
                    margin: 0;
                }
                .invoice-table .item-details {
                    font-size: 0.9em;
                    color: #555;
                    margin: 5px 0 0 0;
                }
                .invoice-table .price {
                    text-align: right;
                    white-space: nowrap;
                    font-weight: bold;
                    font-size: 1.1em;
                }
            </style>
            """);

        tableBuilder.append("<table class='invoice-table'>");
        tableBuilder.append("<tbody>");

        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            tableBuilder.append("<tr><td colspan='2'>Không có chi tiết đơn hàng.</td></tr>");
        } else {
            for (OrderDetail detail : order.getOrderDetails()) {
                // Lấy ảnh (fallback nếu không có ảnh)
                String imageUrl = (detail.getDish() != null && detail.getDish().getPicture() != null)
                        ? detail.getDish().getPicture()
                        : "https://via.placeholder.com/65"; // Ảnh dự phòng

                String dishName = (detail.getDish() != null) ? detail.getDish().getDishName() : "Món không xác định";

                tableBuilder.append("<tr>");

                // Cột 1: Chi tiết (Hình ảnh + Tên + Topping/Note)
                tableBuilder.append("<td style='display: flex; align-items: center; border: none;'>");

                // Hình ảnh
                tableBuilder.append(String.format("<img src='%s' alt='%s' class='item-image'>", imageUrl, dishName));

                // Thông tin
                tableBuilder.append("<div class='item-info'>");
                tableBuilder.append(String.format("<p class='item-name'>%s</p>", dishName));

                // Xây dựng chuỗi chi tiết (Topping và Ghi chú)
                StringBuilder detailsText = new StringBuilder();

                // Thêm Topping
                if (detail.getOrderToppings() != null && !detail.getOrderToppings().isEmpty()) {
                    for (OrderTopping topping : detail.getOrderToppings()) {
                        String toppingName = (topping.getTopping() != null) ? topping.getTopping().getName() : "Topping";
                        detailsText.append(String.format("%s (x%d)<br>", toppingName, topping.getQuantity()));
                    }
                }
                // Thêm Ghi chú
                if (detail.getNote() != null && !detail.getNote().isEmpty()) {
                    detailsText.append(String.format("<em>Ghi chú: %s</em>", detail.getNote()));
                }

                if (detailsText.length() > 0) {
                    tableBuilder.append(String.format("<p class='item-details'>%s</p>", detailsText.toString()));
                }

                tableBuilder.append("</div>"); // end item-info
                tableBuilder.append("</td>"); // end cột 1

                // Cột 2: Thành tiền (của line item này)
                tableBuilder.append(String.format("<td class='price' style='border: none;'>%s</td>", currencyFormatter.format(detail.getTotalPrice())));

                tableBuilder.append("</tr>");
            }
        }

        tableBuilder.append("</tbody></table>");
        return tableBuilder.toString();
    }
}