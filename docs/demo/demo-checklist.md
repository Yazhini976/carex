# CAREX — Live Hackathon Pre-Demo Verification Checklist

Run through this checklist 15 minutes before presenting to ensure 100% demonstration reliability.

---

## 1. Environment & Infrastructure Check

| Check Item | Command / URL | Expected Result | Status |
| :--- | :--- | :--- | :---: |
| **PostgreSQL 17 Database** | `localhost:5432` | Connected, `carex_db` accessible | [x] |
| **Spring Boot Backend** | `http://localhost:8080/actuator/health` | `{"status":"UP"}` | [x] |
| **Swagger / OpenAPI UI** | `http://localhost:8080/swagger-ui/index.html` | All REST endpoints visible and documented | [x] |
| **React 18 Frontend** | `http://localhost:5173` | Landing page rendered with green health indicators | [x] |
| **Automated Tests** | `mvn test` in `backend/` | **80 / 80 tests passing (0 failures)** | [x] |
| **Frontend Smoke Tests** | `npm test` in `frontend/` | **4 / 4 suites passing** | [x] |

---

## 2. Demo User Credentials Verification

All demo accounts use password: `Password123!`

- [x] **Patient**: `alice@carex.com` $\rightarrow$ Logs into Patient Dashboard
- [x] **Patient**: `bob@carex.com` $\rightarrow$ Logs into Waitlist candidate view
- [x] **Doctor**: `priya@carex.com` $\rightarrow$ Logs into Dermatology Queue & Timetable
- [x] **Doctor**: `arun@carex.com` $\rightarrow$ Logs into High-workload Cardiology view
- [x] **Doctor**: `karthik@carex.com` $\rightarrow$ Logs into General Medicine view
- [x] **Admin**: `admin@carex.com` $\rightarrow$ Logs into CAREX Command Center

---

## 3. End-to-End User Flow Verification

### Flow 1: Smart Consultation Navigation
- [x] Enter symptom: *"itchy skin rash and red patches on arm"*
- [x] Click **Get Recommendation**
- [x] Confirm **Dermatology (88% confidence)** suggestion with non-diagnostic disclaimer banner
- [x] Click **View Matched Doctors**

### Flow 2: Explainable Doctor Matching & Booking
- [x] Verify Dr. Priya Nair card displays **92% match score** with 4-point criteria breakdown
- [x] Click **Book Appointment**
- [x] Select Mode (`ONLINE`), Slot, and Confirm
- [x] Verify Confirmation Screen: **CX-XXXX** format, 12–18m estimated wait, and arrival guidance
- [x] Verify Patient Journey Timeline reflects status `BOOKED`

### Flow 3: Smart Waitlist & Offer
- [x] Switch to `bob@carex.com`
- [x] Open `/patient/waitlist`
- [x] Verify active waitlist entry and matching slot offer card (`[ ACCEPT ] [ DECLINE ]`)

### Flow 4: Admin Command Center & Intelligence
- [x] Switch to `admin@carex.com`
- [x] Open `/admin/dashboard` $\rightarrow$ Verify 5 top KPIs, Consultation Breakdown, and Workload snapshot
- [x] Open `/admin/workload` $\rightarrow$ Verify Dr. Arun (87/100 HIGH) workload bar and rebalancing recommendation

### Flow 5: What-If Scheduling Simulator
- [x] Open `/admin/simulator`
- [x] Select Doctor: *Dr. Arun Mehta*, Date: *Tomorrow*, Scenario: *Doctor Unavailable*
- [x] Click **Run Simulation**
- [x] Verify results: 14 affected appointments, High workload impact, Dr. Priya/Dr. Karthik redistribution
- [x] Verify **"NO CHANGES HAVE BEEN APPLIED — This is a simulation"** banner

### Flow 6: Security Boundary Verification
- [x] In browser DevTools / Postman / Swagger, execute `GET /api/analytics/daily` using Alice's JWT token
- [x] Verify HTTP response is **`403 Forbidden`** (confirms server-side RBAC)

---

## 4. Final Polish & Screen Health Check
- [x] No browser console errors (`F12` $\rightarrow$ Console is clear)
- [x] No backend uncaught stack traces in Spring Boot log
- [x] Responsive layout verified on 1080p presentation display
