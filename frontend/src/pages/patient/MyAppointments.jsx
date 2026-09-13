import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar, Plus, AlertCircle } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import appointmentService from '../../services/appointmentService';
import PageHeader from '../../components/common/PageHeader';
import AppointmentCard from '../../components/common/AppointmentCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';

export const MyAppointments = () => {
  const { patientId } = useAuth();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('UPCOMING');
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadAppointments();
  }, [patientId]);

  const loadAppointments = async () => {
    if (!patientId) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const data = await appointmentService.getPatientAppointments(patientId);
      setAppointments(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError('Unable to load appointments. Please check connection.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id) => {
    const reason = window.prompt('Please provide a reason for cancellation (optional):');
    if (reason === null) return; // User pressed cancel on prompt

    try {
      await appointmentService.cancelAppointment(id, reason || 'Cancelled by patient');
      loadAppointments();
    } catch (err) {
      alert(err.message || 'Failed to cancel appointment.');
    }
  };

  const filteredAppointments = appointments.filter((a) => {
    if (activeTab === 'UPCOMING') {
      return a.status === 'BOOKED' || a.status === 'CONFIRMED';
    }
    if (activeTab === 'COMPLETED') {
      return a.status === 'COMPLETED';
    }
    if (activeTab === 'CANCELLED') {
      return a.status === 'CANCELLED';
    }
    if (activeTab === 'NO_SHOW') {
      return a.status === 'NO_SHOW';
    }
    return true;
  });

  return (
    <div className="page-body">
      <PageHeader
        title="My Appointments"
        subtitle="Track your scheduled appointments, consultation history, and cancel or review visits."
        actions={
          <button
            onClick={() => navigate('/patient/book')}
            className="btn btn-primary"
          >
            <Plus size={16} />
            <span>Book New</span>
          </button>
        }
      />

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Tabs */}
      <div className="tab-list">
        <button
          className={`tab-button ${activeTab === 'UPCOMING' ? 'active' : ''}`}
          onClick={() => setActiveTab('UPCOMING')}
        >
          Upcoming (
          {appointments.filter((a) => a.status === 'BOOKED' || a.status === 'CONFIRMED').length}
          )
        </button>
        <button
          className={`tab-button ${activeTab === 'COMPLETED' ? 'active' : ''}`}
          onClick={() => setActiveTab('COMPLETED')}
        >
          Completed ({appointments.filter((a) => a.status === 'COMPLETED').length})
        </button>
        <button
          className={`tab-button ${activeTab === 'CANCELLED' ? 'active' : ''}`}
          onClick={() => setActiveTab('CANCELLED')}
        >
          Cancelled ({appointments.filter((a) => a.status === 'CANCELLED').length})
        </button>
        <button
          className={`tab-button ${activeTab === 'NO_SHOW' ? 'active' : ''}`}
          onClick={() => setActiveTab('NO_SHOW')}
        >
          No-Show ({appointments.filter((a) => a.status === 'NO_SHOW').length})
        </button>
      </div>

      {loading ? (
        <LoadingSpinner text="Loading appointment records..." />
      ) : filteredAppointments.length === 0 ? (
        <EmptyState
          icon={Calendar}
          title={`No ${activeTab.toLowerCase()} appointments`}
          description={
            activeTab === 'UPCOMING'
              ? 'You do not have any upcoming consultations scheduled.'
              : `No appointments found under the ${activeTab.toLowerCase()} category.`
          }
          actionLabel={activeTab === 'UPCOMING' ? 'Book Appointment' : null}
          onAction={() => navigate('/patient/book')}
        />
      ) : (
        <div className="grid-cards">
          {filteredAppointments.map((appt) => (
            <AppointmentCard
              key={appt.appointmentId}
              appointment={appt}
              role="PATIENT"
              onView={(item) => navigate(`/patient/appointments/${item.appointmentId}`)}
              onCancel={handleCancel}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default MyAppointments;
