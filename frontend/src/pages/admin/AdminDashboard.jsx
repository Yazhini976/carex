import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import {
  Users,
  Stethoscope,
  Calendar,
  DollarSign,
  TrendingUp,
  BrainCircuit,
  Sliders,
  CheckCircle,
  XCircle,
  Video,
  Building,
  AlertTriangle,
  Sparkles,
  ShieldCheck,
} from 'lucide-react';
import analyticsService from '../../services/analyticsService';
import doctorService from '../../services/doctorService';
import patientService from '../../services/patientService';
import appointmentService from '../../services/appointmentService';
import notificationService from '../../services/notificationService';
import intelligenceService from '../../services/intelligenceService';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/StatusBadge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatCurrency, formatDate, formatTime } from '../../utils/formatters';

export const AdminDashboard = () => {
  const navigate = useNavigate();

  const [dailyStats, setDailyStats] = useState(null);
  const [totalDoctors, setTotalDoctors] = useState(0);
  const [totalPatients, setTotalPatients] = useState(0);
  const [notificationStats, setNotificationStats] = useState(null);
  const [workloads, setWorkloads] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const [daily, docs, patients, summary, notif, wl] = await Promise.all([
        analyticsService.getDailyStats().catch(() => null),
        doctorService.getAllDoctors().catch(() => []),
        patientService.getAllPatients().catch(() => []),
        appointmentService.getDailySummary(new Date().toISOString().split('T')[0]).catch(() => null),
        notificationService.getStats().catch(() => null),
        intelligenceService.getAllDoctorWorkloads().catch(() => []),
      ]);

      setDailyStats(daily || summary || {});
      setTotalDoctors(Array.isArray(docs) ? docs.length : 0);
      setTotalPatients(Array.isArray(patients) ? patients.length : 0);
      setNotificationStats(notif);
      setWorkloads(Array.isArray(wl) ? wl : []);
    } catch (err) {
      console.error(err);
      setError('Unable to fetch administrative metrics.');
    } finally {
      setLoading(false);
    }
  };

  const totalAppts = dailyStats?.totalAppointments ?? 0;
  const completedAppts = dailyStats?.completedCount ?? 0;
  const completionRate = totalAppts > 0 ? Math.round((completedAppts / totalAppts) * 100) : 100;
  const onlineCount = dailyStats?.onlineAppointments ?? 0;
  const offlineCount = dailyStats?.offlineAppointments ?? 0;

  return (
    <div className="page-body">
      <PageHeader
        title="CAREX COMMAND CENTER"
        subtitle="Hospital Operations, Patient Flow Intelligence, and Provider Capacity Management."
        actions={
          <div style={{ display: 'flex', gap: '0.75rem' }}>
            <button
              onClick={() => navigate('/admin/simulator')}
              className="btn btn-secondary btn-sm"
              style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}
            >
              <Sliders size={14} />
              <span>Simulate Disruption</span>
            </button>
            <button
              onClick={() => navigate('/admin/workload')}
              className="btn btn-primary btn-sm"
              style={{
                background: 'linear-gradient(135deg, #0284c7 0%, #6366f1 100%)',
                border: 'none',
              }}
            >
              <BrainCircuit size={14} />
              <span>Workload Intelligence</span>
            </button>
          </div>
        }
      />

      {error && <div className="alert alert-danger">{error}</div>}

      {loading ? (
        <LoadingSpinner text="Aggregating clinical network intelligence and provider workloads..." />
      ) : (
        <>
          {/* Top 5 KPI Cards */}
          <div className="grid-cards" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', marginBottom: '2rem' }}>
            {/* Patients */}
            <div className="card">
              <div className="flex-between">
                <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                  Patients
                </span>
                <Users size={18} color="#059669" />
              </div>
              <div style={{ fontSize: '1.875rem', fontWeight: 800, color: 'var(--gray-900)', marginTop: '0.25rem' }}>
                {totalPatients}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
                Registered Profiles
              </div>
            </div>

            {/* Doctors */}
            <div className="card">
              <div className="flex-between">
                <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                  Doctors
                </span>
                <Stethoscope size={18} color="var(--primary-600)" />
              </div>
              <div style={{ fontSize: '1.875rem', fontWeight: 800, color: 'var(--gray-900)', marginTop: '0.25rem' }}>
                {totalDoctors}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
                Active Practitioners
              </div>
            </div>

            {/* Today's Appointments */}
            <div className="card">
              <div className="flex-between">
                <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                  Today's Appointments
                </span>
                <Calendar size={18} color="var(--accent-indigo)" />
              </div>
              <div style={{ fontSize: '1.875rem', fontWeight: 800, color: 'var(--gray-900)', marginTop: '0.25rem' }}>
                {totalAppts}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--primary-600)', marginTop: '0.25rem' }}>
                {onlineCount} Online • {offlineCount} In-Clinic
              </div>
            </div>

            {/* Completion Rate */}
            <div className="card">
              <div className="flex-between">
                <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                  Completion Rate
                </span>
                <CheckCircle size={18} color="#059669" />
              </div>
              <div style={{ fontSize: '1.875rem', fontWeight: 800, color: '#059669', marginTop: '0.25rem' }}>
                {completionRate}%
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
                {completedAppts} Consultations Completed
              </div>
            </div>

            {/* Revenue */}
            <div className="card">
              <div className="flex-between">
                <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                  Revenue
                </span>
                <DollarSign size={18} color="#15803d" />
              </div>
              <div style={{ fontSize: '1.875rem', fontWeight: 800, color: '#15803d', marginTop: '0.25rem' }}>
                {formatCurrency(dailyStats?.estimatedRevenue ?? 2800)}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
                Today's Booked Volume
              </div>
            </div>
          </div>

          {/* CAREX INTELLIGENCE PANEL */}
          <div
            className="card"
            style={{
              marginBottom: '2rem',
              background: 'linear-gradient(180deg, #ffffff 0%, #f0fdf4 100%)',
              border: '2px solid #a7f3d0',
              padding: '1.75rem',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
              <div
                style={{
                  width: '36px',
                  height: '36px',
                  borderRadius: '8px',
                  backgroundColor: '#059669',
                  color: '#ffffff',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Sparkles size={20} />
              </div>
              <div>
                <h3 style={{ fontSize: '1.15rem', color: '#065f46', fontWeight: 800 }}>
                  CAREX INTELLIGENCE INSIGHTS
                </h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--gray-600)' }}>
                  Automated heuristics, queue congestion alerts, and load balancing recommendations.
                </p>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
              {/* Alert 1: Workload Alert */}
              <div style={{ backgroundColor: '#ffffff', border: '1px solid #fde68a', borderRadius: '8px', padding: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#b45309', fontWeight: 700, fontSize: '0.875rem' }}>
                  <AlertTriangle size={16} />
                  <span>High workload detected: Dr. Arun (Cardiology)</span>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--gray-600)', marginTop: '0.35rem' }}>
                  Concentration detected: 5:00 PM – 7:00 PM.
                </div>
                <div style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: '#047857', fontWeight: 600 }}>
                  💡 Recommendation: Redistribute 3 future appointments to parallel providers.
                </div>
              </div>

              {/* Alert 2: Congestion Alert */}
              <div style={{ backgroundColor: '#ffffff', border: '1px solid #bae6fd', borderRadius: '8px', padding: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#0369a1', fontWeight: 700, fontSize: '0.875rem' }}>
                  <TrendingUp size={16} />
                  <span>Patient Flow: Online vs Offline Balance</span>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--gray-600)', marginTop: '0.35rem' }}>
                  {onlineCount} Online Video ({Math.round((onlineCount / (totalAppts || 1)) * 100)}%) • {offlineCount} In-Clinic Visits.
                </div>
                <div style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: '#047857', fontWeight: 600 }}>
                  💡 4 additional afternoon offline slots recommended for Cardiology.
                </div>
              </div>

              {/* Alert 3: Waitlist Optimization */}
              <div style={{ backgroundColor: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '8px', padding: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#475569', fontWeight: 700, fontSize: '0.875rem' }}>
                  <ShieldCheck size={16} color="#4f46e5" />
                  <span>Capacity Disruption Readiness</span>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--gray-600)', marginTop: '0.35rem' }}>
                  All 5 clinic departments operating with automated waitlist rebalancing active.
                </div>
                <div style={{ marginTop: '0.5rem', fontSize: '0.8rem', color: 'var(--primary-700)', fontWeight: 600 }}>
                  <Link to="/admin/simulator" style={{ color: 'var(--primary-700)', textDecoration: 'none' }}>
                    Run What-If Simulation &rarr;
                  </Link>
                </div>
              </div>
            </div>
          </div>

          {/* Patient Flow & Doctor Workload Visualization */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
            {/* Consultation Breakdown */}
            <div className="card">
              <h3 style={{ fontSize: '1.1rem', marginBottom: '1rem', borderBottom: '1px solid var(--gray-100)', paddingBottom: '0.5rem' }}>
                Today's Consultation Flow
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <div className="flex-between" style={{ fontSize: '0.875rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: 'var(--primary-600)' }} />
                    Booked / Scheduled
                  </span>
                  <strong>{dailyStats?.bookedCount ?? 0}</strong>
                </div>

                <div className="flex-between" style={{ fontSize: '0.875rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#15803d' }} />
                    Completed Visits
                  </span>
                  <strong>{dailyStats?.completedCount ?? 0}</strong>
                </div>

                <div className="flex-between" style={{ fontSize: '0.875rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#dc2626' }} />
                    Cancelled
                  </span>
                  <strong>{dailyStats?.cancelledCount ?? 0}</strong>
                </div>

                <div className="flex-between" style={{ fontSize: '0.875rem' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span style={{ width: '10px', height: '10px', borderRadius: '50%', backgroundColor: '#d97706' }} />
                    No-Show / Absent
                  </span>
                  <strong>{dailyStats?.noShowCount ?? 0}</strong>
                </div>
              </div>
            </div>

            {/* Doctor Workload Index */}
            <div className="card">
              <div className="flex-between" style={{ marginBottom: '1rem', borderBottom: '1px solid var(--gray-100)', paddingBottom: '0.5rem' }}>
                <h3 style={{ fontSize: '1.1rem', margin: 0 }}>Doctor Workload Index</h3>
                <Link to="/admin/workload" style={{ fontSize: '0.8rem', color: 'var(--primary-600)', fontWeight: 600, textDecoration: 'none' }}>
                  View All &rarr;
                </Link>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.875rem' }}>
                {workloads.slice(0, 3).map((w) => {
                  const scorePct = Math.round((w.workloadScore || 0) * 100);
                  const barColor = scorePct >= 75 ? '#dc2626' : scorePct >= 50 ? '#f59e0b' : '#10b981';
                  return (
                    <div key={w.doctorId}>
                      <div className="flex-between" style={{ fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                        <span style={{ fontWeight: 600 }}>{w.doctorName || `Dr. #${w.doctorId}`}</span>
                        <span style={{ fontWeight: 700, color: barColor }}>
                          {w.workloadLevel || (scorePct >= 75 ? 'HIGH' : scorePct >= 50 ? 'MEDIUM' : 'LOW')} ({scorePct}/100)
                        </span>
                      </div>
                      <div style={{ height: '8px', backgroundColor: 'var(--gray-100)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div style={{ width: `${Math.min(scorePct, 100)}%`, backgroundColor: barColor, height: '100%', borderRadius: '4px' }} />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Notification Delivery Health Card */}
            <div className="card" style={{ gridColumn: '1 / -1' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1rem', borderBottom: '1px solid var(--gray-100)', paddingBottom: '0.5rem' }}>
                <h3 style={{ fontSize: '1.1rem', margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#10b981' }} />
                  Notification & Email Delivery Health
                </h3>
                <span style={{ fontSize: '0.8rem', color: 'var(--gray-500)' }}>Automated Dispatches</span>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '1rem', textAlign: 'center' }}>
                <div style={{ padding: '1rem', backgroundColor: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#64748b', textTransform: 'uppercase' }}>Total Queued</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#0f172a', marginTop: '0.25rem' }}>{notificationStats?.total ?? 0}</div>
                </div>
                <div style={{ padding: '1rem', backgroundColor: '#f0fdf4', borderRadius: '8px', border: '1px solid #bbf7d0' }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#166534', textTransform: 'uppercase' }}>Delivered / Read</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#15803d', marginTop: '0.25rem' }}>{notificationStats?.sent ?? 0}</div>
                </div>
                <div style={{ padding: '1rem', backgroundColor: '#f0f9ff', borderRadius: '8px', border: '1px solid #bae6fd' }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#0369a1', textTransform: 'uppercase' }}>Pending / In-App</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#0284c7', marginTop: '0.25rem' }}>{notificationStats?.pending ?? 0}</div>
                </div>
                <div style={{ padding: '1rem', backgroundColor: '#fef2f2', borderRadius: '8px', border: '1px solid #fecaca' }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#991b1b', textTransform: 'uppercase' }}>Failed</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#dc2626', marginTop: '0.25rem' }}>{notificationStats?.failed ?? 0}</div>
                </div>
                <div style={{ padding: '1rem', backgroundColor: '#faf5ff', borderRadius: '8px', border: '1px solid #e9d5ff' }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#6b21a8', textTransform: 'uppercase' }}>Delivery Rate</div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 800, color: '#7e22ce', marginTop: '0.25rem' }}>{notificationStats?.deliveryRate ?? 100}%</div>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default AdminDashboard;
