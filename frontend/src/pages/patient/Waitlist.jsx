import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Clock,
  Plus,
  AlertCircle,
  CheckCircle2,
  Calendar,
  Sparkles,
  Video,
  Building,
  XCircle,
  Check,
  CheckCircle,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import waitlistService from '../../services/waitlistService';
import specialtyService from '../../services/specialtyService';
import doctorService from '../../services/doctorService';
import patientService from '../../services/patientService';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDate } from '../../utils/formatters';

export const Waitlist = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, profile, patientId } = useAuth();

  const queryParams = new URLSearchParams(location.search);
  const initialSpecialtyId = queryParams.get('specialtyId') || '';
  const initialDoctorId = queryParams.get('doctorId') || '';

  const [waitlists, setWaitlists] = useState([]);
  const [specialties, setSpecialties] = useState([]);
  const [doctors, setDoctors] = useState([]);

  // Form State
  const [selectedSpecialtyId, setSelectedSpecialtyId] = useState(initialSpecialtyId);
  const [selectedDoctorId, setSelectedDoctorId] = useState(initialDoctorId);
  const [preferredMode, setPreferredMode] = useState('ONLINE');
  const [preferredDate, setPreferredDate] = useState(
    new Date().toISOString().split('T')[0]
  );

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [cancellingId, setCancellingId] = useState(null);

  const activePatientId = patientId || profile?.patientId || profile?.id;

  useEffect(() => {
    loadData();
  }, [activePatientId]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [specs, docs] = await Promise.all([
        specialtyService.getAllSpecialties(),
        doctorService.getAllDoctors(),
      ]);
      setSpecialties(Array.isArray(specs) ? specs : []);
      setDoctors(Array.isArray(docs) ? docs : []);

      if (activePatientId) {
        const wl = await waitlistService.getPatientWaitlists(activePatientId);
        setWaitlists(Array.isArray(wl) ? wl : []);
      }
    } catch (err) {
      console.error(err);
      setError('Failed to load waitlist entries.');
    } finally {
      setLoading(false);
    }
  };

  const handleJoinWaitlist = async (e) => {
    e.preventDefault();

    let effectivePatientId = activePatientId;
    if (!effectivePatientId && user?.userId) {
      try {
        const p = await patientService.getPatientByUserId(user.userId);
        if (p) {
          effectivePatientId = p.patientId || p.id;
        }
      } catch (err) {
        console.warn('Could not auto-fetch patient profile during waitlist join', err);
      }
    }

    if (!effectivePatientId) {
      setError('Patient profile not found. Please log in.');
      return;
    }
    if (!selectedSpecialtyId) {
      setError('Please select a specialty.');
      return;
    }

    setSubmitting(true);
    setError(null);
    setSuccess(null);

    try {
      const payload = {
        patientId: effectivePatientId,
        specialtyId: parseInt(selectedSpecialtyId, 10),
        preferredDoctorId: selectedDoctorId ? parseInt(selectedDoctorId, 10) : null,
        preferredDate,
        preferredMode,
      };

      await waitlistService.joinWaitlist(payload);
      
      const specObj = specialties.find(s => s.specialtyId === parseInt(selectedSpecialtyId, 10));
      setSuccess({
        specialty: specObj?.specialtyName || 'Selected Specialty',
        mode: preferredMode,
        date: formatDate(preferredDate),
      });

      setSelectedSpecialtyId('');
      setSelectedDoctorId('');

      // Reload waitlists
      const wl = await waitlistService.getPatientWaitlists(effectivePatientId);
      setWaitlists(Array.isArray(wl) ? wl : []);
    } catch (err) {
      console.error(err);
      setError(err.message || 'Failed to join waitlist.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelWaitlist = async (id) => {
    if (!window.confirm('Decline and cancel this waitlist entry?')) return;
    try {
      await waitlistService.cancelWaitlist(id);
      loadData();
    } catch (err) {
      alert(err.message || 'Failed to cancel waitlist request');
    }
  };

  return (
    <div className="page-body">
      <PageHeader
        title="Smart Waitlist"
        subtitle="Automated queue that continuously matches you with cancelled or newly opened slots."
      />

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Success Card */}
      {success && (
        <div
          className="card fade-in"
          style={{
            backgroundColor: '#f0fdf4',
            border: '2px solid #a7f3d0',
            padding: '1.5rem',
            marginBottom: '2rem',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
            <CheckCircle2 size={24} color="#059669" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <h3 style={{ fontSize: '1.15rem', color: '#065f46', fontWeight: 700 }}>
                ✓ You are on the Smart Waitlist
              </h3>
              <div style={{ fontSize: '0.875rem', color: 'var(--gray-700)', marginTop: '0.35rem', lineHeight: 1.6 }}>
                <div><strong>Preferred:</strong> {success.specialty} • {success.mode} • {success.date}</div>
                <div style={{ color: '#047857', fontWeight: 600, marginTop: '0.25rem' }}>
                  CAREX will notify you when a matching slot becomes available.
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
        {/* Join Waitlist Form */}
        <div className="card">
          <div style={{ marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Sparkles size={20} color="var(--primary-600)" />
              <span>Join Smart Waitlist</span>
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--gray-600)', marginTop: '0.25rem' }}>
              Slots full? Don't want to keep checking? Join the queue and we'll reserve the next cancellation for you.
            </p>
          </div>

          <form onSubmit={handleJoinWaitlist}>
            <div className="form-group">
              <label className="form-label">Specialty Category *</label>
              <select
                value={selectedSpecialtyId}
                onChange={(e) => setSelectedSpecialtyId(e.target.value)}
                required
                className="form-select"
              >
                <option value="">-- Choose Specialty --</option>
                {specialties.map((s) => (
                  <option key={s.specialtyId} value={s.specialtyId}>
                    {s.specialtyName}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Preferred Doctor (Optional)</label>
              <select
                value={selectedDoctorId}
                onChange={(e) => setSelectedDoctorId(e.target.value)}
                className="form-select"
              >
                <option value="">Any Available Specialist</option>
                {doctors.map((d) => (
                  <option key={d.doctorId} value={d.doctorId}>
                    {d.doctorName || d.name} ({d.primarySpecialty || 'General'})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Preferred Date *</label>
              <input
                type="date"
                min={new Date().toISOString().split('T')[0]}
                value={preferredDate}
                onChange={(e) => setPreferredDate(e.target.value)}
                required
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Preferred Mode *</label>
              <select
                value={preferredMode}
                onChange={(e) => setPreferredMode(e.target.value)}
                className="form-select"
              >
                <option value="ONLINE">Online Video Consultation</option>
                <option value="OFFLINE">In-Clinic Hospital Visit</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="btn btn-primary btn-lg"
              style={{ width: '100%', marginTop: '0.5rem' }}
            >
              <span>{submitting ? 'Submitting to Smart Queue...' : 'JOIN SMART WAITLIST'}</span>
            </button>
          </form>
        </div>

        {/* Active Waitlists List */}
        <div>
          <h3 style={{ fontSize: '1.15rem', marginBottom: '1.25rem' }}>
            My Active Waitlist Requests
          </h3>

          {loading ? (
            <LoadingSpinner text="Checking waitlist queue & slot match status..." />
          ) : waitlists.length === 0 ? (
            <EmptyState
              icon={Clock}
              title="No Active Waitlist Entries"
              description="When all appointment slots are full, joining the waitlist puts you in priority line for automatic slot recovery."
            />
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {waitlists.map((wl) => (
                <div key={wl.waitlistId} className="card" style={{ padding: '1.25rem' }}>
                  <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                    <h4 style={{ fontSize: '1.05rem', color: 'var(--gray-900)', fontWeight: 700 }}>
                      {wl.specialtyName || 'Specialist Consultation'}
                    </h4>
                    <StatusBadge status={wl.status} />
                  </div>

                  {/* Slot Offer Card */}
                  {wl.status === 'OFFERED' && (
                    <div
                      className="fade-in"
                      style={{
                        margin: '1rem 0',
                        padding: '1rem',
                        backgroundColor: '#e0e7ff',
                        border: '2px solid #818cf8',
                        borderRadius: '8px',
                        color: '#312e81',
                      }}
                    >
                      <div style={{ fontSize: '0.95rem', fontWeight: 800 }}>
                        🎉 MATCHING SLOT FOUND
                      </div>
                      <div style={{ fontSize: '0.85rem', marginTop: '0.25rem', color: '#4338ca' }}>
                        <strong>{wl.preferredDoctorName || 'Dr. Priya'}</strong> • 5:00 PM • {wl.preferredMode}
                      </div>
                      <div style={{ fontSize: '0.8rem', color: '#4338ca', marginTop: '0.15rem' }}>
                        This slot is being offered to you.
                      </div>
                      <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.75rem' }}>
                        <button
                          onClick={() =>
                            navigate(
                              `/patient/book?doctorId=${wl.preferredDoctorId || ''}&specialtyId=${wl.specialtyId}&mode=${wl.preferredMode}`
                            )
                          }
                          className="btn btn-primary btn-sm"
                        >
                          ACCEPT
                        </button>
                        <button
                          onClick={() => handleCancelWaitlist(wl.waitlistId)}
                          className="btn btn-secondary btn-sm"
                        >
                          DECLINE
                        </button>
                      </div>
                    </div>
                  )}

                  <div
                    style={{
                      fontSize: '0.825rem',
                      color: 'var(--gray-600)',
                      display: 'flex',
                      flexWrap: 'wrap',
                      gap: '1rem',
                      marginTop: '0.5rem',
                    }}
                  >
                    <div>
                      <strong>Doctor:</strong> {wl.preferredDoctorName || 'Any Specialist'}
                    </div>
                    <div>
                      <strong>Date:</strong> {formatDate(wl.preferredDate)}
                    </div>
                    <div>
                      <strong>Mode:</strong> {wl.preferredMode}
                    </div>
                  </div>

                  {wl.status === 'WAITING' && (
                    <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '0.75rem' }}>
                      <button
                        onClick={() => handleCancelWaitlist(wl.waitlistId)}
                        className="btn btn-secondary btn-sm"
                        style={{ color: '#dc2626' }}
                      >
                        <XCircle size={14} />
                        <span>Cancel Request</span>
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Waitlist;
