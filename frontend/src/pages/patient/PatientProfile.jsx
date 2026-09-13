import React, { useState, useEffect } from 'react';
import { User, Phone, Mail, MapPin, Heart, AlertCircle, CheckCircle2 } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import patientService from '../../services/patientService';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const PatientProfile = () => {
  const { user, profile, refreshProfile } = useAuth();

  const [formData, setFormData] = useState({
    dateOfBirth: '',
    gender: 'OTHER',
    bloodGroup: '',
    address: '',
    emergencyContact: '',
    medicalHistory: '',
    allergies: '',
  });

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (profile) {
      setFormData({
        dateOfBirth: profile.dateOfBirth || '',
        gender: profile.gender || 'OTHER',
        bloodGroup: profile.bloodGroup || '',
        address: profile.address || '',
        emergencyContact: profile.emergencyContact || '',
        medicalHistory: profile.medicalHistory || '',
        allergies: profile.allergies || '',
      });
    }
  }, [profile]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (success) setSuccess(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!profile?.patientId) {
      setError('Patient record not found.');
      return;
    }

    setSaving(true);
    setError(null);
    setSuccess(null);

    try {
      await patientService.updatePatient(profile.patientId, formData);
      setSuccess('Profile updated successfully!');
      if (refreshProfile) refreshProfile();
    } catch (err) {
      setError(err.message || 'Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="page-body" style={{ maxWidth: '800px' }}>
      <PageHeader
        title="Patient Profile"
        subtitle="Manage your personal information, emergency contacts, and medical history."
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

      <div className="card" style={{ marginBottom: '2rem' }}>
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
            <User size={32} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>{user?.name}</h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--gray-500)' }}>
              {user?.email} • Patient ID #{profile?.patientId || 'N/A'}
            </p>
          </div>
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem' }}>
            <div className="form-group">
              <label className="form-label">Date of Birth</label>
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Gender</label>
              <select
                name="gender"
                value={formData.gender}
                onChange={handleChange}
                className="form-select"
              >
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Blood Group</label>
              <input
                type="text"
                name="bloodGroup"
                value={formData.bloodGroup}
                onChange={handleChange}
                placeholder="e.g. O+, A+, B-"
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Emergency Contact Phone</label>
              <input
                type="tel"
                name="emergencyContact"
                value={formData.emergencyContact}
                onChange={handleChange}
                placeholder="+1 555 999 8888"
                className="form-input"
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Address</label>
            <input
              type="text"
              name="address"
              value={formData.address}
              onChange={handleChange}
              placeholder="123 Health Ave, Suite 400"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Allergies</label>
            <input
              type="text"
              name="allergies"
              value={formData.allergies}
              onChange={handleChange}
              placeholder="e.g. Penicillin, Peanuts (or None)"
              className="form-input"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Medical History / Chronic Conditions</label>
            <textarea
              rows={3}
              name="medicalHistory"
              value={formData.medicalHistory}
              onChange={handleChange}
              placeholder="Hypertension, Asthma, past surgeries..."
              className="form-textarea"
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
            <button
              type="submit"
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Saving...' : 'Save Profile Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PatientProfile;
