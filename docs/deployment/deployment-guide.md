# CAREX Deployment Guide

This guide outlines deployment options for the **CAREX Intelligent Doctor Appointment & Patient Flow Management System**.

---

## 1. Local Containerized Deployment (Docker Compose)

### Prerequisites
- Docker Engine $\ge$ 24.0
- Docker Compose $\ge$ 2.20

### Quickstart
1. Clone repository and navigate to root:
   ```bash
   cd CAREX
   ```
2. Copy environment template:
   ```bash
   cp .env.example .env
   ```
3. Start all services in detached mode:
   ```bash
   docker compose up --build -d
   ```
4. Access services:
   - **Frontend Web UI:** `http://localhost:5173` (or `http://localhost:80`)
   - **Backend REST API:** `http://localhost:8080/api`
   - **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
   - **Actuator Health Check:** `http://localhost:8080/actuator/health`

5. Verify service health:
   ```bash
   docker compose ps
   ```

6. Stop services:
   ```bash
   docker compose down
   ```

---

## 2. Cloud Deployment (PaaS: Render / Railway / Fly.io)

### Backend (Web Service / Container)
- **Runtime:** Docker (`deployment/Dockerfile.backend`)
- **Port:** `8080`
- **Environment Variables:**
  - `DB_HOST`: `<managed-postgres-host>`
  - `DB_PORT`: `5432`
  - `DB_NAME`: `carex`
  - `DB_USERNAME`: `<db-user>`
  - `DB_PASSWORD`: `<db-password>`
  - `JWT_SECRET`: `<secure-256bit-secret-key>`
  - `CAREX_EMAIL_ENABLED`: `true` (or `false` for staging)
  - `CAREX_CORS_ALLOWED_ORIGINS`: `https://your-frontend-domain.app`
- **Health Check Path:** `/actuator/health`

### Frontend (Static Site / Vercel / Netlify)
- **Build Command:** `npm run build`
- **Output Directory:** `dist`
- **Environment Variable:**
  - `VITE_API_BASE_URL`: `https://your-backend-domain.app/api`

---

## 3. Production Best Practices & Security Checklist

1. **Database:**
   - Use managed PostgreSQL 17 with automated daily backups.
   - Use SSL connections (`sslmode=require` in JDBC URL).
2. **Secrets Management:**
   - Store `JWT_SECRET`, `DB_PASSWORD`, and `MAIL_PASSWORD` in secret managers (AWS Secrets Manager, GCP Secret Manager, or platform environment vaults).
   - Never commit plain `.env` files.
3. **CORS:**
   - Restrict `CAREX_CORS_ALLOWED_ORIGINS` to the exact production domain.
4. **Rate Limiting:**
   - In-memory rate limiting protects `/api/auth/login` and `/api/auth/register`. For multi-instance distributed deployments, a reverse-proxy (e.g. Cloudflare / AWS WAF / Nginx) can be layered in front.
