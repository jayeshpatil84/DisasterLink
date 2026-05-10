import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi } from '../services/api';
import { useDisasterStore } from '../store/useDisasterStore';

export default function LoginPage() {
  const navigate = useNavigate();
  const setAuth = useDisasterStore((s) => s.setAuth);

  const [mode, setMode] = useState('login'); // 'login' | 'register'
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [form, setForm] = useState({
    username: '', password: '', email: '', role: 'DISTRICT_OFFICER',
  });

  const handleChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = mode === 'login'
        ? await authApi.login(form.username, form.password)
        : await authApi.register(form);

      const { token, role, userId, username } = res.data;
      localStorage.setItem('dl_token', token);
      setAuth({ id: userId, username, role }, token);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.error || 'Something went wrong. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        {/* Logo */}
        <div style={styles.logoRow}>
          <div style={styles.logoIcon}>🚨</div>
          <div>
            <h1 style={styles.logoTitle}>DisasterLink</h1>
            <p style={styles.logoSub}>Real-time disaster coordination</p>
          </div>
        </div>

        {/* Tab switcher */}
        <div style={styles.tabs}>
          {['login', 'register'].map((t) => (
            <button
              key={t}
              style={{ ...styles.tab, ...(mode === t ? styles.tabActive : {}) }}
              onClick={() => { setMode(t); setError(''); }}
            >
              {t === 'login' ? 'Sign In' : 'Register'}
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit} style={styles.form}>
          <div className="form-group">
            <label className="form-label">Username</label>
            <input
              className="form-input"
              name="username"
              value={form.username}
              onChange={handleChange}
              placeholder="Enter username"
              required
            />
          </div>

          {mode === 'register' && (
            <div className="form-group">
              <label className="form-label">Email</label>
              <input
                className="form-input"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="Enter email"
                required
              />
            </div>
          )}

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              className="form-input"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="Enter password"
              required
            />
          </div>

          {mode === 'register' && (
            <div className="form-group">
              <label className="form-label">Role</label>
              <select
                className="form-select"
                name="role"
                value={form.role}
                onChange={handleChange}
              >
                <option value="DISTRICT_OFFICER">District Officer</option>
                <option value="VOLUNTEER">Volunteer</option>
                <option value="VICTIM">Victim / Citizen</option>
              </select>
            </div>
          )}

          {error && <div style={styles.error}>{error}</div>}

          <button
            className="btn btn-primary"
            type="submit"
            disabled={loading}
            style={{ width: '100%', justifyContent: 'center', padding: '12px' }}
          >
            {loading ? 'Please wait...' : (mode === 'login' ? 'Sign In' : 'Create Account')}
          </button>

          {mode === 'login' && (
            <div style={styles.demoHint}>
              <strong>Demo credentials:</strong><br />
              District Officer: officer / password123<br />
              Volunteer: volunteer1 / password123
            </div>
          )}
        </form>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    background: 'var(--color-bg)',
    padding: '16px',
  },
  card: {
    width: '100%',
    maxWidth: '400px',
    background: 'var(--color-surface)',
    borderRadius: '16px',
    border: '1px solid var(--color-border)',
    padding: '32px',
    boxShadow: 'var(--shadow-lg)',
  },
  logoRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    marginBottom: '24px',
  },
  logoIcon: { fontSize: '32px' },
  logoTitle: { fontSize: '22px', fontWeight: '700', color: 'var(--color-text)' },
  logoSub: { fontSize: '12px', color: 'var(--color-text-muted)' },
  tabs: {
    display: 'flex',
    background: 'var(--color-bg)',
    borderRadius: '8px',
    padding: '4px',
    marginBottom: '24px',
  },
  tab: {
    flex: 1,
    padding: '8px',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    background: 'transparent',
    color: 'var(--color-text-muted)',
    fontWeight: '500',
    fontSize: '14px',
  },
  tabActive: {
    background: 'var(--color-surface)',
    color: 'var(--color-text)',
    boxShadow: '0 1px 3px rgba(0,0,0,0.3)',
  },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  error: {
    padding: '10px 12px',
    background: 'rgba(220,38,38,0.15)',
    border: '1px solid rgba(220,38,38,0.4)',
    borderRadius: '8px',
    color: '#fca5a5',
    fontSize: '13px',
  },
  demoHint: {
    padding: '10px 12px',
    background: 'rgba(30,64,175,0.15)',
    border: '1px solid rgba(30,64,175,0.3)',
    borderRadius: '8px',
    color: 'var(--color-text-muted)',
    fontSize: '12px',
    lineHeight: '1.6',
  },
};
