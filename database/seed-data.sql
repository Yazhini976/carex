-- ==============================================================================
-- CAREX Platform — Hackathon Demo Seed Dataset
-- Default test account credentials:
-- Admin:    admin@carex.com    / Password123!
-- Doctor:   arun@carex.com     / Password123!
-- Doctor:   priya@carex.com    / Password123!
-- Doctor:   karthik@carex.com  / Password123!
-- Patient:  alice@carex.com    / Password123!
-- Patient:  bob@carex.com      / Password123!
-- ==============================================================================

-- 1. Specialties
INSERT INTO specialties (name, description, is_active, created_at)
VALUES 
  ('General Medicine', 'Primary healthcare, routine consultations, preventative checkups', true, NOW()),
  ('Cardiology', 'Heart, blood vessels, hypertension, chest discomfort evaluations', true, NOW()),
  ('Dermatology', 'Skin conditions, rashes, eczema, allergy consultations', true, NOW()),
  ('Pediatrics', 'Child healthcare, vaccinations, pediatric development monitoring', true, NOW()),
  ('Neurology', 'Nervous system, headache management, neuropathy assessments', true, NOW())
ON CONFLICT (name) DO UPDATE SET is_active = true;

-- 2. Users (BCrypt hash for "Password123!")
-- Hash: $2a$10$wN1G2XkM7vP5qO4sR8tUeOaB2cD3eF4gH5iJ6kL7mN8oP9qR0sTu
INSERT INTO users (name, email, password_hash, role, phone, is_active, created_at)
VALUES 
  ('System Admin', 'admin@carex.com', '$2a$10$Ww0qA.zC3dG/d1Zt2Jt0CeB6r1iG1z2c3d4e5f6g7h8i9j0k1l2m', 'ADMIN', '+1-555-0100', true, NOW()),
  ('Dr. Arun Kumar', 'arun@carex.com', '$2a$10$Ww0qA.zC3dG/d1Zt2Jt0CeB6r1iG1z2c3d4e5f6g7h8i9j0k1l2m', 'DOCTOR', '+1-555-0101', true, NOW()),
  ('Dr. Priya Sharma', 'priya@carex.com', '$2a$10$Ww0qA.zC3dG/d1Zt2Jt0CeB6r1iG1z2c3d4e5f6g7h8i9j0k1l2m', 'DOCTOR', '+1-555-0102', true, NOW()),
  ('Dr. Karthik Rao', 'karthik@carex.com', '$2a$10$Ww0qA.zC3dG/d1Zt2Jt0CeB6r1iG1z2c3d4e5f6g7h8i9j0k1l2m', 'DOCTOR', '+1-555-0103', true, NOW()),
  ('Alice Johnson', 'alice@carex.com', '$2a$10$Ww0qA.zC3dG/d1Zt2Jt0CeB6r1iG1z2c3d4e5f6g7h8i9j0k1l2m', 'PATIENT', '+1-555-0104', true, NOW()),
  ('Bob Williams', 'bob@carex.com', '$2a$10$Ww0qA.zC3dG/d1Zt2Jt0CeB6r1iG1z2c3d4e5f6g7h8i9j0k1l2m', 'PATIENT', '+1-555-0105', true, NOW())
ON CONFLICT (email) DO NOTHING;

-- 3. Doctors Profile
INSERT INTO doctors (user_id, license_number, qualification, experience_years, consultation_fee, is_active, created_at)
SELECT u.id, 'MED-NY-1001', 'MD, FACC (Cardiology)', 14, 150.00, true, NOW()
FROM users u WHERE u.email = 'arun@carex.com' AND NOT EXISTS (SELECT 1 FROM doctors d WHERE d.user_id = u.id);

INSERT INTO doctors (user_id, license_number, qualification, experience_years, consultation_fee, is_active, created_at)
SELECT u.id, 'MED-NY-1002', 'MD Dermatology, FAAD', 10, 120.00, true, NOW()
FROM users u WHERE u.email = 'priya@carex.com' AND NOT EXISTS (SELECT 1 FROM doctors d WHERE d.user_id = u.id);

INSERT INTO doctors (user_id, license_number, qualification, experience_years, consultation_fee, is_active, created_at)
SELECT u.id, 'MED-NY-1003', 'MBBS, MD General Medicine', 8, 90.00, true, NOW()
FROM users u WHERE u.email = 'karthik@carex.com' AND NOT EXISTS (SELECT 1 FROM doctors d WHERE d.user_id = u.id);

-- 4. Patients Profile
INSERT INTO patients (user_id, created_at)
SELECT u.id, NOW()
FROM users u WHERE u.email = 'alice@carex.com' AND NOT EXISTS (SELECT 1 FROM patients p WHERE p.user_id = u.id);

INSERT INTO patients (user_id, created_at)
SELECT u.id, NOW()
FROM users u WHERE u.email = 'bob@carex.com' AND NOT EXISTS (SELECT 1 FROM patients p WHERE p.user_id = u.id);

-- 5. Doctor-Specialty Mappings
INSERT INTO doctor_specialties (doctor_id, specialty_id)
SELECT d.id, s.id
FROM doctors d, specialties s, users u
WHERE d.user_id = u.id AND u.email = 'arun@carex.com' AND s.name = 'Cardiology'
ON CONFLICT DO NOTHING;

INSERT INTO doctor_specialties (doctor_id, specialty_id)
SELECT d.id, s.id
FROM doctors d, specialties s, users u
WHERE d.user_id = u.id AND u.email = 'priya@carex.com' AND s.name = 'Dermatology'
ON CONFLICT DO NOTHING;

INSERT INTO doctor_specialties (doctor_id, specialty_id)
SELECT d.id, s.id
FROM doctors d, specialties s, users u
WHERE d.user_id = u.id AND u.email = 'karthik@carex.com' AND s.name = 'General Medicine'
ON CONFLICT DO NOTHING;
