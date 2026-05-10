import React, { useState, useEffect } from 'react';
import { volunteerApi } from '../services/api';
import { useDisasterStore } from '../store/useDisasterStore';
import { sendLocationUpdate } from '../services/websocket';

const SKILLS = [
  'MEDICAL', 'RESCUE', 'BOAT_OPERATOR',
  'STRUCTURAL_ENGINEER', 'FIREFIGHTER',
  'LOGISTICS', 'COMMUNICATION', 'FIRST_AID',
];

export default function VolunteerPage() {
  const { user } = useDisasterStore();
  const [registered, setRegistered] = useState(false);
  const [volunteer, setVolunteer] = useState(null);
  const [sharing, setSharing] = useState(false);
  const [locationWatcher, setLocationWatcher] = useState(null);
  const [lastLocation, setLastLocation] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const [form, setForm] = useState({
    name: user?.username || '',
    phone: '',
    email: '',
    skills: [],
    userId: user?.id,
  });

  const handleSkillToggle = (skill) => {
    setForm(f => ({
      ...f,
      skills: f.skills.includes(skill)
        ? f.skills.filter(s => s !== skill)
        : [...f.skills, skill],
    }));
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const res = await volunteerApi.register(form);
      setVolunteer(res.data);
      setRegistered(true);
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const startLocationSharing = () => {
    if (!navigator.geolocation || !volunteer) return;

    const watcher = navigator.geolocation.watchPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        setLastLocation({ lat: latitude, lng: longitude });

        // Update via REST API
        volunteerApi.updateLocation(volunteer.id, latitude, longitude);
        // Also push via WebSocket (lower latency)
        sendLocationUpdate(volunteer.id, latitude, longitude);
      },
      (err) => console.error('Geolocation error:', err),
      { enableHighAccuracy: true, maximumAge: 10000, timeout: 15000 }
    );

    setLocationWatcher(watcher);
    setSharing(true);
  };

  const stopLocationSharing = () => {
    if (locationWatcher) {
      navigator.geolocation.clearWatch(locationWatcher);
      setLocationWatcher(null);
    }
    setSharing(false);
  };

  useEffect(() => {
    return () => {
      if (locationWatcher) navigator.geolocation.clearWatch(locationWatcher);
    };
  }, [locationWatcher]);

  if (registered && volunteer) {
    return (
      <div style={styles.page}>
        <div style={styles.card}>
          <div style={styles.successHeader}>
            <span style={{ fontSize: '32px' }}>✅</span>
            <div>
              <h2>Registered as Volunteer</h2>
              <p style={{ color: 'var(--color-text-muted)', fontSize: '13px' }}>
                Volunteer ID: #{volunteer.id}
              </p>
            </div>
          </div>

          <div style={styles.skillTags}>
            {volunteer.skills?.map(s => (
              <span key={s} style={styles.skillTag}>{s}</span>
            ))}
          </div>

          <div style={styles.locationSection}>
            <h3 style={{ marginBottom: '8px' }}>📍 Location Sharing</h3>
            <p style={{ color: 'var(--color-text-muted)', fontSize: '13px', marginBottom: '16px' }}>
              Share your live GPS to enable auto-assignment to nearby SOS beacons.
            </p>

            {lastLocation && (
              <div style={styles.locationBadge}>
                Last: {lastLocation.lat.toFixed(4)}, {lastLocation.lng.toFixed(4)}
              </div>
            )}

            <button
              className={`btn ${sharing ? 'btn-ghost' : 'btn-primary'}`}
              onClick={sharing ? stopLocationSharing : startLocationSharing}
              style={{ width: '100%', justifyContent: 'center' }}
            >
              {sharing ? '⏹ Stop Sharing Location' : '▶ Start Sharing Location'}
            </button>

            {sharing && (
              <div style={styles.sharingBadge}>
                🟢 Sharing location · You may receive assignment notifications
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h2 style={styles.title}>🙋 Volunteer Registration</h2>
        <p style={{ color: 'var(--color-text-muted)', fontSize: '13px', marginBottom: '24px' }}>
          Register your skills to be auto-assigned to nearby disaster SOS beacons.
        </p>

        <form onSubmit={handleRegister} style={styles.form}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label">Full Name *</label>
              <input className="form-input" name="name" required
                value={form.name}
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                placeholder="Your full name" />
            </div>
            <div className="form-group">
              <label className="form-label">Phone *</label>
              <input className="form-input" name="phone" required type="tel"
                value={form.phone}
                onChange={e => setForm(f => ({ ...f, phone: e.target.value }))}
                placeholder="+91 XXXXXXXXXX" />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Email</label>
            <input className="form-input" name="email" type="email"
              value={form.email}
              onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
              placeholder="email@example.com" />
          </div>

          <div className="form-group">
            <label className="form-label">Skills (select all that apply)</label>
            <div style={styles.skillGrid}>
              {SKILLS.map(skill => (
                <button
                  key={skill}
                  type="button"
                  style={{
                    ...styles.skillBtn,
                    ...(form.skills.includes(skill) ? styles.skillBtnActive : {}),
                  }}
                  onClick={() => handleSkillToggle(skill)}
                >
                  {skill.replace(/_/g, ' ')}
                </button>
              ))}
            </div>
          </div>

          {error && (
            <div style={{ padding: '10px', background: 'rgba(220,38,38,0.1)',
                          border: '1px solid rgba(220,38,38,0.3)',
                          borderRadius: '8px', color: '#fca5a5', fontSize: '13px' }}>
              {error}
            </div>
          )}

          <button className="btn btn-primary" type="submit" disabled={loading}
            style={{ width: '100%', justifyContent: 'center', padding: '12px' }}>
            {loading ? 'Registering...' : '✓ Register as Volunteer'}
          </button>
        </form>
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh', display: 'flex',
    justifyContent: 'center', padding: '24px 16px', overflowY: 'auto',
  },
  card: {
    width: '100%', maxWidth: '520px',
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
    borderRadius: '16px', padding: '28px',
    height: 'fit-content',
  },
  title: { fontSize: '20px', fontWeight: '700', marginBottom: '8px' },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  skillGrid: { display: 'flex', flexWrap: 'wrap', gap: '8px' },
  skillBtn: {
    padding: '6px 12px', borderRadius: '20px',
    border: '1px solid var(--color-border)',
    background: 'transparent',
    color: 'var(--color-text-muted)',
    cursor: 'pointer', fontSize: '12px', fontWeight: '500',
  },
  skillBtnActive: {
    background: 'rgba(30,64,175,0.2)',
    borderColor: '#3b82f6',
    color: '#93c5fd',
  },
  successHeader: {
    display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px',
  },
  skillTags: { display: 'flex', flexWrap: 'wrap', gap: '8px', marginBottom: '24px' },
  skillTag: {
    padding: '4px 10px', borderRadius: '20px', fontSize: '12px',
    background: 'rgba(30,64,175,0.2)', color: '#93c5fd',
    border: '1px solid rgba(59,130,246,0.3)',
  },
  locationSection: {
    background: 'var(--color-bg)', borderRadius: '10px',
    padding: '16px',
  },
  locationBadge: {
    fontSize: '12px', color: 'var(--color-text-muted)',
    fontFamily: 'monospace', marginBottom: '12px',
    padding: '6px 10px', background: 'var(--color-surface)',
    borderRadius: '6px',
  },
  sharingBadge: {
    marginTop: '12px', padding: '8px 12px',
    background: 'rgba(22,163,74,0.1)',
    border: '1px solid rgba(22,163,74,0.3)',
    borderRadius: '8px', fontSize: '12px', color: '#86efac',
    textAlign: 'center',
  },
};
