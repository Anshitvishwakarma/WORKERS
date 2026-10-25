# FixMate Backend API Service

FixMate is a robust, production-grade Spring Boot 3 REST API microservice powering the FixMate home services booking platform. It features complete role-based security, stateless JWT authentication, MySQL/H2 database support, and OpenAPI 3 documentation.

---

## 1. Technical Stack

- **Java**: 21 / 24 LTS compatible (Compiled with JDK 24)
- **Framework**: Spring Boot 3.3.5
- **Security**: Spring Security 6 with JJWT 0.12.6 (stateless HMAC-SHA256)
- **Data Access**: Spring Data JPA / Hibernate ORM
- **Database**: MySQL 8.0 (default) with zero-config in-memory H2 profile fallback
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`, etc.)
- **API Documentation**: SpringDoc OpenAPI 2.6.0 (Swagger UI)
- **Testing**: JUnit 5, Spring Boot Test, MockMvc, H2 database (22 automated integration tests)

---

## 2. Directory Structure

```text
FixMate/backend/
├── pom.xml                                   # Maven dependencies & build configuration
├── src/
│   ├── main/
│   │   ├── java/com/fixmate/
│   │   │   ├── FixMateApplication.java       # Main Spring Boot application entrypoint
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java      # Auto-seeds default admin, services & demo accounts
│   │   │   │   └── OpenApiConfig.java        # Swagger/OpenAPI JWT security scheme configuration
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java      # Dashboard metrics, user moderation & service catalog
│   │   │   │   ├── AuthController.java       # Login, register & identity verification endpoints
│   │   │   │   ├── BookingController.java    # Customer & provider booking lifecycle workflows
│   │   │   │   ├── ProviderController.java   # Provider service offerings & public search
│   │   │   │   └── ServiceController.java    # Public service catalog & lookup endpoints
│   │   │   ├── dto/                          # Clean request & response Data Transfer Objects
│   │   │   ├── entity/                       # JPA entities with relationships & constraints
│   │   │   ├── enums/                        # Role and BookingStatus enumerations
│   │   │   ├── exception/                    # Global exception handler with standard RFC7807 error format
│   │   │   ├── mapper/                       # EntityMapper for safe entity-to-DTO conversion
│   │   │   ├── repository/                   # Spring Data JPA repositories with custom queries
│   │   │   ├── security/                     # JwtUtil, JwtFilter, CustomUserDetailsService & SecurityConfig
│   │   │   └── service/                      # Business interfaces and transactional implementations
│   │   └── resources/
│   │       ├── application.properties        # Main configuration (MySQL datasource & JWT settings)
│   │       ├── application-h2.properties     # Standalone in-memory profile configuration
│   │       ├── application-test.properties   # Integration test profile configuration
│   │       └── data.sql                      # Reference SQL seed data
│   └── test/
│       └── java/com/fixmate/                 # 22 Comprehensive MockMvc integration tests
└── README.md
```

---

## 3. Database & Profiles

### Profile 1: MySQL 8 (Default)
To run against your local MySQL 8 instance:
1. Ensure MySQL is running on port 3306.
2. Create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS fixmate_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Set your environment variables (or adjust `src/main/resources/application.properties`):
   ```bash
   # Windows PowerShell
   $env:DB_URL="jdbc:mysql://localhost:3306/fixmate_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   $env:DB_USERNAME="root"
   $env:DB_PASSWORD="your_mysql_password"
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Profile 2: H2 In-Memory (Zero Configuration Demo)
For immediate standalone evaluation without a pre-configured MySQL server:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
Or when running the packaged JAR:
```bash
java -jar target/fixmate-backend-1.0.0.jar --spring.profiles.active=h2
```
H2 Web Console is available at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:fixmate_db`
- Username: `sa`
- Password: *(leave blank)*

---

## 4. Default Seed Credentials

Upon first startup, `DataInitializer.java` automatically initializes demo data:

| Role | Email | Password | Full Name / Description |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin@fixmate.com` | `AdminPassword123` | System Administrator |
| **CUSTOMER** | `customer@fixmate.com` | `Password123` | John Doe |
| **PROVIDER** | `plumber@fixmate.com` | `Password123` | Robert Davis (Ace Plumbing & Drainage) |
| **PROVIDER** | `electric@fixmate.com` | `Password123` | Sarah Jenkins (VoltMaster Electrical) |

*Note: All passwords are hashed using BCrypt. Passwords are never stored in plaintext.*

---

## 5. Security Architecture

- **Stateless Sessions**: `SessionCreationPolicy.STATELESS` ensures zero HTTP session memory overhead.
- **HMAC-SHA256 JWT**: JJWT 0.12.6 verifies cryptographic digital signatures and expiration.
- **Role Authority Matching**: Authorities are mapped with `ROLE_` prefix (`ROLE_CUSTOMER`, `ROLE_PROVIDER`, `ROLE_ADMIN`).
- **Endpoint Security**:
  - `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/services/**`, `GET /api/providers/**` are **PUBLIC**.
  - `POST /api/bookings`, `GET /api/bookings/my`, `PATCH /api/bookings/{id}/cancel` require **CUSTOMER**.
  - `GET /api/provider/bookings`, `PATCH /api/provider/bookings/{id}/*`, `POST /api/providers/me/services` require **PROVIDER**.
  - `/api/admin/**` endpoints strictly require **ADMIN**.
  - Cross-tenant authorization prevents users from reading or altering other users' private bookings.
- **Error Responses**: Unauthorized (401) and Forbidden (403) requests return structured JSON via `RestAuthenticationEntryPoint` and `RestAccessDeniedHandler`.

---

## 6. Building and Running Automated Tests

Run the complete test suite:
```bash
mvn clean test
```

### Test Suite Summary (22 Tests, 0 Failures):
- `AuthIntegrationTest`: Register customer, duplicate email conflict (409), login validation, incorrect password failure (401).
- `SecurityAndRoleAuthorizationTest`: Unauthenticated request rejection (401), customer denied admin endpoint (403), provider denied customer bookings (403).
- `ServiceAndProviderIntegrationTest`: Public catalog retrieval, provider offerings search, provider adding/updating service offering.
- `BookingWorkflowIntegrationTest`: Complete booking lifecycle (Create -> Provider Accept -> Provider Complete), status transitions, cancellation rules.
- `AdminIntegrationTest`: KPI metrics calculation, user deactivation/activation, service creation, admin booking overview.

---

## 7. Swagger / OpenAPI Documentation

With the backend running, access the interactive OpenAPI documentation:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

To test protected endpoints in Swagger UI:
1. Click **Authorize** (lock icon).
2. Enter your JWT token in the format: `Bearer <YOUR_TOKEN>`.
3. Execute endpoints directly from the browser.
