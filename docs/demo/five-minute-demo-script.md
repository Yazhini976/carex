# CAREX — 5 to 7 Minute Live Hackathon Demo Script

**Presenters:** Team CAREX  
**Audience:** Technical Judges & Evaluation Panel  
**Total Target Time:** 5:30 – 6:30 minutes  
**Setup Check:** Backend running on port 8080, Frontend on port 5173, PostgreSQL initialized with deterministic seed data.

---

## Timeline & Scene-by-Scene Script

```text
[ 0:00 - 0:30 ] Intro & Problem Statement
[ 0:30 - 1:00 ] Architecture & Landing Page Overview
[ 1:00 - 2:00 ] Scene 1 & 2: Smart Consultation & Explainable Doctor Matching
[ 2:00 - 3:00 ] Scene 3: Booking Flow & Appointment Journey
[ 3:00 - 4:00 ] Scene 4: Smart Waitlist & Cancellation Recovery
[ 4:00 - 5:30 ] Scene 5: Admin Command Center & Workload Intelligence
[ 5:30 - 6:30 ] Scene 6: What-If Scheduling Simulator (WOW Moment)
[ 6:30 - 7:00 ] Scene 7: Enterprise Security Boundary & Closing
```

---

### [0:00 – 0:30] Introduction & Problem
**Speaker:**
> "Good morning, judges. We are presenting **CAREX: The Intelligent Doctor Appointment and Patient Flow Management System**.
> 
> Traditional clinic booking systems are static: patients face confusion about which specialty to book, waiting rooms suffer from unpredictable crowding, and when a doctor takes sudden leave, clinic managers face administrative chaos. 
> 
> CAREX solves this by combining transactional booking with deterministic scheduling intelligence, dynamic patient-flow tracking, and disruption simulation."

---

### [0:30 – 1:00] Architecture & Landing Page
**Action:** Show the CAREX Landing Page (`http://localhost:5173`).
**Speaker:**
> "Here is the CAREX landing page. You will immediately notice our live health monitor showing the status of the Spring Boot backend, PostgreSQL database, and notification service.
> 
> The system is built with **React 18** on the frontend and **Spring Boot 3.3** on the backend, secured with stateless JWT and role-based access control.
> 
> Let's log in as our first persona: a patient named Alice."
**Action:** Click the 1-click **Patient Demo** button (or log in with `alice@carex.com` / `Password123!`).

---

### [1:00 – 2:00] Scene 1 & 2: Smart Consultation & Doctor Matching
**Action:** Navigate to **Smart Consultation** (`/patient/smart-consultation`).
**Speaker:**
> "Patients often don't know the medical department they need. In our **Smart Consultation** navigation screen, Alice describes her issue: *'I have itchy skin rashes and red patches on my arm.'*
> 
> When we click **Get Recommendation**, CAREX analyzes the input using deterministic keyword mapping and suggests **Dermatology** with an **88% confidence score**.
> 
> Notice our prominent clinical disclaimer: *CAREX provides appointment navigation assistance and does not provide medical diagnosis.*
> 
> Now, we click **View Matched Doctors**."

**Action:** Show the Ranked Doctor Cards (`/patient/book-appointment`).
**Speaker:**
> "Here is our **Explainable Doctor Matching**. Rather than a black-box score, CAREX calculates an explainable match score—here Dr. Priya Nair has a **92% match score**.
> 
> It provides a transparent breakdown:
> - ✓ Specialty match
> - ✓ Requested mode available (Online)
> - ✓ Available today
> - ✓ Low estimated waiting time (12 minutes)
> 
> This gives patients complete clarity on why a practitioner is recommended."

---

### [2:00 – 3:00] Scene 3: Booking Flow & Patient Journey
**Action:** Click **Book Appointment** for Dr. Priya, step through Mode $\rightarrow$ Slot $\rightarrow$ Confirm.
**Speaker:**
> "Our 5-step booking flow keeps selections persistent in the sidebar to prevent accidental loss. Let's select an Online consultation slot and click **Confirm Booking**.
> 
> Upon booking, a database pessimistic lock prevents double-booking, and an asynchronous notification event is dispatched safely to our email engine.
> 
> Here is the confirmation screen:
> - Formatted Appointment ID: **CX-1024**
> - Mode-specific guidance: For online consultations, the secure video link opens 5 minutes prior.
> - Estimated clinic waiting time: **12–18 minutes**.
> 
> On the Appointment Details page, Alice can track the 5-stage patient journey timeline from *Booked* through *Consultation* and *Completed*."

---

### [3:00 – 4:00] Scene 4: Smart Waitlist & Cancellation Recovery
**Action:** Log out and log in as Bob (`bob@carex.com` / `Password123!`), navigate to **Waitlist** (`/patient/waitlist`).
**Speaker:**
> "When a popular doctor's schedule is completely booked, traditional systems turn patients away. CAREX offers our **Smart Waitlist**.
> 
> Bob joined the waitlist for Cardiology. When another patient cancels an appointment, the CAREX Waitlist Engine evaluates queued patients by wait duration and preferred mode.
> 
> Notice what Bob sees: **🎉 MATCHING SLOT FOUND**. A newly opened slot with Dr. Priya is offered directly to Bob with one-click **Accept** or **Decline** actions. This eliminates idle clinic capacity."

---

### [4:00 – 5:30] Scene 5: Admin Command Center & Workload Intelligence
**Action:** Log out and log in as Admin (`admin@carex.com` / `Password123!`), open the **Command Center** (`/admin/dashboard`).
**Speaker:**
> "Now let's switch to the **CAREX Command Center** used by clinic directors and administrators.
> 
> At the top, we see real-time operational KPIs: total active patients, doctors, today's appointment velocity, completion rates, and an Online vs. Offline consultation breakdown.
> 
> When we open **Workload Intelligence**, CAREX continuously tracks practitioner utilization on a 0–100 scale.
> 
> Look at Dr. Arun Mehta: his workload index is **87/100 (HIGH)**, and the system highlights a peak concentration between 5 PM and 7 PM with an automated recommendation: *Redistribute 3 future appointments to prevent congestion.*"

---

### [5:30 – 6:30] Scene 6: What-If Scheduling Simulator (WOW Moment)
**Action:** Navigate to **Scheduling Simulator** (`/admin/simulator`).
**Speaker:**
> "Now for our standout innovation: the **What-If Scheduling Simulator**.
> 
> Suppose Dr. Arun calls in sick for tomorrow. Instead of guessing the impact or scrambling through spreadsheets, the administrator selects *Dr. Arun*, chooses tomorrow's date, selects *Doctor Unavailable*, and clicks **Run Simulation**.
> 
> In milliseconds, the simulation engine computes the downstream blast radius:
> - **14 appointments affected**
> - **High Workload Impact**
> - **Estimated wait increase: +16 minutes**
> - **Automated redistribution plan**: Reassign 4 patients to Dr. Priya and 3 to Dr. Karthik.
> 
> Most importantly, notice the banner: **SIMULATION — NO CHANGES APPLIED**. This is a mathematical sandbox that empowers hospital staff to evaluate recovery options before making any changes to production appointments."

---

### [6:30 – 7:00] Scene 7: Security Boundary & Closing
**Action:** Show an unauthorized API request test (or open DevTools / Postman / Swagger).
**Speaker:**
> "Finally, let's verify our security boundaries. Frontend navigation is never the only line of defense.
> 
> If a logged-in patient tries to access admin endpoints like `/api/analytics/daily` or `/api/admin/users`, Spring Security intercepts the JWT claims and returns a strict **`403 Forbidden`**. All passwords are encrypted with BCrypt, queries use parameterized JPA repositories against SQL injection, and a token bucket rate limiter protects our public endpoints.
> 
> In conclusion: CAREX is fully tested with 80 backend automated tests, 100% green frontend builds, and ready for containerized deployment.
> 
> CAREX empowers healthcare facilities to **Book smarter, wait less, and manage better.**
> 
> Thank you, and we welcome your questions!"
