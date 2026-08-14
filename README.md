<div >

# StockBase — Full Stack Inventory Management System
</div>

![CI](https://github.com/surya-prabhav-gurram/stockbase/actions/workflows/ci.yml/badge.svg)

---

## Live Application

Frontend:

```text
https://fanciful-narwhal-46443d.netlify.app/
```

Backend API:

```text
https://stockbase-rpe7.onrender.com
```

Note: The backend is deployed on Render free tier. If inactive, the first request may take 30 to 60 seconds while the service wakes up.

---

## Overview

StockBase is a production-grade full-stack inventory management system built with **React**, **Spring Boot**, and **PostgreSQL**.

The application supports secure user authentication, role-based access control, inventory tracking, product management, stock transaction auditing, low-stock alerts, supplier and category management, reporting dashboards, and CSV export functionality.

This project demonstrates a complete cloud-deployed full-stack architecture using:

```text
React Frontend       → Netlify
Spring Boot Backend  → Render Docker Deployment
PostgreSQL Database  → Neon Cloud Database
```

---

## Resume Bullet

> Built and deployed a full-stack Inventory Management System using React, Spring Boot, PostgreSQL, Netlify, Render, and Neon, featuring JWT authentication, role-based access control, inventory transaction tracking, low-stock alerts, supplier management, reporting dashboards, CSV export functionality, and RESTful API integration.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, React Router, Recharts, Axios |
| Backend | Spring Boot 3.2, Java 17 |
| Database | PostgreSQL hosted on Neon |
| Authentication | Spring Security, JWT |
| ORM | Spring Data JPA, Hibernate |
| Validation | Jakarta Bean Validation |
| Charts | Recharts |
| CSV Export | Apache Commons CSV |
| Backend Deployment | Render using Docker |
| Frontend Deployment | Netlify |
| Database Deployment | Neon PostgreSQL |

---

## Features

### Authentication & Authorization

- JWT-based login and registration
- Role-based access control
- ADMIN users can create, update, and delete records
- USER accounts can view inventory and record transactions
- Token stored in localStorage
- Axios interceptor attaches JWT token to API requests
- Auto-redirect to login on unauthorized requests
- Spring Security protected backend routes

---

### Product Management

- Full product CRUD operations
- Unique SKU validation
- Product name and SKU search
- Category-based filtering
- Supplier association
- Per-product reorder threshold
- Automatic inventory status calculation

Product status values:

```text
In Stock
Low Stock
Out of Stock
```

---

### Inventory Transactions

- Stock In
- Stock Out
- Manual Adjustment
- Quantity validation
- Prevents over-withdrawing when stock is insufficient
- Records transaction type, user, timestamp, and quantity changes
- Maintains full inventory audit history

---

### Reorder Alerts

- Detects products at or below reorder threshold
- Displays real-time low-stock alerts
- Provides suggested restock quantity
- Supports quick restock workflow

---

### Reports

- Inventory value by category
- Supplier-wise inventory report
- Low-stock report
- Interactive dashboard charts using Recharts
- CSV export for product inventory
- CSV export for low-stock products

---

## Demo Accounts

```text
Admin
Email: admin@stockbase.com
Password: admin123

User
Email: user@stockbase.com
Password: user123
```

---

## Data Model

```text
users           categories      suppliers
  ↑                 ↑               ↑
  └── inventory_transactions    products
                                   │
                              category_id
                              supplier_id
```

---

## System Architecture

```text
Client Browser
     │
     ▼
React Frontend
     │
     ▼
Axios API Layer
     │
     ▼
Spring Boot REST API
     │
     ▼
Spring Security + JWT
     │
     ▼
Spring Data JPA / Hibernate
     │
     ▼
Neon PostgreSQL Database
```

---

## Cloud Deployment Architecture

```text
Netlify
  └── Hosts React production build

Render
  └── Runs Spring Boot backend inside Docker container

Neon
  └── Hosts PostgreSQL production database
```

---

## REST API Reference

### Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/auth/me` | Authenticated |

---

### Products

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/products` | All authenticated users |
| GET | `/api/products/{id}` | All authenticated users |
| GET | `/api/products/low-stock` | All authenticated users |
| GET | `/api/products/search?q=` | All authenticated users |
| POST | `/api/products` | Admin |
| PUT | `/api/products/{id}` | Admin |
| DELETE | `/api/products/{id}` | Admin |

---

### Transactions

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/transactions` | All authenticated users |
| GET | `/api/transactions/recent?limit=20` | All authenticated users |
| GET | `/api/transactions/product/{id}` | All authenticated users |
| POST | `/api/transactions` | All authenticated users |

---

### Reports

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/reports/dashboard` | All authenticated users |
| GET | `/api/reports/inventory-by-category` | All authenticated users |
| GET | `/api/reports/inventory-by-supplier` | All authenticated users |
| GET | `/api/reports/low-stock` | All authenticated users |
| GET | `/api/reports/export/products.csv` | All authenticated users |
| GET | `/api/reports/export/low-stock.csv` | All authenticated users |

---

## Testing & Continuous Integration

The backend ships with an automated test suite covering the core business logic and security rules, run on every push and pull request via GitHub Actions (`.github/workflows/ci.yml`).

| Test | Type | What it proves |
|---|---|---|
| `TransactionServiceTest` | Unit (Mockito) | Stock-in/out/adjustment quantity math, insufficient-stock rejection, and the audit record (before/after quantities, performing user) |
| `ProductServiceTest` | Unit (Mockito) | SKU uniqueness, SKU normalisation, and not-found handling for products and referenced entities |
| `ReportServiceTest` | Unit (Mockito) | Dashboard aggregation (inventory value, stock-level counts) and CSV export shape |
| `ProductTest` | Unit | Stock-status boundaries that drive reorder alerts |
| `SerializationSecurityTest` | Unit | No password (hash) leaks in serialized `User` or `TransactionResponse` JSON |
| `ProductSecurityIntegrationTest` | Integration (`@SpringBootTest` + H2) | Real filter chain + `@PreAuthorize`: reads open to any authenticated user, writes admin-only (403 for non-admin), anonymous refused, health endpoint public |
| `StockTransactionIntegrationTest` | Integration (`@SpringBootTest` + H2) | The locked stock-movement path persists valid changes and rejects over-withdrawal, keeping stock ≥ 0 |
| `StockbaseApplicationTests` | Integration | The full application context starts cleanly |

Integration tests run against an in-memory H2 database (PostgreSQL compatibility mode), so no external database is needed.

Run the suite locally:

```bash
cd backend
mvn test
```

---

## Robustness & Production Concerns

- **No sensitive data over the wire.** Stock transactions are returned through a dedicated `TransactionResponse` DTO instead of the raw JPA entity, so the eagerly-loaded acting user's password hash can never be serialized to a client. The `User.password` field is additionally marked write-only as defense in depth.
- **Concurrency-safe stock movements.** `record()` fetches the product under a pessimistic row-level write lock (`SELECT … FOR UPDATE`), so two simultaneous stock-outs on the same product are serialized and can't both pass the availability check and oversell below zero.
- **Interactive API docs.** OpenAPI 3 with Swagger UI at `/swagger-ui.html`, including a Bearer-JWT "Authorize" flow so endpoints can be exercised from the browser.
- **Health & metrics.** Spring Boot Actuator exposes a public `/actuator/health` (used by the Render health check) and `/actuator/info`.
- **Pagination & sorting.** `GET /api/products/page?page=0&size=20&sort=name` for large catalogs, alongside the existing full-list endpoint.

---

## API Documentation & Health

| Path | Purpose | Access |
|---|---|---|
| `/swagger-ui.html` | Interactive OpenAPI docs | Public |
| `/v3/api-docs` | OpenAPI JSON spec | Public |
| `/actuator/health` | Liveness/health probe | Public |
| `/actuator/info` | Build/app info | Public |

---

## Quick Start

### Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 14 or higher
- Maven

---

### 1. Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/stockbase.git
cd stockbase
```

---

### 2. Database Setup

Create a local PostgreSQL database:

```sql
CREATE DATABASE stockbase;
```

---

### 3. Backend Setup

```bash
cd backend
mvn clean package -DskipTests
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

---

### 4. Frontend Setup

```bash
cd frontend
npm install
npm start
```

Frontend runs on:

```text
http://localhost:3000
```

---

## Environment Variables

For production deployment, configure the following variables in Render:

```env
DATABASE_URL=your_jdbc_postgresql_url
DATABASE_USERNAME=your_database_username
DATABASE_PASSWORD=your_database_password
JWT_SECRET=your_secure_jwt_secret
SPRING_PROFILES_ACTIVE=prod
CORS_ORIGINS=https://your-netlify-app.netlify.app
```

Important: Never commit real database credentials, passwords, or JWT secrets to GitHub.

---

## Docker Deployment

The backend is deployed on Render using Docker.

Example Dockerfile:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/stockbase-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Frontend Production API Configuration

The frontend communicates with the deployed backend through Axios.

Example:

```js
const api = axios.create({
  baseURL: 'https://stockbase-rpe7.onrender.com'
});
```

---

## Deployment Summary

| Component | Platform | Status |
|---|---|---|
| Frontend | Netlify | Deployed |
| Backend | Render | Deployed |
| Database | Neon PostgreSQL | Deployed |
| Authentication | JWT + Spring Security | Working |
| Seed Data | DataSeeder | Loaded |
| Reports | Recharts + REST API | Working |

---

## Project Structure

```text
stockbase/
├── Dockerfile
├── render.yaml
├── LICENSE
├── .gitignore
├── .github/
│   └── workflows/
│       └── ci.yml
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/stockbase/
│       │   ├── StockbaseApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── OpenApiConfig.java
│       │   │   └── DataSeeder.java
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── model/
│       │   ├── dto/
│       │   │   └── TransactionResponse.java
│       │   ├── security/
│       │   └── exception/
│       └── test/java/com/stockbase/
│           ├── StockbaseApplicationTests.java
│           ├── model/ProductTest.java
│           ├── service/
│           │   ├── TransactionServiceTest.java
│           │   ├── ProductServiceTest.java
│           │   ├── ReportServiceTest.java
│           │   └── StockTransactionIntegrationTest.java
│           └── security/
│               ├── ProductSecurityIntegrationTest.java
│               └── SerializationSecurityTest.java
│
└── frontend/
    ├── package.json
    ├── netlify.toml
    └── src/
        ├── api/
        │   └── index.js
        ├── context/
        │   └── AuthContext.js
        ├── components/
        │   ├── UI.js
        │   ├── Sidebar.js
        │   ├── ProductModal.js
        │   └── TransactionModal.js
        └── pages/
            ├── AuthPage.js
            ├── Dashboard.js
            ├── Products.js
            ├── Transactions.js
            ├── ReorderAlerts.js
            ├── Categories.js
            ├── Suppliers.js
            └── Reports.js
```

---

## Engineering Highlights

- Full-stack production deployment
- Dockerized Spring Boot backend
- Cloud PostgreSQL database integration
- JWT authentication and protected routes
- Role-based access control
- RESTful API architecture
- Inventory transaction auditing
- Real-time low-stock monitoring
- Analytics dashboard with Recharts
- CSV export functionality
- Responsive React frontend
- Cloud-ready environment variable configuration
- Automated unit + integration test suite (JUnit 5, Mockito, Spring Security Test, H2)
- Continuous integration via GitHub Actions
- Response DTOs preventing sensitive-data exposure
- Pessimistic locking for race-free stock updates
- OpenAPI/Swagger interactive documentation
- Actuator health and metrics endpoints
- Paginated and sortable list endpoints

---

## Future Improvements

```text
Docker Compose for local development
Flyway/Liquibase schema migrations (replacing ddl-auto=update in production)
Redis caching for reports
WebSocket-based real-time stock alerts
Barcode scanner support
Multi-warehouse inventory management
AI-based demand forecasting
Advanced sales and purchase order modules
Expanded controller-layer and end-to-end test coverage
```

---

## Author

**Surya Prabhav Gurram**  
Master’s in Computer Science  
University of Oklahoma

Focus areas:

```text
Full-Stack Development
Database Systems
Artificial Intelligence
Cloud Deployment
Backend Engineering
System Architecture
```

---

<div align="center">

<p>
  <img src="https://readme-typing-svg.herokuapp.com?font=Inter&weight=500&size=18&duration=3500&pause=1000&center=true&vCenter=true&width=800&lines=Built+with+React%2C+Spring+Boot%2C+PostgreSQL%2C+Docker%2C+Netlify%2C+Render%2C+and+Neon;Designed+as+a+production-grade+inventory+management+platform" alt="Footer Animation" />
</p>

</div>
