import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Calendar,
  Users,
  Clock,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  PlayCircle,
  Video,
  Building,
  Sparkles,
  UserCheck,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import appointmentService from '../../services/appointmentService';
import intelligenceService from '../../services/intelligenceService';
import PageHeader from '../../components/common/PageHeader';
import AppointmentCard from '../../components/common/AppointmentCard';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDate, formatTime } from '../../utils/formatters';

export const DoctorDashboard = () => {
  const { user, doctorId } = useAuth();
  const navigate = useNavigate();

  const todayStr = new Date().toISOString().split('T')[0];
  const [appointments, setAppointments] = useState([]);
  const [workload, setWorkload] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  };

  useEffect(() => {
    loadDashboard();
  }, [doctorId]);

  const loadDashboard = async () => {
    if (!doctorId) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const [apptsRes, wlRes] = await Promise.all([
        appointmentService.getDoctorAppointments(doctorId, todayStr),
        intelligenceService.getDoctorWorkload(doctorId).catch(() => null),
      ]);

      setAppointments(Array.isArray(apptsRes) ? apptsRes : []);
      setWorkload(wlRes);
    } catch (err) {
      console.error(err);
      setError('Unable to load doctor dashboard data.');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (id) => {
    try {
      await appointmentService.confirmAppointment(id);
      loadDashboard();
    } catch (e) {
      alert(e.message || 'Error confirming appointment');
    }
  };

  const handleComplete = async (id) => {
    try {
      await appointmentService.completeAppointment(id);
      loadDashboard();
    } catch (e) {
      alert(e.message || 'Error completing appointment');
    }
  };

  const handleNoShow = async (id) => {
    if (!window.confirm('Mark this appointment as No-Show?')) return;
    try {
      await appointmentService.markNoShow(id);
      loadDashboard();
    } catch (e) {
      alert(e.message || 'Error marking no-show');
    }
  };

  const handleCancel = async (id) => {
    const reason = window.prompt('Cancellation reason (optional):');
    if (reason === null) return;
    try {
      await appointmentService.cancelAppointment(id, reason || 'Cancelled by provider');
      loadDashboard();
    } catch (e) {
      alert(e.message || 'Error cancelling appointment');
    }
  };

  // Metrics
  const totalToday = appointments.length;
  const activeQueue = appointments.filter((a) => a.status === 'CONFIRMED' || a.status === 'BOOKED');
  const completedCount = appointments.filter((a) => a.status === 'COMPLETED').length;
  const noShowCount = appointments.filter((a) => a.status === 'NO_SHOW').length;

  const currentPatient = activeQueue[0] || null;
  const nextPatient = activeQueue[1] || null;
  const upcomingPatients = activeQueue.slice(2);

  return (
    <div className="page-body">
      <PageHeader
        title={`${getGreeting()}, Dr. ${user?.name || 'Practitioner'}`}
        subtitle={`Clinical Queue & Appointments Overview — ${formatDate(todayStr)}`}
        actions={
          <button
            onClick={() => navigate('/doctor/queue')}
            className="btn btn-primary"
            style={{
              background: 'linear-gradient(135deg, #0284c7 0%, #0d9488 100%)',
              border: 'none',
              boxShadow: 'var(--shadow-md)',
            }}
          >
            <PlayCircle size={16} />
            <span>Open Live Queue</span>
          </button>
        }
      />

      {/* 5 KPI Metric Cards */}
      <div className="grid-cards" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(190px, 1fr))', marginBottom: '2rem' }}>
        <div className="card">
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
            Today's Appointments
          </span>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--gray-900)', marginTop: '0.25rem' }}>
            {totalToday}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
            Scheduled Visits
          </div>
        </div>

        <div className="card">
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
            Current Queue
          </span>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--primary-600)', marginTop: '0.25rem' }}>
            {activeQueue.length}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--primary-600)', marginTop: '0.25rem' }}>
            Waiting in Line
          </div>
        </div>

        <div className="card">
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
            Completed
          </span>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#15803d', marginTop: '0.25rem' }}>
            {completedCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
            Finished Consultations
          </div>
        </div>

        <div className="card">
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
            No-Show
          </span>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: '#b45309', marginTop: '0.25rem' }}>
            {noShowCount}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
            Missed Visits
          </div>
        </div>

        <div className="card">
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
            Workload Index
          </span>
          <div style={{ fontSize: '1.75rem', fontWeight: 800, color: 'var(--accent-indigo)', marginTop: '0.25rem' }}>
            {workload?.workloadScore ? `${Math.round(workload.workloadScore * 100)}/100` : '32/100'}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
            {workload?.workloadLevel || 'OPTIMAL'} Load
          </div>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner text="Loading practice schedule & patient queue..." />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
          {/* Patient Queue Flow Spotlight */}
          <div>
            <div className="flex-between" style={{ marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.15rem' }}>Patient Queue Spotlight</h3>
              <Link to="/doctor/queue" style={{ fontSize: '0.825rem', color: 'var(--primary-600)', fontWeight: 600, textDecoration: 'none' }}>
                Full Live Queue &rarr;
              </Link>
            </div>

            {/* Current Patient */}
            {currentPatient ? (
              <div
                className="card"
                style={{
                  border: '2px solid #bae6fd',
                  backgroundColor: '#f0f9ff',
                  padding: '1.5rem',
                  marginBottom: '1.25rem',
                }}
              >
                <div className="flex-between" style={{ marginBottom: '0.75rem' }}>
                  <span style={{ fontSize: '0.75rem', fontWeight: 800, color: 'var(--primary-700)', textTransform: 'uppercase' }}>
                    CURRENT PATIENT
                  </span>
                  <StatusBadge status={currentPatient.status} />
                </div>

                <h4 style={{ fontSize: '1.3rem', fontWeight: 800, color: 'var(--gray-900)' }}>
                  Patient #{currentPatient.patientId} {currentPatient.patientName ? `(${currentPatient.patientName})` : ''}
                </h4>
                <div style={{ fontSize: '0.85rem', color: 'var(--gray-600)', marginTop: '0.25rem' }}>
                  Scheduled: {formatTime(currentPatient.slotStartTime)} • {currentPatient.mode}
                </div>

                <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.25rem' }}>
                  <button
                    onClick={() => handleComplete(currentPatient.appointmentId)}
                    className="btn btn-success btn-sm"
                    style={{ flex: 1 }}
                  >
                    <CheckCircle2 size={16} />
                    <span>Complete Consultation</span>
                  </button>
                  <button
                    onClick={() => handleNoShow(currentPatient.appointmentId)}
                    className="btn btn-secondary btn-sm"
                    style={{ color: '#b45309' }}
                  >
                    Mark No-Show
                  </button>
                </div>
              </div>
            ) : (
              <div className="card" style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--gray-400)', marginBottom: '1.25rem' }}>
                No patient currently in consultation.
              </div>
            )}

            {/* Next & Upcoming Queue */}
            <div className="card">
              <h4 style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase', marginBottom: '0.75rem' }}>
                NEXT & UPCOMING PATIENTS
              </h4>

              {nextPatient && (
                <div style={{ padding: '0.75rem', backgroundColor: '#fffbeb', border: '1px solid #fde68a', borderRadius: '6px', marginBottom: '0.75rem' }}>
                  <span style={{ fontSize: '0.7rem', fontWeight: 800, color: '#92400e', textTransform: 'uppercase' }}>NEXT:</span>
                  <div style={{ fontWeight: 700, color: '#92400e', fontSize: '0.95rem' }}>
                    Patient #{nextPatient.patientId} {nextPatient.patientName ? `(${nextPatient.patientName})` : ''} — {formatTime(nextPatient.slotStartTime)} ({nextPatient.mode})
                  </div>
                </div>
              )}

              {upcomingPatients.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  <span style={{ fontSize: '0.7rem', fontWeight: 800, color: 'var(--gray-400)', textTransform: 'uppercase' }}>UPCOMING:</span>
                  {upcomingPatients.map((p, idx) => (
                    <div key={p.appointmentId} style={{ fontSize: '0.85rem', color: 'var(--gray-700)', padding: '0.35rem 0', borderBottom: idx < upcomingPatients.length - 1 ? '1px solid var(--gray-100)' : 'none' }}>
                      Patient #{p.patientId} • {formatTime(p.slotStartTime)} • {p.mode}
                    </div>
                  ))}
                </div>
              ) : !nextPatient ? (
                <div style={{ color: 'var(--gray-400)', fontSize: '0.85rem' }}>No upcoming queue.</div>
              ) : null}
            </div>
          </div>

          {/* Today's Full Schedule List */}
          <div>
            <div className="flex-between" style={{ marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.15rem' }}>Today's Appointments ({appointments.length})</h3>
              <Link to="/doctor/appointments" style={{ fontSize: '0.825rem', color: 'var(--primary-600)', fontWeight: 600, textDecoration: 'none' }}>
                All Dates &rarr;
              </Link>
            </div>

            {appointments.length === 0 ? (
              <EmptyState
                icon={Calendar}
                title="No appointments scheduled today"
                description="Your calendar is open for today."
              />
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', maxHeight: '520px', overflowY: 'auto' }}>
                {appointments.map((appt) => (
                  <AppointmentCard
                    key={appt.appointmentId}
                    appointment={appt}
                    role="DOCTOR"
                    onConfirm={handleConfirm}
                    onComplete={handleComplete}
                    onNoShow={handleNoShow}
                    onCancel={handleCancel}
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

export default DoctorDashboard;
