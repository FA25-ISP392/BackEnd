# 🍽️ Restaurant Booking & Order Management System

Hệ thống quản lý đặt bàn và đặt món nhà hàng thông minh với tính năng gợi ý món ăn cá nhân hóa dựa trên AI.

## 📖 Giới thiệu

Đây là một giải pháp toàn diện cho việc quản lý nhà hàng, bao gồm:
- **Đặt bàn trước** - Khách hàng có thể đặt chỗ trước khi đến nhà hàng
- **Order tại bàn** - Gọi món trực tiếp khi đã ngồi vào bàn
- **AI Suggestion** - Gợi ý món ăn thông minh dựa trên mục tiêu sức khỏe cá nhân (ổn định cân nặng, cải thiện sức khỏe)
- **Thanh toán linh hoạt** - Hỗ trợ thanh toán tiền mặt và thanh toán online qua PayOS
- **Quản lý nhà hàng** - Dashboard cho nhà hàng quản lý kế hoạch món ăn, đơn hàng, booking và doanh thu

## ✨ Tính năng chính

### 👥 Khách hàng
- 🎫 Đặt bàn trước theo thời gian
- 🍜 Order món ăn tại bàn
- 🤖 Nhận gợi ý món ăn từ AI phù hợp với:
  - Mục tiêu ổn định cân nặng
  - Cải thiện sức khỏe
  - Mức độ hoạt động hàng ngày
- 💳 Thanh toán qua nhiều phương thức (tiền mặt, PayOS)
- 📜 Theo dõi lịch sử đặt bàn và order
- ✅ Xác thực email

### 🏪 Nhà hàng/Quản lý
- 📅 Lên kế hoạch món ăn hàng ngày (Daily Plan)
- 📊 Theo dõi tình trạng đơn hàng real-time
- 📆 Quản lý booking và trạng thái bàn
- 💰 Thống kê doanh thu chi tiết
- 🍽️ Quản lý thực đơn (món ăn, topping)
- 👨‍💼 Quản lý nhân viên và phân quyền
- 📈 Dashboard thống kê tổng quan

## 🛠️ Tech Stack

### Backend
- **Framework:** Spring Boot 3.2.2
- **Language:** Java 21
- **Build Tool:** Maven 3.9.5+
- **Database:** MySQL
- **Security:** Spring Security + OAuth2 Resource Server + JWT
- **API Documentation:** OpenAPI/Swagger
- **Payment Integration:** PayOS Java SDK
- **Cloud Storage:** Cloudinary
- **Async Processing:** Spring Async
- **Mapping:** MapStruct
- **Validation:** Spring Validation

### Frontend
- **Framework:** React
- **Language:** JavaScript/TypeScript

### Công nghệ khác
- **AI/ML:** Service gợi ý món ăn dựa trên thông tin cá nhân
- **Email Service:** Spring Mail
- **Scheduling:** Spring Scheduler

## 📦 Yêu cầu hệ thống

### Backend Requirements
- Java SDK 21 hoặc cao hơn
- Maven 3.9.5 hoặc cao hơn
- MySQL Server 8.0+
- Port 8080 available (hoặc cấu hình trong application.yaml)

### Frontend Requirements
- Node.js 16+ và npm/yarn
- Port 3000 available (default React port)

## 🚀 Cài đặt và Chạy

### Backend Setup

1. **Clone repository**
```bash
git clone <repository-url>
cd BackEnd
```

2. **Cấu hình Database**
- Tạo database MySQL:
```sql
CREATE DATABASE restaurant_booking;
```

3. **Cấu hình application.yaml**
Chỉnh sửa file `src/main/resources/application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/restaurant_booking
    username: your_username
    password: your_password
```

4. **Cấu hình PayOS**
Thêm PayOS credentials vào `application.yaml`:
```yaml
payos:
  client-id: your_client_id
  api-key: your_api_key
  checksum-key: your_checksum_key
```

5. **Cấu hình Cloudinary**
Thêm Cloudinary credentials:
```yaml
cloudinary:
  cloud-name: your_cloud_name
  api-key: your_api_key
  api-secret: your_api_secret
```

6. **Build và chạy application**
```bash
# Build project
mvn clean package

# Chạy application
mvn spring-boot:run
```

Application sẽ chạy tại: `http://localhost:8080`

### Frontend Setup

```bash
cd frontend
npm install
npm start
```

Frontend sẽ chạy tại: `http://localhost:3000`

## ⚙️ Cấu hình

### Environment Variables
Các biến môi trường quan trọng trong `application.yaml`:

```yaml
# Database
spring.datasource.url
spring.datasource.username
spring.datasource.password

# JWT
jwt.secret
jwt.expiration

# PayOS
payos.client-id
payos.api-key
payos.checksum-key

# Cloudinary
cloudinary.cloud-name
cloudinary.api-key
cloudinary.api-secret

# Email
spring.mail.username
spring.mail.password
```

## 📁 Cấu trúc dự án Backend

```
src/main/java/com/isp392/
├── configuration/          # Cấu hình ứng dụng
│   ├── SecurityConfig     # Cấu hình bảo mật
│   ├── OpenApiConfig      # Cấu hình Swagger
│   ├── PayOSConfig        # Cấu hình PayOS
│   ├── CloudinaryConfig   # Cấu hình Cloudinary
│   └── AsyncConfig        # Cấu hình xử lý bất đồng bộ
├── controller/            # REST API Controllers
│   ├── AuthenticationController
│   ├── BookingController
│   ├── OrdersController
│   ├── DishController
│   ├── PaymentController
│   ├── AIChatController   # AI suggestion
│   └── StatisticController
├── service/               # Business Logic
├── repository/            # Data Access Layer
├── entity/                # JPA Entities
│   ├── Account
│   ├── Booking
│   ├── Orders
│   ├── Dish
│   ├── Payment
│   └── DailyPlan
├── dto/                   # Data Transfer Objects
│   ├── request/
│   └── response/
├── mapper/                # MapStruct Mappers
├── enums/                 # Enumerations
│   ├── BookingStatus
│   ├── PaymentStatus
│   ├── TableStatus
│   └── OrderDetailStatus
├── exception/             # Exception Handling
└── scheduler/             # Scheduled Tasks
```

## 📖 API Documentation

Sau khi chạy ứng dụng, truy cập Swagger UI tại:
```
http://localhost:8080/swagger-ui.html
```

### Các endpoint chính:

#### Authentication
- `POST /auth/register` - Đăng ký tài khoản
- `POST /auth/login` - Đăng nhập
- `POST /auth/logout` - Đăng xuất
- `POST /auth/refresh-token` - Refresh JWT token

#### Booking
- `GET /bookings` - Lấy danh sách booking
- `POST /bookings` - Tạo booking mới
- `PUT /bookings/{id}` - Cập nhật booking
- `DELETE /bookings/{id}` - Hủy booking

#### Orders
- `GET /orders` - Lấy danh sách đơn hàng
- `POST /orders` - Tạo đơn hàng
- `PUT /orders/{id}` - Cập nhật đơn hàng
- `GET /orders/{id}/details` - Chi tiết đơn hàng

#### Dishes
- `GET /dishes` - Lấy danh sách món ăn
- `POST /dishes` - Thêm món ăn mới
- `PUT /dishes/{id}` - Cập nhật món ăn
- `DELETE /dishes/{id}` - Xóa món ăn

#### AI Suggestion
- `POST /ai-chat` - Nhận gợi ý món ăn từ AI

#### Payment
- `POST /payment/create` - Tạo thanh toán
- `POST /payment/payos-webhook` - Webhook từ PayOS
- `GET /payment/status/{id}` - Kiểm tra trạng thái thanh toán

#### Statistics
- `GET /statistics/revenue` - Thống kê doanh thu
- `GET /statistics/orders` - Thống kê đơn hàng
- `GET /statistics/bookings` - Thống kê booking

## 💾 Database Schema

### Các bảng chính:

- **account** - Tài khoản người dùng
- **customer** - Thông tin khách hàng
- **staff** - Thông tin nhân viên
- **booking** - Đặt bàn
- **table_entity** - Bàn ăn
- **dish** - Món ăn
- **topping** - Topping cho món ăn
- **orders** - Đơn hàng
- **order_detail** - Chi tiết đơn hàng
- **payment** - Thanh toán
- **daily_plan** - Kế hoạch món ăn hàng ngày

### Enums quan trọng:

- **BookingStatus:** PENDING, CONFIRMED, CANCELLED, COMPLETED
- **PaymentStatus:** PENDING, PAID, FAILED, REFUNDED
- **PaymentMethod:** CASH, PAYOS
- **TableStatus:** AVAILABLE, OCCUPIED, RESERVED
- **OrderDetailStatus:** PENDING, PREPARING, READY, SERVED
- **Role:** ADMIN, STAFF, CUSTOMER

## 🔐 Authentication & Authorization

Hệ thống sử dụng JWT (JSON Web Token) cho authentication:

1. User đăng nhập với username/password
2. Server trả về access token và refresh token
3. Client gửi access token trong header `Authorization: Bearer <token>`
4. Khi access token hết hạn, sử dụng refresh token để lấy token mới

### Roles và Permissions:
- **ADMIN:** Full access, quản lý toàn bộ hệ thống
- **STAFF:** Quản lý đơn hàng, booking, bàn
- **CUSTOMER:** Đặt bàn, order món, xem lịch sử


## 📝 Notes

- Hệ thống sử dụng Spring Async cho xử lý email và notification
- PayOS webhook được cấu hình tự động verify signature
- Cloudinary được dùng để lưu trữ hình ảnh món ăn
- AI suggestion được tích hợp qua AIChatController
- Scheduled tasks tự động cập nhật trạng thái booking và orders

---

**Developed with ❤️**
