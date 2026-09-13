import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Stethoscope, Video, Building, AlertCircle } from 'lucide-react';
import doctorService from '../../services/doctorService';
import specialtyService from '../../services/specialtyService';
import PageHeader from '../../components/common/PageHeader';
import DoctorCard from '../../components/common/DoctorCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import EmptyState from '../../components/common/EmptyState';

export const FindDoctor = () => {
  const navigate = useNavigate();

  const [doctors, setDoctors] = useState([]);
  const [specialties, setSpecialties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSpecialty, setSelectedSpecialty] = useState('');
  const [selectedMode, setSelectedMode] = useState('ALL');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [docsRes, specsRes] = await Promise.all([
        doctorService.getActiveDoctors(),
        specialtyService.getActiveSpecialties(),
      ]);
      setDoctors(Array.isArray(docsRes) ? docsRes : []);
      setSpecialties(Array.isArray(specsRes) ? specsRes : []);
    } catch (err) {
      console.error(err);
      setError('Unable to load doctors and specialties. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const filteredDoctors = doctors.filter((doc) => {
    const nameMatch = (doc.doctorName || doc.name || '')
      .toLowerCase()
      .includes(searchQuery.toLowerCase());

    const specialtyMatch =
      !selectedSpecialty ||
      (doc.primarySpecialty &&
        doc.primarySpecialty.toLowerCase() === selectedSpecialty.toLowerCase());

    const modeMatch =
      selectedMode === 'ALL' ||
      (selectedMode === 'ONLINE' && doc.allowsOnline) ||
      (selectedMode === 'OFFLINE' && doc.allowsOffline);

    return nameMatch && specialtyMatch && modeMatch;
  });

  const handleBook = (doctor) => {
    navigate(`/patient/book?doctorId=${doctor.doctorId}`);
  };

  return (
    <div className="page-body">
      <PageHeader
        title="Find a Doctor"
        subtitle="Search top specialists, check consultation modes, and book your appointment."
      />

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {/* Filter Controls */}
      <div className="card" style={{ marginBottom: '2rem', padding: '1.25rem' }}>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
            gap: '1rem',
            alignItems: 'center',
          }}
        >
          <div style={{ position: 'relative' }}>
            <input
              type="text"
              placeholder="Search doctor by name..."
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

          <div>
            <select
              value={selectedSpecialty}
              onChange={(e) => setSelectedSpecialty(e.target.value)}
              className="form-select"
            >
              <option value="">All Specialties</option>
              {specialties.map((spec) => (
                <option key={spec.specialtyId || spec.id} value={spec.specialtyName || spec.name}>
                  {spec.specialtyName || spec.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <select
              value={selectedMode}
              onChange={(e) => setSelectedMode(e.target.value)}
              className="form-select"
            >
              <option value="ALL">All Consultation Modes</option>
              <option value="ONLINE">Online Video Consultation</option>
              <option value="OFFLINE">In-Clinic Visit</option>
            </select>
          </div>
        </div>
      </div>

      {/* Doctor Cards Grid */}
      {loading ? (
        <LoadingSpinner text="Finding available doctors..." />
      ) : filteredDoctors.length === 0 ? (
        <EmptyState
          icon={Stethoscope}
          title="No doctors match your filter"
          description="Try broadening your search query or selecting a different specialty."
          actionLabel="Clear Filters"
          onAction={() => {
            setSearchQuery('');
            setSelectedSpecialty('');
            setSelectedMode('ALL');
          }}
        />
      ) : (
        <div className="grid-cards">
          {filteredDoctors.map((doc) => (
            <DoctorCard
              key={doc.doctorId}
              doctor={doc}
              onBook={handleBook}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default FindDoctor;
