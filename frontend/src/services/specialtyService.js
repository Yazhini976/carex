import api from './api';

export const specialtyService = {
  getAllSpecialties: async () => {
    const response = await api.get('/specialties');
    const data = Array.isArray(response.data) ? response.data : [];
    return data.map((item) => ({
      ...item,
      specialtyId: item.id,
      specialtyName: item.name,
    }));
  },

  getActiveSpecialties: async () => {
    const response = await api.get('/specialties/active');
    const data = Array.isArray(response.data) ? response.data : [];
    return data.map((item) => ({
      ...item,
      specialtyId: item.id,
      specialtyName: item.name,
    }));
  },

  getSpecialtyById: async (id) => {
    const response = await api.get(`/specialties/${id}`);
    return response.data;
  },

  createSpecialty: async (data) => {
    const response = await api.post('/specialties', data);
    return response.data;
  },

  updateSpecialty: async (id, data) => {
    const response = await api.put(`/specialties/${id}`, data);
    return response.data;
  },

  deleteSpecialty: async (id) => {
    const response = await api.delete(`/specialties/${id}`);
    return response.data;
  },
};

export default specialtyService;
