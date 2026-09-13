import api from './api';

export const appointmentService = {
  bookAppointment: async (appointmentData) => {
    const response = await api.post('/appointments', appointmentData);
    return response.data;
  },

  getAppointmentById: async (id) => {
    const response = await api.get(`/appointments/${id}`);
    return response.data;
  },

  getPatientAppointments: async (patientId) => {
    const response = await api.get(`/appointments/patient/${patientId}`);
    return response.data;
  },

  getDoctorAppointments: async (doctorId, date = null) => {
    const url = date
      ? `/appointments/doctor/${doctorId}?date=${date}`
      : `/appointments/doctor/${doctorId}`;
    const response = await api.get(url);
    return response.data;
  },

  confirmAppointment: async (id) => {
    const response = await api.put(`/appointments/${id}/confirm`);
    return response.data;
  },

  completeAppointment: async (id) => {
    const response = await api.put(`/appointments/${id}/complete`);
    return response.data;
  },

  cancelAppointment: async (id, reason = null) => {
    const response = await api.put(`/appointments/${id}/cancel`, null, {
      params: reason ? { reason } : {},
    });
    return response.data;
  },

  markNoShow: async (id) => {
    const response = await api.put(`/appointments/${id}/no-show`);
    return response.data;
  },

  getDailySummary: async (date) => {
    const response = await api.get(`/appointments/summary/daily?date=${date}`);
    return response.data;
  },
};

export default appointmentService;
