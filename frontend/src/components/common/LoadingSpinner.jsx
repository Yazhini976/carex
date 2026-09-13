import React from 'react';
import { Loader2 } from 'lucide-react';

export const LoadingSpinner = ({ size = 24, text = 'Loading...', className = '' }) => {
  return (
    <div
      className={`flex-center ${className}`}
      style={{
        padding: '2rem',
        flexDirection: 'column',
        gap: '0.75rem',
        color: 'var(--primary-600)',
      }}
    >
      <Loader2
        size={size}
        style={{
          animation: 'spin 1s linear infinite',
        }}
      />
      {text && (
        <span style={{ fontSize: '0.875rem', color: 'var(--gray-500)', fontWeight: 500 }}>
          {text}
        </span>
      )}
      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default LoadingSpinner;
