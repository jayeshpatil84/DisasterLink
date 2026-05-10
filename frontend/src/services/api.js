import axios from 'axios';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080',
  timeout: 10000,
});

// Attach JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('dl_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 globally - redirect to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('dl_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ── Auth ──────────────────────────────────────────────
export const authApi = {
  login: (username, password) =>
    api.post('/api/auth/login', { username, password }),
  register: (data) =>
    api.post('/api/auth/register', data),
};

// ── SOS Beacons ───────────────────────────────────────
export const sosApi = {
  create: (data) => api.post('/api/sos', data),
  getActive: () => api.get('/api/sos/active'),
  getAll: () => api.get('/api/sos'),
  updateStatus: (id, status) => api.patch(`/api/sos/${id}/status`, { status }),
};

// ── Volunteers ────────────────────────────────────────
export const volunteerApi = {
  getAll: () => api.get('/api/volunteer'),
  getAvailable: () => api.get('/api/volunteer/available'),
  register: (data) => api.post('/api/volunteer/register', data),
  updateLocation: (id, lat, lng) =>
    api.patch(`/api/volunteer/${id}/location`, { lat, lng }),
  release: (id) => api.patch(`/api/volunteer/${id}/release`),
};

export default api;
