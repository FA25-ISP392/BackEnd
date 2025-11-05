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
import java.util.stream.Collectors;

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

    //    @Async("taskExecutor")
//    public void sendPaymentSuccessEmail(String toEmail, String customerName, Orders order, PaymentMethod method, LocalDateTime paidAt) {
//        String subject = "Hóa đơn thanh toán cho đơn hàng #" + order.getOrderId();
//
//        // 1. Chuẩn bị các định dạng
//        Locale vietnameseLocale = new Locale("vi", "VN");
//        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);
//        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");
//        DateTimeFormatter orderDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        // 2. Tạo bảng chi tiết món ăn (gọi helper đã được thiết kế lại)
//        String invoiceTableHtml = generateHtmlInvoiceItemsTable(order, currencyFormatter);
//
//        // 3. Tính tổng tiền
//        double totalAmount = order.getOrderDetails().stream()
//                .mapToDouble(OrderDetail::getTotalPrice)
//                .sum();
//        double shippingFee = 0.0;
//        String formattedSubtotal = currencyFormatter.format(totalAmount);
//        String formattedShipping = currencyFormatter.format(shippingFee);
//        String formattedGrandTotal = currencyFormatter.format(totalAmount + shippingFee);
//        // 4. Định dạng các chuỗi
//        String formattedOrderDate = order.getOrderDate() != null ? order.getOrderDate().format(orderDateFormatter) : "N/A";
//        String formattedPaidAt = paidAt.format(dateFormatter);
//        String paymentMethodString = (method == PaymentMethod.CASH) ? "Tiền mặt" : "Chuyển khoản Ngân hàng";
//
//        // 5. Tạo nội dung email
//        String body = String.format("""
//                            <div style="font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f3f4f6; padding: 40px 0; margin: 0;">
//                                            <div style="max-width: 680px; margin: 0 auto; background-color: #ffffff; border-radius: 18px; overflow: hidden; box-shadow: 0 18px 45px rgba(15, 23, 42, 0.12);">
//                                                <div style="background: linear-gradient(135deg,#f97316,#facc15); padding: 32px; text-align: center; color: #ffffff;">
//                                                    <div style="font-size: 13px; letter-spacing: 4px; text-transform: uppercase; opacity: 0.85;">Biên nhận thanh toán</div>
//                                                    <h1 style="margin: 12px 0 0; font-size: 30px; font-weight: 700;">Thanh toán thành công</h1>
//                                                    <p style="margin: 14px 0 0; font-size: 15px; opacity: 0.9;">Cảm ơn bạn, %s! Đơn hàng #%d đã được xác nhận.</p>
//                                </div>
//                                <div style="padding: 32px;">
//                                                        <table width="100%%" style="border-collapse: separate; border-spacing: 0 16px; margin-bottom: 24px;">
//                                                            <tr>
//                                                                <td style="width: 50%%; padding: 0;">
//                                                                    <div style="border: 1px solid #e2e8f0; border-radius: 14px; padding: 18px 20px; height: 100%%;">
//                                                                        <div style="font-size: 12px; letter-spacing: 2px; text-transform: uppercase; color: #94a3b8; margin-bottom: 8px;">Thông tin hóa đơn</div>
//                                                                        <div style="font-size: 18px; font-weight: 600; color: #0f172a; margin-bottom: 6px;">Mã đơn hàng #%d</div>
//                                                                        <div style="color: #475569; font-size: 14px; line-height: 1.6;">
//                                                                            <span style="display:block;">Khách hàng: <strong>%s</strong></span>
//                                                                            <span style="display:block;">Ngày đặt: <strong>%s</strong></span>
//                                                                        </div>
//                                                                    </div>
//                                                                </td>
//                                                                <td style="width: 50%%; padding: 0; padding-left: 16px;">
//                                                                    <div style="border: 1px solid #e2e8f0; border-radius: 14px; padding: 18px 20px; background-color: #f8fafc; height: 100%%;">
//                                                                        <div style="font-size: 12px; letter-spacing: 2px; text-transform: uppercase; color: #94a3b8; margin-bottom: 8px;">Thanh toán</div>
//                                                                        <div style="color: #0f172a; font-size: 15px; font-weight: 600; margin-bottom: 6px;">%s</div>
//                                                                        <div style="color: #16a34a; font-size: 13px; font-weight: 600;">Đã thanh toán lúc %s</div>
//                                                                    </div>
//                                                                </td>
//                                                            </tr>
//                                                        </table>
//
//                                                        <h3 style="font-size: 18px; font-weight: 600; color: #0f172a; margin: 0 0 16px;">Tóm tắt đơn hàng</h3>
//
//                                                        %s
//
//                                                        <table width="100%%" style="border-collapse: collapse; margin-top: 24px;">
//                                                                                    <tr>
//                                                                                        <td style="padding: 8px 0; color: #475569; font-size: 14px;">Tổng từng phần</td>
//                                                                                        <td style="padding: 8px 0; color: #0f172a; font-size: 14px; text-align: right;">%s</td>
//                                                                                    </tr>
//                                                                                    <tr>
//                                                                                        <td style="padding: 8px 0; color: #475569; font-size: 14px;">Phí vận chuyển</td>
//                                                                                        <td style="padding: 8px 0; color: #0f172a; font-size: 14px; text-align: right;">%s</td>
//                                                                                    </tr>
//                                                                                    <tr>
//                                                                                        <td style="padding: 12px 0; color: #0f172a; font-size: 18px; font-weight: 700;">Tổng cộng</td>
//                                                                                        <td style="padding: 12px 0; color: #0f172a; font-size: 18px; font-weight: 700; text-align: right;">%s</td>
//                                                                                    </tr>
//                                                                                </table>
//
//                                                                                <p style="color: #94a3b8; font-size: 12px; line-height: 1.6; margin-top: 32px; text-align: center;">Nếu bạn cần hỗ trợ, vui lòng liên hệ qua hotline <strong>0123 456 789</strong> hoặc email <strong>support@nhahang.com</strong>.</p>
//                                                                            </div>
//                                                            </div>
//                                                        </div>
//                        """, customerName, order.getOrderId(), order.getOrderId(), customerName, formattedOrderDate,
//                paymentMethodString, formattedPaidAt, invoiceTableHtml, formattedSubtotal, formattedShipping, formattedGrandTotal);
//
//        send(toEmail, subject, body);
//        log.info("Payment success email (invoice) sent to {} for order #{}", toEmail, order.getOrderId());
//    }
//
//
//    private String generateHtmlInvoiceItemsTable(Orders order, NumberFormat currencyFormatter) {
//        StringBuilder tableBuilder = new StringBuilder();
//
//        // CSS cho bảng (Thiết kế lại đơn giản, giống hình tham khảo Beats)
//        tableBuilder.append("""
//                    <style>
//                        .invoice-table {
//                            width: 100%;
//                            border-collapse: collapse;
//                            font-size: 14px;
//                            color: #333;
//                        }
//                        .invoice-table thead th {
//                            text-align: left;
//                            padding: 10px 8px;
//                            border-bottom: 2px solid #ddd;
//                            color: #666;
//                            font-weight: 600;
//                            font-size: 13px;
//                            text-transform: uppercase;
//                        }
//                        .invoice-table tbody td {
//                            padding: 10px 8px;
//                            border-bottom: 1px solid #f0f0f0;
//                            vertical-align: middle;
//                        }
//                        .invoice-table .item-cell {
//                            display: flex;
//                            align-items: center;
//                            gap: 12px;
//                        }
//                        .invoice-table .item-thumb {
//                            width: 10px;
//                            height: 10px;
//                            object-fit: cover;
//                            border-radius: 6px;
//                            box-shadow: 0 1px 4px rgba(0,0,0,0.08);
//                        }
//                        .invoice-table .item-text {
//                            flex: 1;
//                        }
//                        .invoice-table .item-name {
//                            font-weight: 600;
//                            font-size: 14px;
//                            color: #111827;
//                            margin: 0;
//                        }
//                        .invoice-table .item-meta {
//                            font-size: 12.5px;
//                            color: #64748b;
//                            margin-top: 4px;
//                            line-height: 1.4;
//                        }
//                        .invoice-table .qty,
//                        .invoice-table .price {
//                            text-align: right;
//                            font-weight: 600;
//                            color: #111827;
//                            white-space: nowrap;
//                        }
//                        .invoice-table .qty {
//                            width: 50px;
//                        }
//                        .invoice-table .price {
//                            width: 100px;
//                        }
//                    </style>
//                """);
//
//        // HTML Bảng
//        tableBuilder.append("<table class='invoice-table' width='100%' cellspacing='0' cellpadding='0'>");
//        tableBuilder.append("<thead><tr>");
//        tableBuilder.append("<th style='width: 60%;'>Món</th>"); // Cột Món (rộng nhất)
//        tableBuilder.append("<th class='qty'>SL</th>");         // Cột SL
//        tableBuilder.append("<th class='price'>Giá</th>");      // Cột Giá
//        tableBuilder.append("</tr></thead>");
//        tableBuilder.append("<tbody>");
//
//        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
//            tableBuilder.append("<tr><td colspan='3'>Không có chi tiết đơn hàng.</td></tr>");
//        } else {
//            for (OrderDetail detail : order.getOrderDetails()) {
//                String imageUrl = (detail.getDish() != null && detail.getDish().getPicture() != null)
//                        ? detail.getDish().getPicture()
//                        : "https://via.placeholder.com/60"; // Ảnh dự phòng 60x60
//                String dishName = (detail.getDish() != null) ? detail.getDish().getDishName() : "Món không xác định";
//
//                tableBuilder.append("<tr>");
//
//                // Cột 1: Món (Hình + Tên + Meta)
//                tableBuilder.append("<td>");
//                tableBuilder.append("<div class='item-cell'>");
//                tableBuilder.append(String.format("<img src='%s' alt='%s' class='item-thumb'>", imageUrl, dishName));
//                tableBuilder.append("<div>"); // div bọc text
//                tableBuilder.append(String.format("<p class='item-name'>%s</p>", dishName));
//
//                // Xây dựng meta (Topping/Note)
//                StringBuilder metaBuilder = new StringBuilder();
//                if (detail.getOrderToppings() != null && !detail.getOrderToppings().isEmpty()) {
//                    String toppingText = detail.getOrderToppings().stream()
//                            .map(ot -> {
//                                String name = (ot.getTopping() != null) ? ot.getTopping().getName() : "Topping";
//                                return name + (ot.getQuantity() != null && ot.getQuantity() > 1 ? " x" + ot.getQuantity() : "");
//                            })
//                            .collect(Collectors.joining(", "));
//                    metaBuilder.append("<strong>Topping:</strong> ").append(toppingText);
//                }
//                if (detail.getNote() != null && !detail.getNote().isBlank()) {
//                    if (metaBuilder.length() > 0) metaBuilder.append("<br>");
//                    metaBuilder.append("<strong style='color:#dc2626;'>Ghi chú:</strong> ").append(detail.getNote().trim());
//                }
//
//                if (metaBuilder.length() > 0) {
//                    tableBuilder.append(String.format("<div class='item-meta'>%s</div>", metaBuilder.toString()));
//                }
//
//                tableBuilder.append("</div>"); // end div bọc text
//                tableBuilder.append("</div>"); // end item-cell
//                tableBuilder.append("</td>");
//
//                // Cột 2: Số lượng (Giả định là 1 cho mỗi order detail)
//                tableBuilder.append("<td class='qty'>1</td>");
//
//                // Cột 3: Giá
//                tableBuilder.append(String.format("<td class='price'>%s</td>", currencyFormatter.format(detail.getTotalPrice())));
//
//                tableBuilder.append("</tr>");
//            }
//        }
//
//        tableBuilder.append("</tbody></table>");
//        return tableBuilder.toString();
//    }
//
//    private String formatEnumLabel(Enum<?> value) {
//        if (value == null) {
//            return "";
//        }
//        String[] words = value.name().toLowerCase(Locale.ROOT).split("_");
//        StringBuilder builder = new StringBuilder();
//        for (String word : words) {
//            if (word.isEmpty()) {
//                continue;
//            }
//            if (builder.length() > 0) {
//                builder.append(' ');
//            }
//            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
//        }
//        return builder.toString();
//    }
//
//    private String truncateText(String text, int maxLength) {
//        if (text == null) {
//            return "";
//        }
//        String trimmed = text.trim();
//        if (trimmed.length() <= maxLength) {
//            return trimmed;
//        }
//        int endIndex = Math.max(0, maxLength - 3);
//        return trimmed.substring(0, endIndex) + "...";
//    }
    @Async("taskExecutor")
    public void sendPaymentSuccessEmail(String toEmail, String customerName, Orders order, PaymentMethod method, LocalDateTime paidAt) {
        String subject = "Xác nhận thanh toán đơn hàng #" + order.getOrderId();

        Locale vietnameseLocale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy");

        String invoiceTable = generateHtmlInvoiceItemsTable(order, currencyFormatter);
        double subtotal = order.getOrderDetails().stream().mapToDouble(OrderDetail::getTotalPrice).sum();
        double shipping = 0;
        String subtotalStr = currencyFormatter.format(subtotal);
        String shippingStr = currencyFormatter.format(shipping);
        String totalStr = currencyFormatter.format(subtotal + shipping);
        String paidAtStr = paidAt.format(dateFormatter);

        String methodStr = switch (method) {
            case CASH -> "Tiền mặt";
            case BANK_TRANSFER -> "Chuyển khoản ngân hàng";
            default -> method.name();
        };

        String htmlBody = """
                <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
                <title>Order Confirmation</title>
                <style>
                  @import url('https://fonts.googleapis.com/css?family=Open+Sans:400,700');
                  body { margin:0; padding:0; background:#e1e1e1; font-family:'Open Sans',sans-serif; }
                  table { border-collapse:collapse; }
                  .fullTable { width:600px; background:#fff; border-radius:10px; }
                  .section { padding:30px 40px; }
                  .heading { font-size:20px; font-weight:700; color:#111827; margin-bottom:10px; }
                  .subtext { font-size:13px; color:#555; line-height:1.5; }
                  .invoice-table { width:100%%; border-collapse:collapse; font-size:13px; color:#333; margin-top:15px; }
                  .invoice-table th { text-align:left; border-bottom:2px solid #eee; padding:8px 0; font-weight:600; color:#555; text-transform:uppercase; font-size:12px; }
                  .invoice-table td { padding:10px 0; border-bottom:1px solid #f0f0f0; vertical-align:middle; }
                  .item-cell { display:flex; align-items:center; gap:10px; }
                  .item-thumb { width:10px; height:10px; object-fit:cover; border-radius:6px; border:1px solid #eee; }
                  .item-name { font-weight:600; color:#111827; margin:0; font-size:13px; }
                  .item-meta { font-size:12px; color:#64748b; margin-top:2px; }
                  .qty, .price { text-align:right; font-weight:600; color:#111827; white-space:nowrap; }
                  .total-table td { font-size:13px; color:#333; padding:5px 0; }
                  .total-table strong { font-size:14px; color:#111827; }
                </style>
                
                <body>
                  <table align="center" class="fullTable" cellpadding="0" cellspacing="0">
                    <tr><td class="section" style="text-align:left;">
                      <div class="heading"> <b>Nhà hàng Món Của Bạn</b> </div>
                      <div class="heading">Cảm ơn quý khách, %s!</div>
                      <p class="subtext">
                        Đơn hàng của bạn đã được thanh toán thành công.<br/>
                        <strong>Mã đơn hàng:</strong> #%d<br/>
                        <strong>Phương thức:</strong> %s<br/>
                        <strong>Thời gian thanh toán:</strong> %s
                      </p>
                
                      <h3 style="font-size:16px; margin-top:25px;">Tóm tắt đơn hàng</h3>
                      %s
                
                      <table width="100%%" class="total-table" style="margin-top:20px;">
                        <tr><td style="text-align:right;"><strong>Tổng cộng</strong></td><td style="text-align:right;"><strong>%s</strong></td></tr>
                      </table>
                
                      <p class="subtext" style="margin-top:30px; text-align:center; color:#888;">
                        Nếu bạn cần hỗ trợ, vui lòng liên hệ hotline <strong>0123 456 789</strong> hoặc email <strong>moncuaban@gmail.com</strong><br/>
                        © 2025 <b>Nhà hàng Món Của Bạn</b> | Mọi quyền được bảo lưu.
                      </p>
                    </td></tr>
                  </table>
                </body>
                """.formatted(customerName, order.getOrderId(), methodStr, paidAtStr, invoiceTable, subtotalStr, shippingStr, totalStr);

        send(toEmail, subject, htmlBody);
        log.info("✅ Gửi email xác nhận thanh toán thành công cho {} đơn hàng #{}", toEmail, order.getOrderId());
    }

    // *** THAY THẾ HÀM CŨ BẰNG HÀM NÀY ***

    private String generateHtmlInvoiceItemsTable(Orders order, NumberFormat currencyFormatter) {
        StringBuilder sb = new StringBuilder();

        // Bảng chính, đã được inline CSS
        sb.append("<table width='100%' style='border-collapse:collapse; font-size:13px; color:#333; margin-top:15px;'><thead><tr>")

                // Các tiêu đề cột (TH) đã inline
                .append("<th style='text-align:left; border-bottom:2px solid #eee; padding:8px 0; font-weight:600; color:#555; text-transform:uppercase; font-size:12px;'>Món</th>")
                .append("<th style='text-align:right; border-bottom:2px solid #eee; padding:8px 0; font-weight:600; color:#555; text-transform:uppercase; font-size:12px; white-space:nowrap;'>SL</th>")
                .append("<th style='text-align:right; border-bottom:2px solid #eee; padding:8px 0; font-weight:600; color:#555; text-transform:uppercase; font-size:12px; white-space:nowrap;'>Giá</th></tr></thead><tbody>");

        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            sb.append("<tr><td colspan='3' style='padding:10px 0; border-bottom:1px solid #f0f0f0;'>Không có chi tiết đơn hàng.</td></tr>");
        } else {
            for (OrderDetail d : order.getOrderDetails()) {
                String img = (d.getDish() != null && d.getDish().getPicture() != null)
                        ? d.getDish().getPicture() : "https://via.placeholder.com/40"; // 40px là kích thước hợp lý
                String name = (d.getDish() != null) ? d.getDish().getDishName() : "Món không xác định";

                sb.append("<tr>");

                // Cột 1: Món (Sử dụng <table> lồng nhau thay cho flex)
                sb.append("<td style='padding:10px 0; border-bottom:1px solid #f0f0f0; vertical-align:middle;'>");

                // Bảng lồng nhau (cách an toàn nhất cho email)
                sb.append("<table cellpadding='0' cellspacing='0' style='border-collapse:collapse;'><tr>");

                // Cột 1.1: Hình ảnh (ĐÃ SỬA VỚI CSS INLINE VÀ SIZE 40PX)
                sb.append("<td style='padding-right:10px; vertical-align:top;'>");
                sb.append(String.format(
                        "<img src='%s' alt='%s' style='width:40px; height:40px; object-fit:cover; border-radius:6px; border:1px solid #eee;'>",
                        img, name
                ));
                sb.append("</td>");

                // Cột 1.2: Tên và Ghi chú (Đã inline)
                sb.append("<td style='vertical-align:top;'>");
                sb.append("<p style='margin:0; font-weight:600; color:#111827; font-size:13px;'>").append(name).append("</p>");
                if (d.getNote() != null && !d.getNote().isBlank()) {
                    sb.append("<div style='font-size:12px; color:#64748b; margin-top:2px;'><strong>Ghi chú:</strong> ").append(d.getNote()).append("</div>");
                }
                sb.append("</td>");
                sb.append("</tr></table>"); // Hết bảng lồng nhau
                sb.append("</td>"); // Hết cột Món

                // Cột 2: Số lượng (Đã inline)
                sb.append("<td style='padding:10px 0; border-bottom:1px solid #f0f0f0; vertical-align:middle; text-align:right; font-weight:600; color:#111827; white-space:nowrap;'>1</td>");

                // Cột 3: Giá (Đã inline)
                sb.append("<td style='padding:10px 0; border-bottom:1px solid #f0f0f0; vertical-align:middle; text-align:right; font-weight:600; color:#111827; white-space:nowrap;'>")
                        .append(currencyFormatter.format(d.getTotalPrice())).append("</td></tr>");
            }
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }
}