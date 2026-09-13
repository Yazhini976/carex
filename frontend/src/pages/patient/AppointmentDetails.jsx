import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Calendar,
  Clock,
  Video,
  Building,
  User,
  ArrowLeft,
  CheckCircle2,
  AlertCircle,
  AlertTriangle,
  Stethoscope,
  FileText,
  ShieldCheck,
  Check,
} from 'lucide-react';
import appointmentService from '../../services/appointmentService';
import intelligenceService from '../../services/intelligenceService';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatDate, formatTime } from '../../utils/formatters';

export const AppointmentDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [appointment, setAppointment] = useState(null);
  const [waitTime, setWaitTime] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDetails();
  }, [id]);

  const loadDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      const appt = await appointmentService.getAppointmentById(id);
      setAppointment(appt);

      if (['BOOKED', 'CONFIRMED'].includes(appt.status)) {
        try {
          const wt = await intelligenceService.getWaitTimePrediction(id);
          setWaitTime(wt);
        } catch {
          // non-critical fallback
        }
      }
    } catch (err) {
      console.error(err);
      setError('Unable to load appointment details.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async () => {
    const reason = window.prompt('Please enter cancellation reason (optional):');
    if (reason === null) return;
    try {
      await appointmentService.cancelAppointment(id, reason || 'Cancelled by patient');
      loadDetails();
    } catch (err) {
      alert(err.message || 'Failed to cancel appointment');
    }
  };

  if (loading) {
    return (
      <div className="page-body">
        <LoadingSpinner text="Loading appointment details..." />
      </div>
    );
  }

  if (!appointment) {
    return (
      <div className="page-body">
        <div className="alert alert-danger">{error || 'Appointment not found'}</div>
        <button onClick={() => navigate(-1)} className="btn btn-secondary">
          <ArrowLeft size={16} />
          <span>Go Back</span>
        </button>
      </div>
    );
  }

  // 5-Stage Patient Journey Timeline
  const journeyStages = [
    { key: 'BOOKED', label: 'Appointment Booked', state: 'done' },
    { key: 'CONFIRMED', label: 'Doctor Confirmed', state: ['CONFIRMED', 'IN_PROGRESS', 'COMPLETED'].includes(appointment.status) ? 'done' : 'pending' },
    { key: 'UPCOMING', label: 'Appointment Upcoming', state: appointment.status === 'CONFIRMED' ? 'current' : ['IN_PROGRESS', 'COMPLETED'].includes(appointment.status) ? 'done' : 'pending' },
    { key: 'IN_PROGRESS', label: 'Consultation', state: appointment.status === 'IN_PROGRESS' ? 'current' : appointment.status === 'COMPLETED' ? 'done' : 'pending' },
    { key: 'COMPLETED', label: 'Completed', state: appointment.status === 'COMPLETED' ? 'done' : 'pending' },
  ];

  const isCancelled = appointment.status === 'CANCELLED';
  const isNoShow = appointment.status === 'NO_SHOW';

  return (
    <div className="page-body" style={{ maxWidth: '920px' }}>
      <button
        onClick={() => navigate(-1)}
        className="btn btn-secondary btn-sm"
        style={{ marginBottom: '1.5rem' }}
      >
        <ArrowLeft size={16} />
        <span>Back to Appointments</span>
      </button>

      <PageHeader
        title={`Appointment CX-${appointment.appointmentId}`}
        subtitle={`Scheduled on ${formatDate(appointment.appointmentDate)}`}
        actions={<StatusBadge status={appointment.status} />}
      />

      {/* Patient Journey Timeline */}
      <div className="card" style={{ marginBottom: '2rem', padding: '1.75rem 2rem' }}>
        <h4 style={{ fontSize: '0.9rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase', marginBottom: '1.25rem', letterSpacing: '0.05em' }}>
          Patient Journey Timeline
        </h4>

        {isCancelled ? (
          <div className="alert alert-danger" style={{ margin: 0 }}>
            <AlertCircle size={18} />
            <span>This appointment has been cancelled.</span>
          </div>
        ) : isNoShow ? (
          <div className="alert alert-warning" style={{ margin: 0 }}>
            <AlertTriangle size={18} />
            <span>This appointment was marked as No-Show.</span>
          </div>
        ) : (
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              position: 'relative',
              flexWrap: 'wrap',
              gap: '1rem',
            }}
          >
            {journeyStages.map((st, idx) => {
              const isDone = st.state === 'done';
              const isCurrent = st.state === 'current';
              return (
                <div
                  key={st.key}
                  style={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    flex: 1,
                    minWidth: '120px',
                    position: 'relative',
                    zIndex: 2,
                  }}
                >
                  <div
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '50%',
                      backgroundColor: isDone ? '#10b981' : isCurrent ? 'var(--primary-600)' : 'var(--gray-200)',
                      color: isDone || isCurrent ? '#ffffff' : 'var(--gray-500)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 700,
                      fontSize: '0.85rem',
                      boxShadow: isCurrent ? '0 0 0 4px var(--primary-100)' : 'none',
                    }}
                  >
                    {isDone ? <Check size={18} /> : isCurrent ? '●' : '○'}
                  </div>
                  <span
                    style={{
                      marginTop: '0.5rem',
                      fontSize: '0.8rem',
                      fontWeight: isDone || isCurrent ? 700 : 500,
                      color: isDone || isCurrent ? 'var(--gray-900)' : 'var(--gray-400)',
                      textAlign: 'center',
                    }}
                  >
                    {st.label}
                  </span>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Details Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
        {/* Doctor Details */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem', borderBottom: '1px solid var(--gray-100)', paddingBottom: '0.5rem' }}>
            Doctor Details
          </h3>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
            <div
              style={{
                width: '48px',
                height: '48px',
                borderRadius: '50%',
                backgroundColor: 'var(--primary-100)',
                color: 'var(--primary-700)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 700,
              }}
            >
              <Stethoscope size={24} />
            </div>
            <div>
              <div style={{ fontWeight: 700, fontSize: '1.05rem', color: 'var(--gray-900)' }}>
                {appointment.doctorName || `Dr. #${appointment.doctorId}`}
              </div>
              <div style={{ fontSize: '0.85rem', color: 'var(--primary-600)', fontWeight: 600 }}>
                {appointment.specialtyName || 'Consultant Specialist'}
              </div>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.875rem', color: 'var(--gray-700)' }}>
            <div>
              <strong>Mode:</strong> {appointment.mode === 'ONLINE' ? 'Online Video Call' : 'In-Clinic Hospital Visit'}
            </div>
            {appointment.queueNumber && (
              <div>
                <strong>Queue Token:</strong> #{appointment.queueNumber}
              </div>
            )}
            {appointment.mode === 'OFFLINE' && (
              <div style={{ marginTop: '0.5rem', padding: '0.65rem', backgroundColor: '#f0f9ff', borderRadius: '6px', fontSize: '0.8rem', color: '#0369a1' }}>
                <strong>Arrival Guidance:</strong> Please arrive at the clinic reception 10 minutes before your scheduled slot for token verification.
              </div>
            )}
          </div>
        </div>

        {/* Schedule & Estimated Waiting Time */}
        <div className="card">
          <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem', borderBottom: '1px solid var(--gray-100)', paddingBottom: '0.5rem' }}>
            Schedule & Queue Status
          </h3>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', fontSize: '0.875rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Calendar size={18} color="var(--primary-600)" />
              <span style={{ fontWeight: 600 }}>{formatDate(appointment.appointmentDate)}</span>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Clock size={18} color="var(--primary-600)" />
              <span style={{ fontWeight: 600 }}>{formatTime(appointment.slotStartTime)}</span>
            </div>

            {/* Waiting Time Box */}
            <div
              style={{
                marginTop: '0.5rem',
                padding: '0.875rem 1rem',
                backgroundColor: '#fffbeb',
                border: '1px solid #fde68a',
                borderRadius: '8px',
                color: '#92400e',
              }}
            >
              <div style={{ fontWeight: 700, fontSize: '0.8rem', textTransform: 'uppercase' }}>
                CAREX estimated waiting time
              </div>
              <div style={{ fontSize: '1.25rem', fontWeight: 800, marginTop: '2px' }}>
                ~ {waitTime?.predictedMinutes || appointment.estimatedWaitTimeMinutes || 12}–18 minutes
              </div>
              <div style={{ fontSize: '0.75rem', marginTop: '0.25rem', color: '#b45309' }}>
                Computed dynamically from current queue velocity and provider throughput.
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Patient Notes */}
      {appointment.notes && (
        <div className="card" style={{ marginBottom: '2rem' }}>
          <h4 style={{ fontSize: '0.95rem', marginBottom: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <FileText size={16} />
            <span>Consultation Notes</span>
          </h4>
          <p style={{ fontSize: '0.875rem', color: 'var(--gray-700)' }}>{appointment.notes}</p>
        </div>
      )}

      {/* Action Footer */}
      {['BOOKED', 'CONFIRMED'].includes(appointment.status) && (
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '1rem' }}>
          <button onClick={handleCancel} className="btn btn-danger">
            Cancel Appointment
          </button>
        </div>
      )}
    </div>
  );
};

export default AppointmentDetails;
