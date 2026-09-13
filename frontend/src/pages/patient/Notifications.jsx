import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  Bell, CheckCircle, Clock, Calendar, AlertTriangle, 
  ShieldAlert, CheckCheck, RefreshCw, Filter 
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import notificationService from '../../services/notificationService';

export const Notifications = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState('ALL');

  useEffect(() => {
    if (user?.userId) {
      loadNotifications();
    }
  }, [user?.userId]);

  const loadNotifications = async () => {
    setLoading(true);
    try {
      if (!user?.userId) return;
      const countRes = await notificationService.getUnreadCount(user.userId);
      setUnreadCount(countRes?.unreadCount || 0);

      const listRes = await notificationService.getUserNotifications(user.userId);
      setNotifications(Array.isArray(listRes) ? listRes : []);
    } catch (err) {
      console.error('Failed to load notifications', err);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      loadNotifications();
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      if (user?.userId) {
        await notificationService.markAllAsRead(user.userId);
        loadNotifications();
      }
    } catch (err) {
      console.error(err);
    }
  };

  const getFilteredNotifications = () => {
    if (activeFilter === 'UNREAD') {
      return notifications.filter(n => n.status === 'PENDING');
    }
    if (activeFilter === 'REMINDERS') {
      return notifications.filter(n => n.type === 'APPOINTMENT_REMINDER');
    }
    if (activeFilter === 'BOOKINGS') {
      return notifications.filter(n => 
        n.type?.startsWith('APPOINTMENT_') && n.type !== 'APPOINTMENT_REMINDER'
      );
    }
    if (activeFilter === 'ALERTS') {
      return notifications.filter(n => 
        n.type === 'DOCTOR_UNAVAILABLE' || n.type === 'SCHEDULING_ALERT' || n.type === 'WAITLIST_SLOT_AVAILABLE'
      );
    }
    return notifications;
  };

  const getIcon = (type) => {
    switch (type) {
      case 'APPOINTMENT_CONFIRMED':
      case 'APPOINTMENT_COMPLETED':
        return <CheckCircle size={20} style={{ color: '#10b981' }} />;
      case 'APPOINTMENT_REMINDER':
        return <Clock size={20} style={{ color: '#f59e0b' }} />;
      case 'DOCTOR_UNAVAILABLE':
      case 'SCHEDULING_ALERT':
        return <ShieldAlert size={20} style={{ color: '#ef4444' }} />;
      case 'WAITLIST_SLOT_AVAILABLE':
        return <Calendar size={20} style={{ color: '#6366f1' }} />;
      default:
        return <Bell size={20} style={{ color: '#0284c7' }} />;
    }
  };

  const filtered = getFilteredNotifications();

  return (
    <div className="container" style={{ padding: '2rem 1.5rem', maxWidth: '800px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 800, color: '#0f172a', margin: 0, display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Bell className="text-primary" size={28} />
            Notifications
          </h1>
          <p style={{ color: '#64748b', fontSize: '0.9rem', marginTop: '0.25rem', marginBottom: 0 }}>
            Real-time appointment updates, schedule reminders, and clinic alerts
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <button
            onClick={loadNotifications}
            className="btn btn-secondary btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}
            title="Refresh"
          >
            <RefreshCw size={14} /> Refresh
          </button>
          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllRead}
              className="btn btn-primary btn-sm"
              style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}
            >
              <CheckCheck size={14} /> Mark all read
            </button>
          )}
        </div>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem', overflowX: 'auto', paddingBottom: '4px' }}>
        {[
          { id: 'ALL', label: `All (${notifications.length})` },
          { id: 'UNREAD', label: `Unread (${unreadCount})` },
          { id: 'REMINDERS', label: 'Reminders' },
          { id: 'BOOKINGS', label: 'Bookings' },
          { id: 'ALERTS', label: 'Alerts' },
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveFilter(tab.id)}
            style={{
              padding: '0.5rem 1rem',
              borderRadius: '9999px',
              border: 'none',
              fontSize: '0.85rem',
              fontWeight: 600,
              cursor: 'pointer',
              backgroundColor: activeFilter === tab.id ? '#0284c7' : '#f1f5f9',
              color: activeFilter === tab.id ? '#ffffff' : '#475569',
              transition: 'all 0.15s ease',
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* List */}
      <div style={{ backgroundColor: '#ffffff', borderRadius: '12px', border: '1px solid #e2e8f0', overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' }}>
        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#94a3b8' }}>
            <RefreshCw className="animate-spin" size={24} style={{ margin: '0 auto 0.75rem auto' }} />
            <div>Loading notifications...</div>
          </div>
        ) : filtered.length === 0 ? (
          <div style={{ padding: '3.5rem 1.5rem', textAlign: 'center', color: '#94a3b8' }}>
            <Bell size={40} style={{ margin: '0 auto 1rem auto', opacity: 0.4 }} />
            <div style={{ fontWeight: 600, fontSize: '1rem', color: '#475569' }}>No notifications in this filter</div>
            <div style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>You're all caught up!</div>
          </div>
        ) : (
          filtered.map((n) => {
            const isUnread = n.status === 'PENDING';
            return (
              <div
                key={n.id || n.notificationId}
                style={{
                  padding: '1.25rem',
                  borderBottom: '1px solid #f1f5f9',
                  backgroundColor: isUnread ? '#f0f9ff' : '#ffffff',
                  display: 'flex',
                  gap: '1rem',
                  alignItems: 'flex-start',
                  transition: 'background-color 0.15s ease',
                }}
              >
                <div style={{ marginTop: '3px', flexShrink: 0 }}>{getIcon(n.type)}</div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '0.5rem' }}>
                    <span style={{ 
                      fontSize: '0.75rem', 
                      fontWeight: 700, 
                      textTransform: 'uppercase', 
                      letterSpacing: '0.5px',
                      color: isUnread ? '#0284c7' : '#64748b' 
                    }}>
                      {n.type?.replace(/_/g, ' ')}
                    </span>
                    <span style={{ fontSize: '0.75rem', color: '#94a3b8' }}>
                      {n.createdAt ? new Date(n.createdAt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) : ''}
                    </span>
                  </div>
                  <p style={{ 
                    margin: '0.5rem 0 0 0', 
                    fontSize: '0.925rem', 
                    color: isUnread ? '#0f172a' : '#334155', 
                    fontWeight: isUnread ? 600 : 400,
                    lineHeight: '1.45' 
                  }}>
                    {n.message}
                  </p>

                  <div style={{ marginTop: '0.75rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    {n.appointmentId && (
                      <Link
                        to={`/patient/appointments/${n.appointmentId}`}
                        style={{
                          fontSize: '0.8rem',
                          color: '#0284c7',
                          textDecoration: 'none',
                          fontWeight: 600,
                        }}
                      >
                        View Appointment #{n.appointmentId} →
                      </Link>
                    )}
                    {isUnread && (
                      <button
                        onClick={() => handleMarkAsRead(n.id || n.notificationId)}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#64748b',
                          fontSize: '0.75rem',
                          cursor: 'pointer',
                          textDecoration: 'underline',
                          padding: 0,
                        }}
                      >
                        Mark as read
                      </button>
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default Notifications;
