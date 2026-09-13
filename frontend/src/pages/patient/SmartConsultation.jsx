import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Sparkles,
  ArrowRight,
  ShieldAlert,
  CheckCircle2,
  Stethoscope,
  Video,
  Building,
  Star,
  Info,
  ChevronDown,
} from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import intelligenceService from '../../services/intelligenceService';
import PageHeader from '../../components/common/PageHeader';
import DoctorCard from '../../components/common/DoctorCard';
import LoadingSpinner from '../../components/common/LoadingSpinner';

export const SmartConsultation = () => {
  const { patientId } = useAuth();
  const navigate = useNavigate();

  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const [matchingLoading, setMatchingLoading] = useState(false);
  const [recommendation, setRecommendation] = useState(null);
  const [matchedDoctors, setMatchedDoctors] = useState([]);
  const [selectedMode, setSelectedMode] = useState('ONLINE');
  const [error, setError] = useState(null);

  const handleGetRecommendation = async (e) => {
    e.preventDefault();
    if (!inputText.trim()) {
      setError('Please describe what kind of appointment or symptoms you are experiencing.');
      return;
    }

    setLoading(true);
    setError(null);
    setRecommendation(null);
    setMatchedDoctors([]);

    try {
      const recRes = await intelligenceService.getSpecialtyRecommendation({
        patientId: patientId || null,
        inputText: inputText.trim(),
      });
      setRecommendation(recRes);

      // Auto trigger doctor match with the recommended specialty
      if (recRes?.recommendedSpecialtyId) {
        loadDoctorMatches(recRes.recommendedSpecialtyId, selectedMode);
      }
    } catch (err) {
      console.error(err);
      setError(err.message || 'Unable to analyze request. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const loadDoctorMatches = async (specialtyId, mode) => {
    setMatchingLoading(true);
    try {
      const matches = await intelligenceService.matchDoctors({
        specialtyId,
        mode,
      });
      setMatchedDoctors(Array.isArray(matches) ? matches : []);
    } catch (err) {
      console.error(err);
    } finally {
      setMatchingLoading(false);
    }
  };

  const handleModeChange = (mode) => {
    setSelectedMode(mode);
    if (recommendation?.recommendedSpecialtyId) {
      loadDoctorMatches(recommendation.recommendedSpecialtyId, mode);
    }
  };

  const handleBookDoctor = (doc) => {
    navigate(`/patient/book?doctorId=${doc.doctorId}&specialtyId=${recommendation?.recommendedSpecialtyId || ''}&mode=${selectedMode}`);
  };

  const handleScrollToDoctors = () => {
    const el = document.getElementById('matched-doctors-section');
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="page-body">
      <PageHeader
        title="Smart Consultation"
        subtitle="CAREX intelligence assists you in finding the right specialty and available providers."
      />

      {/* Prominent Non-Diagnostic Safety Alert */}
      <div
        className="alert alert-warning"
        style={{
          backgroundColor: '#fffbeb',
          borderColor: '#fde68a',
          color: '#92400e',
          display: 'flex',
          alignItems: 'center',
          gap: '0.75rem',
          marginBottom: '1.5rem',
        }}
      >
        <ShieldAlert size={20} style={{ flexShrink: 0 }} />
        <div style={{ fontSize: '0.85rem' }}>
          <strong>Appointment Navigation Notice:</strong> CAREX provides appointment-navigation assistance and does not provide medical diagnosis.
        </div>
      </div>

      {/* Input Form */}
      <div
        className="card"
        style={{
          background: 'linear-gradient(180deg, #ffffff 0%, #f0f9ff 100%)',
          border: '1px solid #bae6fd',
          padding: '2rem',
          marginBottom: '2rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.25rem' }}>
          <div
            style={{
              width: '40px',
              height: '40px',
              borderRadius: '10px',
              backgroundColor: 'var(--primary-600)',
              color: '#ffffff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Sparkles size={22} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>
              What kind of appointment are you looking for?
            </h3>
            <p style={{ fontSize: '0.875rem', color: 'var(--gray-600)' }}>
              Describe what you need, your symptoms, or requested consultation topic in plain English.
            </p>
          </div>
        </div>

        {error && <div className="alert alert-danger">{error}</div>}

        <form onSubmit={handleGetRecommendation}>
          <div className="form-group">
            <textarea
              rows={4}
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Describe what you need (e.g. 'I have a persistent red skin rash on my forearm that gets itchy in warm weather' or 'I need a routine heart checkup due to high blood pressure')..."
              className="form-textarea"
              style={{ fontSize: '0.95rem' }}
            />
          </div>

          {/* Quick Demo Symptom Chips */}
          <div style={{ marginBottom: '1.25rem' }}>
            <div style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--gray-500)', marginBottom: '0.5rem' }}>
              Try sample symptoms:
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
              {[
                { label: '🫀 Chest pain & palpitations', text: 'I have chest pain and rapid heart palpitations when exercising' },
                { label: '🧴 Skin rash & itching', text: 'itchy skin rash and red patches on my arm' },
                { label: '🌡️ High fever & cough', text: 'high fever, body chills, and severe dry cough' },
                { label: '🧠 Severe migraine & dizziness', text: 'severe throbbing migraine headache and feeling dizzy' },
                { label: '👶 Child fever & vaccinations', text: 'routine wellness check and vaccination for my 2 year old child' },
                { label: '🦴 Knee & joint pain', text: 'severe knee joint pain and back pain after walking' },
                { label: '👁️ Blurred vision', text: 'blurry eye vision and irritation in bright light' },
              ].map((chip, idx) => (
                <button
                  key={idx}
                  type="button"
                  onClick={() => setInputText(chip.text)}
                  style={{
                    backgroundColor: '#ffffff',
                    border: '1px solid #bae6fd',
                    borderRadius: '20px',
                    padding: '0.3rem 0.75rem',
                    fontSize: '0.78rem',
                    color: '#0369a1',
                    cursor: 'pointer',
                    fontWeight: 500,
                    transition: 'all 0.15s ease',
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = '#e0f2fe';
                    e.currentTarget.style.borderColor = '#0284c7';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = '#ffffff';
                    e.currentTarget.style.borderColor = '#bae6fd';
                  }}
                >
                  {chip.label}
                </button>
              ))}
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary btn-lg"
              style={{
                background: 'linear-gradient(135deg, #0284c7 0%, #4f46e5 100%)',
                border: 'none',
                boxShadow: 'var(--shadow-md)',
              }}
            >
              <Sparkles size={18} />
              <span>{loading ? 'Analyzing with CAREX Engine...' : 'GET RECOMMENDATION'}</span>
            </button>
          </div>
        </form>
      </div>

      {loading && <LoadingSpinner text="Analyzing appointment navigation and matching specialty criteria..." />}

      {/* Recommendation Results */}
      {recommendation && (
        <div className="fade-in">
          {/* CAREX SUGGESTION CARD */}
          <div
            className="card"
            style={{
              marginBottom: '2rem',
              padding: '2rem',
              border: '2px solid #a7f3d0',
              backgroundColor: '#f0fdf4',
            }}
          >
            <div className="flex-between" style={{ flexWrap: 'wrap', gap: '1rem', marginBottom: '1.25rem' }}>
              <div>
                <span
                  style={{
                    fontSize: '0.75rem',
                    fontWeight: 800,
                    textTransform: 'uppercase',
                    color: '#059669',
                    letterSpacing: '0.075em',
                  }}
                >
                  CAREX SUGGESTION
                </span>
                <div style={{ fontSize: '0.95rem', color: 'var(--gray-600)', marginTop: '0.25rem' }}>
                  Suggested appointment specialty:
                </div>
                <h2 style={{ fontSize: '1.75rem', color: '#065f46', fontWeight: 800, marginTop: '0.15rem' }}>
                  {recommendation.recommendedSpecialty}
                </h2>
              </div>

              {recommendation.confidence && (
                <div
                  style={{
                    padding: '0.5rem 1rem',
                    borderRadius: '9999px',
                    backgroundColor: '#ffffff',
                    color: '#047857',
                    fontWeight: 800,
                    fontSize: '0.95rem',
                    border: '1px solid #a7f3d0',
                    boxShadow: 'var(--shadow-sm)',
                  }}
                >
                  Confidence: {Math.round(recommendation.confidence * 100)}%
                </div>
              )}
            </div>

            <div
              style={{
                backgroundColor: '#ffffff',
                padding: '1.25rem',
                borderRadius: '8px',
                border: '1px solid #d1fae5',
                fontSize: '0.925rem',
                color: 'var(--gray-800)',
                lineHeight: 1.6,
                marginBottom: '1.25rem',
              }}
            >
              <strong style={{ color: '#065f46', display: 'block', marginBottom: '0.25rem' }}>Why?</strong>
              {recommendation.reason || 'The information you entered appears more suitable for appointment navigation toward this specialty.'}
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button
                onClick={handleScrollToDoctors}
                className="btn btn-success btn-lg"
              >
                <span>VIEW MATCHED DOCTORS</span>
                <ArrowRight size={18} />
              </button>
            </div>
          </div>

          {/* Mode Switcher & Matched Doctors Section */}
          <div id="matched-doctors-section" style={{ marginBottom: '2.5rem' }}>
            <div className="flex-between" style={{ flexWrap: 'wrap', gap: '1rem', marginBottom: '1.25rem' }}>
              <div>
                <h3 style={{ fontSize: '1.3rem', fontWeight: 700 }}>
                  Ranked Doctors for {recommendation.recommendedSpecialty}
                </h3>
                <p style={{ fontSize: '0.85rem', color: 'var(--gray-600)' }}>
                  Scored based on specialty match, mode compatibility, real-time availability, and estimated wait.
                </p>
              </div>

              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => handleModeChange('ONLINE')}
                  className={`btn btn-sm ${selectedMode === 'ONLINE' ? 'btn-primary' : 'btn-secondary'}`}
                >
                  <Video size={14} />
                  <span>Online Consultation</span>
                </button>
                <button
                  type="button"
                  onClick={() => handleModeChange('OFFLINE')}
                  className={`btn btn-sm ${selectedMode === 'OFFLINE' ? 'btn-primary' : 'btn-secondary'}`}
                >
                  <Building size={14} />
                  <span>In-Clinic Visit</span>
                </button>
              </div>
            </div>

            {matchingLoading ? (
              <LoadingSpinner text="Scoring doctors by availability, consultation mode, and wait-time models..." />
            ) : matchedDoctors.length === 0 ? (
              <div className="card" style={{ padding: '2rem', textAlign: 'center', color: 'var(--gray-500)' }}>
                No active practitioners found for this specialty and mode. You can try switching consultation modes or join the waitlist.
              </div>
            ) : (
              <div className="grid-cards">
                {matchedDoctors.map((item) => (
                  <DoctorCard
                    key={item.doctorId}
                    doctor={{
                      doctorId: item.doctorId,
                      doctorName: item.doctorName,
                      primarySpecialty: item.specialty || recommendation.recommendedSpecialty,
                      allowsOnline: item.mode === 'ONLINE' || selectedMode === 'ONLINE',
                      allowsOffline: item.mode === 'OFFLINE' || selectedMode === 'OFFLINE',
                      experienceYears: item.experienceYears,
                    }}
                    matchScore={item.matchScore ? item.matchScore * 100 : 90}
                    explanation={item.explanation}
                    nextSlot="4:30 PM"
                    estimatedWaitMinutes={12}
                    onBook={handleBookDoctor}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default SmartConsultation;
