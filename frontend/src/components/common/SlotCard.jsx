import React from 'react';
import { Clock, Video, Building } from 'lucide-react';
import { formatTime } from '../../utils/formatters';

export const SlotCard = ({ slot, selected, onSelect }) => {
  const isAvailable = slot.status === 'AVAILABLE';

  return (
    <div
      onClick={() => isAvailable && onSelect && onSelect(slot)}
      style={{
        padding: '0.75rem 1rem',
        borderRadius: 'var(--radius-sm)',
        border: `2px solid ${selected ? 'var(--primary-600)' : isAvailable ? 'var(--gray-200)' : 'var(--gray-100)'}`,
        backgroundColor: selected ? 'var(--primary-50)' : isAvailable ? '#ffffff' : 'var(--gray-100)',
        cursor: isAvailable ? 'pointer' : 'not-allowed',
        opacity: isAvailable ? 1 : 0.6,
        transition: 'all 0.15s ease',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.35rem',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontWeight: 600, fontSize: '0.875rem', color: selected ? 'var(--primary-700)' : 'var(--gray-800)' }}>
          <Clock size={14} />
          <span>{formatTime(slot.startTime)}</span>
        </div>
        <span
          style={{
            fontSize: '0.65rem',
            fontWeight: 700,
            textTransform: 'uppercase',
            color: isAvailable ? 'var(--accent-emerald)' : 'var(--gray-400)',
          }}
        >
          {slot.status}
        </span>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.75rem', color: 'var(--gray-500)' }}>
        {slot.mode === 'ONLINE' ? <Video size={12} /> : <Building size={12} />}
        <span>{slot.mode}</span>
      </div>
    </div>
  );
};

export default SlotCard;
