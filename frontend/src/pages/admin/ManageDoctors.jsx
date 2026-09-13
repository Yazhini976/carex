import React, { useState, useEffect } from 'react';
import {
  Stethoscope,
  Plus,
  Search,
  CheckCircle2,
  XCircle,
  Video,
  Building,
  AlertCircle,
  ToggleLeft,
  ToggleRight,
} from 'lucide-react';
import doctorService from '../../services/doctorService';
import specialtyService from '../../services/specialtyService';
import PageHeader from '../../components/common/PageHeader';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatCurrency } from '../../utils/formatters';

export const ManageDoctors = () => {
  const [doctors, setDoctors] = useState([]);
  const [specialties, setSpecialties] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // New Doctor form
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    phone: '',
    experienceYears: 5,
    consultationFee: 100,
    specialtyId: '',
    allowsOnline: true,
    allowsOffline: true,
    bio: '',
  });

  useEffect(() => {
    loadDoctors();
  }, []);

  const loadDoctors = async () => {
    setLoading(true);
    setError(null);
    try {
      const [docs, specs] = await Promise.all([
        doctorService.getAllDoctors(),
        specialtyService.getActiveSpecialties(),
      ]);
      setDoctors(Array.isArray(docs) ? docs : []);
      setSpecialties(Array.isArray(specs) ? specs : []);
    } catch (err) {
      console.error(err);
      setError('Unable to load doctors list.');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (id) => {
    try {
      await doctorService.toggleDoctorStatus(id);
      loadDoctors();
    } catch (err) {
      alert(err.message || 'Failed to toggle doctor status');
    }
  };

  const handleCreateDoctor = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      await doctorService.createDoctor({
        ...formData,
        experienceYears: parseInt(formData.experienceYears, 10),
        consultationFee: parseFloat(formData.consultationFee),
        specialtyId: formData.specialtyId ? parseInt(formData.specialtyId, 10) : undefined,
      });
      setModalOpen(false);
      loadDoctors();
    } catch (err) {
      setError(err.message || 'Failed to register doctor.');
    } finally {
      setSubmitting(false);
    }
  };

  const filteredDoctors = doctors.filter((d) =>
    (d.doctorName || d.name || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="page-body">
      <PageHeader
        title="Doctors Management"
        subtitle="Manage hospital practitioners, active clinical privileges, and consultation capabilities."
        actions={
          <button onClick={() => setModalOpen(true)} className="btn btn-primary">
            <Plus size={16} />
            <span>Add Doctor</span>
          </button>
        }
      />

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Search Bar */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
        <div style={{ position: 'relative', maxWidth: '400px' }}>
          <input
            type="text"
            placeholder="Search doctors by name..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="form-input"
            style={{ paddingLeft: '2.5rem' }}
          />
          <Search
            size={18}
            style={{
              position: 'absolute',
              left: '0.75rem',
              top: '50%',
              transform: 'translateY(-50)',
              color: 'var(--gray-400)',
            }}
          />
        </div>
      </div>

      {loading ? (
        <LoadingSpinner text="Loading doctors..." />
      ) : filteredDoctors.length === 0 ? (
        <EmptyState
          icon={Stethoscope}
          title="No doctors found"
          description="Click 'Add Doctor' to add a practitioner to the system."
        />
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Doctor</th>
                <th>Specialty</th>
                <th>Experience</th>
                <th>Fee</th>
                <th>Modes</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Toggle</th>
              </tr>
            </thead>
            <tbody>
              {filteredDoctors.map((doc) => (
                <tr key={doc.doctorId}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{doc.doctorName || doc.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>
                      ID #{doc.doctorId} • {doc.email}
                    </div>
                  </td>
                  <td>{doc.primarySpecialty || 'General'}</td>
                  <td>{doc.experienceYears || 0} yrs</td>
                  <td>{formatCurrency(doc.consultationFee || 0)}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.35rem' }}>
                      {doc.allowsOnline && <span title="Online"><Video size={14} color="var(--accent-teal)" /></span>}
                      {doc.allowsOffline && <span title="Offline"><Building size={14} color="var(--primary-600)" /></span>}
                    </div>
                  </td>
                  <td>
                    <span
                      style={{
                        padding: '0.2rem 0.5rem',
                        borderRadius: '9999px',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        backgroundColor: doc.isActive ? '#dcfce7' : '#fee2e2',
                        color: doc.isActive ? '#15803d' : '#b91c1c',
                      }}
                    >
                      {doc.isActive ? 'ACTIVE' : 'INACTIVE'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      onClick={() => handleToggleStatus(doc.doctorId)}
                      className="btn btn-secondary btn-sm"
                      title={doc.isActive ? 'Deactivate' : 'Activate'}
                    >
                      {doc.isActive ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add Doctor Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Register New Doctor"
      >
        <form onSubmit={handleCreateDoctor}>
          <div className="form-group">
            <label className="form-label">Doctor Full Name *</label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="form-input"
              placeholder="Dr. Emily Watson"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Email *</label>
              <input
                type="email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                className="form-input"
                placeholder="emily@hospital.com"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Initial Password *</label>
              <input
                type="password"
                required
                minLength={6}
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                className="form-input"
                placeholder="••••••••"
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Primary Specialty</label>
              <select
                value={formData.specialtyId}
                onChange={(e) => setFormData({ ...formData, specialtyId: e.target.value })}
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
              <label className="form-label">Fee ($)</label>
              <input
                type="number"
                min={0}
                value={formData.consultationFee}
                onChange={(e) => setFormData({ ...formData, consultationFee: e.target.value })}
                className="form-input"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Bio / Specialty Summary</label>
            <textarea
              rows={2}
              value={formData.bio}
              onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
              className="form-textarea"
            />
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
              {submitting ? 'Registering...' : 'Register Doctor'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ManageDoctors;
