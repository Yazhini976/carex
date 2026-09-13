import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Search,
  Sparkles,
  CalendarCheck,
  Clock,
  User,
  Users,
  Stethoscope,
  Tags,
  BarChart3,
  BrainCircuit,
  Sliders,
  CalendarRange
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';

export const Sidebar = ({ isOpen = true, onClose }) => {
  const { role } = useAuth();

  const patientNav = [
    { to: '/patient/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/patient/smart-consultation', label: 'Smart Consultation', icon: Sparkles, badge: 'AI' },
    { to: '/patient/find-doctor', label: 'Find a Doctor', icon: Search },
    { to: '/patient/appointments', label: 'My Appointments', icon: CalendarCheck },
    { to: '/patient/waitlist', label: 'Smart Waitlist', icon: Clock },
    { to: '/patient/profile', label: 'My Profile', icon: User },
  ];

  const doctorNav = [
    { to: '/doctor/dashboard', label: 'Doctor Dashboard', icon: LayoutDashboard },
    { to: '/doctor/queue', label: 'Live Queue & Wait-Time', icon: Clock, badge: 'LIVE' },
    { to: '/doctor/appointments', label: 'My Schedule', icon: CalendarCheck },
    { to: '/doctor/availability', label: 'Manage Availability', icon: CalendarRange },
    { to: '/doctor/profile', label: 'Doctor Profile', icon: User },
  ];

  const adminNav = [
    { to: '/admin/dashboard', label: 'Overview Dashboard', icon: LayoutDashboard },
    { to: '/admin/doctors', label: 'Manage Doctors', icon: Stethoscope },
    { to: '/admin/patients', label: 'Manage Patients', icon: Users },
    { to: '/admin/specialties', label: 'Manage Specialties', icon: Tags },
    { to: '/admin/workload', label: 'Workload Intelligence', icon: BrainCircuit, badge: 'AI' },
    { to: '/admin/simulator', label: 'Schedule Simulator', icon: Sliders, badge: 'NEW' },
    { to: '/admin/analytics', label: 'Analytics & Trends', icon: BarChart3 },
  ];

  const navItems = role === 'PATIENT' ? patientNav : role === 'DOCTOR' ? doctorNav : role === 'ADMIN' ? adminNav : [];

  return (
    <aside
      style={{
        width: '260px',
        backgroundColor: '#ffffff',
        borderRight: '1px solid var(--gray-200)',
        display: 'flex',
        flexDirection: 'column',
        flexShrink: 0,
        height: 'calc(100vh - 64px)',
        position: 'sticky',
        top: '64px',
        overflowY: 'auto',
      }}
    >
      <div style={{ padding: '1.25rem 1rem' }}>
        <div
          style={{
            fontSize: '0.75rem',
            fontWeight: 700,
            textTransform: 'uppercase',
            letterSpacing: '0.05em',
            color: 'var(--gray-400)',
            marginBottom: '0.75rem',
            paddingLeft: '0.75rem',
          }}
        >
          {role} PORTAL
        </div>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                style={({ isActive }) => ({
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '0.625rem 0.75rem',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '0.875rem',
                  fontWeight: isActive ? 600 : 500,
                  color: isActive ? 'var(--primary-700)' : 'var(--gray-700)',
                  backgroundColor: isActive ? 'var(--primary-50)' : 'transparent',
                  textDecoration: 'none',
                  transition: 'all 0.15s ease',
                })}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <Icon size={18} />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span
                    style={{
                      fontSize: '0.65rem',
                      fontWeight: 700,
                      padding: '0.15rem 0.4rem',
                      borderRadius: '4px',
                      backgroundColor: item.badge === 'AI' ? '#e0e7ff' : '#fef3c7',
                      color: item.badge === 'AI' ? '#4338ca' : '#b45309',
                    }}
                  >
                    {item.badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>
    </aside>
  );
};

export default Sidebar;
