import api from './api';

export const waitlistService = {
  joinWaitlist: async (data) => {
    const response = await api.post('/waitlist', data);
    return response.data;
  },

  getPatientWaitlists: async (patientId) => {
    const response = await api.get(`/waitlist/patient/${patientId}`);
    return response.data;
  },

  getActiveWaitlists: async () => {
    const response = await api.get('/waitlist/active');
    return response.data;
  },

  getWaitlistById: async (id) => {
    const response = await api.get(`/waitlist/${id}`);
    return response.data;
  },

  cancelWaitlist: async (id) => {
    const response = await api.put(`/waitlist/${id}/cancel`);
    return response.data;
  },
};

export default waitlistService;
