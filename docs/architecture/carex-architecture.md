# CAREX Enterprise Architecture Document

## 1. System Overview & Technology Stack

CAREX is architected as an enterprise-grade, highly cohesive, loosely coupled web application designed for high availability, fault tolerance, and deterministic performance in healthcare scheduling.

```text
                                 ┌────────────────────────┐
                                 │   React 18 + Vite SPA  │
                                 │   (Tailored Glass UI)  │
                                 └───────────┬────────────┘
                                             │ HTTP / REST / JWT Bearer
                                             ▼
                                 ┌────────────────────────┐
                                 │   Nginx Reverse Proxy  │
                                 │   (Docker Port 80)     │
                                 └───────────┬────────────┘
                                             │ /api proxy pass
                                             ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                               Spring Boot 3.3.3 Backend API                            │
├────────────────────────────┬─────────────────────────────┬─────────────────────────────┤
│      Core Service Layer    │     Intelligence Engine     │    Notification Engine      │
│  - Slot pessimistic locks  │  - Specialty recommendation │  - Multi-channel router     │
│  - HCL mode constraints    │  - Doctor matching algorithm│  - 30-min duplicate-proof   │
│  - State machine lifecycle │  - Wait-time queue models   │    background scheduler     │
│  - Priority waitlist queue │  - What-If capacity sim     │  - Responsive HTML emails   │
│  - Actuator health metrics │  - Disruption recovery      │  - In-app database inbox    │
└────────────────────────────┴──────────────┬──────────────┴─────────────────────────────┘
                                            │ JPA / Hibernate ORM
                                            ▼
                                 ┌────────────────────────┐
                                 │  PostgreSQL 17 Engine  │
                                 │  (Schema + Seed Data)  │
                                 └────────────────────────┘
```

---

## 2. Layered Architecture Breakdown

### A. Frontend Layer (React 18 + Vite)
- **State Management:** Custom React Context (`AuthContext`) for centralized JWT management and role routing.
- **Service Abstraction:** Axios API clients (`appointmentService.js`, `intelligenceService.js`, `notificationService.js`) with request/response interceptors automatically attaching `Authorization: Bearer <token>`.
- **User Portals:**
  - **Patient Portal:** Smart Consultation navigation, explainable doctor match cards, 5-step booking flow, waitlist management, in-app notification inbox.
  - **Doctor Portal:** Clinical schedule dashboard, live queue management, timetable availability editor.
  - **Admin Portal:** CAREX Command Center, What-If capacity disruption simulator, workload intelligence analyzer.

### B. Security & Controller Layer (Spring Security 6 + JJWT)
- **Stateless Authentication:** Every request is authenticated via `JwtAuthenticationFilter` verifying HMAC-SHA256 signed JWT tokens.
- **Role-Based Access Control (RBAC):** Endpoints are protected via `@PreAuthorize("hasRole('PATIENT')")`, `@PreAuthorize("hasRole('DOCTOR')")`, or `@PreAuthorize("hasRole('ADMIN')")`.
- **Object Ownership Guards:** `SecurityUtils.isCurrentUser(userId)` strictly verifies that patients can only access their own appointments, waitlists, and notifications.
- **Rate Limiting:** `RateLimitingFilter` safeguards public endpoints against brute-force attempts.

### C. Core Service & Transaction Layer
- **Pessimistic Slot Locking:** `SlotRepository.findByIdWithLock(slotId)` locks slot records during booking transactions using `SELECT ... FOR UPDATE` to prevent race conditions.
- **HCL Online/Offline Constraint:** `AppointmentServiceImpl.bookAppointment(...)` enforces that a patient cannot book online and offline appointments with the same doctor.
- **State Machine Transitions:** Manages lifecycle transitions:
  $$\text{BOOKED} \longrightarrow \text{CONFIRMED} \longrightarrow \text{IN\_PROGRESS} \longrightarrow \text{COMPLETED}$$
  with support for `CANCELLED` and `NO_SHOW`.

### D. CAREX Intelligence Engine
- **Deterministic, Non-Diagnostic:** Uses structured mathematical models without external LLM dependencies, guaranteeing instant response times and medical safety.
- **8 Core Models:**
  1. `SpecialtyRecommendationService`: Keyword classification across 14 clinical categories.
  2. `DoctorMatchingService`: Multi-factor weighted scoring.
  3. `WaitTimePredictionService`: Queue velocity multiplier ($\text{position} \times 15\text{ mins} + \text{buffer}$).
  4. `SmartWaitlistService`: Priority ranking for slot recovery.
  5. `WorkloadIntelligenceService`: 0–100 provider utilization index.
  6. `SchedulingSimulationService`: Strictly read-only capacity disruption sandbox.
  7. `DisruptionRecoveryService`: Optimal patient reassignment recommendations.
  8. `NoShowPredictionService`: Historical attendance risk analyzer.

### E. Notification & Reminder Engine
- **Multi-Channel Dispatcher:** Asynchronously dispatches events to PostgreSQL in-app notifications and `JavaMailSender` SMTP.
- **Fault Isolation:** Notification or SMTP failures are safely caught and logged, guaranteeing that appointment booking transactions are never rolled back due to email issues.
- **Duplicate-Proof 30-Min Reminder Daemon:** Scheduled fixed-rate Spring job scanning for upcoming appointments within a 30-minute window, verifying idempotency via `existsByAppointmentIdAndType(...)`.

---

## 3. Data Model & Persistence Layer (PostgreSQL 17)

- `users`: Core identity table with BCrypt password hashes and role enums (`PATIENT`, `DOCTOR`, `ADMIN`).
- `doctors`: Clinical provider profile with license number, experience, and consultation fees.
- `patients`: Patient profile linked 1:1 with user identity.
- `specialties`: Medical department taxonomy.
- `doctor_specialties`: Join table mapping doctors to primary and secondary specialties.
- `doctor_availabilities`: Recurring weekly availability rules.
- `slots`: Individual 15/30-minute bookable calendar slots with mode (`ONLINE` vs `OFFLINE`) and status (`AVAILABLE`, `LOCKED`, `BOOKED`).
- `appointments`: Scheduled consultations linking patient, doctor, slot, and status history.
- `appointment_status_histories`: Immutable audit trail of status transitions.
- `waitlists`: Priority waiting queue for fully booked slots.
- `notifications`: User notification table storing delivery channels, status, and payload.
- `workload_snapshots`: Historical provider capacity index logs.
