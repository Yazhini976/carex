import React, { useState, useEffect } from 'react';
import { Users, Search, User, Phone, Mail, AlertCircle } from 'lucide-react';
import patientService from '../../services/patientService';
import PageHeader from '../../components/common/PageHeader';
import Modal from '../../components/common/Modal';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';
import { formatDate } from '../../utils/formatters';

export const ManagePatients = () => {
  const [patients, setPatients] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPatient, setSelectedPatient] = useState(null);

  useEffect(() => {
    loadPatients();
  }, []);

  const loadPatients = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await patientService.getAllPatients();
      setPatients(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError('Unable to load patients roster.');
    } finally {
      setLoading(false);
    }
  };

  const filteredPatients = patients.filter((p) =>
    (p.name || '').toLowerCase().includes(searchQuery.toLowerCase()) ||
    (p.email || '').toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="page-body">
      <PageHeader
        title="Patients Directory"
        subtitle="Manage and view registered patient profiles and clinical records."
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
            placeholder="Search patients by name or email..."
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
              transform: 'translateY(-50%)',
              color: 'var(--gray-400)',
            }}
          />
        </div>
      </div>

      {loading ? (
        <LoadingSpinner text="Loading patient records..." />
      ) : filteredPatients.length === 0 ? (
        <EmptyState
          icon={Users}
          title="No patients found"
          description="No registered patients match your search filter."
        />
      ) : (
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Patient</th>
                <th>Phone</th>
                <th>Gender</th>
                <th>Blood Group</th>
                <th>Registered On</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredPatients.map((patient) => (
                <tr key={patient.patientId}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{patient.name}</div>
                    <div style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>
                      ID #{patient.patientId} • {patient.email}
                    </div>
                  </td>
                  <td>{patient.phone || 'N/A'}</td>
                  <td>{patient.gender || 'N/A'}</td>
                  <td>{patient.bloodGroup || 'N/A'}</td>
                  <td>{formatDate(patient.createdAt)}</td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      onClick={() => setSelectedPatient(patient)}
                      className="btn btn-secondary btn-sm"
                    >
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Patient Details Modal */}
      <Modal
        isOpen={!!selectedPatient}
        onClose={() => setSelectedPatient(null)}
        title="Patient Profile Details"
      >
        {selectedPatient && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', fontSize: '0.875rem' }}>
            <div>
              <strong>Full Name:</strong> {selectedPatient.name}
            </div>
            <div>
              <strong>Email:</strong> {selectedPatient.email}
            </div>
            <div>
              <strong>Phone:</strong> {selectedPatient.phone || 'N/A'}
            </div>
            <div>
              <strong>Date of Birth:</strong> {formatDate(selectedPatient.dateOfBirth)}
            </div>
            <div>
              <strong>Address:</strong> {selectedPatient.address || 'N/A'}
            </div>
            <div>
              <strong>Emergency Contact:</strong> {selectedPatient.emergencyContact || 'N/A'}
            </div>
            <div>
              <strong>Allergies:</strong> {selectedPatient.allergies || 'None recorded'}
            </div>
            <div>
              <strong>Medical History:</strong> {selectedPatient.medicalHistory || 'None recorded'}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default ManagePatients;
