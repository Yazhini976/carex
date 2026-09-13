import api from './api';

export const availabilityService = {
  getAvailabilityByDoctor: async (doctorId) => {
    const response = await api.get(`/availability/doctor/${doctorId}`);
    return response.data;
  },

  getAvailabilityById: async (id) => {
    const response = await api.get(`/availability/${id}`);
    return response.data;
  },

  createAvailability: async (data) => {
    const response = await api.post('/availability', data);
    return response.data;
  },

  updateAvailability: async (id, data) => {
    const response = await api.put(`/availability/${id}`, data);
    return response.data;
  },

  deactivateAvailability: async (id) => {
    const response = await api.delete(`/availability/${id}`);
    return response.data;
  },
};

export default availabilityService;
