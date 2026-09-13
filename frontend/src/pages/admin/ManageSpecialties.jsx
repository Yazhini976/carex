import React, { useState, useEffect } from 'react';
import { Building, Plus, Trash2, Edit2, CheckCircle2, AlertCircle } from 'lucide-react';
import specialtyService from '../../services/specialtyService';
import PageHeader from '../../components/common/PageHeader';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';

export const ManageSpecialties = () => {
  const [specialties, setSpecialties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const [formData, setFormData] = useState({
    specialtyName: '',
    description: '',
  });

  useEffect(() => {
    loadSpecialties();
  }, []);

  const loadSpecialties = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await specialtyService.getAllSpecialties();
      setSpecialties(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError('Unable to load medical specialties.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!formData.specialtyName.trim()) return;

    setSubmitting(true);
    setError(null);
    setSuccess(null);

    try {
      await specialtyService.createSpecialty(formData);
      setSuccess('Specialty added successfully!');
      setModalOpen(false);
      setFormData({ specialtyName: '', description: '' });
      loadSpecialties();
    } catch (err) {
      setError(err.message || 'Failed to create specialty.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this specialty?')) return;
    try {
      await specialtyService.deleteSpecialty(id);
      loadSpecialties();
    } catch (err) {
      alert(err.message || 'Failed to delete specialty.');
    }
  };

  return (
    <div className="page-body">
      <PageHeader
        title="Clinical Specialties & Departments"
        subtitle="Configure hospital departments, medical specialties, and clinical taxonomy."
        actions={
          <button onClick={() => setModalOpen(true)} className="btn btn-primary">
            <Plus size={16} />
            <span>Add Specialty</span>
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
        <LoadingSpinner text="Loading specialties..." />
      ) : specialties.length === 0 ? (
        <EmptyState
          icon={Building}
          title="No specialties found"
          description="Click 'Add Specialty' to register a hospital medical department."
        />
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Specialty ID</th>
                <th>Name</th>
                <th>Description</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {specialties.map((s) => (
                <tr key={s.specialtyId}>
                  <td style={{ fontWeight: 600 }}>#{s.specialtyId}</td>
                  <td style={{ fontWeight: 600, color: 'var(--primary-700)' }}>
                    {s.specialtyName}
                  </td>
                  <td>{s.description || 'No description provided'}</td>
                  <td>
                    <span
                      style={{
                        padding: '0.2rem 0.5rem',
                        borderRadius: '9999px',
                        fontSize: '0.75rem',
                        fontWeight: 600,
                        backgroundColor: s.isActive ? '#dcfce7' : '#fee2e2',
                        color: s.isActive ? '#15803d' : '#b91c1c',
                      }}
                    >
                      {s.isActive ? 'ACTIVE' : 'INACTIVE'}
                    </span>
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      onClick={() => handleDelete(s.specialtyId)}
                      className="btn btn-secondary btn-sm"
                      style={{ color: '#dc2626' }}
                      title="Delete"
                    >
                      <Trash2 size={14} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Add Specialty Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        title="Add Medical Specialty"
      >
        <form onSubmit={handleCreate}>
          <div className="form-group">
            <label className="form-label">Specialty Name *</label>
            <input
              type="text"
              required
              value={formData.specialtyName}
              onChange={(e) => setFormData({ ...formData, specialtyName: e.target.value })}
              className="form-input"
              placeholder="e.g. Cardiology, Orthopedics, Pediatrics"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Description</label>
            <textarea
              rows={3}
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="form-textarea"
              placeholder="Diagnosis and treatment of heart and vascular disorders..."
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
              {submitting ? 'Creating...' : 'Save Specialty'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};

export default ManageSpecialties;
