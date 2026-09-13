export const ROLES = {
  PATIENT: 'PATIENT',
  DOCTOR: 'DOCTOR',
  ADMIN: 'ADMIN',
};

export const APPOINTMENT_STATUS = {
  BOOKED: 'BOOKED',
  CONFIRMED: 'CONFIRMED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
  NO_SHOW: 'NO_SHOW',
};

export const APPOINTMENT_MODE = {
  ONLINE: 'ONLINE',
  OFFLINE: 'OFFLINE',
};

export const WAITLIST_STATUS = {
  WAITING: 'WAITING',
  OFFERED: 'OFFERED',
  FULFILLED: 'FULFILLED',
  EXPIRED: 'EXPIRED',
  CANCELLED: 'CANCELLED',
};

export const DAYS_OF_WEEK = [
  { value: 1, label: 'Monday' },
  { value: 2, label: 'Tuesday' },
  { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' },
  { value: 5, label: 'Friday' },
  { value: 6, label: 'Saturday' },
  { value: 7, label: 'Sunday' },
];

export const STATUS_COLORS = {
  BOOKED: { bg: '#e0f2fe', text: '#0369a1', border: '#bae6fd' },
  CONFIRMED: { bg: '#dcfce7', text: '#15803d', border: '#bbf7d0' },
  COMPLETED: { bg: '#f0fdf4', text: '#166534', border: '#86efac' },
  CANCELLED: { bg: '#fee2e2', text: '#b91c1c', border: '#fecaca' },
  NO_SHOW: { bg: '#fef3c7', text: '#b45309', border: '#fde68a' },
  WAITING: { bg: '#fef3c7', text: '#b45309', border: '#fde68a' },
  OFFERED: { bg: '#e0e7ff', text: '#4338ca', border: '#c7d2fe' },
  FULFILLED: { bg: '#dcfce7', text: '#15803d', border: '#bbf7d0' },
  EXPIRED: { bg: '#f3f4f6', text: '#4b5563', border: '#e5e7eb' },
};
