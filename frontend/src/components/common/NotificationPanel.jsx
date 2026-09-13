import React from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle, Clock, Calendar, AlertTriangle, ShieldAlert, ArrowRight } from 'lucide-react';

export const NotificationPanel = ({ notifications, unreadCount, onMarkRead, onMarkAllRead, onClose }) => {
  const getIcon = (type) => {
    switch (type) {
      case 'APPOINTMENT_CONFIRMED':
      case 'APPOINTMENT_COMPLETED':
        return <CheckCircle size={16} className="text-emerald-500" style={{ color: '#10b981' }} />;
      case 'APPOINTMENT_REMINDER':
        return <Clock size={16} className="text-amber-500" style={{ color: '#f59e0b' }} />;
      case 'DOCTOR_UNAVAILABLE':
      case 'SCHEDULING_ALERT':
        return <ShieldAlert size={16} className="text-red-500" style={{ color: '#ef4444' }} />;
      case 'WAITLIST_SLOT_AVAILABLE':
        return <Calendar size={16} className="text-indigo-500" style={{ color: '#6366f1' }} />;
      default:
        return <AlertTriangle size={16} className="text-blue-500" style={{ color: '#3b82f6' }} />;
    }
  };

  return (
    <div
      style={{
        position: 'absolute',
        top: '48px',
        right: 0,
        width: '360px',
        backgroundColor: '#ffffff',
        border: '1px solid var(--gray-200, #e2e8f0)',
        borderRadius: '12px',
        boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
        zIndex: 1000,
        overflow: 'hidden',
        animation: 'fadeIn 0.15s ease-out',
      }}
    >
      {/* Header */}
      <div
        style={{
          padding: '0.875rem 1rem',
          borderBottom: '1px solid var(--gray-100, #f1f5f9)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          backgroundColor: '#f8fafc',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#0f172a' }}>Notifications</span>
          {unreadCount > 0 && (
            <span
              style={{
                backgroundColor: '#ef4444',
                color: '#ffffff',
                fontSize: '0.7rem',
                fontWeight: 700,
                padding: '2px 7px',
                borderRadius: '9999px',
              }}
            >
              {unreadCount} new
            </span>
          )}
        </div>
        {unreadCount > 0 && (
          <button
            onClick={onMarkAllRead}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--primary-600, #0284c7)',
              fontSize: '0.75rem',
              cursor: 'pointer',
              fontWeight: 600,
            }}
          >
            Mark all read
          </button>
        )}
      </div>

      {/* Notifications List */}
      <div style={{ maxHeight: '320px', overflowY: 'auto' }}>
        {notifications.length === 0 ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: '#94a3b8', fontSize: '0.875rem' }}>
            No recent notifications
          </div>
        ) : (
          notifications.map((n) => {
            const isUnread = n.status === 'PENDING';
            return (
              <div
                key={n.id || n.notificationId}
                onClick={() => onMarkRead(n.id || n.notificationId)}
                style={{
                  padding: '0.875rem 1rem',
                  borderBottom: '1px solid #f1f5f9',
                  backgroundColor: isUnread ? '#f0f9ff' : '#ffffff',
                  cursor: 'pointer',
                  display: 'flex',
                  gap: '0.75rem',
                  alignItems: 'flex-start',
                  transition: 'background-color 0.15s ease',
                }}
              >
                <div style={{ marginTop: '2px', flexShrink: 0 }}>{getIcon(n.type)}</div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div
                    style={{
                      fontSize: '0.825rem',
                      color: isUnread ? '#0f172a' : '#475569',
                      fontWeight: isUnread ? 600 : 400,
                      lineHeight: '1.35',
                    }}
                  >
                    {n.message}
                  </div>
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.5rem',
                      marginTop: '0.25rem',
                      fontSize: '0.7rem',
                      color: '#94a3b8',
                    }}
                  >
                    <span>{n.type?.replace(/_/g, ' ')}</span>
                    {n.createdAt && <span>• {new Date(n.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {/* Footer */}
      <div
        style={{
          padding: '0.75rem 1rem',
          borderTop: '1px solid #f1f5f9',
          backgroundColor: '#f8fafc',
          textAlign: 'center',
        }}
      >
        <Link
          to="/notifications"
          onClick={onClose}
          style={{
            fontSize: '0.8rem',
            color: 'var(--primary-600, #0284c7)',
            textDecoration: 'none',
            fontWeight: 600,
            display: 'inline-flex',
            alignItems: 'center',
            gap: '0.35rem',
          }}
        >
          View all notifications <ArrowRight size={14} />
        </Link>
      </div>
    </div>
  );
};

export default NotificationPanel;
