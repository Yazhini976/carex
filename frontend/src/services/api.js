import axios from 'axios';
import { storage } from '../utils/storage';

const getBaseUrl = () => {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }
  if (typeof window !== 'undefined' && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
    return 'https://carex-backend-7hsp.onrender.com/api';
  }
  return 'http://localhost:8080/api';
};

const api = axios.create({
  baseURL: getBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Attach JWT Token to every outgoing request if available
api.interceptors.request.use(
  (config) => {
    const token = storage.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle errors gracefully
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      
      // Auto-logout on 401 Unauthorized
      if (status === 401) {
        storage.clearAuth();
        if (window.location.pathname !== '/login' && window.location.pathname !== '/register' && window.location.pathname !== '/') {
          window.location.href = '/login?expired=true';
        }
      }

      // Extract meaningful error message
      const message =
        data?.message ||
        data?.error ||
        (typeof data === 'string' ? data : null) ||
        (status === 403
          ? 'You do not have permission to perform this action.'
          : status === 404
          ? 'The requested resource was not found.'
          : status === 409
          ? 'Conflict: The slot or item is no longer available.'
          : status === 429
          ? 'Too many requests. Please slow down and try again later.'
          : 'An unexpected error occurred. Please try again.');

      const customError = new Error(message);
      customError.status = status;
      customError.data = data;
      return Promise.reject(customError);
    }

    if (error.request) {
      const targetUrl = getBaseUrl();
      const networkError = new Error(`Cannot connect to CAREX server (${targetUrl}). Please check your connection or verify backend status.`);
      networkError.status = 0;
      return Promise.reject(networkError);
    }

    return Promise.reject(error);
  }
);

export default api;
