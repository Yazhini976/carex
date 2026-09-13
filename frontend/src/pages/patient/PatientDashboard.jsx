import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Sparkles,
  Search,
  CalendarCheck,
  Clock,
  User,
  ArrowRight,
  AlertCircle,
  CheckCircle2,
  Calendar,
  Bell,
  Video,
  Building,
  ShieldCheck,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import appointmentService from '../../services/appointmentService';
import waitlistService from '../../services/waitlistService';
import notificationService from '../../services/notificationService';
import intelligenceService from '../../services/intelligenceService';
import PageHeader from '../../components/common/PageHeader';
import AppointmentCard from '../../components/common/AppointmentCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import StatusBadge from '../../components/common/StatusBadge';
import { formatDate, formatTime } from '../../utils/formatters';

export const PatientDashboard = () => {
  const { user, patientId } = useAuth();
  const navigate = useNavigate();

  const [upcomingAppointment, setUpcomingAppointment] = useState(null);
  const [estimatedWait, setEstimatedWait] = useState(null);
  const [recentAppointments, setRecentAppointments] = useState([]);
  const [waitlists, setWaitlists] = useState([]);
  const [unreadNotifCount, setUnreadNotifCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  };

  useEffect(() => {
    loadDashboardData();
  }, [patientId]);

  const loadDashboardData = async () => {
    if (!patientId) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      // 1. Fetch patient's appointments
      const appts = await appointmentService.getPatientAppointments(patientId);
      const list = Array.isArray(appts) ? appts : [];

      // Find first active upcoming appointment
      const upcoming = list.find(
        (a) => a.status === 'BOOKED' || a.status === 'CONFIRMED'
      );
      setUpcomingAppointment(upcoming || null);
      setRecentAppointments(list.slice(0, 4));

      // 2. Fetch dynamic wait time prediction if upcoming exists
      if (upcoming?.appointmentId) {
        try {
          const wt = await intelligenceService.getWaitTimePrediction(upcoming.appointmentId);
          setEstimatedWait(wt);
        } catch {
          // fallback default calculation
          setEstimatedWait(null);
        }
      }

      // 3. Fetch patient's waitlists
      const wl = await waitlistService.getPatientWaitlists(patientId);
      setWaitlists(Array.isArray(wl) ? wl.slice(0, 3) : []);

      // 4. Fetch notification count
      if (user?.userId) {
        const notifData = await notificationService.getUnreadCount(user.userId).catch(() => ({ unreadCount: 0 }));
        setUnreadNotifCount(notifData.unreadCount || 0);
      }
    } catch (err) {
      console.error(err);
      setError('Unable to load dashboard data. Please check your connection.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelAppointment = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return;
    try {
      await appointmentService.cancelAppointment(id, 'Patient request from dashboard');
      loadDashboardData();
    } catch (err) {
      alert(err.message || 'Failed to cancel appointment');
    }
  };

  return (
    <div className="page-body">
      {/* Top Welcome Header */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          flexWrap: 'wrap',
          gap: '1.25rem',
          marginBottom: '2rem',
          paddingBottom: '1.5rem',
          borderBottom: '1px solid var(--gray-200)',
        }}
      >
        <div>
          <h1 style={{ fontSize: '1.875rem', fontWeight: 800, color: 'var(--gray-900)' }}>
            {getGreeting()}, {user?.name || 'Patient'}
          </h1>
          <p style={{ fontSize: '1.05rem', color: 'var(--gray-600)', marginTop: '0.25rem' }}>
            How can CAREX help today?
          </p>
        </div>

        <button
          onClick={() => navigate('/patient/smart-consultation')}
          className="btn btn-primary btn-lg"
          style={{
            background: 'linear-gradient(135deg, #0284c7 0%, #4f46e5 100%)',
            border: 'none',
            boxShadow: 'var(--shadow-md)',
          }}
        >
          <Sparkles size={18} />
          <span>Find the Right Doctor</span>
        </button>
      </div>

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* 4 Core Summary Cards */}
      <div
        className="grid-cards"
        style={{
          gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
          marginBottom: '2.5rem',
        }}
      >
        {/* Card 1: Next Appointment */}
        <div className="card" style={{ borderLeft: '4px solid var(--primary-600)' }}>
          <div className="flex-between">
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
              Next Appointment
            </span>
            <CalendarCheck size={18} color="var(--primary-600)" />
          </div>
          {upcomingAppointment ? (
            <div style={{ marginTop: '0.5rem' }}>
              <div style={{ fontSize: '1.15rem', fontWeight: 700, color: 'var(--gray-900)' }}>
                {upcomingAppointment.doctorName || `Dr. #${upcomingAppointment.doctorId}`}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--gray-600)', marginTop: '2px' }}>
                {formatDate(upcomingAppointment.appointmentDate)} at {formatTime(upcomingAppointment.slotStartTime)}
              </div>
            </div>
          ) : (
            <div style={{ marginTop: '0.5rem', color: 'var(--gray-500)', fontSize: '0.9rem' }}>
              No upcoming appointment
            </div>
          )}
        </div>

        {/* Card 2: Estimated Waiting Time */}
        <div className="card" style={{ borderLeft: '4px solid #f59e0b' }}>
          <div className="flex-between">
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
              Estimated Waiting Time
            </span>
            <Clock size={18} color="#d97706" />
          </div>
          <div style={{ marginTop: '0.5rem' }}>
            <div style={{ fontSize: '1.35rem', fontWeight: 800, color: '#b45309' }}>
              {upcomingAppointment
                ? `~ ${estimatedWait?.predictedMinutes || upcomingAppointment.estimatedWaitTimeMinutes || 15} mins`
                : '—'}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)', marginTop: '2px' }}>
              {upcomingAppointment ? 'Live predictive queue estimate' : 'When you book an appointment'}
            </div>
          </div>
        </div>

        {/* Card 3: Waitlist Status */}
        <div className="card" style={{ borderLeft: '4px solid var(--accent-indigo)' }}>
          <div className="flex-between">
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
              Waitlist Status
            </span>
            <Clock size={18} color="var(--accent-indigo)" />
          </div>
          <div style={{ marginTop: '0.5rem' }}>
            <div style={{ fontSize: '1.35rem', fontWeight: 800, color: 'var(--gray-900)' }}>
              {waitlists.length} Active {waitlists.length === 1 ? 'Request' : 'Requests'}
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--primary-600)', marginTop: '2px' }}>
              {waitlists.some((w) => w.status === 'OFFERED') ? '🎉 Slot offer available!' : 'Auto recovery active'}
            </div>
          </div>
        </div>

        {/* Card 4: Notifications */}
        <div
          className="card"
          onClick={() => navigate('/patient/notifications')}
          style={{ borderLeft: '4px solid var(--accent-emerald)', cursor: 'pointer' }}
        >
          <div className="flex-between">
            <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
              Notifications
            </span>
            <Bell size={18} color="#059669" />
          </div>
          <div style={{ marginTop: '0.5rem' }}>
            <div style={{ fontSize: '1.35rem', fontWeight: 800, color: '#15803d' }}>
              {unreadNotifCount} Unread
            </div>
            <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)', marginTop: '2px' }}>
              Click to view message center &rarr;
            </div>
          </div>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner text="Loading patient profile & schedule..." />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
          {/* Upcoming Appointment Section */}
          <div>
            <div className="flex-between" style={{ marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.15rem' }}>Next Appointment</h3>
              {upcomingAppointment && (
                <Link
                  to={`/patient/appointments/${upcomingAppointment.appointmentId}`}
                  style={{ fontSize: '0.825rem', color: 'var(--primary-600)', fontWeight: 600, textDecoration: 'none' }}
                >
                  View Details &rarr;
                </Link>
              )}
            </div>

            {upcomingAppointment ? (
              <AppointmentCard
                appointment={upcomingAppointment}
                role="PATIENT"
                onView={(appt) => navigate(`/patient/appointments/${appt.appointmentId}`)}
                onCancel={handleCancelAppointment}
              />
            ) : (
              <EmptyState
                icon={Calendar}
                title="No upcoming appointment."
                description="You have no scheduled doctor visits right now."
                actionLabel="Find a Doctor"
                onAction={() => navigate('/patient/find-doctor')}
              />
            )}

            {/* Waitlist overview */}
            <div style={{ marginTop: '2rem' }}>
              <div className="flex-between" style={{ marginBottom: '1rem' }}>
                <h3 style={{ fontSize: '1.15rem' }}>Waitlist Status</h3>
                <Link
                  to="/patient/waitlist"
                  style={{ fontSize: '0.825rem', color: 'var(--primary-600)', fontWeight: 600, textDecoration: 'none' }}
                >
                  Manage Waitlist &rarr;
                </Link>
              </div>

              {waitlists.length === 0 ? (
                <div className="card" style={{ padding: '1.25rem', textAlign: 'center', color: 'var(--gray-500)', fontSize: '0.875rem' }}>
                  No active waitlist requests. When slots are full, join the waitlist to receive automated cancellation openings.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {waitlists.map((wl) => (
                    <div
                      key={wl.waitlistId}
                      className="card"
                      style={{
                        padding: '1rem',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                      }}
                    >
                      <div>
                        <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>
                          {wl.specialtyName || 'Specialist Consultation'}
                        </div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)', marginTop: '2px' }}>
                          Preferred: {formatDate(wl.preferredDate)} • {wl.preferredMode}
                        </div>
                      </div>
                      <StatusBadge status={wl.status} />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Recent Appointments */}
          <div>
            <div className="flex-between" style={{ marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.15rem' }}>Recent History</h3>
              <Link
                to="/patient/appointments"
                style={{ fontSize: '0.825rem', color: 'var(--primary-600)', fontWeight: 600, textDecoration: 'none' }}
              >
                See All &rarr;
              </Link>
            </div>

            {recentAppointments.length === 0 ? (
              <EmptyState
                icon={CalendarCheck}
                title="No Appointment History"
                description="Your past consultation history and status will appear here."
              />
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {recentAppointments.map((appt) => (
                  <AppointmentCard
                    key={appt.appointmentId}
                    appointment={appt}
                    role="PATIENT"
                    onView={(item) => navigate(`/patient/appointments/${item.appointmentId}`)}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default PatientDashboard;
