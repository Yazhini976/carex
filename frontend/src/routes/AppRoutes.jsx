import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';

// Public pages
import LandingPage from '../pages/LandingPage';
import Login from '../pages/auth/Login';
import Register from '../pages/auth/Register';
import Notifications from '../pages/patient/Notifications';

// Route guards
import ProtectedRoute from './ProtectedRoute';
import RoleRoute from './RoleRoute';

// Patient pages
import PatientDashboard from '../pages/patient/PatientDashboard';
import FindDoctor from '../pages/patient/FindDoctor';
import SmartConsultation from '../pages/patient/SmartConsultation';
import BookAppointment from '../pages/patient/BookAppointment';
import MyAppointments from '../pages/patient/MyAppointments';
import AppointmentDetails from '../pages/patient/AppointmentDetails';
import Waitlist from '../pages/patient/Waitlist';
import PatientProfile from '../pages/patient/PatientProfile';

// Doctor pages
import DoctorDashboard from '../pages/doctor/DoctorDashboard';
import DoctorAppointments from '../pages/doctor/DoctorAppointments';
import DoctorAvailability from '../pages/doctor/DoctorAvailability';
import DoctorQueue from '../pages/doctor/DoctorQueue';
import DoctorProfile from '../pages/doctor/DoctorProfile';

// Admin pages
import AdminDashboard from '../pages/admin/AdminDashboard';
import ManageDoctors from '../pages/admin/ManageDoctors';
import ManagePatients from '../pages/admin/ManagePatients';
import ManageSpecialties from '../pages/admin/ManageSpecialties';
import ManageAvailability from '../pages/admin/ManageAvailability';
import ManageAppointments from '../pages/admin/ManageAppointments';
import Analytics from '../pages/admin/Analytics';
import WorkloadIntelligence from '../pages/admin/WorkloadIntelligence';
import SchedulingSimulator from '../pages/admin/SchedulingSimulator';

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Authenticated Notifications Center */}
      <Route
        path="/notifications"
        element={
          <ProtectedRoute>
            <Notifications />
          </ProtectedRoute>
        }
      />

      {/* Patient Portal Routes */}
      <Route
        path="/patient/*"
        element={
          <ProtectedRoute>
            <RoleRoute allowedRoles={['PATIENT']}>
              <Routes>
                <Route path="dashboard" element={<PatientDashboard />} />
                <Route path="find-doctor" element={<FindDoctor />} />
                <Route path="smart-consultation" element={<SmartConsultation />} />
                <Route path="book" element={<BookAppointment />} />
                <Route path="appointments" element={<MyAppointments />} />
                <Route path="appointments/:id" element={<AppointmentDetails />} />
                <Route path="waitlist" element={<Waitlist />} />
                <Route path="profile" element={<PatientProfile />} />
                <Route path="*" element={<Navigate to="dashboard" replace />} />
              </Routes>
            </RoleRoute>
          </ProtectedRoute>
        }
      />

      {/* Doctor Portal Routes */}
      <Route
        path="/doctor/*"
        element={
          <ProtectedRoute>
            <RoleRoute allowedRoles={['DOCTOR']}>
              <Routes>
                <Route path="dashboard" element={<DoctorDashboard />} />
                <Route path="appointments" element={<DoctorAppointments />} />
                <Route path="availability" element={<DoctorAvailability />} />
                <Route path="queue" element={<DoctorQueue />} />
                <Route path="profile" element={<DoctorProfile />} />
                <Route path="*" element={<Navigate to="dashboard" replace />} />
              </Routes>
            </RoleRoute>
          </ProtectedRoute>
        }
      />

      {/* Admin Portal Routes */}
      <Route
        path="/admin/*"
        element={
          <ProtectedRoute>
            <RoleRoute allowedRoles={['ADMIN']}>
              <Routes>
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="doctors" element={<ManageDoctors />} />
                <Route path="patients" element={<ManagePatients />} />
                <Route path="specialties" element={<ManageSpecialties />} />
                <Route path="availability" element={<ManageAvailability />} />
                <Route path="appointments" element={<ManageAppointments />} />
                <Route path="analytics" element={<Analytics />} />
                <Route path="workload" element={<WorkloadIntelligence />} />
                <Route path="simulator" element={<SchedulingSimulator />} />
                <Route path="*" element={<Navigate to="dashboard" replace />} />
              </Routes>
            </RoleRoute>
          </ProtectedRoute>
        }
      />

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default AppRoutes;
