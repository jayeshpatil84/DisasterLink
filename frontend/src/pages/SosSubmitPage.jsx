import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { sosApi } from '../services/api';

export default function SosSubmitPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // 1=form, 2=locating, 3=success
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [submittedBeacon, setSubmittedBeacon] = useState(null);

  const [form, setForm] = useState({
    victimName: '',
    contactPhone: '',
    latitude: null,
    longitude: null,
    locationDescription: '',
    disasterType: 'FLOOD',
    situationDetails: '',
    affectedCount: 1,
  });

  // Auto-detect GPS
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          setForm(f => ({
            ...f,
            latitude: pos.coords.latitude,
            longitude: pos.coords.longitude,
          }));
        },
        () => {
          // Fallback: default to Mumbai
          setForm(f => ({ ...f, latitude: 19.0760, longitude: 72.8777 }));
        }
      );
    }
  }, []);

  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setForm(f => ({ ...f, [name]: type === 'number' ? Number(value) : value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.latitude || !form.longitude) {
      setError('Location is required. Please allow GPS access.');
      return;
    }
    setError('');
    setLoading(true);
    setStep(2);

    try {
      const res = await sosApi.create(form);
      setSubmittedBeacon(res.data);
      setStep(3);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to send SOS. Try again.');
      setStep(1);
    } finally {
      setLoading(false);
    }
  };

  if (step === 3 && submittedBeacon) {
    return <SuccessScreen beacon={submittedBeacon} onBack={() => navigate('/dashboard')} />;
  }

  if (step === 2) {
    return (
      <div style={styles.centerPage}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '48px', marginBottom: '16px' }}>📡</div>
          <h2 style={{ marginBottom: '8px' }}>Sending SOS...</h2>
          <p style={{ color: 'var(--color-text-muted)' }}>
            Connecting to DisasterLink coordination center
          </p>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <div style={styles.header}>
          <button
            style={styles.backBtn}
            onClick={() => navigate('/dashboard')}
          >
            ← Back
          </button>
          <h2 style={styles.title}>🆘 Send Emergency SOS</h2>
          <p style={styles.subtitle}>
            Your SOS will appear on the district officer's live map instantly.
            AI triage will prioritize your request automatically.
          </p>
        </div>

        {/* Location status */}
        <div style={{
          ...styles.locationBanner,
          background: form.latitude ? 'rgba(22,163,74,0.1)' : 'rgba(217,119,6,0.1)',
          borderColor: form.latitude ? 'rgba(22,163,74,0.3)' : 'rgba(217,119,6,0.3)',
        }}>
          <span>{form.latitude ? '📍' : '⏳'}</span>
          {form.latitude
            ? `GPS detected: ${form.latitude.toFixed(4)}, ${form.longitude.toFixed(4)}`
            : 'Detecting your location...'
          }
        </div>

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.grid2}>
            <div className="form-group">
              <label className="form-label">Your Name (optional)</label>
              <input className="form-input" name="victimName"
                value={form.victimName} onChange={handleChange}
                placeholder="Full name" />
            </div>
            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <input className="form-input" name="contactPhone" type="tel"
                value={form.contactPhone} onChange={handleChange}
                placeholder="+91 XXXXXXXXXX" />
            </div>
          </div>

          <div style={styles.grid2}>
            <div className="form-group">
              <label className="form-label">Disaster Type *</label>
              <select className="form-select" name="disasterType"
                value={form.disasterType} onChange={handleChange} required>
                {DISASTER_TYPES.map(d => (
                  <option key={d.value} value={d.value}>{d.label}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">People Affected</label>
              <input className="form-input" name="affectedCount" type="number"
                min="1" max="99999"
                value={form.affectedCount} onChange={handleChange} />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Situation Details</label>
            <textarea
              className="form-textarea"
              name="situationDetails"
              value={form.situationDetails}
              onChange={handleChange}
              placeholder="Describe the emergency: injuries, structural damage, water level, immediate dangers..."
              rows={4}
            />
          </div>

          <div className="form-group">
            <label className="form-label">Location Description</label>
            <input className="form-input" name="locationDescription"
              value={form.locationDescription} onChange={handleChange}
              placeholder="Landmark, building name, street, area..." />
          </div>

          {error && <div style={styles.error}>{error}</div>}

          <button
            className="btn btn-danger"
            type="submit"
            disabled={loading || !form.latitude}
            style={{ width: '100%', justifyContent: 'center', padding: '14px', fontSize: '16px' }}
          >
            🆘 Send SOS Now
          </button>

          <p style={styles.smsHint}>
            No internet? SMS <strong>"SOS FLOOD [LOCATION] [COUNT] PEOPLE"</strong> to{' '}
            <strong>+91-XXXXXXXXXX</strong>
          </p>
        </form>
      </div>
    </div>
  );
}

function SuccessScreen({ beacon, onBack }) {
  return (
    <div style={styles.centerPage}>
      <div style={styles.successCard}>
        <div style={{ fontSize: '56px', marginBottom: '16px' }}>✅</div>
        <h2 style={{ marginBottom: '8px' }}>SOS Sent Successfully</h2>
        <p style={{ color: 'var(--color-text-muted)', marginBottom: '20px' }}>
          Your emergency has been logged and is being coordinated.
        </p>
        <div style={styles.successDetails}>
          <div><strong>SOS ID:</strong> #{beacon.id}</div>
          <div><strong>Priority:</strong> {beacon.triagePriority} (AI-assigned)</div>
          <div><strong>Status:</strong> {beacon.status}</div>
        </div>
        <p style={{ fontSize: '13px', color: 'var(--color-text-muted)', marginBottom: '20px' }}>
          A volunteer will be auto-assigned. You'll receive an SMS confirmation if a phone number was provided.
        </p>
        <button className="btn btn-primary" onClick={onBack}>
          ← Back to Dashboard
        </button>
      </div>
    </div>
  );
}

const DISASTER_TYPES = [
  { value: 'FLOOD',      label: '🌊 Flood'       },
  { value: 'EARTHQUAKE', label: '🌍 Earthquake'   },
  { value: 'FIRE',       label: '🔥 Fire'         },
  { value: 'CYCLONE',    label: '🌀 Cyclone'      },
  { value: 'LANDSLIDE',  label: '⛰️ Landslide'   },
  { value: 'OTHER',      label: '⚠️ Other'        },
];

const styles = {
  page: {
    minHeight: '100vh', overflowY: 'auto',
    padding: '24px 16px', display: 'flex', justifyContent: 'center',
  },
  card: {
    width: '100%', maxWidth: '600px',
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
    borderRadius: '16px', padding: '28px',
  },
  header: { marginBottom: '20px' },
  backBtn: {
    background: 'transparent', border: 'none',
    color: 'var(--color-text-muted)', cursor: 'pointer',
    fontSize: '13px', marginBottom: '12px',
  },
  title: { fontSize: '22px', fontWeight: '700', marginBottom: '8px' },
  subtitle: { color: 'var(--color-text-muted)', fontSize: '13px' },
  locationBanner: {
    display: 'flex', alignItems: 'center', gap: '8px',
    padding: '10px 12px', borderRadius: '8px',
    border: '1px solid', fontSize: '13px',
    marginBottom: '20px',
  },
  form: { display: 'flex', flexDirection: 'column', gap: '16px' },
  grid2: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' },
  error: {
    padding: '10px 12px',
    background: 'rgba(220,38,38,0.15)',
    border: '1px solid rgba(220,38,38,0.4)',
    borderRadius: '8px',
    color: '#fca5a5', fontSize: '13px',
  },
  smsHint: {
    textAlign: 'center', fontSize: '12px',
    color: 'var(--color-text-muted)',
    padding: '8px', background: 'var(--color-bg)',
    borderRadius: '6px',
  },
  centerPage: {
    minHeight: '100vh', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
    padding: '24px',
  },
  successCard: {
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
    borderRadius: '16px', padding: '40px',
    textAlign: 'center', maxWidth: '440px',
  },
  successDetails: {
    background: 'var(--color-bg)', borderRadius: '8px',
    padding: '14px 16px', marginBottom: '16px',
    textAlign: 'left', fontSize: '13px',
    display: 'flex', flexDirection: 'column', gap: '6px',
  },
};
