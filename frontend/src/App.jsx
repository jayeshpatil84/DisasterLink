import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useDisasterStore } from './store/useDisasterStore';
import { connectWebSocket, disconnectWebSocket } from './services/websocket';
import { sosApi, volunteerApi } from './services/api';

// Pages
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import SosSubmitPage from './pages/SosSubmitPage';
import VolunteerPage from './pages/VolunteerPage';

// Global styles
import './App.css';

function App() {
  const { user, token, setBeacons, setVolunteers } = useDisasterStore();

  useEffect(() => {
    if (!token) return;

    // Request notification permission
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }

    // Connect WebSocket for real-time events
    connectWebSocket();

    // Load initial data
    loadInitialData();

    return () => disconnectWebSocket();
  }, [token]);

  const loadInitialData = async () => {
    try {
      const [sosRes, volRes] = await Promise.all([
        sosApi.getActive(),
        volunteerApi.getAvailable(),
      ]);
      setBeacons(sosRes.data);
      setVolunteers(volRes.data);
    } catch (err) {
      console.error('Failed to load initial data:', err);
    }
  };

  // Protected route wrapper
  const ProtectedRoute = ({ children, roles }) => {
    if (!user || !token) return <Navigate to="/login" replace />;
    if (roles && !roles.includes(user.role)) return <Navigate to="/dashboard" replace />;
    return children;
  };

  return (
    <Router>
      <Routes>
        <Route path="/login" element={
          user ? <Navigate to="/dashboard" replace /> : <LoginPage />
        } />

        <Route path="/dashboard" element={
          <ProtectedRoute roles={['DISTRICT_OFFICER', 'ADMIN', 'VOLUNTEER']}>
            <DashboardPage />
          </ProtectedRoute>
        } />

        <Route path="/sos" element={
          <ProtectedRoute>
            <SosSubmitPage />
          </ProtectedRoute>
        } />

        <Route path="/volunteer" element={
          <ProtectedRoute roles={['VOLUNTEER', 'ADMIN']}>
            <VolunteerPage />
          </ProtectedRoute>
        } />

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
