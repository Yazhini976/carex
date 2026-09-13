# CAREX Hackathon Innovation & Judge Summary

## Executive Summary
CAREX solves the three primary operational bottlenecks in modern healthcare:
1. **Inefficient Patient Triage:** Patients struggle to identify the correct medical department.
2. **Unpredictable Clinic Waiting Times:** Hospital waiting rooms experience severe congestion.
3. **Provider Burnout & Schedule Fragility:** Clinics lack tools to simulate or recover from practitioner absences.

---

## The 7 Core Innovations of CAREX

### 1. Explainable Doctor Matching
Unlike generic black-box AI recommendations, CAREX calculates an explainable match score ($0–100\%$) based on 6 weighted clinical parameters:
$$\text{Score} = 0.40 \cdot S_{\text{specialty}} + 0.20 \cdot M_{\text{mode}} + 0.20 \cdot A_{\text{availability}} + 0.10 \cdot W_{\text{wait}} + 0.05 \cdot E_{\text{experience}} + 0.05 \cdot C_{\text{active}}$$
Each matched card provides human-readable verification checks (e.g., *✓ Specialty match*, *✓ Requested mode available*, *✓ Low estimated wait*).

### 2. Predictive Patient Flow & Dynamic Wait Times
CAREX continuously computes live queue wait times based on preceding appointments, provider consultation velocity, and transition buffers. Patients see real-time wait estimates ($12–18\text{ mins}$) before arriving, reducing physical waiting room congestion.

### 3. Smart Waitlist & Automated Slot Recovery
When a patient cancels an appointment, CAREX does not leave the slot idle. The **Smart Waitlist Engine** evaluates waiting candidates, scores their priority based on wait duration and specialty match, and automatically sends a slot offer notification (`ACCEPT` / `DECLINE`).

### 4. Provider Workload Intelligence & Burnout Prevention
CAREX monitors provider capacity across time blocks (e.g., morning vs evening shifts). It classifies workload as `LOW` ($<40\%$), `MEDIUM` ($40–70\%$), `HIGH` ($70–85\%$), or `CRITICAL` ($>85\%$), alerting administrators to peak concentrations and recommending proactive appointment redistribution.

### 5. What-If Scheduling Simulator (Read-Only Disruption Sandbox)
Hospital administrators can model hypothetical disruptions—such as a cardiologist taking emergency leave or an unexpected patient surge—without altering production records. The mathematical simulator estimates:
- Number of affected appointments (e.g. 14 patients)
- Aggregate workload impact (`HIGH`)
- Potential queue wait increase ($+16\text{ mins}$)
- Recommended reassignment distribution across parallel specialists

### 6. Disruption Recovery Engine
Pairs with the What-If Simulator to generate concrete, one-to-one alternative slot proposals for displaced patients, enabling administrators to review and approve rebalancing in seconds.

### 7. Secure Enterprise Healthcare Architecture
- **Stateless JWT Security** with BCrypt hashing and HMAC-SHA256 signatures.
- **Granular RBAC** (`PATIENT`, `DOCTOR`, `ADMIN`).
- **Object Ownership Verification** preventing unauthorized data access.
- **Transactional Safety:** Email/notification dispatching is completely decoupled from booking commits to ensure zero rollbacks on SMTP timeouts.
- **Duplicate-Proof 30-Minute Reminders** utilizing idempotent database verification.

---

## Non-Diagnostic Positioning
All AI components within CAREX are strictly designed for **appointment navigation and operational triage**. Explicit safety disclaimers are prominently displayed on all patient interfaces to maintain full compliance with healthcare regulations.
