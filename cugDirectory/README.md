# 📡 CUG Directory — IRCTC Internal Staff Lookup

> A secure, internal web application for searching and managing Corporate Unified Group (CUG) mobile number records for IRCTC railway employees.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/Dev%20DB-MySQL-blue?style=flat-square&logo=mysql)
![PostgreSQL](https://img.shields.io/badge/Prod%20DB-PostgreSQL-316192?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker)
![License](https://img.shields.io/badge/License-Internal%20Use-lightgrey?style=flat-square)

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Screenshots](#-screenshots)
- [Getting Started](#-getting-started)
- [Environment Variables](#-environment-variables)
- [Deployment on Render](#-deployment-on-render)
- [API Reference](#-api-reference)
- [Project Structure](#-project-structure)
- [Security](#-security)

---

## 🔍 About

**CUG Directory** is an internal staff directory system that allows authorized IRCTC personnel to:

- Look up CUG mobile numbers by employee name or CUG number
- View full employee details (designation, department, location, entitlement)
- Admin panel with full CRUD — edit records, add new entries, bulk-import from Excel
- Complete audit trail — every search and admin action is logged with timestamp and username

---

## ✨ Features

### 👤 User Features
- 🔒 **Secure login** — session-based authentication, no data visible without login
- 🔎 **Button-triggered search** — results only appear after explicit search (security by design)
- 📋 **Detailed record view** — full employee info in a clean slide-out panel
- 🔑 **Change password** — self-service password management

### 🛡️ Admin Features
- 📊 **Dashboard** — total records, active users, recent activity log
- ✏️ **Edit records** — search any CUG record and edit any field via modal
- ➕ **Add single record** — form to add one employee at a time
- 📂 **Bulk Excel import** — upload `.xlsx` file, shows inserted/updated/skipped count
- 👥 **User management** — create users, enable/disable accounts
- 📜 **Audit log** — every action logged: logins, searches, edits, imports

### ⚙️ Technical
- 📱 **Fully responsive** — mobile-first design with animated hamburger menu
- 🏓 **Keep-alive endpoint** — `/api/ping` prevents Render free-tier spin-down
- 🌐 **Multi-environment config** — separate dev (MySQL) and prod (PostgreSQL) profiles
- 🔁 **Zero startup re-import** — data is persisted in DB; no Excel reload on every restart
- 🐳 **Docker ready** — production Dockerfile included

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6 |
| ORM | Spring Data JPA / Hibernate |
| Templates | Thymeleaf |
| Dev Database | MySQL 8 |
| Prod Database | PostgreSQL |
| Build | Maven |
| Deployment | Docker + Render |
| Frontend | Vanilla HTML / CSS / JS (no frameworks) |

---

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8 running locally

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/cug-directory.git
cd cug-directory
```

### 2. Create the MySQL database

```sql
CREATE DATABASE cug_directory CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure environment variables

Copy the dev template and fill in your local values:

```bash
# The app reads these at startup — set them in your shell or IDE run config
set DB_URL=jdbc:mysql://localhost:3306/cug_directory?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
set DB_USERNAME=root
set DB_PASSWORD=your_mysql_password
set ADMIN_USERNAME=admin
set ADMIN_PASSWORD=changeme123
```

> 💡 Or edit `.env.dev` and load it into your shell before running.

### 4. Place your Excel data file

Put your `cug-source.xlsx` in the `data/` folder at the project root.
The file must have a sheet named **`CUG_Records`** with these columns:

| Column | Required |
|---|---|
| CUG Number | ✅ |
| Full Name | ✅ |
| Designation | — |
| Department | — |
| Employee Code | — |
| Grade | — |
| Location | — |
| Office / Unit | — |
| Status | — |
| Entitlement (Rs) | — |
| Roaming Plan | — |

> After the first run, data is stored in MySQL. You can manage records via the Admin panel.

### 5. Run the application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Visit: **http://localhost:8080**

Default admin login:
- **Username:** `admin`
- **Password:** `changeme123` ← change this immediately in the Admin panel

---

## 🔐 Environment Variables

### Dev profile (MySQL — local)

| Variable | Description | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active profile | `dev` |
| `PORT` | Server port | `8080` |
| `DB_URL` | JDBC connection URL | Local MySQL |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | — |
| `ADMIN_USERNAME` | Admin panel username | `admin` |
| `ADMIN_PASSWORD` | Admin panel password | `changeme123` |

### Prod profile (PostgreSQL — Render)

> ⚠️ **All prod variables must be set as secret environment variables in the Render dashboard. Never hardcode them.**

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Must be `prod` |
| `PORT` | `10000` (Render's default) |
| `DB_URL` | `jdbc:postgresql://host:5432/cug_directory` |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password 🔒 |
| `ADMIN_USERNAME` | Admin login username |
| `ADMIN_PASSWORD` | Admin login password 🔒 |

---

## ☁️ Deployment on Render

### Option A — Docker (recommended)

1. Push your code to GitHub
2. Go to [render.com](https://render.com) → New → Web Service → Connect your repo
3. Select **Docker** as runtime (Dockerfile is included)
4. Set all environment variables listed above as **Secret Variables**
5. Add a **Free PostgreSQL** database on Render and copy its Internal URL to `DB_URL`
6. Deploy!

### Keep-alive (prevent free-tier spin-down)

Render's free tier sleeps after 15 minutes of inactivity. Prevent this:

1. Sign up for **[UptimeRobot](https://uptimerobot.com)** (free)
2. Add a new monitor:
   - **Monitor Type:** HTTP(s)
   - **URL:** `https://your-app.onrender.com/api/ping`
   - **Interval:** Every 5 minutes
3. Done — your app stays awake 24/7

The `/api/ping` endpoint returns:
```json
{
  "status": "UP",
  "service": "CUG Directory",
  "time": "2026-07-11T10:00:00Z",
  "uptime": "3600s"
}
```

### Option B — render.yaml (blueprint)

The `render.yaml` is included for one-click deploy. Fill in secret values in the Render dashboard after connecting.

---

## 📡 API Reference

All API endpoints (except public ones) require a valid session cookie.

### Public Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/ping` | Health check / keep-alive |
| `POST` | `/api/auth/login` | Login with username + password |
| `POST` | `/api/auth/logout` | Logout, destroy session |
| `GET` | `/api/auth/me` | Get current logged-in user info |

### User Endpoints (requires login)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/cug/search?q=` | Search CUG records |
| `POST` | `/api/cug/log-search` | Log a deliberate search action |
| `POST` | `/api/auth/change-password` | Change own password |

### Admin Endpoints (requires ADMIN role)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/admin/stats` | Dashboard stats |
| `GET` | `/api/admin/logs` | Activity audit log |
| `GET` | `/api/admin/users` | List all users |
| `POST` | `/api/admin/users` | Create new user |
| `PUT` | `/api/admin/users/{id}/toggle` | Enable / disable user |
| `GET` | `/api/admin/records/search?q=` | Search records (admin) |
| `PUT` | `/api/admin/records/{id}` | Edit a record |
| `POST` | `/api/admin/records` | Add a single record |
| `POST` | `/api/admin/data/import` | Bulk import from Excel |

---

## 📁 Project Structure

```
cugDirectory/
├── src/
│   └── main/
│       ├── java/com/irctc/cugDirectory/
│       │   ├── config/
│       │   │   ├── SecurityConfig.java        # Spring Security rules
│       │   │   └── AdminAccountSeeder.java    # Seeds default admin on first run
│       │   ├── controller/
│       │   │   ├── AuthController.java        # Login / logout / me
│       │   │   ├── CugController.java         # Search + search logging
│       │   │   ├── AdminController.java       # All admin CRUD endpoints
│       │   │   └── PingController.java        # Keep-alive endpoint
│       │   ├── model/
│       │   │   ├── CugRecord.java             # Employee/CUG entity
│       │   │   ├── User.java                  # App user entity
│       │   │   └── AccessLog.java             # Audit log entity
│       │   ├── repository/                    # Spring Data JPA repos
│       │   ├── service/
│       │   │   ├── ExcelImportService.java    # Excel upsert logic
│       │   │   └── AccessLogService.java      # Audit logging
│       │   └── loader/
│       │       └── ExcelDataLoader.java       # Disabled — data already in DB
│       └── resources/
│           ├── application.properties          # Shared base config
│           ├── application-dev.properties      # Dev overrides (MySQL)
│           ├── application-prod.properties     # Prod overrides (PostgreSQL)
│           ├── static/
│           │   ├── style.css                  # Global responsive stylesheet
│           │   └── auth-helper.js             # Shared auth utility
│           └── templates/                     # Thymeleaf HTML pages
│               ├── login.html
│               ├── search.html
│               ├── admin.html
│               ├── admin_users.html
│               ├── admin_records.html
│               └── change_password.html
├── data/
│   └── cug-source.xlsx                        # Source Excel file (gitignored)
├── Dockerfile                                 # Production Docker image
├── render.yaml                                # Render deploy blueprint
├── .env.dev                                   # Dev env vars (gitignored)
├── .env.prod                                  # Prod env template (gitignored)
├── .gitignore
└── pom.xml
```

---

## 🔒 Security

- **Session-based auth** — no JWT tokens; secure server-side sessions via Spring Security
- **No data on load** — search results only appear after explicit button click (never on page load)
- **Role-based access** — `USER` can only search; `ADMIN` can edit, add, import, manage users
- **Audit trail** — every login, search, edit, and import is logged with timestamp and IP-friendly username
- **No secrets in code** — all credentials come from environment variables; `render.yaml` has zero hardcoded values
- **CSRF disabled** (justified) — same-origin SPA served from the same domain; session cookie is `HttpOnly` + `SameSite=Strict` in production
- **Password hashing** — BCrypt via Spring Security's `DelegatingPasswordEncoder`

---

## 📄 License

Internal use only — IRCTC / Indian Railways.  
Not for public distribution.

---

<div align="center">
  Built with ❤️ for IRCTC Internal Operations
</div>
