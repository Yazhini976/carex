import React from 'react';
import { User, Stethoscope, Video, Building, Calendar, Check, Clock, Sparkles } from 'lucide-react';
import StatusBadge from './StatusBadge';

export const DoctorCard = ({ doctor, onBook, matchScore, explanation, nextSlot = '4:30 PM', estimatedWaitMinutes = 12 }) => {
  if (!doctor) return null;

  const score = matchScore !== undefined ? Math.round(matchScore) : null;

  return (
    <div
      className="card"
      style={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        position: 'relative',
        transition: 'transform 0.15s ease, box-shadow 0.15s ease',
      }}
    >
      {/* Header with Doctor Name & Specialty */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '0.75rem' }}>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
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
              flexShrink: 0,
              fontWeight: 700,
              fontSize: '1.1rem',
            }}
          >
            {doctor.doctorName ? doctor.doctorName.replace('Dr. ', '').charAt(0) : <User size={24} />}
          </div>
          <div>
            <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--gray-900)' }}>
              {doctor.doctorName || doctor.name || `Dr. #${doctor.doctorId}`}
            </h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', color: 'var(--primary-600)', fontSize: '0.825rem', marginTop: '2px', fontWeight: 600 }}>
              <Stethoscope size={14} />
              <span>{doctor.primarySpecialty || doctor.specialty || 'General Practitioner'}</span>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.35rem' }}>
          {doctor.allowsOnline && (
            <span title="Online Consultations" style={{ color: 'var(--accent-teal)' }}>
              <Video size={16} />
            </span>
          )}
          {doctor.allowsOffline && (
            <span title="In-Clinic Consultations" style={{ color: 'var(--primary-600)' }}>
              <Building size={16} />
            </span>
          )}
        </div>
      </div>

      {/* CAREX MATCH Progress Bar */}
      {score !== null && (
        <div
          style={{
            marginTop: '1rem',
            padding: '0.75rem',
            backgroundColor: '#f0f9ff',
            border: '1px solid #bae6fd',
            borderRadius: '8px',
          }}
        >
          <div className="flex-between" style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: 'var(--primary-800)', marginBottom: '0.35rem' }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Sparkles size={12} color="var(--primary-600)" />
              CAREX MATCH
            </span>
            <span style={{ fontSize: '0.85rem' }}>{score}%</span>
          </div>

          <div
            style={{
              height: '8px',
              backgroundColor: '#e0f2fe',
              borderRadius: '4px',
              overflow: 'hidden',
            }}
          >
            <div
              style={{
                width: `${score}%`,
                backgroundColor: 'var(--primary-600)',
                height: '100%',
                borderRadius: '4px',
                transition: 'width 0.4s ease',
              }}
            />
          </div>

          {/* Explainable Match Breakdown */}
          <div style={{ marginTop: '0.65rem', display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.75rem', color: 'var(--gray-700)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Check size={13} color="#059669" />
              <span>Specialty match</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Check size={13} color="#059669" />
              <span>Requested mode available</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Check size={13} color="#059669" />
              <span>Available today</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
              <Check size={13} color="#059669" />
              <span>Low estimated wait</span>
            </div>
          </div>
        </div>
      )}

      {/* Slot & Wait Metrics */}
      <div style={{ marginTop: '1rem', flex: 1 }}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', backgroundColor: 'var(--gray-50)', padding: '0.75rem', borderRadius: '6px', fontSize: '0.8rem' }}>
          <div>
            <span style={{ color: 'var(--gray-500)', fontSize: '0.7rem', display: 'block', textTransform: 'uppercase' }}>Next Slot</span>
            <strong style={{ color: 'var(--gray-800)' }}>{nextSlot}</strong>
          </div>
          <div>
            <span style={{ color: 'var(--gray-500)', fontSize: '0.7rem', display: 'block', textTransform: 'uppercase' }}>Estimated Wait</span>
            <strong style={{ color: '#b45309' }}>~ {estimatedWaitMinutes} min</strong>
          </div>
        </div>

        {explanation && !score && (
          <div style={{ marginTop: '0.5rem', padding: '0.5rem', backgroundColor: 'var(--gray-50)', borderRadius: '6px', fontSize: '0.75rem', color: 'var(--gray-600)' }}>
            {explanation}
          </div>
        )}
      </div>

      {/* Action Footer */}
      <div style={{ marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid var(--gray-100)' }}>
        {onBook && (
          <button
            onClick={() => onBook(doctor)}
            className="btn btn-primary"
            style={{ width: '100%' }}
          >
            <Calendar size={14} />
            <span>Book Appointment</span>
          </button>
        )}
      </div>
    </div>
  );
};

export default DoctorCard;
