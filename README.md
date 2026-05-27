<div >

# StockBase — Full Stack Inventory Management System
</div>

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
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/stockbase/
│       ├── StockbaseApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   └── DataSeeder.java
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       ├── security/
│       └── exception/
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

---

## Future Improvements

```text
Docker Compose for local development
CI/CD pipeline with GitHub Actions
Redis caching for reports
WebSocket-based real-time stock alerts
Barcode scanner support
Multi-warehouse inventory management
AI-based demand forecasting
Advanced sales and purchase order modules
Unit and integration test coverage
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
