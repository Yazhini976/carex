import React, { useState, useEffect } from 'react';
import {
  TrendingUp,
  DollarSign,
  Calendar,
  Users,
  Video,
  Building,
  BarChart3,
  AlertCircle,
} from 'lucide-react';
import analyticsService from '../../services/analyticsService';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { formatCurrency } from '../../utils/formatters';

export const Analytics = () => {
  const [dailyData, setDailyData] = useState(null);
  const [appointmentData, setAppointmentData] = useState(null);
  const [revenueData, setRevenueData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadAnalytics();
  }, []);

  const loadAnalytics = async () => {
    setLoading(true);
    setError(null);
    try {
      const [daily, appts, rev] = await Promise.all([
        analyticsService.getDailyStats().catch(() => null),
        analyticsService.getAppointmentStats().catch(() => null),
        analyticsService.getRevenueStats().catch(() => null),
      ]);

      setDailyData(daily || {});
      setAppointmentData(appts || {});
      setRevenueData(rev || {});
    } catch (err) {
      console.error(err);
      setError('Unable to load analytics trends.');
    } finally {
      setLoading(false);
    }
  };

  const totalAppts = (dailyData?.totalAppointments || appointmentData?.total || 1);
  const onlineCount = dailyData?.onlineAppointments || 0;
  const offlineCount = dailyData?.offlineAppointments || 0;
  const onlinePct = Math.round((onlineCount / (onlineCount + offlineCount || 1)) * 100);
  const offlinePct = 100 - onlinePct;

  return (
    <div className="page-body">
      <PageHeader
        title="Hospital Flow & Clinical Analytics"
        subtitle="Operational metrics, patient throughput, mode distribution, and revenue trends."
      />

      {error && (
        <div className="alert alert-danger">
          <AlertCircle size={18} />
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <LoadingSpinner text="Computing operational performance metrics..." />
      ) : (
        <>
          {/* Top Trends Cards */}
          <div className="grid-cards" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', marginBottom: '2rem' }}>
            <div className="card">
              <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                Consultation Volume
              </span>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--primary-700)', marginTop: '0.35rem' }}>
                {dailyData?.totalAppointments ?? 0}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
                Scheduled Visits
              </div>
            </div>

            <div className="card">
              <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                Completion Rate
              </span>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: '#15803d', marginTop: '0.35rem' }}>
                {dailyData?.completedCount && dailyData?.totalAppointments
                  ? `${Math.round((dailyData.completedCount / dailyData.totalAppointments) * 100)}%`
                  : '100%'}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
                {dailyData?.completedCount ?? 0} Completed Consultations
              </div>
            </div>

            <div className="card">
              <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                No-Show Rate
              </span>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: '#b45309', marginTop: '0.35rem' }}>
                {dailyData?.noShowCount && dailyData?.totalAppointments
                  ? `${Math.round((dailyData.noShowCount / dailyData.totalAppointments) * 100)}%`
                  : '0%'}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
                {dailyData?.noShowCount ?? 0} Missed Visits
              </div>
            </div>

            <div className="card">
              <span style={{ fontSize: '0.75rem', fontWeight: 700, color: 'var(--gray-500)', textTransform: 'uppercase' }}>
                Total Revenue
              </span>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: '#15803d', marginTop: '0.35rem' }}>
                {formatCurrency(revenueData?.totalRevenue || dailyData?.estimatedRevenue || 0)}
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--gray-400)', marginTop: '0.25rem' }}>
                Consultation Collections
              </div>
            </div>
          </div>

          {/* Visual Analytics Sections */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '2rem' }}>
            {/* Online vs Offline Consultation Distribution */}
            <div className="card">
              <h3 style={{ fontSize: '1.1rem', marginBottom: '1.25rem' }}>
                Consultation Mode Distribution
              </h3>

              <div style={{ marginBottom: '1.5rem' }}>
                <div style={{ height: '24px', display: 'flex', borderRadius: '12px', overflow: 'hidden', backgroundColor: 'var(--gray-100)' }}>
                  <div
                    style={{
                      width: `${onlinePct}%`,
                      backgroundColor: 'var(--accent-teal)',
                      transition: 'width 0.5s ease',
                    }}
                    title={`Online Video: ${onlinePct}%`}
                  />
                  <div
                    style={{
                      width: `${offlinePct}%`,
                      backgroundColor: 'var(--primary-600)',
                      transition: 'width 0.5s ease',
                    }}
                    title={`In-Clinic: ${offlinePct}%`}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-around', fontSize: '0.875rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Video size={16} color="var(--accent-teal)" />
                  <div>
                    <strong>{onlineCount}</strong> Online Video ({onlinePct}%)
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Building size={16} color="var(--primary-600)" />
                  <div>
                    <strong>{offlineCount}</strong> In-Clinic ({offlinePct}%)
                  </div>
                </div>
              </div>
            </div>

            {/* Appointment Lifecycle Breakdown */}
            <div className="card">
              <h3 style={{ fontSize: '1.1rem', marginBottom: '1.25rem' }}>
                Appointment Status Distribution
              </h3>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                <div>
                  <div className="flex-between" style={{ fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                    <span>Booked / Pending</span>
                    <strong>{dailyData?.bookedCount ?? 0}</strong>
                  </div>
                  <div style={{ height: '8px', backgroundColor: 'var(--gray-100)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${((dailyData?.bookedCount || 0) / totalAppts) * 100}%`, backgroundColor: '#0284c7', height: '100%' }} />
                  </div>
                </div>

                <div>
                  <div className="flex-between" style={{ fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                    <span>Completed Visits</span>
                    <strong>{dailyData?.completedCount ?? 0}</strong>
                  </div>
                  <div style={{ height: '8px', backgroundColor: 'var(--gray-100)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${((dailyData?.completedCount || 0) / totalAppts) * 100}%`, backgroundColor: '#15803d', height: '100%' }} />
                  </div>
                </div>

                <div>
                  <div className="flex-between" style={{ fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                    <span>Cancelled</span>
                    <strong>{dailyData?.cancelledCount ?? 0}</strong>
                  </div>
                  <div style={{ height: '8px', backgroundColor: 'var(--gray-100)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${((dailyData?.cancelledCount || 0) / totalAppts) * 100}%`, backgroundColor: '#dc2626', height: '100%' }} />
                  </div>
                </div>

                <div>
                  <div className="flex-between" style={{ fontSize: '0.85rem', marginBottom: '0.25rem' }}>
                    <span>No-Shows</span>
                    <strong>{dailyData?.noShowCount ?? 0}</strong>
                  </div>
                  <div style={{ height: '8px', backgroundColor: 'var(--gray-100)', borderRadius: '4px', overflow: 'hidden' }}>
                    <div style={{ width: `${((dailyData?.noShowCount || 0) / totalAppts) * 100}%`, backgroundColor: '#d97706', height: '100%' }} />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default Analytics;
