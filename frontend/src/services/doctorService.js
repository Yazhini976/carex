import api from './api';

export const doctorService = {
  getAllDoctors: async () => {
    const response = await api.get('/doctors');
    const data = Array.isArray(response.data) ? response.data : [];
    return data.map((d) => ({
      ...d,
      doctorId: d.id,
      doctorName: d.name,
      primarySpecialty: d.specialties?.[0]?.name || 'General Medicine',
    }));
  },

  getActiveDoctors: async () => {
    const response = await api.get('/doctors/active');
    const data = Array.isArray(response.data) ? response.data : [];
    return data.map((d) => ({
      ...d,
      doctorId: d.id,
      doctorName: d.name,
      primarySpecialty: d.specialties?.[0]?.name || 'General Medicine',
    }));
  },

  getDoctorById: async (id) => {
    const response = await api.get(`/doctors/${id}`);
    const d = response.data;
    return d ? {
      ...d,
      doctorId: d.id,
      doctorName: d.name,
      primarySpecialty: d.specialties?.[0]?.name || 'General Medicine',
    } : null;
  },

  getDoctorByUserId: async (userId) => {
    const response = await api.get(`/doctors/user/${userId}`);
    return response.data;
  },

  createDoctor: async (data) => {
    const response = await api.post('/doctors', data);
    return response.data;
  },

  updateDoctor: async (id, data) => {
    const response = await api.put(`/doctors/${id}`, data);
    return response.data;
  },

  toggleDoctorStatus: async (id) => {
    const response = await api.patch(`/doctors/${id}/status`);
    return response.data;
  },
};

export default doctorService;
