import React, { useState, useEffect } from 'react';
import {
  Users,
  Clock,
  CheckCircle2,
  AlertTriangle,
  Play,
  RotateCw,
  Video,
  Building,
  ArrowRight,
  Sparkles,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import appointmentService from '../../services/appointmentService';
import intelligenceService from '../../services/intelligenceService';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatTime } from '../../utils/formatters';

export const DoctorQueue = () => {
  const { doctorId } = useAuth();
  const todayStr = new Date().toISOString().split('T')[0];

  const [queue, setQueue] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadQueue();
    const interval = setInterval(loadQueue, 15000); // Poll every 15s for live updates
    return () => clearInterval(interval);
  }, [doctorId]);

  const loadQueue = async (isManual = false) => {
    if (!doctorId) {
      setLoading(false);
      return;
    }

    if (isManual) setRefreshing(true);
    try {
      const data = await appointmentService.getDoctorAppointments(doctorId, todayStr);
      const list = Array.isArray(data) ? data : [];
      // Active queue are CONFIRMED or BOOKED appointments
      const activeList = list
        .filter((a) => a.status === 'CONFIRMED' || a.status === 'BOOKED')
        .sort((a, b) => (a.queueNumber || 999) - (b.queueNumber || 999));

      setQueue(activeList);
    } catch (err) {
      console.error(err);
      if (isManual) setError('Failed to refresh queue.');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const handleCallNext = async () => {
    if (queue.length === 0) return;
    const current = queue[0];
    try {
      await appointmentService.completeAppointment(current.appointmentId);
      loadQueue(true);
    } catch (err) {
      alert(err.message || 'Error updating queue');
    }
  };

  const handleNoShow = async (id) => {
    if (!window.confirm('Mark this patient as No-Show and call next?')) return;
    try {
      await appointmentService.markNoShow(id);
      loadQueue(true);
    } catch (err) {
      alert(err.message || 'Error marking no-show');
    }
  };

  const currentPatient = queue[0] || null;
  const nextPatient = queue[1] || null;
  const upcomingQueue = queue.slice(2);

  return (
    <div className="page-body">
      <PageHeader
        title="Live Consultation Queue & Patient Flow"
        subtitle="Manage live patient flow, queue numbers, and consultation throughput."
        actions={
          <button
            onClick={() => loadQueue(true)}
            disabled={refreshing}
            className="btn btn-secondary btn-sm"
          >
            <RotateCw size={14} className={refreshing ? 'spin' : ''} />
            <span>{refreshing ? 'Syncing...' : 'Live Sync'}</span>
          </button>
        }
      />

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <LoadingSpinner text="Fetching live queue and patient tokens..." />
      ) : queue.length === 0 ? (
        <EmptyState
          icon={Users}
          title="Patient Queue is Empty"
          description="There are no active waiting patients for today's clinic session."
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
          {/* Left Column: Current Active Patient */}
          <div>
            <div
              style={{
                fontSize: '0.8rem',
                fontWeight: 700,
                textTransform: 'uppercase',
                color: 'var(--primary-600)',
                letterSpacing: '0.05em',
                marginBottom: '0.75rem',
              }}
            >
              CURRENT CONSULTATION
            </div>

            {currentPatient && (
              <div
                className="card"
                style={{
                  border: '2px solid var(--primary-600)',
                  boxShadow: 'var(--shadow-lg)',
                  background: 'linear-gradient(180deg, #ffffff 0%, #f0f9ff 100%)',
                  padding: '1.75rem',
                }}
              >
                <div className="flex-between" style={{ marginBottom: '1rem' }}>
                  <span
                    style={{
                      fontSize: '1.25rem',
                      fontWeight: 800,
                      color: 'var(--primary-700)',
                    }}
                  >
                    Token #{currentPatient.queueNumber || '1'}
                  </span>
                  <StatusBadge status={currentPatient.status} />
                </div>

                <h3 style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--gray-900)' }}>
                  {currentPatient.patientName || `Patient #${currentPatient.patientId}`}
                </h3>

                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '1rem',
                    color: 'var(--gray-600)',
                    fontSize: '0.875rem',
                    margin: '0.75rem 0 1.25rem 0',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                    <Clock size={16} />
                    <span>Slot: {formatTime(currentPatient.slotStartTime)}</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                    {currentPatient.mode === 'ONLINE' ? <Video size={16} color="var(--accent-teal)" /> : <Building size={16} color="var(--primary-600)" />}
                    <span>{currentPatient.mode}</span>
                  </div>
                </div>

                {currentPatient.notes && (
                  <div
                    style={{
                      backgroundColor: '#ffffff',
                      border: '1px solid var(--gray-200)',
                      padding: '0.75rem 1rem',
                      borderRadius: '6px',
                      fontSize: '0.85rem',
                      color: 'var(--gray-700)',
                      marginBottom: '1.5rem',
                    }}
                  >
                    <strong>Reason / Chief Complaint:</strong> {currentPatient.notes}
                  </div>
                )}

                <div style={{ display: 'flex', gap: '0.75rem' }}>
                  <button
                    onClick={handleCallNext}
                    className="btn btn-success btn-lg"
                    style={{ flex: 1 }}
                  >
                    <CheckCircle2 size={18} />
                    <span>Complete & Call Next</span>
                  </button>
                  <button
                    onClick={() => handleNoShow(currentPatient.appointmentId)}
                    className="btn btn-secondary"
                    style={{ color: '#b45309' }}
                  >
                    No Show
                  </button>
                </div>
              </div>
            )}
          </div>

          {/* Right Column: Next Patient and Upcoming Queue */}
          <div>
            {/* NEXT Patient */}
            {nextPatient && (
              <div style={{ marginBottom: '2rem' }}>
                <div
                  style={{
                    fontSize: '0.8rem',
                    fontWeight: 700,
                    textTransform: 'uppercase',
                    color: '#d97706',
                    letterSpacing: '0.05em',
                    marginBottom: '0.75rem',
                  }}
                >
                  NEXT IN LINE
                </div>

                <div
                  className="card"
                  style={{
                    border: '1px solid #fde68a',
                    backgroundColor: '#fffbeb',
                    padding: '1.25rem',
                  }}
                >
                  <div className="flex-between">
                    <div>
                      <div style={{ fontWeight: 700, fontSize: '1.1rem', color: '#92400e' }}>
                        Token #{nextPatient.queueNumber || '2'} — {nextPatient.patientName || `Patient #${nextPatient.patientId}`}
                      </div>
                      <div style={{ fontSize: '0.8rem', color: '#b45309', marginTop: '0.25rem' }}>
                        Scheduled Time: {formatTime(nextPatient.slotStartTime)} • {nextPatient.mode}
                      </div>
                    </div>
                    <StatusBadge status={nextPatient.status} />
                  </div>
                </div>
              </div>
            )}

            {/* Upcoming Queue List */}
            <div>
              <div
                style={{
                  fontSize: '0.8rem',
                  fontWeight: 700,
                  textTransform: 'uppercase',
                  color: 'var(--gray-500)',
                  letterSpacing: '0.05em',
                  marginBottom: '0.75rem',
                }}
              >
                UPCOMING QUEUE ({upcomingQueue.length} PATIENTS)
              </div>

              {upcomingQueue.length === 0 ? (
                <div className="card" style={{ padding: '1rem', color: 'var(--gray-400)', fontSize: '0.875rem' }}>
                  No additional patients waiting in queue.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {upcomingQueue.map((item, idx) => (
                    <div
                      key={item.appointmentId}
                      className="card"
                      style={{
                        padding: '1rem 1.25rem',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <div
                          style={{
                            width: '32px',
                            height: '32px',
                            borderRadius: '50%',
                            backgroundColor: 'var(--gray-100)',
                            fontWeight: 700,
                            fontSize: '0.85rem',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          #{item.queueNumber || idx + 3}
                        </div>
                        <div>
                          <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>
                            {item.patientName || `Patient #${item.patientId}`}
                          </div>
                          <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>
                            {formatTime(item.slotStartTime)} • {item.mode}
                          </div>
                        </div>
                      </div>

                      <StatusBadge status={item.status} />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DoctorQueue;
