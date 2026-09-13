import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Activity, LogOut, Menu } from 'lucide-react';
import useAuth from '../../hooks/useAuth';
import StatusBadge from './StatusBadge';
import NotificationBell from './NotificationBell';

export const Navbar = ({ onToggleSidebar }) => {
  const { user, role, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getHomeLink = () => {
    if (!isAuthenticated) return '/';
    if (role === 'PATIENT') return '/patient/dashboard';
    if (role === 'DOCTOR') return '/doctor/dashboard';
    if (role === 'ADMIN') return '/admin/dashboard';
    return '/';
  };

  return (
    <header
      style={{
        height: '64px',
        backgroundColor: '#ffffff',
        borderBottom: '1px solid var(--gray-200)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 1.5rem',
        position: 'sticky',
        top: 0,
        zIndex: 50,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {isAuthenticated && onToggleSidebar && (
          <button
            onClick={onToggleSidebar}
            style={{
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              color: 'var(--gray-600)',
              display: 'flex',
              padding: '0.25rem',
            }}
          >
            <Menu size={22} />
          </button>
        )}
        <Link
          to={getHomeLink()}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            textDecoration: 'none',
            color: 'var(--gray-900)',
          }}
        >
          <div
            style={{
              width: '32px',
              height: '32px',
              borderRadius: '8px',
              backgroundColor: 'var(--primary-600)',
              color: '#ffffff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Activity size={20} />
          </div>
          <div>
            <span style={{ fontWeight: 800, fontSize: '1.25rem', letterSpacing: '-0.025em', color: 'var(--primary-700)' }}>
              CAREX
            </span>
            <span style={{ fontSize: '0.65rem', display: 'block', color: 'var(--gray-400)', marginTop: '-4px', fontWeight: 600 }}>
              INTELLIGENT HEALTH
            </span>
          </div>
        </Link>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {isAuthenticated ? (
          <>
            {/* Notification Bell Component */}
            <NotificationBell />

            {/* User Info & Role Badge */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div style={{ textAlign: 'right', display: 'none', md: 'block' }}>
                <div style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--gray-900)' }}>
                  {user.name}
                </div>
                <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '2px' }}>
                  <StatusBadge status={role} />
                </div>
              </div>
              <button
                onClick={handleLogout}
                title="Logout"
                className="btn btn-secondary btn-sm"
                style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}
              >
                <LogOut size={16} />
                <span>Logout</span>
              </button>
            </div>
          </>
        ) : (
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <Link to="/login" className="btn btn-secondary btn-sm">
              Sign In
            </Link>
            <Link to="/register" className="btn btn-primary btn-sm">
              Register
            </Link>
          </div>
        )}
      </div>
    </header>
  );
};

export default Navbar;
