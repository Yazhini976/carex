import api from './api';

export const analyticsService = {
  getDailyStats: async (date = null) => {
    const url = date ? `/analytics/daily?date=${date}` : '/analytics/daily';
    const response = await api.get(url);
    return response.data;
  },

  getAppointmentStats: async () => {
    const response = await api.get('/analytics/appointments');
    return response.data;
  },

  getWorkloadStats: async () => {
    const response = await api.get('/analytics/workload');
    return response.data;
  },

  getRevenueStats: async () => {
    const response = await api.get('/analytics/revenue');
    return response.data;
  },
};

export default analyticsService;
