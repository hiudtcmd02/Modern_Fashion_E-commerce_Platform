# Modern Fashion E-Commerce API 🛍️

*Đọc bằng ngôn ngữ khác: [English](README.md) | [Tiếng Việt](README-vi.md)*

Hệ thống RESTful API mạnh mẽ, dễ mở rộng được thiết kế chuyên biệt cho nền tảng thương mại điện tử mảng quần áo và giày dép. Xây dựng theo kiến trúc **Modular Monolith**, hệ thống cung cấp toàn bộ nghiệp vụ cốt lõi từ quản lý danh mục và sản phẩm, xác thực bảo mật người dùng cho đến quy trình đặt hàng an toàn với cơ chế kiểm soát đồng thời (Concurrency Control) khắt khe.

## 🚀 Tính năng nổi bật

### 👤 Xác thực & Người dùng (Identity Module)
* **Xác thực JWT:** Đăng nhập/Đăng ký an toàn với Access Token.
* **Blacklist Token:** Xử lý đăng xuất an toàn và dọn dẹp các token hết hạn bằng Cron Job chạy ngầm.
* **Xác thực OTP qua Email:** Tích hợp chống spam mail gửi OTP cho luồng Kích hoạt tài khoản và Quên mật khẩu.
* **Phân quyền (RBAC):** Tách biệt quyền hạn truy cập rõ ràng giữa `ROLE_ADMIN` và `ROLE_CUSTOMER`.
* **Sổ địa chỉ:** Quản lý danh sách địa chỉ nhận hàng với logic địa chỉ mặc định.

### 📦 Sản phẩm & Danh mục (Catalog Module)
* **Biến thể đa cấp (SKU):** Hệ thống ma trận quản lý sản phẩm chuyên sâu theo tổ hợp Kích cỡ (Size) và Màu sắc.
* **Xóa mềm (Soft Deletion):** Lưu trữ an toàn Danh mục và Sản phẩm mà không vi phạm tính toàn vẹn dữ liệu (Khóa ngoại).
* **Tìm kiếm & Lọc động:** Truy vấn JPA/Native Query mạnh mẽ để lọc sản phẩm theo danh mục, khoảng giá và tình trạng kho hàng.
* **Tích hợp Cloud Media:** Tải lên, quản lý và xóa ảnh sản phẩm trực tiếp với **Cloudinary**.

### 🛒 Giỏ hàng (Cart Module)
* **Giỏ hàng thông minh:** Thêm sản phẩm, cập nhật số lượng và kiểm tra tồn kho tự động.
* **Mini Cart:** Tối ưu hóa dữ liệu trả về phục vụ cho giao diện giỏ hàng thu nhỏ trên Header.

### 🧾 Đơn hàng (Order Module)
* **Vòng đời đơn hàng:** Theo dõi trạng thái: Đang chờ xử lý -> Đang chuẩn bị đơn hàng -> Đang giao hàng -> Hoàn thành/ Đã hủy/ Đã trả hàng.
* **Kiểm soát đồng thời (Pessimistic Locking):** Ngăn chặn lỗi Race Condition và bán vượt số lượng tồn kho (over-selling) khi đặt hàng bằng cách khóa dòng dữ liệu dưới Database (`LockModeType.PESSIMISTIC_WRITE`).
* **Bảo vệ toàn vẹn dữ liệu:** Đối chiếu khắt khe tổng tiền gửi lên từ Frontend và giá trị tính toán thực tế dưới Database để chặn các hành vi sửa đổi giá.
* **Hoàn trả tồn kho:** Tự động hoàn lại số lượng sản phẩm vào kho nếu đơn hàng bị hủy hoặc trả lại.

### 💳 Tích hợp Thanh toán (VNPAY)
* **Bảo mật giao dịch:** Xác thực chữ ký điện tử HMAC-SHA512 chống giả mạo và can thiệp URL.
* **Webhook IPN Tin cậy:** Giao tiếp Server-to-Server đảm bảo đối soát trạng thái thanh toán chính xác tuyệt đối ngay cả khi khách hàng tắt trình duyệt.
* **Kiểm soát Rủi ro (Edge Cases):** Cấu trúc phòng thủ chặt chẽ chống lại lỗi Thanh toán kép (Double Payments), Mất dữ liệu và Độ trễ mạng (Network Latency) - Tự động phát tín hiệu khẩn cấp bằng Email để Kế toán hoàn tiền bù trừ.
* **Vớt vát luồng tiền (Salvage Logic):** Khôi phục mượt mà các giao dịch bị khách hàng hủy ngang nhưng lại "quay xe" thanh toán thành công trong thời gian link còn hiệu lực.

## 🛠️ Công nghệ sử dụng

* **Core:** Java 17, Spring Boot 4.0.3
* **Data Access:** Spring Data JPA, Hibernate
* **Database:** MySQL 8.0.34
* **Security:** Spring Security, JWT (JSON Web Token)
* **Documentation:** SpringDoc OpenAPI 3 (Swagger UI)
* **Third-Party Services:** Cloudinary (Lưu trữ ảnh), JavaMailSender (Gửi Email SMTP), VNPAY API (Thanh toán trực tuyến), Ngrok (Tạo đường dẫn để kết nối IPN trên local với VNPay Sandbox)
* **Kiến trúc:** Modular Monolith (Identity, Catalog, Cart, Order)

## 📖 Tài liệu API (Swagger UI)

Toàn bộ API được viết tài liệu đặc tả chuẩn OpenAPI 3. Bao gồm schema chi tiết, mô tả tham số và khả năng test trực tiếp trên giao diện.

1. Chạy dự án.
2. Truy cập đường dẫn: `http://localhost:8080/swagger-ui/index.html`

## ⚙️ Hướng dẫn Cài đặt

### Yêu cầu môi trường
* JDK 17
* Maven
* MySQL Server 8.x
* Tài khoản Cloudinary (Gói Free)
* Tài khoản Gmail (Để tạo App Password cho SMTP)
* Tài khoản VNPAY Sandbox & Ngrok (Để public port local phục vụ cấu hình IPN Webhook của VNPAY).

### Cài đặt

1.  **Clone mã nguồn và di chuyển vào thư mục dự án:**
    ```bash
    cd Modern_Fashion_E-commerce_Platform
    ```

2.  **Biến môi trường:**
    Tạo file `.env` ở thư mục gốc dựa trên mẫu sau:
    ```env
    DB_URL=jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&allowPublicKeyRetrieval=true
    DB_USERNAME=root
    DB_PASSWORD=your_db_password
    
    MAIL_USERNAME=your_email@gmail.com
    MAIL_PASSWORD=your_app_password
    
    JWT_SECRET=your_very_long_secret_key_here_for_jwt
    JWT_EXPIRATION=86400000
    
    CLOUDINARY_CLOUD_NAME=your_cloud_name
    CLOUDINARY_API_KEY=your_api_key
    CLOUDINARY_API_SECRET=your_api_secret
    
    VNPAY_TMN_CODE=your_vnpay_tmn_code
    VNPAY_HASH_SECRET=your_vnpay_hash_secret
    VNPAY_PAY_URL=your_vnpay_pay_url
    VNPAY_RETURN_URL=your_vnpay_return_url
    ```

3.  **Build và Chạy project:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

4.  **Cấu hình VNPAY IPN (Webhook):**
    * Khởi chạy Ngrok để public port của backend (Ví dụ: `ngrok http 8080`).
    * Copy đường dẫn công khai HTTPS do Ngrok cấp.
    * Đăng nhập vào trang quản trị **VNPAY Sandbox Merchant**.
    * Tìm đến mục cấu hình hệ thống của bạn và dán link IPN URL theo định dạng: `<đường-dẫn-ngrok-của-bạn>/api/v1/payments/vnpay-ipn`.

## 🗺️ Lộ trình phát triển (Sắp ra mắt)

- [x] **Thanh toán Online:** Tích hợp cổng thanh toán VNPAY qua cơ chế Webhook/IPN.
- [ ] **Hệ thống Khuyến mãi:** Quản lý cấu hình mã giảm giá Voucher (Giảm %, Giảm tiền mặt, Giới hạn lượt dùng).
- [ ] **Tương tác khách hàng:** Đánh giá, Bình luận sản phẩm và Danh sách yêu thích (Wishlist).
- [ ] **Dashboard Admin:** Biểu đồ thống kê doanh thu và số lượng bán ra.

## 👨‍💻 Tác giả

**Đinh Trung Hiếu**
* **Email:** hieudinhtrung1102@gmail.com
* **LinkedIn:** www.linkedin.com/in/hiếu-đinh-trung-73b4a5369
* **GitHub:** https://github.com/hiudtcmd02