import api from './api';

export const patientService = {
  getAllPatients: async () => {
    const response = await api.get('/patients');
    const data = response.data;
    if (Array.isArray(data)) {
      return data.map(p => ({ ...p, patientId: p.patientId || p.id, id: p.id || p.patientId }));
    }
    return data;
  },

  getPatientById: async (id) => {
    const response = await api.get(`/patients/${id}`);
    const data = response.data;
    if (data) {
      data.patientId = data.patientId || data.id;
      data.id = data.id || data.patientId;
    }
    return data;
  },

  getPatientByUserId: async (userId) => {
    const response = await api.get(`/patients/user/${userId}`);
    const data = response.data;
    if (data) {
      data.patientId = data.patientId || data.id;
      data.id = data.id || data.patientId;
    }
    return data;
  },

  createPatient: async (data) => {
    const response = await api.post('/patients', data);
    const resData = response.data;
    if (resData) {
      resData.patientId = resData.patientId || resData.id;
      resData.id = resData.id || resData.patientId;
    }
    return resData;
  },

  updatePatient: async (id, data) => {
    const response = await api.put(`/patients/${id}`, data);
    const resData = response.data;
    if (resData) {
      resData.patientId = resData.patientId || resData.id;
      resData.id = resData.id || resData.patientId;
    }
    return resData;
  },
};

export default patientService;
