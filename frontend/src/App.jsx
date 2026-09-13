import React, { useState } from 'react';
import { BrowserRouter, useLocation } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import useAuth from './hooks/useAuth';
import Navbar from './components/common/Navbar';
import Sidebar from './components/common/Sidebar';
import AppRoutes from './routes/AppRoutes';

const AppLayout = () => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(
    typeof window !== 'undefined' ? window.innerWidth > 768 : true
  );

  // Auto-close sidebar on mobile whenever the route changes
  React.useEffect(() => {
    if (typeof window !== 'undefined' && window.innerWidth <= 768) {
      setSidebarOpen(false);
    }
  }, [location.pathname]);

  // Hide sidebar on public pages (Landing page, login, register)
  const isPublicPage =
    location.pathname === '/' ||
    location.pathname === '/login' ||
    location.pathname === '/register';

  const showSidebar = isAuthenticated && !isPublicPage;

  return (
    <div className="app-container" style={{ flexDirection: 'column' }}>
      <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />
      <div style={{ display: 'flex', flex: 1, position: 'relative' }}>
        {showSidebar && (
          <>
            <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
            {sidebarOpen && (
              <div
                className="sidebar-backdrop"
                onClick={() => setSidebarOpen(false)}
                aria-label="Close navigation"
              />
            )}
          </>
        )}
        <main className="main-content">
          <AppRoutes />
        </main>
      </div>
    </div>
  );
};

export const App = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppLayout />
      </AuthProvider>
    </BrowserRouter>
  );
};

export default App;