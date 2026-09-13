import React from 'react';
import { STATUS_COLORS } from '../../utils/constants';

export const StatusBadge = ({ status, text }) => {
  if (!status) return null;

  const styleConfig = STATUS_COLORS[status] || {
    bg: '#f3f4f6',
    text: '#4b5563',
    border: '#e5e7eb',
  };

  const displayText = text || status.replace('_', ' ');

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: '0.2rem 0.6rem',
        borderRadius: '9999px',
        fontSize: '0.75rem',
        fontWeight: '600',
        backgroundColor: styleConfig.bg,
        color: styleConfig.text,
        border: `1px solid ${styleConfig.border}`,
        letterSpacing: '0.025em',
        textTransform: 'uppercase',
      }}
    >
      {displayText}
    </span>
  );
};

export default StatusBadge;
