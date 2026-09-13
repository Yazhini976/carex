import React, { createContext, useState, useEffect, useCallback } from 'react';
import { storage } from '../utils/storage';
import authService from '../services/authService';
import patientService from '../services/patientService';
import doctorService from '../services/doctorService';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [role, setRole] = useState(null);
  const [profile, setProfile] = useState(null); // Linked patient or doctor entity
  const [loading, setLoading] = useState(true);

  // Helper to load role-specific profile (patientId or doctorId)
  const loadProfile = async (userData) => {
    if (!userData || !userData.userId || !userData.role) return null;
    try {
      if (userData.role === 'PATIENT') {
        const patientData = await patientService.getPatientByUserId(userData.userId);
        if (patientData) {
          patientData.patientId = patientData.patientId || patientData.id;
        }
        return patientData;
      } else if (userData.role === 'DOCTOR') {
        const doctorData = await doctorService.getDoctorByUserId(userData.userId);
        if (doctorData) {
          doctorData.doctorId = doctorData.doctorId || doctorData.id;
        }
        return doctorData;
      }
    } catch (err) {
      console.warn('Could not load specific profile for user', err);
    }
    return null;
  };

  // Restore authentication on mount / page refresh
  useEffect(() => {
    const initializeAuth = async () => {
      const storedToken = storage.getToken();
      const storedUser = storage.getUser();

      if (storedToken && storedUser) {
        setToken(storedToken);
        setUser(storedUser);
        setRole(storedUser.role);

        // Fetch fresh profile in background
        const linkedProfile = await loadProfile(storedUser);
        if (linkedProfile) {
          setProfile(linkedProfile);
        }
      }
      setLoading(false);
    };

    initializeAuth();
  }, []);

  const login = async (credentials) => {
    setLoading(true);
    try {
      const response = await authService.login(credentials);
      // response: { token, tokenType: "Bearer", userId, name, email, role }
      const authUser = {
        userId: response.userId,
        name: response.name,
        email: response.email,
        role: response.role,
      };

      storage.setToken(response.token);
      storage.setUser(authUser);

      setToken(response.token);
      setUser(authUser);
      setRole(authUser.role);

      const linkedProfile = await loadProfile(authUser);
      if (linkedProfile) {
        setProfile(linkedProfile);
      }

      setLoading(false);
      return { success: true, user: authUser };
    } catch (error) {
      setLoading(false);
      throw error;
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const response = await authService.register(userData);
      setLoading(false);
      return { success: true, user: response };
    } catch (error) {
      setLoading(false);
      throw error;
    }
  };

  const logout = useCallback(() => {
    storage.clearAuth();
    setUser(null);
    setToken(null);
    setRole(null);
    setProfile(null);
  }, []);

  const refreshProfile = async () => {
    if (user) {
      const freshProfile = await loadProfile(user);
      if (freshProfile) {
        setProfile(freshProfile);
      }
    }
  };

  const currentPatientId = profile && role === 'PATIENT' ? (profile.patientId || profile.id) : null;
  const currentDoctorId = profile && role === 'DOCTOR' ? (profile.doctorId || profile.id) : null;

  const value = {
    user,
    token,
    role,
    profile,
    patientId: currentPatientId,
    doctorId: currentDoctorId,
    isAuthenticated: !!token && !!user,
    loading,
    login,
    register,
    logout,
    refreshProfile,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
