import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import {
  Calendar as CalendarIcon,
  Clock,
  Video,
  Building,
  CheckCircle2,
  AlertCircle,
  ArrowLeft,
  ChevronRight,
  Sparkles,
  ShieldCheck,
  Check,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import doctorService from '../../services/doctorService';
import specialtyService from '../../services/specialtyService';
import slotService from '../../services/slotService';
import appointmentService from '../../services/appointmentService';
import PageHeader from '../../components/common/PageHeader';
import SlotCard from '../../components/common/SlotCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatDate, formatTime } from '../../utils/formatters';

export const BookAppointment = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, profile, patientId } = useAuth();

  const queryParams = new URLSearchParams(location.search);
  const initialDoctorId = queryParams.get('doctorId') || '';
  const initialSpecialtyId = queryParams.get('specialtyId') || '';
  const initialMode = queryParams.get('mode') || 'ONLINE';

  const [doctors, setDoctors] = useState([]);
  const [specialties, setSpecialties] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState(initialDoctorId);
  const [selectedSpecialtyId, setSelectedSpecialtyId] = useState(initialSpecialtyId);
  const [selectedMode, setSelectedMode] = useState(initialMode);
  
  const todayStr = new Date().toISOString().split('T')[0];
  const [selectedDate, setSelectedDate] = useState(todayStr);

  const [availableSlots, setAvailableSlots] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [notes, setNotes] = useState('');

  const [loadingDoctors, setLoadingDoctors] = useState(true);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [confirmedAppointment, setConfirmedAppointment] = useState(null);

  useEffect(() => {
    loadMetadata();
  }, []);

  const loadMetadata = async () => {
    setLoadingDoctors(true);
    setError(null);
    try {
      const [docs, specs] = await Promise.all([
        doctorService.getAllDoctors(),
        specialtyService.getAllSpecialties(),
      ]);
      setDoctors(Array.isArray(docs) ? docs : []);
      setSpecialties(Array.isArray(specs) ? specs : []);
    } catch (e) {
      console.error(e);
      setError('Could not load clinical metadata. Please check connection.');
    } finally {
      setLoadingDoctors(false);
    }
  };

  useEffect(() => {
    if (selectedDoctorId && selectedDate) {
      // Load slots for ALL modes — user sees both ONLINE and OFFLINE slots
      loadSlots(selectedDoctorId, selectedDate, null);
    } else {
      setAvailableSlots([]);
      setSelectedSlot(null);
    }
  }, [selectedDoctorId, selectedDate]); // mode removed — selecting a slot auto-syncs mode

  const loadSlots = async (docId, date, mode) => {
    setLoadingSlots(true);
    setSelectedSlot(null);
    setError(null);
    try {
      // Pass mode=null to load all slots regardless of consultation type
      const slots = await slotService.getAvailableSlots(docId, date, mode);
      setAvailableSlots(Array.isArray(slots) ? slots : []);
    } catch (e) {
      console.error(e);
      setAvailableSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  const handleBookingSubmit = async (e) => {
    e.preventDefault();

    let effectivePatientId = patientId || profile?.patientId || profile?.id;
    if (!effectivePatientId && user?.userId) {
      try {
        const p = await patientService.getPatientByUserId(user.userId);
        if (p) {
          effectivePatientId = p.patientId || p.id;
        }
      } catch (err) {
        console.warn('Could not auto-fetch patient profile during booking', err);
      }
    }

    if (!effectivePatientId) {
      setError('Patient profile not found. Please log in.');
      return;
    }
    if (!selectedDoctorId) {
      setError('Please select a doctor.');
      return;
    }
    if (!selectedSlot) {
      setError('Please select an available time slot.');
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      // Resolve specialtyId: use selected, fallback to doctor's first specialty, then first available
      let specId = selectedSpecialtyId
        ? parseInt(selectedSpecialtyId, 10)
        : null;

      if (!specId) {
        const docObj = doctors.find(d => d.doctorId === parseInt(selectedDoctorId, 10) || d.id === parseInt(selectedDoctorId, 10));
        if (docObj?.specialties?.length > 0) {
          specId = docObj.specialties[0].id || docObj.specialties[0].specialtyId;
        }
      }
      if (!specId && specialties.length > 0) {
        specId = specialties[0].specialtyId || specialties[0].id;
      }

      // Resolve slotId (backend returns 'id', service normalizes to 'slotId')
      const resolvedSlotId = selectedSlot.slotId || selectedSlot.id;
      // Always use the slot's own mode — backend enforces slot.mode === request.mode
      const resolvedMode = selectedSlot.mode || selectedMode;

      if (!specId || !resolvedSlotId) {
        setError('Missing required booking fields. Please select a specialty and time slot.');
        setSubmitting(false);
        return;
      }

      const payload = {
        patientId: effectivePatientId,
        doctorId: parseInt(selectedDoctorId, 10),
        specialtyId: parseInt(specId, 10),
        slotId: resolvedSlotId,
        mode: resolvedMode,
        notes: notes.trim() || undefined,
      };

      const result = await appointmentService.bookAppointment(payload);

      // Normalize appointment response field names (backend uses slotDate not appointmentDate)
      const normalizedResult = {
        ...result,
        appointmentId: result.appointmentId || result.id,
        appointmentDate: result.appointmentDate || result.slotDate || selectedDate,
        doctorName: result.doctorName || selectedDoctorObj?.doctorName || `Dr. #${payload.doctorId}`,
        specialtyName: result.specialtyName || selectedSpecialtyObj?.specialtyName || '',
        slotStartTime: result.slotStartTime || selectedSlot?.startTime,
        mode: result.mode || resolvedMode,
      };

      console.log('[CAREX] Appointment booked successfully:', normalizedResult);
      setConfirmedAppointment(normalizedResult);
    } catch (err) {
      const errMsg = err.message || '';
      console.error('[CAREX] Booking error:', err);
      if (errMsg.toLowerCase().includes('slot') || errMsg.toLowerCase().includes('concurrent') || errMsg.toLowerCase().includes('already booked')) {
        setError('This slot was just booked by another patient. Please select another slot.');
        if (selectedDoctorId && selectedDate) {
          loadSlots(selectedDoctorId, selectedDate, selectedMode);
        }
      } else {
        setError(errMsg || 'Unable to complete appointment booking. Please try another slot.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const selectedDoctorObj = doctors.find((d) => d.doctorId === parseInt(selectedDoctorId, 10));
  const selectedSpecialtyObj = specialties.find((s) => s.specialtyId === parseInt(selectedSpecialtyId, 10));

  // Compute current step index
  const getCurrentStep = () => {
    if (!selectedSpecialtyId && !selectedDoctorId) return 1;
    if (!selectedDoctorId) return 2;
    if (!selectedMode) return 3;
    if (!selectedSlot) return 4;
    return 5;
  };

  const activeStep = getCurrentStep();

  // Confirmation View
  if (confirmedAppointment) {
    return (
      <div className="page-body flex-center" style={{ minHeight: '75vh' }}>
        <div
          className="card fade-in"
          style={{
            maxWidth: '580px',
            width: '100%',
            padding: '2.5rem 2rem',
            textAlign: 'center',
            borderRadius: 'var(--radius-lg)',
            boxShadow: 'var(--shadow-xl)',
            border: '2px solid #a7f3d0',
          }}
        >
          <div
            style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              backgroundColor: '#dcfce7',
              color: '#15803d',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '1rem',
            }}
          >
            <CheckCircle2 size={38} />
          </div>

          <span style={{ fontSize: '0.8rem', fontWeight: 800, color: '#059669', letterSpacing: '0.05em', textTransform: 'uppercase' }}>
            ✓ APPOINTMENT CONFIRMED
          </span>

          <h2 style={{ fontSize: '1.6rem', color: 'var(--gray-900)', marginTop: '0.25rem' }}>
            Your Booking is Secured
          </h2>
          <p style={{ color: 'var(--gray-500)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
            A confirmation notification has been queued for dispatch.
          </p>

          <div
            style={{
              backgroundColor: 'var(--gray-50)',
              borderRadius: 'var(--radius-md)',
              padding: '1.25rem',
              margin: '1.5rem 0',
              textAlign: 'left',
              display: 'grid',
              gridTemplateColumns: '1fr 1fr',
              gap: '1rem',
              fontSize: '0.875rem',
              border: '1px solid var(--gray-200)',
            }}
          >
            <div>
              <span style={{ color: 'var(--gray-500)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                Appointment ID
              </span>
              <div style={{ fontWeight: 800, color: 'var(--primary-700)', fontSize: '1.05rem' }}>
                CX-{confirmedAppointment.appointmentId}
              </div>
            </div>

            <div>
              <span style={{ color: 'var(--gray-500)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                Mode
              </span>
              <div style={{ fontWeight: 600, color: 'var(--gray-800)' }}>
                {confirmedAppointment.mode === 'ONLINE' ? 'ONLINE (Video Call)' : 'OFFLINE (In-Clinic Visit)'}
              </div>
            </div>

            <div>
              <span style={{ color: 'var(--gray-500)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                Doctor & Specialty
              </span>
              <div style={{ fontWeight: 700, color: 'var(--gray-900)' }}>
                {confirmedAppointment.doctorName || selectedDoctorObj?.doctorName || `Dr. #${confirmedAppointment.doctorId}`}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--primary-600)' }}>
                {confirmedAppointment.specialtyName || selectedSpecialtyObj?.specialtyName || 'Specialist'}
              </div>
            </div>

            <div>
              <span style={{ color: 'var(--gray-500)', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 600 }}>
                Date & Time
              </span>
              <div style={{ fontWeight: 600, color: 'var(--gray-900)' }}>
                {formatDate(confirmedAppointment.appointmentDate)}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--gray-600)' }}>
                {formatTime(confirmedAppointment.slotStartTime)}
              </div>
            </div>

            <div style={{ gridColumn: '1 / -1', borderTop: '1px solid var(--gray-200)', paddingTop: '0.75rem' }}>
              <span style={{ color: '#b45309', fontSize: '0.75rem', textTransform: 'uppercase', fontWeight: 700 }}>
                CAREX estimated waiting time
              </span>
              <div style={{ fontWeight: 800, color: '#b45309', fontSize: '1rem', marginTop: '2px' }}>
                ~ {confirmedAppointment.estimatedWaitTimeMinutes || 12}–18 minutes
              </div>
            </div>

            {confirmedAppointment.mode === 'OFFLINE' && (
              <div style={{ gridColumn: '1 / -1', backgroundColor: '#f0f9ff', padding: '0.75rem', borderRadius: '6px', fontSize: '0.8rem', color: '#0369a1' }}>
                <strong>Arrival Guidance:</strong> Please arrive at the clinic reception 10 minutes before your scheduled slot for token verification.
              </div>
            )}
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center' }}>
            <Link
              to={`/patient/appointments/${confirmedAppointment.appointmentId}`}
              className="btn btn-primary"
            >
              VIEW APPOINTMENT
            </Link>
            <Link to="/patient/dashboard" className="btn btn-secondary">
              Back to Dashboard
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-body">
      <PageHeader
        title="Book an Appointment"
        subtitle="Schedule your doctor visit with real-time slot locking and predictive flow management."
      />

      {/* 5-Step Process Indicator */}
      <div className="card" style={{ marginBottom: '2rem', padding: '1.25rem 1.5rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.75rem' }}>
          {[
            { step: 1, label: '1. Specialty' },
            { step: 2, label: '2. Doctor' },
            { step: 3, label: '3. Mode' },
            { step: 4, label: '4. Slot' },
            { step: 5, label: '5. Confirm' },
          ].map((s) => {
            const isCompleted = activeStep > s.step;
            const isCurrent = activeStep === s.step;
            return (
              <div
                key={s.step}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  fontSize: '0.875rem',
                  fontWeight: isCurrent ? 700 : isCompleted ? 600 : 500,
                  color: isCurrent ? 'var(--primary-700)' : isCompleted ? '#059669' : 'var(--gray-400)',
                }}
              >
                <div
                  style={{
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    backgroundColor: isCurrent ? 'var(--primary-600)' : isCompleted ? '#10b981' : 'var(--gray-200)',
                    color: '#ffffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '0.75rem',
                    fontWeight: 700,
                  }}
                >
                  {isCompleted ? <Check size={14} /> : s.step}
                </div>
                <span>{s.label}</span>
              </div>
            );
          })}
        </div>
      </div>

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {loadingDoctors ? (
        <LoadingSpinner text="Loading doctors and schedule options..." />
      ) : (
        <form onSubmit={handleBookingSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
            {/* Left Column: Selections */}
            <div className="card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '1.25rem' }}>
                Appointment Parameters
              </h3>

              <div className="form-group">
                <label className="form-label">Specialty Category</label>
                <select
                  value={selectedSpecialtyId}
                  onChange={(e) => setSelectedSpecialtyId(e.target.value)}
                  className="form-select"
                >
                  <option value="">-- All Specialties --</option>
                  {specialties.map((s) => (
                    <option key={s.specialtyId || s.id} value={s.specialtyId || s.id}>
                      {s.specialtyName || s.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Select Doctor *</label>
                <select
                  value={selectedDoctorId}
                  onChange={(e) => setSelectedDoctorId(e.target.value)}
                  required
                  className="form-select"
                >
                  <option value="">-- Choose a Practitioner --</option>
                  {doctors.map((d) => (
                    <option key={d.doctorId} value={d.doctorId}>
                      {d.doctorName || d.name} ({d.primarySpecialty || 'General'})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Consultation Mode *</label>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                  <button
                    type="button"
                    onClick={() => setSelectedMode('ONLINE')}
                    className={`btn ${selectedMode === 'ONLINE' ? 'btn-primary' : 'btn-secondary'}`}
                    style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
                  >
                    <Video size={16} />
                    <span>Online Video</span>
                  </button>

                  <button
                    type="button"
                    onClick={() => setSelectedMode('OFFLINE')}
                    className={`btn ${selectedMode === 'OFFLINE' ? 'btn-primary' : 'btn-secondary'}`}
                    style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}
                  >
                    <Building size={16} />
                    <span>In-Clinic Visit</span>
                  </button>
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">Date *</label>
                <input
                  type="date"
                  min={todayStr}
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                  required
                  className="form-input"
                />
              </div>

              <div className="form-group">
                <label className="form-label">Consultation Notes (Optional)</label>
                <textarea
                  rows={2}
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Describe reasons for visit, ongoing symptoms, or prior consultations..."
                  className="form-textarea"
                />
              </div>
            </div>

            {/* Right Column: Slot Picker & Persistent Selection Summary */}
            <div className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              <h3 style={{ fontSize: '1.15rem', marginBottom: '1.25rem' }}>
                Select Open Time Slot
              </h3>

              {/* Persistent Selected Summary Box */}
              <div
                style={{
                  backgroundColor: 'var(--gray-50)',
                  border: '1px solid var(--gray-200)',
                  borderRadius: '8px',
                  padding: '1rem',
                  marginBottom: '1.25rem',
                  fontSize: '0.85rem',
                }}
              >
                <div style={{ fontWeight: 700, color: 'var(--gray-900)', marginBottom: '0.35rem' }}>
                  Selected Consultation Summary:
                </div>
                <div style={{ color: 'var(--gray-600)', display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                  <div><strong>Doctor:</strong> {selectedDoctorObj?.doctorName || 'Not selected'}</div>
                  <div><strong>Date & Mode:</strong> {formatDate(selectedDate)} • {selectedMode}</div>
                  <div><strong>Slot:</strong> {selectedSlot ? `${formatTime(selectedSlot.startTime)} - ${formatTime(selectedSlot.endTime)}` : 'Please pick a slot below'}</div>
                </div>
              </div>

              {!selectedDoctorId ? (
                <div className="flex-center" style={{ flex: 1, color: 'var(--gray-400)', textAlign: 'center', padding: '2rem' }}>
                  Please choose a doctor on the left to view available slots.
                </div>
              ) : loadingSlots ? (
                <LoadingSpinner text="Fetching open calendar slots with lock verification..." />
              ) : availableSlots.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '2rem', color: 'var(--gray-500)', flex: 1 }}>
                  <Clock size={36} color="var(--gray-300)" style={{ margin: '0 auto 0.75rem auto' }} />
                  <p style={{ fontWeight: 600, color: 'var(--gray-700)' }}>No available slots for this date & mode.</p>
                  <p style={{ fontSize: '0.8rem', marginTop: '0.25rem' }}>
                    Don't want to keep checking? Join the smart waitlist for automatic notification.
                  </p>
                  <button
                    type="button"
                    onClick={() => navigate(`/patient/waitlist?specialtyId=${selectedSpecialtyId}&doctorId=${selectedDoctorId}`)}
                    className="btn btn-primary btn-sm"
                    style={{ marginTop: '1rem' }}
                  >
                    JOIN SMART WAITLIST
                  </button>
                </div>
              ) : (
                <div style={{ flex: 1 }}>
                  <div
                    style={{
                      display: 'grid',
                      gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))',
                      gap: '0.75rem',
                      marginBottom: '1.5rem',
                      maxHeight: '340px',
                      overflowY: 'auto',
                    }}
                  >
                    {availableSlots.map((slot) => (
                      <SlotCard
                        key={slot.slotId}
                        slot={slot}
                        selected={selectedSlot?.slotId === slot.slotId}
                        onSelect={(s) => {
                          setSelectedSlot(s);
                          // Auto-sync mode toggle to match the selected slot's mode
                          if (s.mode) setSelectedMode(s.mode);
                        }}
                      />
                    ))}
                  </div>

                  {selectedSlot && (
                    <div
                      style={{
                        padding: '0.75rem 1rem',
                        backgroundColor: '#f0fdf4',
                        border: '1px solid #bbf7d0',
                        borderRadius: '6px',
                        fontSize: '0.85rem',
                        color: '#166534',
                        marginBottom: '1.5rem',
                      }}
                    >
                      ✓ Selected Slot: <strong>{formatTime(selectedSlot.startTime)} - {formatTime(selectedSlot.endTime)}</strong> ({selectedSlot.mode})
                    </div>
                  )}
                </div>
              )}

              <div style={{ borderTop: '1px solid var(--gray-200)', paddingTop: '1rem', display: 'flex', justifyContent: 'flex-end' }}>
                <button
                  type="submit"
                  disabled={submitting || !selectedSlot}
                  className="btn btn-primary btn-lg"
                  style={{ width: '100%' }}
                >
                  <span>{submitting ? 'Locking Slot & Booking...' : 'Confirm Appointment Booking'}</span>
                  {!submitting && <ChevronRight size={18} />}
                </button>
              </div>
            </div>
          </div>
        </form>
      )}
    </div>
  );
};

export default BookAppointment;
