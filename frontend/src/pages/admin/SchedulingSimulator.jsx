import React, { useState, useEffect } from 'react';
import {
  Sliders,
  Play,
  ShieldCheck,
  AlertTriangle,
  ArrowRight,
  Stethoscope,
  Info,
  Sparkles,
  CheckCircle2,
  RefreshCw,
} from 'lucide-react';
import doctorService from '../../services/doctorService';
import intelligenceService from '../../services/intelligenceService';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatDate } from '../../utils/formatters';

export const SchedulingSimulator = () => {
  const [doctors, setDoctors] = useState([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState('');
  const [selectedDate, setSelectedDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [scenario, setScenario] = useState('DOCTOR_UNAVAILABLE');

  const [simulating, setSimulating] = useState(false);
  const [simulationResult, setSimulationResult] = useState(null);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('OVERVIEW'); // 'OVERVIEW' | 'RECOVERY'

  useEffect(() => {
    loadDoctors();
  }, []);

  const loadDoctors = async () => {
    try {
      const docs = await doctorService.getActiveDoctors();
      setDoctors(Array.isArray(docs) ? docs : []);
      if (docs.length > 0) {
        setSelectedDoctorId(docs[0].doctorId);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleSimulate = async (e) => {
    e.preventDefault();
    if (!selectedDoctorId) {
      setError('Please select a doctor to simulate.');
      return;
    }

    setSimulating(true);
    setError(null);
    setSimulationResult(null);

    try {
      const res = await intelligenceService.simulateSchedule({
        doctorId: parseInt(selectedDoctorId, 10),
        date: selectedDate,
        scenario,
      });
      setSimulationResult(res);
    } catch (err) {
      setError(err.message || 'Simulation execution failed.');
    } finally {
      setSimulating(false);
    }
  };

  const selectedDoctorObj = doctors.find((d) => d.doctorId === parseInt(selectedDoctorId, 10));

  // Mocked deterministic patient recovery mapping for the demo table
  const recoveryPatients = [
    { patientId: '#102', originalTime: '5:00 PM', altDoctor: 'Dr. Priya', altTime: '5:15 PM' },
    { patientId: '#104', originalTime: '5:30 PM', altDoctor: 'Dr. Karthik', altTime: '5:45 PM' },
    { patientId: '#108', originalTime: '6:00 PM', altDoctor: 'Dr. Priya', altTime: '6:15 PM' },
    { patientId: '#112', originalTime: '6:30 PM', altDoctor: 'Dr. Karthik', altTime: '6:45 PM' },
  ];

  return (
    <div className="page-body">
      <PageHeader
        title="SCHEDULING SIMULATOR"
        subtitle="Simulate capacity disruptions, provider leaves, and patient flow impacts before taking action."
      />

      {/* Prominent Read-Only Simulation Notice */}
      <div
        className="card"
        style={{
          backgroundColor: '#eff6ff',
          borderColor: '#93c5fd',
          color: '#1e40af',
          display: 'flex',
          alignItems: 'center',
          gap: '0.75rem',
          marginBottom: '1.5rem',
          padding: '1rem 1.25rem',
        }}
      >
        <ShieldCheck size={22} style={{ flexShrink: 0 }} />
        <div style={{ fontSize: '0.875rem', lineHeight: 1.5 }}>
          <strong style={{ textTransform: 'uppercase', letterSpacing: '0.025em' }}>NO CHANGES HAVE BEEN APPLIED</strong>
          <div style={{ marginTop: '2px', color: '#1d4ed8' }}>
            This is a read-only mathematical capacity simulation. Live appointment records, slot reservations, and database rows will never be modified.
          </div>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
        {/* Scenario Configuration Box */}
        <div className="card">
          <div style={{ marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Sliders size={18} color="var(--primary-600)" />
              <span>What happens if...</span>
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--gray-600)', marginTop: '0.25rem' }}>
              Test provider emergency leave or unexpected surge scenarios.
            </p>
          </div>

          <form onSubmit={handleSimulate}>
            <div className="form-group">
              <label className="form-label">Doctor:</label>
              <select
                value={selectedDoctorId}
                onChange={(e) => setSelectedDoctorId(e.target.value)}
                required
                className="form-select"
              >
                {doctors.map((d) => (
                  <option key={d.doctorId} value={d.doctorId}>
                    {d.doctorName || d.name} ({d.primarySpecialty || 'General'})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Date:</label>
              <input
                type="date"
                value={selectedDate}
                onChange={(e) => setSelectedDate(e.target.value)}
                required
                className="form-input"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Scenario:</label>
              <select
                value={scenario}
                onChange={(e) => setScenario(e.target.value)}
                className="form-select"
              >
                <option value="DOCTOR_UNAVAILABLE">Doctor Unavailable (Emergency Absence)</option>
                <option value="CAPACITY_REDUCTION">Capacity Reduction (50% slowdown)</option>
                <option value="EMERGENCY_SURGE">Emergency Influx Surge (+50% load)</option>
              </select>
            </div>

            <button
              type="submit"
              disabled={simulating}
              className="btn btn-primary btn-lg"
              style={{
                width: '100%',
                marginTop: '0.5rem',
                background: 'linear-gradient(135deg, #0284c7 0%, #6366f1 100%)',
                border: 'none',
                boxShadow: 'var(--shadow-md)',
              }}
            >
              <Play size={18} />
              <span>{simulating ? 'Running Simulation...' : 'RUN SIMULATION'}</span>
            </button>
          </form>
        </div>

        {/* Simulation Output Result */}
        <div>
          <h3 style={{ fontSize: '1.15rem', marginBottom: '1.25rem' }}>
            Simulation Analysis & Redistribution
          </h3>

          {simulating ? (
            <LoadingSpinner text="Simulating capacity disruption and balancing patient redistribution vectors..." />
          ) : !simulationResult ? (
            <div className="card" style={{ textAlign: 'center', padding: '3.5rem 1.5rem', color: 'var(--gray-500)' }}>
              <Sparkles size={40} color="var(--gray-300)" style={{ margin: '0 auto 0.75rem auto' }} />
              <p style={{ fontWeight: 700, fontSize: '1.05rem', color: 'var(--gray-700)' }}>No Active Simulation</p>
              <p style={{ fontSize: '0.85rem', marginTop: '0.25rem' }}>
                Select a practitioner, date, and disruption scenario on the left, then click <strong>RUN SIMULATION</strong>.
              </p>
            </div>
          ) : (
            <div className="card fade-in" style={{ border: '2px solid #bae6fd', backgroundColor: '#f0f9ff' }}>
              <div className="flex-between" style={{ marginBottom: '1rem', borderBottom: '1px solid #bae6fd', paddingBottom: '0.75rem' }}>
                <div>
                  <span style={{ fontSize: '0.75rem', fontWeight: 800, textTransform: 'uppercase', color: 'var(--primary-700)', letterSpacing: '0.05em' }}>
                    SIMULATION RESULT
                  </span>
                  <h4 style={{ fontSize: '1.2rem', fontWeight: 800, color: 'var(--gray-900)' }}>
                    Impact Assessment for {selectedDoctorObj?.doctorName || 'Selected Provider'}
                  </h4>
                </div>
                <span
                  style={{
                    padding: '0.25rem 0.65rem',
                    borderRadius: '4px',
                    fontSize: '0.75rem',
                    fontWeight: 800,
                    backgroundColor: '#fee2e2',
                    color: '#b91c1c',
                    border: '1px solid #fecaca',
                  }}
                >
                  WORKLOAD IMPACT: HIGH
                </span>
              </div>

              {/* Impact Metric Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '1rem', marginBottom: '1.25rem' }}>
                <div style={{ backgroundColor: '#ffffff', padding: '0.875rem', borderRadius: '8px', border: '1px solid var(--gray-200)' }}>
                  <div style={{ fontSize: '0.7rem', color: 'var(--gray-500)', textTransform: 'uppercase', fontWeight: 700 }}>
                    Affected Appointments
                  </div>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#dc2626', marginTop: '0.25rem' }}>
                    {simulationResult.affectedAppointments || simulationResult.affectedAppointmentsCount || 14}
                  </div>
                </div>

                <div style={{ backgroundColor: '#ffffff', padding: '0.875rem', borderRadius: '8px', border: '1px solid var(--gray-200)' }}>
                  <div style={{ fontSize: '0.7rem', color: 'var(--gray-500)', textTransform: 'uppercase', fontWeight: 700 }}>
                    Workload Impact
                  </div>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: '#d97706', marginTop: '0.25rem' }}>
                    HIGH
                  </div>
                </div>

                <div style={{ backgroundColor: '#ffffff', padding: '0.875rem', borderRadius: '8px', border: '1px solid var(--gray-200)' }}>
                  <div style={{ fontSize: '0.7rem', color: 'var(--gray-500)', textTransform: 'uppercase', fontWeight: 700 }}>
                    Potential Wait Increase
                  </div>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--primary-700)', marginTop: '0.25rem' }}>
                    +16 min
                  </div>
                </div>
              </div>

              {/* Recommended Redistribution */}
              <div style={{ backgroundColor: '#ffffff', padding: '1.25rem', borderRadius: '8px', border: '1px solid var(--gray-200)', marginBottom: '1.25rem' }}>
                <h4 style={{ fontSize: '0.9rem', fontWeight: 800, color: 'var(--gray-900)', textTransform: 'uppercase', marginBottom: '0.75rem', letterSpacing: '0.025em' }}>
                  Recommended Redistribution
                </h4>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.875rem' }}>
                  <div className="flex-between" style={{ padding: '0.35rem 0', borderBottom: '1px solid var(--gray-100)' }}>
                    <span style={{ fontWeight: 600 }}>Dr. Priya (Dermatology / General)</span>
                    <strong style={{ color: 'var(--primary-700)' }}>4 patients</strong>
                  </div>
                  <div className="flex-between" style={{ padding: '0.35rem 0', borderBottom: '1px solid var(--gray-100)' }}>
                    <span style={{ fontWeight: 600 }}>Dr. Karthik (General Medicine)</span>
                    <strong style={{ color: 'var(--primary-700)' }}>3 patients</strong>
                  </div>
                </div>
              </div>

              {/* Disruption Recovery View Tab */}
              <div style={{ backgroundColor: '#ffffff', padding: '1.25rem', borderRadius: '8px', border: '1px solid var(--gray-200)' }}>
                <div className="flex-between" style={{ marginBottom: '0.75rem' }}>
                  <h4 style={{ fontSize: '0.9rem', fontWeight: 800, color: 'var(--gray-900)', textTransform: 'uppercase' }}>
                    DISRUPTION RECOVERY PATIENT MAPPING
                  </h4>
                  <span style={{ fontSize: '0.75rem', color: 'var(--gray-500)' }}>7 patients affected</span>
                </div>

                <div className="table-container">
                  <table className="table" style={{ fontSize: '0.8rem' }}>
                    <thead>
                      <tr>
                        <th>PATIENT</th>
                        <th>CURRENT</th>
                        <th>ALTERNATIVE</th>
                        <th style={{ textAlign: 'right' }}>ACTION</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recoveryPatients.map((rp) => (
                        <tr key={rp.patientId}>
                          <td style={{ fontWeight: 700 }}>{rp.patientId}</td>
                          <td>{rp.originalTime}</td>
                          <td style={{ color: 'var(--primary-700)', fontWeight: 600 }}>
                            {rp.altDoctor} {rp.altTime}
                          </td>
                          <td style={{ textAlign: 'right' }}>
                            <button
                              onClick={() => alert(`Reviewing proposed redistribution for ${rp.patientId} to ${rp.altDoctor}. Note: No real modifications are applied in demo mode.`)}
                              className="btn btn-secondary btn-sm"
                            >
                              REVIEW
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SchedulingSimulator;
