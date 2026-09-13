import React, { useState, useEffect } from 'react';
import { Calendar, Search, Filter, AlertCircle } from 'lucide-react';
import doctorService from '../../services/doctorService';
import appointmentService from '../../services/appointmentService';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDate, formatTime } from '../../utils/formatters';

export const ManageAppointments = () => {
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState('');
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDoctors();
  }, []);

  const loadDoctors = async () => {
    try {
      const docs = await doctorService.getAllDoctors();
      setDoctors(Array.isArray(docs) ? docs : []);
      if (docs.length > 0) {
        setSelectedDoctorId(docs[0].doctorId);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    if (selectedDoctorId) {
      loadAppointments(selectedDoctorId, selectedDate);
    }
  }, [selectedDoctorId, selectedDate]);

  const loadAppointments = async (docId, date) => {
    setLoading(true);
    setError(null);
    try {
      const data = await appointmentService.getDoctorAppointments(docId, date || null);
      setAppointments(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError('Unable to fetch appointment records.');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (id) => {
    const reason = window.prompt('Administrative cancellation reason:');
    if (reason === null) return;
    try {
      await appointmentService.cancelAppointment(id, reason || 'Admin intervention');
      loadAppointments(selectedDoctorId, selectedDate);
    } catch (e) {
      alert(e.message || 'Failed to cancel appointment');
    }
  };

  return (
    <div className="page-body">
      <PageHeader
        title="Master Appointments Directory"
        subtitle="Search, filter, and govern appointment bookings across all hospital departments."
      />

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Filter controls */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1.25rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
          <div>
            <label className="form-label">Doctor Filter</label>
            <select
              value={selectedDoctorId}
              onChange={(e) => setSelectedDoctorId(e.target.value)}
              className="form-select"
            >
              {doctors.map((d) => (
                <option key={d.doctorId} value={d.doctorId}>
                  {d.doctorName || d.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="form-label">Date Filter</label>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="form-input"
            />
          </div>
        </div>
      </div>

      {loading ? (
        <LoadingSpinner text="Fetching appointments..." />
      ) : appointments.length === 0 ? (
        <EmptyState
          icon={Calendar}
          title="No appointments found"
          description="No bookings found for the selected doctor and date filter."
        />
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Appt #</th>
                <th>Patient</th>
                <th>Specialty</th>
                <th>Date & Time</th>
                <th>Mode</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {appointments.map((appt) => (
                <tr key={appt.appointmentId}>
                  <td style={{ fontWeight: 600 }}>
                    #{appt.appointmentNumber || `CX-${appt.appointmentId}`}
                  </td>
                  <td>{appt.patientName || `Patient #${appt.patientId}`}</td>
                  <td>{appt.specialtyName || 'Consultation'}</td>
                  <td>
                    {formatDate(appt.appointmentDate)} at {formatTime(appt.slotStartTime)}
                  </td>
                  <td>{appt.mode}</td>
                  <td>
                    <StatusBadge status={appt.status} />
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    {['BOOKED', 'CONFIRMED'].includes(appt.status) && (
                      <button
                        onClick={() => handleCancel(appt.appointmentId)}
                        className="btn btn-secondary btn-sm"
                        style={{ color: '#dc2626' }}
                      >
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default ManageAppointments;
