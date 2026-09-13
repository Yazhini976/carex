# CAREX — Intelligent Doctor Appointment & Patient Flow Management System

**Tagline:** *Book smarter. Wait less. Manage better.*

CAREX is an enterprise-grade healthcare technology platform engineered for the intelligent orchestration of clinical appointments, dynamic patient flow, and provider capacity management. 

CAREX seamlessly combines **Spring Boot 3.3.3**, **React 18**, **PostgreSQL 17**, **Stateless JWT Security with Role-Based Access Control**, a deterministic **Intelligence Engine**, a non-blocking **Notification & 30-Min Reminder Engine**, and **Multi-Stage Docker Containerization**.

---

## ⚡ Hackathon Demo Quickstart (For Judges)

### 1. Launch the Full Platform
```bash
# Clone and launch all containers
docker compose up --build -d
```

### 2. Live Access URLs
- **Frontend Portal:** [http://localhost:80](http://localhost:80) (or `http://localhost:5173` in dev mode)
- **Backend REST API:** [http://localhost:8080/api](http://localhost:8080/api)
- **Swagger / OpenAPI 3.0 Documentation:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **System & Database Health Probe:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### 3. Pre-Seeded Deterministic Demo Accounts
All accounts use password: `Password123!` (or 1-click fill on the login screen):

| Role | Email | Password | Recommended Demo Screen |
| :--- | :--- | :--- | :--- |
| **PATIENT** | `alice@carex.com` | `Password123!` | Smart Consultation, Doctor Matching, Booking, Waitlist |
| **DOCTOR** | `priya@carex.com` | `Password123!` | Doctor Dashboard, Live Queue Token Flow, Availability |
| **ADMIN** | `admin@carex.com` | `Password123!` | CAREX Command Center, What-If Simulator, Workload |

---

## 🎬 5–7 Minute Judge Demo Flow (Step-by-Step)

### Scene 1 — Patient Smart Consultation
1. Log in as **Patient** (`alice@carex.com` / `Password123!`).
2. Click **"Find the Right Doctor"** or navigate to **Smart Consultation**.
3. Inquire: *"I have an itchy red skin rash on my forearm that gets worse in warm weather."*
4. Click **GET RECOMMENDATION**.
5. Observe **CAREX SUGGESTION**:
   - Suggested Specialty: **Dermatology**
   - Confidence: **88%**
   - Clinical Rationale: Transparent symptom mapping.
   - Non-diagnostic clinical safety disclaimer clearly displayed.

### Scene 2 — Explainable Doctor Match
1. Click **VIEW MATCHED DOCTORS**.
2. Examine ranked doctor cards with **CAREX MATCH Progress Bar (92%)**:
   - ✓ Specialty match
   - ✓ Requested mode available
   - ✓ Available today
   - ✓ Low estimated wait (~12 min)

### Scene 3 — Seamless 5-Step Booking
1. Click **Book Appointment** on Dr. Priya's card.
2. Step indicator: `1. Specialty` $\rightarrow$ `2. Doctor` $\rightarrow$ `3. Mode` $\rightarrow$ `4. Slot` $\rightarrow$ `5. Confirm`.
3. Pick a slot and click **Confirm Appointment Booking**.
4. Confirmation shows `CX-1024`, Estimated wait `12–18 mins`, and arrival guidance.

### Scene 4 — Smart Waitlist Recovery
1. Navigate to **Smart Waitlist**.
2. Submit a preferred date/mode when slots are tight.
3. Show automated matching slot notification card: `🎉 MATCHING SLOT FOUND: Dr. Priya • 5:00 PM • ONLINE [ ACCEPT ] [ DECLINE ]`.

### Scene 5 — Doctor Live Queue Experience
1. Switch to **Doctor** (`priya@carex.com` / `Password123!`).
2. Open **Live Queue**:
   - `CURRENT PATIENT: Patient #18`
   - `NEXT: Patient #19`
   - `UPCOMING: Patient #20, Patient #21`
3. Click **Complete & Call Next** to advance queue velocity.

### Scene 6 — Admin Command Center & What-If Simulator
1. Switch to **Admin** (`admin@carex.com` / `Password123!`).
2. View **CAREX COMMAND CENTER**:
   - Top metrics: Patients, Doctors, Today's Appointments, Completion Rate, Estimated Revenue.
   - Notification & Email Delivery Health widget ($100\%$ delivery rate).
   - Workload alert: *High workload detected on Dr. Arun (5 PM – 7 PM)*.
3. Navigate to **Schedule Simulator**:
   - Scenario: `Dr. Arun Unavailable (Emergency Leave)`.
   - Click **RUN SIMULATION**.
   - Output: `14 appointments affected`, `Impact: HIGH`, `Wait increase: +16 min`.
   - Redistribution: `Dr. Priya (4)`, `Dr. Karthik (3)`.
   - Prominent badge: **NO CHANGES HAVE BEEN APPLIED (Read-Only Simulation)**.
   - Disruption Recovery table mapping alternative appointments.

### Scene 7 — Enterprise Security & RBAC Enforcement
1. Demonstrate API security by making a curl/Postman call to an Admin endpoint (`GET /api/analytics/daily`) using a **Patient's JWT token**.
2. Result: **`403 Forbidden`** (Granular method-level security and ownership enforcement).

---

## 🌟 Top 7 Key Innovations & Differentiators

| Innovation | Description |
| :--- | :--- |
| **1. Explainable Doctor Matching** | Multi-factor weighted score (Specialty 40%, Mode 20%, Availability 20%, Wait Time 10%, Experience 10%) with transparent reason tags. |
| **2. Predictive Patient Flow** | Dynamic queue wait-time estimation based on real-time provider consultation velocity. |
| **3. Smart Waitlist Recovery** | Automated priority queue that instantly offers cancelled openings to waiting patients. |
| **4. Workload Intelligence** | 0–100 provider utilization index identifying burnout risks and load imbalances. |
| **5. What-If Schedule Simulator** | Mathematical capacity disruption sandbox predicting impact before making changes. |
| **6. Disruption Recovery Engine** | Automated rebalancing suggestions across parallel specialists during doctor absence. |
| **7. Enterprise Security Architecture** | Stateless JWT authentication, role guards, object ownership verification, and rate limiting. |

---

## 🛠️ Automated Testing & Build Validation

### Backend Test Suite (80/80 Passing Tests)
```bash
cd backend
mvn clean verify
```
- `EndToEndSmokeTest.java`: Full workflow validation (Register $\rightarrow$ Login $\rightarrow$ Recommend $\rightarrow$ Match $\rightarrow$ Lock Slot $\rightarrow$ Book $\rightarrow$ Notify $\rightarrow$ Simulate).
- `AppointmentServiceTest.java`: Concurrency locks, HCL mode constraints, lifecycle state machine.
- `IntelligenceServiceTest.java`: 12 tests across all deterministic intelligence models.
- `SecurityAuthorizationTest.java`: Role guards and token validation.
- `NotificationTemplateServiceTest.java` & `ReminderSchedulerTest.java`: Safe SMTP retry & duplicate-proof reminders.

### Frontend Test & Production Build
```bash
cd frontend
npm test
npm run build
```

---

## 📁 Architecture & Innovation Docs
- Detailed Architecture Document: [`docs/architecture/carex-architecture.md`](file:///c:/Users/ASUS/OneDrive/Pictures/Desktop/CAREX_Project_Skeleton/CAREX/docs/architecture/carex-architecture.md)
- Innovation & Judge Summary: [`docs/demo/innovation-summary.md`](file:///c:/Users/ASUS/OneDrive/Pictures/Desktop/CAREX_Project_Skeleton/CAREX/docs/demo/innovation-summary.md)
- Postman API Collection: [`tests/postman/CAREX-API.postman_collection.json`](file:///c:/Users/ASUS/OneDrive/Pictures/Desktop/CAREX_Project_Skeleton/CAREX/tests/postman/CAREX-API.postman_collection.json)
