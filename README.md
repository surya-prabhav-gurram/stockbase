# StockBase — Full Stack Inventory Management System

A production-grade inventory management system built with **React**, **Spring Boot**, and **PostgreSQL**.

**Resume bullet:**
> Built a full-stack Inventory Management System using React, Spring Boot, and PostgreSQL with JWT authentication, role-based access control, inventory transaction tracking, low-stock alerts, supplier management, reporting dashboards, and RESTful API integration.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, React Router, Recharts, Axios |
| Backend | Spring Boot 3.2 (Java 17) |
| Database | PostgreSQL |
| Auth | Spring Security + JWT (jjwt) |
| ORM | Spring Data JPA / Hibernate |
| Validation | Jakarta Bean Validation |
| CSV Export | Apache Commons CSV |
| Deployment | Netlify (frontend) + Render/Railway (backend) + Neon/Supabase (DB) |

---

## Features

### Authentication & Authorization
- JWT-based login/register
- Role-based access: **ADMIN** can create/edit/delete; **USER** can view and record transactions
- Token stored in localStorage, attached to all API requests via Axios interceptor
- Auto-redirect to `/login` on 401

### Products
- Full CRUD with unique SKU validation
- Per-product reorder threshold
- Filter by category, status, search by name/SKU
- Automatic status: In Stock / Low Stock / Out of Stock

### Inventory Transactions
- Stock In, Stock Out, Manual Adjustment
- Full audit log: who, when, before/after quantities
- Prevents over-withdrawing (insufficient stock error)

### Reorder Alerts
- Real-time list of all products at/below threshold
- Suggested restock quantity
- One-click quick restock modal

### Reports (Admin only)
- Inventory value by category (bar chart)
- Supplier-wise inventory (bar chart + table)
- Low stock report
- Export to CSV: all products, low-stock items

### Data Model
```
users           categories      suppliers
  ↑                 ↑               ↑
  └── inventory_transactions    products ─┘
                                   │
                              (FK: category_id, supplier_id)
```

---

## Quick Start (Local Dev)

### Prerequisites
- Java 17+ (`java --version`)
- Node.js 18+ (`node --version`)
- PostgreSQL 14+ running locally

### 1. Database Setup
```sql
CREATE DATABASE stockbase;
```

### 2. Backend
```bash
cd backend
# Edit src/main/resources/application.properties
# Set: spring.datasource.username and spring.datasource.password

./mvnw spring-boot:run
# API runs on http://localhost:8080
# Seeds admin@stockbase.com / admin123 and user@stockbase.com / user123
```

### 3. Frontend
```bash
cd frontend
npm install
npm start
# Opens http://localhost:3000
```

---

## REST API Reference

### Auth
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/auth/me` | Authenticated |

### Products
| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/products` | All |
| GET | `/api/products/{id}` | All |
| GET | `/api/products/low-stock` | All |
| GET | `/api/products/search?q=` | All |
| POST | `/api/products` | Admin |
| PUT | `/api/products/{id}` | Admin |
| DELETE | `/api/products/{id}` | Admin |

### Transactions
| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/transactions` | All |
| GET | `/api/transactions/recent?limit=20` | All |
| GET | `/api/transactions/product/{id}` | All |
| POST | `/api/transactions` | All |

### Reports
| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/reports/dashboard` | All |
| GET | `/api/reports/inventory-by-category` | All |
| GET | `/api/reports/inventory-by-supplier` | All |
| GET | `/api/reports/low-stock` | All |
| GET | `/api/reports/export/products.csv` | All |
| GET | `/api/reports/export/low-stock.csv` | All |

---

## Deployment

### Frontend → Netlify
```bash
cd frontend
npm run build
# Drag the build/ folder to netlify.com/drop
# Or: npx netlify-cli deploy --prod --dir=build
```

### Backend → Render
1. Push backend to a GitHub repo
2. Create a new **Web Service** on render.com
3. Set environment variables:
   - `DATABASE_URL` = your Neon/Supabase PostgreSQL URL
   - `JWT_SECRET` = a 256-bit random string
   - `CORS_ORIGINS` = https://your-app.netlify.app
   - `SPRING_PROFILES_ACTIVE` = prod
4. Build command: `./mvnw clean package -DskipTests`
5. Start command: `java -jar target/stockbase-api-1.0.0.jar`

### Database → Neon (free PostgreSQL cloud)
1. Sign up at neon.tech (free tier)
2. Create a new project → copy the connection string
3. Set as `DATABASE_URL` on Render

### Update frontend API URL for production
In `frontend/src/api/index.js`, change:
```js
const api = axios.create({ baseURL: 'https://your-api.onrender.com' });
```

---

## Project Structure

```
stockbase/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/stockbase/
│       ├── StockbaseApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java     ← CORS, JWT filter chain, role rules
│       │   └── DataSeeder.java         ← Seeds demo data on first run
│       ├── controller/                 ← REST endpoints
│       ├── service/                    ← Business logic
│       ├── repository/                 ← JPA queries
│       ├── model/                      ← JPA entities
│       ├── security/                   ← JwtUtil, JwtFilter, UserDetailsService
│       └── exception/                  ← GlobalExceptionHandler
│
└── frontend/
    ├── package.json
    ├── netlify.toml
    └── src/
        ├── api/index.js               ← All Axios API calls
        ├── context/AuthContext.js     ← JWT auth state
        ├── components/
        │   ├── UI.js                  ← Shared icons, Toast, badges
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
