import api from './api';

export const notificationService = {
  getUserNotifications: async (userId) => {
    const response = await api.get(`/notifications/user/${userId}`);
    return response.data;
  },

  getUnreadCount: async (userId) => {
    const response = await api.get(`/notifications/user/${userId}/unread-count`);
    return response.data;
  },

  markAsRead: async (id) => {
    const response = await api.put(`/notifications/${id}/read`);
    return response.data;
  },

  markAllAsRead: async (userId) => {
    const response = await api.put(`/notifications/user/${userId}/read-all`);
    return response.data;
  },

  getStats: async () => {
    const response = await api.get('/notifications/stats');
    return response.data;
  },
};

export default notificationService;
