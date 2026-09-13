import React, { useState, useEffect } from 'react';
import {
  CalendarRange,
  Plus,
  Trash2,
  Clock,
  Video,
  Building,
  AlertCircle,
  CheckCircle2,
  Calendar,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import availabilityService from '../../services/availabilityService';
import PageHeader from '../../components/common/PageHeader';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { DAYS_OF_WEEK } from '../../utils/constants';
import { getDayName, formatTime } from '../../utils/formatters';

export const DoctorAvailability = () => {
  const { doctorId } = useAuth();

  const [availabilities, setAvailabilities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const [formData, setFormData] = useState({
    dayOfWeek: 1,
    startTime: '09:00',
    endTime: '13:00',
    slotDurationMinutes: 15,
    mode: 'ONLINE',
  });

  useEffect(() => {
    loadAvailability();
  }, [doctorId]);

  const loadAvailability = async () => {
    if (!doctorId) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const data = await availabilityService.getAvailabilityByDoctor(doctorId);
      setAvailabilities(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError('Unable to load availability schedule.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!doctorId) return;

    setSubmitting(true);
    setError(null);
    setSuccess(null);

    try {
      const payload = {
        doctorId,
        dayOfWeek: parseInt(formData.dayOfWeek, 10),
        startTime: `${formData.startTime}:00`,
        endTime: `${formData.endTime}:00`,
        slotDurationMinutes: parseInt(formData.slotDurationMinutes, 10),
        mode: formData.mode,
      };

      await availabilityService.createAvailability(payload);
      setSuccess('Availability slot created successfully!');
      setModalOpen(false);
      loadAvailability();
    } catch (err) {
      setError(err.message || 'Failed to create availability rule.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeactivate = async (id) => {
    if (!window.confirm('Deactivate this availability rule?')) return;
    try {
      await availabilityService.deactivateAvailability(id);
      loadAvailability();
    } catch (err) {
      alert(err.message || 'Failed to deactivate availability');
    }
  };

  // Group by day of week
  const dayGroups = [1, 2, 3, 4, 5, 6, 7].map((dayNum) => {
    const dayLabel = getDayName(dayNum);
    const dayRules = availabilities.filter((a) => a.dayOfWeek === dayNum);
    return { dayNum, dayLabel, dayRules };
  });

  return (
    <div className="page-body">
      <PageHeader
        title="Doctor Availability Timetable"
        subtitle="Manage weekly recurring practice hours, slot lengths, and consultation modes."
        actions={
          <button onClick={() => setModalOpen(true)} className="btn btn-primary">
            <Plus size={16} />
            <span>Add Practice Hours</span>
          </button>
        }
      />

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          <CheckCircle2 size={18} />
          <span>{success}</span>
        </div>
      )}

      {loading ? (
        <LoadingSpinner text="Loading weekly availability timetable..." />
      ) : availabilities.length === 0 ? (
        <EmptyState
          icon={CalendarRange}
          title="No availability configured"
          description="Set up your weekly clinic schedule to start accepting patient appointments."
          actionLabel="Add Availability"
          onAction={() => setModalOpen(true)}
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {dayGroups.map((group) => {
            const hasRules = group.dayRules.length > 0;
            return (
              <div
                key={group.dayNum}
                className="card"
                style={{
                  padding: '1.25rem 1.5rem',
                  borderLeft: hasRules ? '4px solid var(--primary-600)' : '4px solid var(--gray-200)',
                }}
              >
                <div className="flex-between" style={{ marginBottom: hasRules ? '1rem' : '0' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <Calendar size={18} color={hasRules ? 'var(--primary-600)' : 'var(--gray-400)'} />
                    <h3 style={{ fontSize: '1.1rem', fontWeight: 700, color: hasRules ? 'var(--gray-900)' : 'var(--gray-400)' }}>
                      {group.dayLabel.toUpperCase()}
                    </h3>
                  </div>

                  {!hasRules && (
                    <span style={{ fontSize: '0.8rem', color: 'var(--gray-400)', fontStyle: 'italic' }}>
                      Off / No clinic hours scheduled
                    </span>
                  )}
                </div>

                {hasRules && (
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
                    {group.dayRules.map((av) => (
                      <div
                        key={av.availabilityId}
                        style={{
                          backgroundColor: av.isActive ? '#f8fafc' : '#fef2f2',
                          border: av.isActive ? '1px solid var(--gray-200)' : '1px solid #fecaca',
                          borderRadius: '8px',
                          padding: '1rem',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                        }}
                      >
                        <div>
                          <div style={{ fontWeight: 700, fontSize: '1rem', color: 'var(--gray-900)' }}>
                            {formatTime(av.startTime)} ───────── {formatTime(av.endTime)}
                          </div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.35rem', fontSize: '0.8rem' }}>
                            <span
                              style={{
                                padding: '0.15rem 0.5rem',
                                borderRadius: '4px',
                                fontWeight: 700,
                                backgroundColor: av.mode === 'ONLINE' ? '#f0fdfa' : '#f0f9ff',
                                color: av.mode === 'ONLINE' ? 'var(--accent-teal)' : 'var(--primary-700)',
                                border: av.mode === 'ONLINE' ? '1px solid #99f6e4' : '1px solid #bae6fd',
                              }}
                            >
                              {av.mode}
                            </span>
                            <span style={{ color: 'var(--gray-500)' }}>
                              {av.slotDurationMinutes} min slots
                            </span>
                          </div>
                        </div>

                        {av.isActive && (
                          <button
                            onClick={() => handleDeactivate(av.availabilityId)}
                            className="btn btn-secondary btn-sm"
                            style={{ color: '#dc2626' }}
                            title="Deactivate rule"
                          >
                            <Trash2 size={14} />
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {/* Add Availability Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Add Recurring Practice Hours"
      >
        <form onSubmit={handleCreate}>
          <div className="form-group">
            <label className="form-label">Day of Week *</label>
            <select
              value={formData.dayOfWeek}
              onChange={(e) => setFormData({ ...formData, dayOfWeek: e.target.value })}
              className="form-select"
            >
              {DAYS_OF_WEEK.map((d) => (
                <option key={d.value} value={d.value}>
                  {d.label}
                </option>
              ))}
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Start Time *</label>
              <input
                type="time"
                value={formData.startTime}
                onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                required
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label className="form-label">End Time *</label>
              <input
                type="time"
                value={formData.endTime}
                onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                required
                className="form-input"
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Slot Duration (Minutes)</label>
              <select
                value={formData.slotDurationMinutes}
                onChange={(e) => setFormData({ ...formData, slotDurationMinutes: e.target.value })}
                className="form-select"
              >
                <option value={10}>10 minutes</option>
                <option value={15}>15 minutes</option>
                <option value={20}>20 minutes</option>
                <option value={30}>30 minutes</option>
                <option value={45}>45 minutes</option>
                <option value={60}>60 minutes</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Consultation Mode *</label>
              <select
                value={formData.mode}
                onChange={(e) => setFormData({ ...formData, mode: e.target.value })}
                className="form-select"
              >
                <option value="ONLINE">ONLINE (Video Call)</option>
                <option value="OFFLINE">OFFLINE (In-Clinic)</option>
              </select>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button
              type="button"
              onClick={() => setModalOpen(false)}
              className="btn btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="btn btn-primary"
            >
              {submitting ? 'Creating...' : 'Save Availability'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default DoctorAvailability;
