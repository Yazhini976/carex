import api from './api';

const normalizeSlot = (slot) => slot ? ({ ...slot, slotId: slot.slotId || slot.id, id: slot.id || slot.slotId }) : slot;
const normalizeSlots = (data) => Array.isArray(data) ? data.map(normalizeSlot) : [];

export const slotService = {
  getAvailableSlots: async (doctorId, date, mode) => {
    let url = `/slots/available?doctorId=${doctorId}&date=${date}`;
    if (mode) {
      url += `&mode=${mode}`;
    }
    const response = await api.get(url);
    return normalizeSlots(response.data);
  },

  getSlotsByDoctorAndDate: async (doctorId, date) => {
    const response = await api.get(`/slots/doctor/${doctorId}?date=${date}`);
    return normalizeSlots(response.data);
  },

  getSlotById: async (id) => {
    const response = await api.get(`/slots/${id}`);
    return normalizeSlot(response.data);
  },

  generateSlots: async (data) => {
    const response = await api.post('/slots/generate', data);
    return normalizeSlots(response.data);
  },
};

export default slotService;
