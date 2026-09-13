import React from 'react';
import { Calendar, Clock, Video, Building, User, ChevronRight, AlertTriangle } from 'lucide-react';
import StatusBadge from './StatusBadge';
import { formatDate, formatTime } from '../../utils/formatters';

export const AppointmentCard = ({
  appointment,
  onView,
  onCancel,
  onConfirm,
  onComplete,
  onNoShow,
  role = 'PATIENT',
}) => {
  if (!appointment) return null;

  return (
    <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--gray-500)' }}>
          #{appointment.appointmentNumber || `CX-${appointment.appointmentId}`}
        </span>
        <StatusBadge status={appointment.status} />
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
        <div
          style={{
            width: '42px',
            height: '42px',
            borderRadius: '50%',
            backgroundColor: 'var(--primary-100)',
            color: 'var(--primary-700)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
          }}
        >
          <User size={20} />
        </div>
        <div>
          <h4 style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--gray-900)' }}>
            {role === 'PATIENT' ? appointment.doctorName || `Dr. #${appointment.doctorId}` : appointment.patientName || `Patient #${appointment.patientId}`}
          </h4>
          <p style={{ fontSize: '0.8rem', color: 'var(--gray-500)' }}>
            {appointment.specialtyName || 'Consultation'}
          </p>
        </div>
      </div>

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '0.5rem',
          backgroundColor: 'var(--gray-50)',
          padding: '0.625rem 0.75rem',
          borderRadius: '6px',
          fontSize: '0.8rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--gray-700)' }}>
          <Calendar size={14} color="var(--primary-600)" />
          <span>{formatDate(appointment.appointmentDate)}</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--gray-700)' }}>
          <Clock size={14} color="var(--primary-600)" />
          <span>{formatTime(appointment.slotStartTime)}</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--gray-700)' }}>
          {appointment.mode === 'ONLINE' ? <Video size={14} color="var(--accent-teal)" /> : <Building size={14} color="var(--primary-600)" />}
          <span>{appointment.mode}</span>
        </div>
        {appointment.queueNumber && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--gray-700)' }}>
            <span style={{ fontWeight: 700, color: 'var(--primary-700)' }}>Token:</span>
            <span>#{appointment.queueNumber}</span>
          </div>
        )}
      </div>

      {appointment.estimatedWaitTimeMinutes !== undefined && appointment.estimatedWaitTimeMinutes !== null && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.75rem', color: '#b45309', backgroundColor: '#fef3c7', padding: '0.35rem 0.6rem', borderRadius: '4px' }}>
          <Clock size={12} />
          <span>Estimated wait: <strong>{appointment.estimatedWaitTimeMinutes} mins</strong></span>
        </div>
      )}

      {/* Action Buttons */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '0.5rem' }}>
        {onView && (
          <button onClick={() => onView(appointment)} className="btn btn-secondary btn-sm">
            <span>Details</span>
            <ChevronRight size={14} />
          </button>
        )}

        {role === 'DOCTOR' && appointment.status === 'BOOKED' && onConfirm && (
          <button onClick={() => onConfirm(appointment.appointmentId)} className="btn btn-primary btn-sm">
            Confirm
          </button>
        )}

        {role === 'DOCTOR' && appointment.status === 'CONFIRMED' && onComplete && (
          <button onClick={() => onComplete(appointment.appointmentId)} className="btn btn-success btn-sm">
            Complete
          </button>
        )}

        {role === 'DOCTOR' && appointment.status === 'CONFIRMED' && onNoShow && (
          <button onClick={() => onNoShow(appointment.appointmentId)} className="btn btn-secondary btn-sm" style={{ color: '#b45309' }}>
            No Show
          </button>
        )}

        {['BOOKED', 'CONFIRMED'].includes(appointment.status) && onCancel && (
          <button onClick={() => onCancel(appointment.appointmentId)} className="btn btn-danger btn-sm">
            Cancel
          </button>
        )}
      </div>
    </div>
  );
};

export default AppointmentCard;
