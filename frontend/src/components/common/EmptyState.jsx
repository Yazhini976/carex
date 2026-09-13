import React from 'react';
import { Inbox } from 'lucide-react';

export const EmptyState = ({
  icon: Icon = Inbox,
  title = 'No records found',
  description = 'There is currently no data to display.',
  actionLabel,
  onAction,
}) => {
  return (
    <div
      className="card flex-center"
      style={{
        flexDirection: 'column',
        padding: '3.5rem 1.5rem',
        textAlign: 'center',
        background: '#ffffff',
      }}
    >
      <div
        style={{
          width: '56px',
          height: '56px',
          borderRadius: '50%',
          backgroundColor: 'var(--gray-100)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: '1rem',
          color: 'var(--gray-400)',
        }}
      >
        <Icon size={28} />
      </div>
      <h3 style={{ fontSize: '1.125rem', marginBottom: '0.375rem', color: 'var(--gray-800)' }}>
        {title}
      </h3>
      <p style={{ maxWidth: '400px', fontSize: '0.875rem', marginBottom: actionLabel ? '1.25rem' : 0 }}>
        {description}
      </p>
      {actionLabel && onAction && (
        <button onClick={onAction} className="btn btn-primary btn-sm">
          {actionLabel}
        </button>
      )}
    </div>
  );
};

export default EmptyState;
