# Modern Fashion E-Commerce API 🛍️

*Read this in other languages: [English](README.md) | [Tiếng Việt](README-vi.md)*

A robust, scalable, and feature-rich RESTful API built specifically for a clothing and footwear e-commerce platform. Designed with a **Modular Monolith Architecture**, this system provides comprehensive core business logic covering everything from category and product management and secure user authentication to a safe order process with strict concurrency control.

## 🚀 Key Features

### 👤 Auth & User Module
* **JWT Authentication:** Secure Login/Register with Access Token.
* **Token Blacklisting:** Safely handle User Logout and clear expired tokens automatically using a scheduled Cron Job.
* **Email OTP Verification:** Anti-spam email OTP system for Account Activation and Forgot Password flows.
* **Role-Based Access Control (RBAC):** Distinct authorities for `ROLE_ADMIN` and `ROLE_CUSTOMER`.
* **Address Book:** Manage multiple shipping addresses with default address logic.

### 📦 Catalog & Product Module
* **Multi-level Variants (SKUs):** Robust matrix system to manage products by Size and Color combinations.
* **Soft Deletion:** Safely archive Categories and Products without violating data integrity (Foreign Key constraints).
* **Dynamic Search & Filtering:** Powerful JPA/Native queries to filter products by category, price range, and inventory status.
* **Cloud Media Integration:** Seamlessly upload, manage, and delete product galleries using **Cloudinary**.

### 🛒 Cart Module
* **Smart Cart Management:** Add items, update quantities, and auto-validate stock availability in real-time.
* **Mini Cart:** Optimized response for header mini-cart display.

### 🧾 Order Module
* **Order Lifecycle:** Track orders through stages: Pending -> Processing -> Shipping -> Completed / Cancelled / Returned.
* **Concurrency Control (Pessimistic Locking):** Prevent race conditions and "over-selling" during checkout by locking database rows (`LockModeType.PESSIMISTIC_WRITE`).
* **Data Integrity:** Strict validation between frontend expected price and backend actual database price to prevent data manipulation.
* **Inventory Rollback:** Automatically restore variant stock if an order is cancelled or returned.

## 🛠️ Tech Stack

* **Core:** Java 17, Spring Boot 4.0.3
* **Data Access:** Spring Data JPA, Hibernate
* **Database:** MySQL 8.0.34
* **Security:** Spring Security, JWT (JSON Web Token)
* **Documentation:** SpringDoc OpenAPI 3 (Swagger UI)
* **Third-Party Services:** Cloudinary (Image Hosting), JavaMailSender (SMTP Email)
* **Architecture:** Modular Monolith (Identity, Catalog, Cart, Order)

## 📖 API Documentation (Swagger UI)

The API is fully documented using OpenAPI 3 specifications. It includes detailed schemas, parameter descriptions, and functional test capabilities.

1. Run the application.
2. Navigate to: `http://localhost:8080/swagger-ui/index.html`

## ⚙️ Getting Started

### Prerequisites
* JDK 17
* Maven
* MySQL Server 8.x
* Cloudinary Account (Free tier)
* Gmail Account (for SMTP App Password)

### Installation

1.  **Clone the repository and move to the project directory:**
    ```bash
    cd Modern_Fashion_E-commerce_Platform
    ```

2.  **Environment Variables:**
    Create a `.env` file in the root directory based on the following template:
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
    ```

3.  **Build and Run:**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```

## 🗺️ Roadmap (Upcoming Features)

- [ ] **Online Payment Integration:** VNPAY gateway integration via Webhooks/IPN.
- [ ] **Promotion Engine:** Voucher management system (Percentage/Fixed discount, Usage limits).
- [ ] **Customer Interactions:** Product Ratings, Reviews, and Wishlist.
- [ ] **Admin Dashboard:** Revenue and Sales statistical charts.

## 👨‍💻 Author

**Dinh Trung Hieu**
* **Email:** hieudinhtrung1102@gmail.com
* **LinkedIn:** www.linkedin.com/in/hiếu-đinh-trung-73b4a5369
* **GitHub:** https://github.com/hiudtcmd02