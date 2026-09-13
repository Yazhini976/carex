# CAREX REST API Documentation

**System:** CAREX — Intelligent Doctor Appointment & Patient Flow Management System  
**Base URL:** `http://localhost:8080/api`  
**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`  
**OpenAPI Spec:** `http://localhost:8080/v3/api-docs`  
**Format:** JSON (`application/json`)  
**Security Model:** Bearer Token (Permissive during current development phase; JWT enforcement configured in next step)

---

## Standard Error Response Format

All error responses across all endpoints follow this standard structure:

```json
{
  "timestamp": "2026-09-11T15:30:00",
  "status": 409,
  "error": "Slot Unavailable",
  "message": "Slot 4 is no longer available",
  "path": "/api/appointments",
  "validationErrors": {
    "field": "error message"
  }
}
```

---

## 1. Authentication (`/api/auth`)

### 1.1 Register User
- **Method:** `POST`
- **URL:** `/api/auth/register`
- **Purpose:** Registers a new user account (patient, doctor, or staff).
- **Request Body:**
```json
{
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "password": "SecurePassword123!",
  "phone": "+1-555-0145",
  "role": "PATIENT"
}
```
- **Response (`201 CREATED`):**
```json
{
  "id": 1,
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "role": "PATIENT",
  "phone": "+1-555-0145",
  "active": true,
  "createdAt": "2026-09-11T10:00:00"
}
```
- **Status Codes:** `201 CREATED`, `400 BAD_REQUEST`, `409 CONFLICT`

### 1.2 Login User
- **Method:** `POST`
- **URL:** `/api/auth/login`
- **Purpose:** Authenticates user credentials and returns a session token.
- **Request Body:**
```json
{
  "email": "jane.doe@example.com",
  "password": "SecurePassword123!"
}
```
- **Response (`200 OK`):**
```json
{
  "token": "carex-session-uuid-token",
  "tokenType": "Bearer",
  "userId": 1,
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "role": "PATIENT"
}
```
- **Status Codes:** `200 OK`, `400 BAD_REQUEST`, `404 NOT_FOUND`

---

## 2. Users (`/api/users`)

| Method | URL | Purpose | Status Code |
|---|---|---|---|
| `GET` | `/api/users` | Retrieve all registered users | `200 OK` |
| `GET` | `/api/users/{id}` | Get user by ID | `200 OK`, `404 NOT_FOUND` |
| `GET` | `/api/users/role/{role}` | Filter users by role (`PATIENT`, `DOCTOR`, `ADMIN`, `STAFF`) | `200 OK` |
| `PUT` | `/api/users/{id}` | Update profile (name, phone) | `200 OK`, `400 BAD_REQUEST` |
| `PUT` | `/api/users/{id}/status?active=true` | Activate or deactivate account | `200 OK` |
| `DELETE` | `/api/users/{id}` | Delete/deactivate user | `204 NO_CONTENT` |

---

## 3. Patients (`/api/patients`)

### 3.1 Create Patient Profile
- **Method:** `POST`
- **URL:** `/api/patients`
- **Request Body:**
```json
{
  "userId": 1,
  "dateOfBirth": "1990-05-15",
  "gender": "FEMALE",
  "emergencyContact": "+1-555-9999"
}
```
- **Response (`201 CREATED`):**
```json
{
  "id": 1,
  "userId": 1,
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "phone": "+1-555-0145",
  "dateOfBirth": "1990-05-15",
  "gender": "FEMALE",
  "emergencyContact": "+1-555-9999",
  "createdAt": "2026-09-11T10:00:00"
}
```

### 3.2 Other Patient Endpoints
| Method | URL | Purpose |
|---|---|---|
| `GET` | `/api/patients` | List all patient profiles |
| `GET` | `/api/patients/{id}` | Get patient by ID |
| `GET` | `/api/patients/user/{userId}` | Get patient by linked User ID |
| `PUT` | `/api/patients/{id}` | Update demographics/emergency contact |
| `DELETE` | `/api/patients/{id}` | Delete patient record |

---

## 4. Doctors (`/api/doctors`)

### 4.1 Create Doctor Profile
- **Method:** `POST`
- **URL:** `/api/doctors`
- **Request Body:**
```json
{
  "userId": 2,
  "licenseNumber": "MED-NY-88990",
  "qualification": "MD Cardiology, FACC",
  "experienceYears": 12,
  "consultationFee": 175.00
}
```
- **Response (`201 CREATED`):**
```json
{
  "id": 1,
  "userId": 2,
  "name": "Dr. Sarah Smith",
  "email": "dr.sarah@carex.com",
  "phone": "+1-555-0200",
  "licenseNumber": "MED-NY-88990",
  "qualification": "MD Cardiology, FACC",
  "experienceYears": 12,
  "consultationFee": 175.00,
  "active": true,
  "createdAt": "2026-09-11T10:00:00",
  "specialties": []
}
```

### 4.2 Other Doctor Endpoints
| Method | URL | Purpose |
|---|---|---|
| `GET` | `/api/doctors` | List all doctors with assigned specialties |
| `GET` | `/api/doctors/active` | List only active practicing doctors |
| `GET` | `/api/doctors/{id}` | Get doctor details by ID |
| `GET` | `/api/doctors/user/{userId}` | Get doctor by linked User ID |
| `PUT` | `/api/doctors/{id}` | Update qualifications, experience, fee, or status |
| `PUT` | `/api/doctors/{id}/status?active=true` | Toggle active status |
| `DELETE` | `/api/doctors/{id}` | Deactivate/delete doctor profile |

---

## 5. Specialties (`/api/specialties`)

| Method | URL | Purpose | Status |
|---|---|---|---|
| `GET` | `/api/specialties` | List all medical specialties | `200 OK` |
| `GET` | `/api/specialties/active` | List only active specialties | `200 OK` |
| `GET` | `/api/specialties/{id}` | Get specialty by ID | `200 OK` |
| `POST` | `/api/specialties` | Create new specialty catalog entry | `201 CREATED` |
| `PUT` | `/api/specialties/{id}` | Update specialty name and description | `200 OK` |
| `PUT` | `/api/specialties/{id}/status?active=true` | Toggle specialty status | `200 OK` |
| `DELETE` | `/api/specialties/{id}` | Delete specialty | `204 NO_CONTENT` |

---

## 6. Doctor Availability (`/api/availability`)

- **`POST /api/availability`**
```json
{
  "doctorId": 1,
  "dayOfWeek": 1,
  "startTime": "09:00:00",
  "endTime": "13:00:00",
  "mode": "HYBRID"
}
```
*Note: `dayOfWeek` uses 0 = Sunday, 1 = Monday ... 6 = Saturday.*

| Method | URL | Purpose |
|---|---|---|
| `GET` | `/api/availability/{id}` | Get availability rule by ID |
| `GET` | `/api/availability/doctor/{doctorId}` | Get all availability rules for doctor |
| `GET` | `/api/availability/doctor/{doctorId}/active` | Get active availability rules for doctor |
| `PUT` | `/api/availability/{id}` | Update availability hours/mode |
| `DELETE` | `/api/availability/{id}` | Delete availability rule |

---

## 7. Doctor Specialties (`/api/doctor-specialties`)

| Method | URL | Parameters | Purpose |
|---|---|---|---|
| `POST` | `/api/doctor-specialties` | `?doctorId=1&specialtyId=2` | Assign specialty to doctor |
| `DELETE` | `/api/doctor-specialties` | `?doctorId=1&specialtyId=2` | Remove specialty from doctor |
| `GET` | `/api/doctor-specialties/doctor/{doctorId}` | Path: `doctorId` | List specialties for doctor |
| `GET` | `/api/doctor-specialties/specialty/{specialtyId}` | Path: `specialtyId` | List doctors for specialty |

---

## 8. Slots (`/api/slots`)

### 8.1 Generate Slots from Availability Pattern
- **Method:** `POST`
- **URL:** `/api/slots/generate`
- **Request Body:**
```json
{
  "doctorId": 1,
  "availabilityId": 1,
  "fromDate": "2026-09-15",
  "toDate": "2026-09-30",
  "slotDurationMinutes": 15
}
```
- **Response (`201 CREATED`):** List of generated `SlotResponse` objects.

### 8.2 Other Slot Endpoints
| Method | URL | Purpose |
|---|---|---|
| `POST` | `/api/slots` | Create a single custom time slot |
| `GET` | `/api/slots/{id}` | Get slot details by ID |
| `GET` | `/api/slots/doctor/{doctorId}` | Get all future available slots for doctor |
| `GET` | `/api/slots/doctor/{doctorId}/date?date=YYYY-MM-DD` | Get all slots for doctor on date |
| `GET` | `/api/slots/available?doctorId=1&date=YYYY-MM-DD` | Get available slots for doctor on date |
| `PUT` | `/api/slots/{id}/block` | Block slot from booking |
| `PUT` | `/api/slots/{id}/release` | Release blocked slot back to available |

---

## 9. Appointments (`/api/appointments`) — Core Workflow

### 9.1 Book Appointment
- **Method:** `POST`
- **URL:** `/api/appointments`
- **Request Body:**
```json
{
  "patientId": 1,
  "doctorId": 1,
  "specialtyId": 1,
  "slotId": 10,
  "mode": "OFFLINE",
  "notes": "Patient experiencing intermittent chest tightness"
}
```
- **Response (`201 CREATED`):**
```json
{
  "appointmentId": 101,
  "patientId": 1,
  "patientName": "Jane Doe",
  "patientEmail": "jane.doe@example.com",
  "patientPhone": "+1-555-0145",
  "doctorId": 1,
  "doctorName": "Dr. Sarah Smith",
  "doctorLicenseNumber": "MED-NY-88990",
  "doctorQualification": "MD Cardiology, FACC",
  "doctorConsultationFee": 175.00,
  "specialtyId": 1,
  "specialtyName": "Cardiology",
  "slotId": 10,
  "slotDate": "2026-09-20",
  "slotStartTime": "10:00:00",
  "slotEndTime": "10:15:00",
  "mode": "OFFLINE",
  "status": "BOOKED",
  "bookedAt": "2026-09-11T10:30:00",
  "cancelledAt": null,
  "completedAt": null,
  "notes": "Patient experiencing intermittent chest tightness"
}
```
- **Status Codes:** `201 CREATED`, `400 BAD_REQUEST`, `409 CONFLICT` (Slot unavailable, doctor not practicing mode, or patient double-booking)

### 9.2 Lifecycle Status Transitions
- **`PUT /api/appointments/{id}/confirm`** — Transitions status `BOOKED -> CONFIRMED`.
- **`PUT /api/appointments/{id}/complete`** — Transitions status `CONFIRMED -> COMPLETED`.
- **`PUT /api/appointments/{id}/cancel`** — Transitions status `* -> CANCELLED`, releases slot, and triggers smart waitlist matching.
- **`PUT /api/appointments/{id}/no-show`** — Marks status as `NO_SHOW`.

Optional request body for lifecycle transitions:
```json
{
  "changedByUserId": 1
}
```

### 9.3 Queries & Summaries
| Method | URL | Purpose |
|---|---|---|
| `GET` | `/api/appointments/{id}` | Get appointment by ID |
| `GET` | `/api/appointments/patient/{patientId}` | Get all appointments for patient |
| `GET` | `/api/appointments/doctor/{doctorId}?date=YYYY-MM-DD` | Get doctor appointments (optional date filter) |
| `GET` | `/api/appointments/status/{status}` | Filter appointments by status |
| `GET` | `/api/appointments/summary/daily?date=YYYY-MM-DD` | Aggregated daily metrics |

---

## 10. Waitlist (`/api/waitlist`)

### 10.1 Join Waitlist
- **Method:** `POST`
- **URL:** `/api/waitlist`
- **Request Body:**
```json
{
  "patientId": 1,
  "specialtyId": 1,
  "preferredDoctorId": 1,
  "preferredDate": "2026-09-22",
  "preferredMode": "OFFLINE"
}
```
- **Response (`201 CREATED`):**
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Jane Doe",
  "specialtyId": 1,
  "specialtyName": "Cardiology",
  "preferredDoctorId": 1,
  "preferredDoctorName": "Dr. Sarah Smith",
  "preferredDate": "2026-09-22",
  "preferredMode": "OFFLINE",
  "status": "WAITING",
  "createdAt": "2026-09-11T10:35:00",
  "fulfilledAt": null
}
```

### 10.2 Waitlist Status Transitions
| Method | URL | Purpose |
|---|---|---|
| `GET` | `/api/waitlist/{id}` | Get waitlist entry by ID |
| `GET` | `/api/waitlist/patient/{patientId}` | Get patient waitlist queue |
| `GET` | `/api/waitlist/matching?specialtyId=1&doctorId=1&mode=OFFLINE&date=YYYY-MM-DD` | Find candidate waiting patients |
| `PUT` | `/api/waitlist/{id}/offer` | Mark status as `OFFERED` |
| `PUT` | `/api/waitlist/{id}/fulfill` | Mark status as `FULFILLED` |
| `PUT` | `/api/waitlist/{id}/cancel` | Cancel waitlist entry |

---

## 11. Notifications (`/api/notifications`)

## 11. Notifications & Reminders (`/api/notifications`)

| Method | URL | Purpose | Access | Status |
|---|---|---|---|---|
| `GET` | `/api/notifications/user/{userId}` | List notifications for authenticated user | `PATIENT`, `DOCTOR`, `ADMIN` (Ownership enforced) | `200 OK` |
| `GET` | `/api/notifications/user/{userId}/unread-count` | Get count of unread/pending notifications | `PATIENT`, `DOCTOR`, `ADMIN` (Ownership enforced) | `200 OK` |
| `PUT` | `/api/notifications/{id}/read` | Mark a notification as read / `SENT` | `PATIENT`, `DOCTOR`, `ADMIN` | `200 OK` |
| `PUT` | `/api/notifications/user/{userId}/read-all` | Bulk mark all notifications as read for user | `PATIENT`, `DOCTOR`, `ADMIN` (Ownership enforced) | `200 OK` |
| `GET` | `/api/notifications/stats` | Retrieve notification & email delivery health metrics | `ADMIN` only | `200 OK` |
| `GET` | `/api/notifications/pending` | List undelivered pending notifications | `ADMIN` only | `200 OK` |
| `POST` | `/api/notifications` | Queue new notification | `ADMIN` only | `201 CREATED` |
| `PUT` | `/api/notifications/{id}/sent` | Mark notification as `SENT` | `ADMIN` only | `200 OK` |
| `PUT` | `/api/notifications/{id}/failed` | Mark notification as `FAILED` | `ADMIN` only | `200 OK` |

### Notification Types Supported
- `APPOINTMENT_BOOKED`: Dispatched immediately upon successful booking.
- `APPOINTMENT_CONFIRMED`: Dispatched when doctor/clinic confirms appointment.
- `APPOINTMENT_CANCELLED`: Dispatched upon cancellation with slot release.
- `APPOINTMENT_COMPLETED`: Post-consultation follow-up notice.
- `APPOINTMENT_NO_SHOW`: Dispatched when patient is marked as no-show.
- `APPOINTMENT_REMINDER`: Automated 30-min duplicate-proof reminder by background scheduler.
- `WAITLIST_SLOT_AVAILABLE`: Dispatched when a cancelled slot opens for waitlist candidates.
- `DOCTOR_UNAVAILABLE`: Critical alert when a doctor becomes unavailable.
- `SCHEDULING_ALERT`: Clinic operations and queue rebalancing alert.

---

## 12. CAREX Intelligence (`/api/intelligence`)

### 12.1 Specialty Recommendation (Navigation Aid)
- **Method:** `POST`
- **URL:** `/api/intelligence/specialty-recommendation`
- **Disclaimer:** Provides appointment navigation guidance only; NOT medical diagnosis.
- **Request:**
```json
{
  "patientId": 1,
  "inputText": "I have been experiencing chest pain, palpitations, and shortness of breath"
}
```
- **Response (`200 OK`):**
```json
{
  "recommendedSpecialtyId": 1,
  "recommendedSpecialty": "Cardiology",
  "reason": "Matched keywords: chest pain, palpitations, shortness of breath",
  "confidence": 0.95,
  "disclaimer": "This recommendation is an appointment-navigation aid only and does NOT constitute medical diagnosis or clinical advice."
}
```

### 12.2 Doctor Matching & Ranking
- **Method:** `POST`
- **URL:** `/api/intelligence/doctor-match`
- **Request:**
```json
{
  "specialtyId": 1,
  "mode": "OFFLINE"
}
```
- **Response (`200 OK`):**
```json
[
  {
    "doctorId": 1,
    "doctorName": "Dr. Sarah Smith",
    "qualification": "MD Cardiology, FACC",
    "experienceYears": 12,
    "consultationFee": 175.00,
    "specialtyId": 1,
    "specialtyName": "Cardiology",
    "specialtyMatch": true,
    "modeMatch": true,
    "hasAvailableSlot": true,
    "estimatedWaitMinutes": 10,
    "score": 96.5,
    "reasoning": "Specialty match (+40), Mode match (+20), Slot available (+20), Experience bonus (+8.5), Low wait bonus (+8.0)"
  }
]
```

### 12.3 Wait-Time Prediction
- **Method:** `GET`
- **URL:** `/api/intelligence/wait-time/{appointmentId}`
- **Response (`200 OK`):**
```json
{
  "appointmentId": 101,
  "predictedMinutes": 15,
  "method": "Deterministic baseline queue model",
  "disclaimer": "Estimated wait time is calculated for scheduling guidance and does NOT guarantee exact consultation start time."
}
```

### 12.4 Workload Intelligence
- **Method:** `GET`
- **URL:** `/api/intelligence/workload/doctor/{doctorId}` (or `/api/intelligence/workload/all`)
- **Response (`200 OK`):**
```json
{
  "doctorId": 1,
  "doctorName": "Doctor #1",
  "currentScore": 45.0,
  "loadLevel": "MODERATE",
  "recommendation": "Workload is within optimal operating capacity.",
  "appointmentCount": 9,
  "completedCount": 4,
  "cancelledCount": 1,
  "noShowCount": 0
}
```

### 12.5 Scheduling Simulation (Read-Only "What-If" Analysis)
- **Method:** `POST`
- **URL:** `/api/intelligence/simulation`
- **Safety:** **Strictly read-only**; never mutates production appointments.
- **Request:**
```json
{
  "doctorId": 1,
  "date": "2026-09-20",
  "scenario": "DOCTOR_UNAVAILABLE"
}
```
- **Response (`200 OK`):**
```json
{
  "doctorId": 1,
  "doctorName": "Doctor #1",
  "date": "2026-09-20",
  "scenario": "DOCTOR_UNAVAILABLE",
  "affectedAppointmentCount": 4,
  "affectedAppointmentIds": [101, 102, 103, 104],
  "candidateAlternativeDoctorIds": [3, 5],
  "candidateAlternativeDoctorNames": ["Dr. Robert Chen", "Dr. Emily Davis"],
  "workloadImpactSummary": "4 appointments require rescheduling or reassignment across 2 alternative doctors.",
  "redistributionSuggestion": "Reassign 2 morning appointments to Dr. Robert Chen and 2 afternoon appointments to Dr. Emily Davis.",
  "readOnly": true
}
```

### 12.6 No-Show Prediction
- **Method:** `GET`
- **URL:** `/api/intelligence/no-show/{appointmentId}`
- **Response (`200 OK`):**
```json
{
  "appointmentId": 101,
  "noShowProbability": 0.10,
  "confidence": "BASELINE_ONLY",
  "method": "Historical clinic baseline model"
}
```

---

## 13. Analytics (`/api/analytics`)

| Method | URL | Description |
|---|---|---|
| `GET` | `/api/analytics/daily?date=YYYY-MM-DD` | Daily aggregated KPIs (total, booked, confirmed, completed, cancelled, no-show) |
| `GET` | `/api/analytics/appointments` | Status breakdown for distribution charts |
| `GET` | `/api/analytics/workload` | Clinic-wide real-time doctor workload snapshots |
| `GET` | `/api/analytics/revenue` | Realized vs pipeline revenue calculations |
