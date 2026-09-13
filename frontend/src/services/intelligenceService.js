import api from './api';

export const intelligenceService = {
  // 1. Smart Specialty Recommendation
  getSpecialtyRecommendation: async ({ patientId, inputText }) => {
    const response = await api.post('/intelligence/specialty-recommendation', {
      patientId,
      inputText,
    });
    return response.data;
  },

  // 2. Explainable Doctor Matching
  matchDoctors: async ({ specialtyId, mode }) => {
    const response = await api.post('/intelligence/doctor-match', {
      specialtyId,
      mode,
    });
    return response.data;
  },

  // 3. Dynamic Wait Time Prediction
  getWaitTimePrediction: async (appointmentId) => {
    const response = await api.get(`/intelligence/wait-time/${appointmentId}`);
    return response.data;
  },

  // 4. Doctor Workload Analysis
  getDoctorWorkload: async (doctorId) => {
    const response = await api.get(`/intelligence/workload/doctor/${doctorId}`);
    return response.data;
  },

  getAllDoctorWorkloads: async () => {
    const response = await api.get('/intelligence/workload/all');
    return response.data;
  },

  // 5. What-If Scheduling Simulation (Read-Only)
  simulateSchedule: async ({ doctorId, date, scenario }) => {
    const response = await api.post('/intelligence/simulation', {
      doctorId,
      date,
      scenario,
    });
    return response.data;
  },

  // 6. No-Show Risk Estimation
  getNoShowPrediction: async (appointmentId) => {
    const response = await api.get(`/intelligence/no-show/${appointmentId}`);
    return response.data;
  },

  // 7. Doctor Unavailability Disruption Recovery
  getRecoveryRecommendations: async ({ doctorId, date }) => {
    const response = await api.post('/intelligence/recovery', {
      doctorId,
      date,
    });
    return response.data;
  },

  // 8. Smart Waitlist Candidate Ranking
  rankWaitlistCandidates: async ({ specialtyId, doctorId, mode, date }) => {
    const response = await api.post('/intelligence/waitlist-rank', {
      specialtyId,
      doctorId,
      mode,
      date,
    });
    return response.data;
  },
};

export default intelligenceService;
