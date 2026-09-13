import React, { useState, useEffect } from 'react';
import { Calendar, Filter, AlertCircle } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import appointmentService from '../../services/appointmentService';
import PageHeader from '../../components/common/PageHeader';
import AppointmentCard from '../../components/common/AppointmentCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';

export const DoctorAppointments = () => {
  const { doctorId } = useAuth();

  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [activeTab, setActiveTab] = useState('ALL');
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadAppointments();
  }, [doctorId, selectedDate]);

  const loadAppointments = async () => {
    if (!doctorId) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const data = await appointmentService.getDoctorAppointments(doctorId, selectedDate || null);
      setAppointments(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError('Unable to fetch doctor appointments.');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (id) => {
    try {
      await appointmentService.confirmAppointment(id);
      loadAppointments();
    } catch (e) {
      alert(e.message || 'Failed to confirm appointment');
    }
  };

  const handleComplete = async (id) => {
    try {
      await appointmentService.completeAppointment(id);
      loadAppointments();
    } catch (e) {
      alert(e.message || 'Failed to complete appointment');
    }
  };

  const handleNoShow = async (id) => {
    if (!window.confirm('Mark this appointment as No-Show?')) return;
    try {
      await appointmentService.markNoShow(id);
      loadAppointments();
    } catch (e) {
      alert(e.message || 'Failed to mark no-show');
    }
  };

  const handleCancel = async (id) => {
    const reason = window.prompt('Cancellation reason (optional):');
    if (reason === null) return;
    try {
      await appointmentService.cancelAppointment(id, reason || 'Cancelled by doctor');
      loadAppointments();
    } catch (e) {
      alert(e.message || 'Failed to cancel appointment');
    }
  };

  const filteredAppointments = appointments.filter((a) => {
    if (activeTab === 'ALL') return true;
    return a.status === activeTab;
  });

  return (
    <div className="page-body">
      <PageHeader
        title="Doctor Schedule & Appointments"
        subtitle="Manage and review patient appointments for your clinical practice."
      />

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Filter Bar */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem 1.25rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '1rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <label className="form-label" style={{ marginBottom: 0 }}>Filter by Date:</label>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="form-input"
              style={{ width: 'auto' }}
            />
            {selectedDate && (
              <button
                onClick={() => setSelectedDate('')}
                className="btn btn-secondary btn-sm"
              >
                Show All Dates
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="tab-list">
        {['ALL', 'BOOKED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'].map((status) => (
          <button
            key={status}
            className={`tab-button ${activeTab === status ? 'active' : ''}`}
            onClick={() => setActiveTab(status)}
          >
            {status.replace('_', ' ')} (
            {status === 'ALL'
              ? appointments.length
              : appointments.filter((a) => a.status === status).length}
            )
          </button>
        ))}
      </div>

      {loading ? (
        <LoadingSpinner text="Fetching schedule records..." />
      ) : filteredAppointments.length === 0 ? (
        <EmptyState
          icon={Calendar}
          title="No appointments match this filter"
          description="Try choosing a different date or selecting 'Show All Dates'."
        />
      ) : (
        <div className="grid-cards">
          {filteredAppointments.map((appt) => (
            <AppointmentCard
              key={appt.appointmentId}
              appointment={appt}
              role="DOCTOR"
              onConfirm={handleConfirm}
              onComplete={handleComplete}
              onNoShow={handleNoShow}
              onCancel={handleCancel}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default DoctorAppointments;
