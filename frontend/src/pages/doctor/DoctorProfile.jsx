import React, { useState, useEffect } from 'react';
import { Stethoscope, DollarSign, Award, FileText, CheckCircle2, AlertCircle } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import doctorService from '../../services/doctorService';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const DoctorProfile = () => {
  const { user, profile, refreshProfile } = useAuth();

  const [formData, setFormData] = useState({
    experienceYears: 0,
    consultationFee: 0,
    allowsOnline: true,
    allowsOffline: true,
    bio: '',
  });

  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (profile) {
      setFormData({
        experienceYears: profile.experienceYears || 0,
        consultationFee: profile.consultationFee || 0,
        allowsOnline: profile.allowsOnline ?? true,
        allowsOffline: profile.allowsOffline ?? true,
        bio: profile.bio || '',
      });
    }
  }, [profile]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value,
    });
    if (success) setSuccess(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!profile?.doctorId) {
      setError('Doctor record not found.');
      return;
    }

    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      await doctorService.updateDoctor(profile.doctorId, {
        ...formData,
        experienceYears: parseInt(formData.experienceYears, 10),
        consultationFee: parseFloat(formData.consultationFee),
      });
      setSuccess('Doctor profile updated successfully!');
      if (refreshProfile) refreshProfile();
    } catch (err) {
      setError(err.message || 'Failed to update doctor profile.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page-body" style={{ maxWidth: '800px' }}>
      <PageHeader
        title="Doctor Profile & Practice Settings"
        subtitle="Manage consultation modes, professional experience, and clinical bio."
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

      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', marginBottom: '1.5rem', paddingBottom: '1.25rem', borderBottom: '1px solid var(--gray-100)' }}>
          <div
            style={{
              width: '64px',
              height: '64px',
              borderRadius: '50%',
              backgroundColor: 'var(--primary-100)',
              color: 'var(--primary-700)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Stethoscope size={32} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>Dr. {user?.name}</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--gray-500)' }}>
              {user?.email} • Doctor ID #{profile?.doctorId || 'N/A'}
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Experience (Years)</label>
              <input
                type="number"
                name="experienceYears"
                min={0}
                value={formData.experienceYears}
                onChange={handleChange}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Consultation Fee ($)</label>
              <input
                type="number"
                name="consultationFee"
                min={0}
                step="0.01"
                value={formData.consultationFee}
                onChange={handleChange}
                className="form-input"
              />
            </div>
          </div>

          <div style={{ margin: '1rem 0' }}>
            <label className="form-label">Consultation Capabilities</label>
            <div style={{ display: 'flex', gap: '2rem', marginTop: '0.5rem' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.875rem' }}>
                <input
                  type="checkbox"
                  name="allowsOnline"
                  checked={formData.allowsOnline}
                  onChange={handleChange}
                />
                <span>Enable Online Video Consultations</span>
              </label>

              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontSize: '0.875rem' }}>
                <input
                  type="checkbox"
                  name="allowsOffline"
                  checked={formData.allowsOffline}
                  onChange={handleChange}
                />
                <span>Enable In-Clinic Visits</span>
              </label>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Professional Biography / Specialty Summary</label>
            <textarea
              rows={4}
              name="bio"
              value={formData.bio}
              onChange={handleChange}
              placeholder="Board-certified specialist with extensive background in patient diagnostics..."
              className="form-textarea"
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
            <button
              type="submit"
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Saving...' : 'Update Doctor Profile'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default DoctorProfile;
