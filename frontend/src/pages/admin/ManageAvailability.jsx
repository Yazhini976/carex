import React, { useState, useEffect } from 'react';
import { CalendarRange, Plus, Stethoscope, CheckCircle2, AlertCircle } from 'lucide-react';
import doctorService from '../../services/doctorService';
import slotService from '../../services/slotService';
import PageHeader from '../../components/common/PageHeader';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import SlotCard from '../../components/common/SlotCard';

export const ManageAvailability = () => {
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState('');
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [generateModalOpen, setGenerateModalOpen] = useState(false);
  const [generating, setGenerating] = useState(false);

  const [genFormData, setGenFormData] = useState({
    startDate: new Date().toISOString().split('T')[0],
    endDate: new Date(Date.now() + 7 * 86400000).toISOString().split('T')[0],
  });

  useEffect(() => {
    loadDoctors();
  }, []);

  const loadDoctors = async () => {
    setLoading(true);
    try {
      const docs = await doctorService.getActiveDoctors();
      setDoctors(Array.isArray(docs) ? docs : []);
      if (docs.length > 0) {
        setSelectedDoctorId(docs[0].doctorId);
      }
    } catch (e) {
      console.error(e);
      setError('Unable to load doctors.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedDoctorId && selectedDate) {
      loadSlots(selectedDoctorId, selectedDate);
    }
  }, [selectedDoctorId, selectedDate]);

  const loadSlots = async (docId, date) => {
    setLoadingSlots(true);
    try {
      const data = await slotService.getSlotsByDoctorAndDate(docId, date);
      setSlots(Array.isArray(data) ? data : []);
    } catch (e) {
      console.error(e);
      setSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  const handleGenerateSlots = async (e) => {
    e.preventDefault();
    if (!selectedDoctorId) return;

    setGenerating(true);
    setError(null);
    setSuccess(null);

    try {
      await slotService.generateSlots({
        doctorId: parseInt(selectedDoctorId, 10),
        startDate: genFormData.startDate,
        endDate: genFormData.endDate,
      });
      setSuccess('Slots successfully generated from recurring availability!');
      setGenerateModalOpen(false);
      loadSlots(selectedDoctorId, selectedDate);
    } catch (err) {
      setError(err.message || 'Failed to generate slots.');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="page-body">
      <PageHeader
        title="Slot Generation & Capacity Management"
        subtitle="Generate appointment slots from doctor availability rules and inspect capacity."
        actions={
          <button
            onClick={() => setGenerateModalOpen(true)}
            className="btn btn-primary"
            disabled={!selectedDoctorId}
          >
            <Plus size={16} />
            <span>Generate Slots</span>
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

      {/* Selector controls */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1.25rem' }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
          <div>
            <label className="form-label">Select Doctor</label>
            <select
              value={selectedDoctorId}
              onChange={(e) => setSelectedDoctorId(e.target.value)}
              className="form-select"
            >
              {doctors.map((d) => (
                <option key={d.doctorId} value={d.doctorId}>
                  {d.doctorName || d.name} ({d.primarySpecialty || 'General'})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="form-label">Select Date</label>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="form-input"
            />
          </div>
        </div>
      </div>

      {/* Slots grid */}
      {loadingSlots ? (
        <LoadingSpinner text="Fetching doctor slots..." />
      ) : slots.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '2.5rem', color: 'var(--gray-500)' }}>
          No slots found for the selected date. Click "Generate Slots" to materialize slots from recurring schedule.
        </div>
      ) : (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(140px, 1fr))',
            gap: '1rem',
          }}
        >
          {slots.map((s) => (
            <SlotCard key={s.slotId} slot={s} />
          ))}
        </div>
      )}

      {/* Generate Slots Modal */}
      <Modal
        isOpen={generateModalOpen}
        onClose={() => setGenerateModalOpen(false)}
        title="Generate Appointment Slots"
      >
        <form onSubmit={handleGenerateSlots}>
          <div className="form-group">
            <label className="form-label">Start Date *</label>
            <input
              type="date"
              required
              value={genFormData.startDate}
              onChange={(e) => setGenFormData({ ...genFormData, startDate: e.target.value })}
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label className="form-label">End Date *</label>
            <input
              type="date"
              required
              value={genFormData.endDate}
              onChange={(e) => setGenFormData({ ...genFormData, endDate: e.target.value })}
              className="form-input"
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1.5rem' }}>
            <button
              type="button"
              onClick={() => setGenerateModalOpen(false)}
              className="btn btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={generating}
              className="btn btn-primary"
            >
              {generating ? 'Generating...' : 'Generate Calendar Slots'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ManageAvailability;
