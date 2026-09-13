import React from 'react';

export const PageHeader = ({ title, subtitle, actions }) => {
  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '1rem',
        marginBottom: '1.75rem',
      }}
    >
      <div>
        <h1 style={{ fontSize: '1.625rem', fontWeight: 700, color: 'var(--gray-900)' }}>
          {title}
        </h1>
        {subtitle && (
          <p style={{ fontSize: '0.875rem', color: 'var(--gray-500)', marginTop: '0.25rem' }}>
            {subtitle}
          </p>
        )}
      </div>
      {actions && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          {actions}
        </div>
      )}
    </div>
  );
};

export default PageHeader;
