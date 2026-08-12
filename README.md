# Smart Lending & Deposit System (SLDS) - Microservices Architecture

Dự án Hệ thống Quản lý Vay & Tiết kiệm Thông minh (**SLDS**) được xây dựng trên kiến trúc **5 Microservices** độc lập, phục vụ các nghiệp vụ Ngân hàng cốt lõi.

---

## 📐 Kiến trúc Hệ thống (System Architecture Diagram)

```text
                               ┌───────────────────────────┐
                               │   Client (Web / Mobile)   │
                               └─────────────┬─────────────┘
                                             │ HTTP / REST
                                             ▼
                        ┌───────────────────────────────────────────┐
                        │  1. API Gateway & Auth Service (8080)     │
                        │  (Spring Cloud Gateway, Security, JWT)    │
                        └──────┬──────────────┬─────────────┬───────┘
                               │              │             │
              ┌────────────────┘              │             └────────────────┐
              ▼                               ▼                              ▼
┌───────────────────────────┐   ┌───────────────────────────┐   ┌───────────────────────────┐
│  2. Loan Service (8081)   │   │ 3. Deposit Service (8082) │   │ 4. Credit Assessment(8083)│
│  (Vay, Lãi suất, Giải ngân)│   │ (Tiết kiệm, Tất toán)     │   │ (Thẩm định async, Scoring)│
└─────────────┬─────────────┘   └─────────────┬─────────────┘   └─────────────┬─────────────┘
              │                               │                               │
              │ Publish Event                 │                               │ Consume Event
              ▼                               │                               ▼
┌───────────────────────────┐                 │                 ┌───────────────────────────┐
│ ActiveMQ Broker (61616)   │─────────────────┼────────────────►│ Elasticsearch Log (9200)  │
└───────────────────────────┘                 │                 └───────────────────────────┘
              │                               │
              └───────────────┬───────────────┘
                              │
                              ▼
           ┌─────────────────────────────────────┐
           │ 5. Core Banking Adapter (8084)      │
           │ (Hạch toán Nợ/Có, Circuit Breaker)  │
           └──────────────────┬──────────────────┘
                              │
                              ▼
           ┌─────────────────────────────────────┐
           │     Core Banking (Oracle DB)        │
           └─────────────────────────────────────┘
```

---

## 🛠️ Công nghệ Sử dụng (Tech Stack)

| Thành phần | Công nghệ / Framework | Vai trò / Chi tiết |
| :--- | :--- | :--- |
| **Backend Framework** | Java 17, Spring Boot 3.2, Spring Cloud | Nền tảng phát triển Microservices |
| **API Gateway & Auth** | Spring Cloud Gateway, Spring Security, JWT | Authentication/Authorization, Rate Limiting, Dynamic Routing |
| **Databases** | PostgreSQL 15, Oracle DB 23c | Lưu trữ hồ sơ vay, sổ tiết kiệm & hạch toán Core Banking |
| **Cache & Session Store**| Redis 7 | Lưu Session/Token JWT, Cache danh mục sản phẩm & lãi suất |
| **Message Broker** | Apache ActiveMQ 5.15 | Xử lý sự kiện bất đồng bộ (Event-driven Architecture) |
| **Search Engine** | Elasticsearch 8.12 | Tra cứu siêu tốc log lịch sử thẩm định & tín dụng |
| **Fault Tolerance** | Resilience4j | Circuit Breaker & Retry bảo vệ kết nối Core Banking |
| **Containerization** | Docker, Docker Compose | Đóng gói và điều phối toàn bộ 11 containers |

---

## 🚀 Chi tiết 5 Microservices

### 1. API Gateway & Auth Service (`slds-gateway-auth-service` - Port 8080)
* **Trách nhiệm**:
  * Cửa ngõ tiếp nhận duy nhất cho toàn bộ request từ Client.
  * Authentication / Authorization bằng JWT & OAuth2, phân quyền RBAC.
  * Rate Limiting (Redis-based) chống DDoS và Dynamic Routing.
* **Tech Stack**: Spring Cloud Gateway, Spring Security, Redis, JWT.

### 2. Loan Service (`slds-loan-service` - Port 8081)
* **Trách nhiệm**:
  * Quản lý vòng đời khoản vay: Tạo hồ sơ vay, tính lịch trả nợ (**Strategy Pattern** - Dư nợ giảm dần / Lãi phẳng), giải ngân, thu nợ.
  * Phát sự kiện (Publish Event) sang **ActiveMQ** (`loan.application.assessment`) khi có hồ sơ mới.
* **Tech Stack**: Spring Boot, PostgreSQL, Redis, ActiveMQ Producer.

### 3. Deposit Service (`slds-deposit-service` - Port 8082)
* **Trách nhiệm**:
  * Mở & tất toán sổ tiết kiệm (đúng hạn hoặc trước hạn áp dụng lãi không kỳ hạn).
  * Quản lý danh mục sản phẩm tiền gửi trực tuyến, cache lãi suất trên Redis.
* **Tech Stack**: Spring Boot, PostgreSQL, Redis Cache.

### 4. Credit Assessment Service (`slds-credit-assessment-service` - Port 8083)
* **Trách nhiệm**:
  * **ActiveMQ Consumer** lắng nghe event hồ sơ vay tự động xử lý async background.
  * Tự động tính điểm tín dụng (Credit Score 300 - 850), tra cứu nhóm nợ xấu CIC (Nhóm 1 - 5).
  * Đẩy audit log và tra cứu lịch sử tín dụng siêu tốc qua **Elasticsearch**.
* **Tech Stack**: Spring Boot, ActiveMQ Consumer, Elasticsearch.

### 5. Core Banking Adapter Service (`slds-core-banking-adapter-service` - Port 8084)
* **Trách nhiệm**:
  * Middleware Layer giao tiếp với Core Banking (Oracle DB).
  * Thực hiện hạch toán ghi Nợ/Có (Journal Posting) khi giải ngân hoặc thu nợ / gửi tiết kiệm.
  * Bảo vệ hệ thống bằng **Resilience4j Circuit Breaker**: Tự kích hoạt Fallback khi Core Banking quá tải/timeout.
* **Tech Stack**: Spring Boot, Oracle DB, Resilience4j (Circuit Breaker & Retry).

---

## 💻 Hướng dẫn Khởi chạy (Quick Start)

### Khởi chạy bằng Docker Compose
```bash
# Khởi chạy toàn bộ 11 Containers (5 Microservices + 5 Infrastructure + 1 Web App)
docker-compose up -d --build

# Kiểm tra trạng thái các service
docker-compose ps
```

### Thử nghiệm REST APIs với Postman
Import file **`SLDS_Postman_Collection.json`** ở thư mục gốc vào Postman để gọi thử đầy đủ API của cả 5 Services.

---

## 📂 Cấu trúc Thư mục Dự án (Project Structure)

```text
slds-microservices/
├── pom.xml                                 # Parent POM quản lý 5 modules
├── docker-compose.yml                      # Điều phối 11 Docker containers
├── SLDS_Postman_Collection.json            # Postman collection full API
├── README.md                               # Tài liệu dự án & Sơ đồ kiến trúc
├── slds-gateway-auth-service/              # Service 1: API Gateway & Auth
├── slds-loan-service/                      # Service 2: Loan Microservice
├── slds-deposit-service/                   # Service 3: Deposit Microservice
├── slds-credit-assessment-service/         # Service 4: Credit Assessment
└── slds-core-banking-adapter-service/      # Service 5: Core Banking Adapter
```
