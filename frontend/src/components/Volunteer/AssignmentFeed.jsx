import React from 'react';
import { useDisasterStore } from '../../store/useDisasterStore';
import { formatDistanceToNow } from 'date-fns';

export default function AssignmentFeed() {
  const { assignments, volunteers, beacons } = useDisasterStore();

  const assignedVols = volunteers.filter(v => v.status === 'ASSIGNED');
  const availableVols = volunteers.filter(v => v.status === 'AVAILABLE');

  return (
    <div style={styles.container}>
      {/* Left: Volunteer status */}
      <div style={styles.panel}>
        <div style={styles.panelHeader}>
          <span>🙋 Volunteers</span>
          <span style={styles.panelCount}>{volunteers.length} total</span>
        </div>

        <div style={styles.statRow}>
          <StatPill label="Available" count={availableVols.length} color="#16a34a" />
          <StatPill label="Assigned" count={assignedVols.length} color="#3b82f6" />
          <StatPill label="Offline"
            count={volunteers.filter(v => v.status === 'OFFLINE').length}
            color="#6b7280" />
        </div>

        <div style={styles.volList}>
          {volunteers.length === 0 && (
            <div style={styles.empty}>No volunteers registered yet.</div>
          )}
          {volunteers.map(v => (
            <VolunteerRow key={v.id} volunteer={v} beacons={beacons} />
          ))}
        </div>
      </div>

      {/* Right: Real-time assignment feed */}
      <div style={styles.panel}>
        <div style={styles.panelHeader}>
          <span>⚡ Live Assignment Feed</span>
          <span style={styles.panelCount}>{assignments.length} events</span>
        </div>

        <div style={styles.feedList}>
          {assignments.length === 0 && (
            <div style={styles.empty}>
              Waiting for real-time assignment events via WebSocket...
            </div>
          )}
          {assignments.map((event, i) => (
            <FeedEvent key={i} event={event} />
          ))}
        </div>
      </div>
    </div>
  );
}

function VolunteerRow({ volunteer, beacons }) {
  const assignedBeacon = volunteer.currentSosId
    ? beacons.find(b => b.id === volunteer.currentSosId)
    : null;

  const statusColors = {
    AVAILABLE: '#16a34a',
    ASSIGNED: '#3b82f6',
    BUSY: '#d97706',
    OFFLINE: '#6b7280',
  };

  return (
    <div style={styles.volRow}>
      <div style={{
        ...styles.statusDot,
        background: statusColors[volunteer.status] || '#6b7280',
        boxShadow: volunteer.status === 'AVAILABLE'
          ? '0 0 6px #16a34a' : 'none',
      }} />

      <div style={styles.volInfo}>
        <div style={styles.volName}>{volunteer.name}</div>
        <div style={styles.volMeta}>
          {volunteer.skills?.join(', ') || 'General'}
          {assignedBeacon && (
            <span style={{ color: '#60a5fa', marginLeft: '8px' }}>
              → SOS #{assignedBeacon.id} ({assignedBeacon.disasterType})
            </span>
          )}
        </div>
      </div>

      <div style={{
        fontSize: '11px',
        color: statusColors[volunteer.status],
        fontWeight: '600',
        marginLeft: 'auto',
      }}>
        {volunteer.status}
      </div>
    </div>
  );
}

function FeedEvent({ event }) {
  const icons = {
    VOLUNTEER_ASSIGNED: '🔗',
    SOS_CREATED: '🆘',
    SOS_UPDATED: '🔄',
    LOCATION_UPDATE: '📍',
  };

  return (
    <div className="slide-in" style={styles.feedEvent}>
      <span style={styles.feedIcon}>{icons[event.event] || '📌'}</span>
      <div style={styles.feedBody}>
        <div style={styles.feedTitle}>
          {event.event === 'VOLUNTEER_ASSIGNED'
            ? `Volunteer #${event.volunteerId} assigned to SOS #${event.beaconId}`
            : event.event?.replace(/_/g, ' ')
          }
        </div>
        {event.volunteerName && (
          <div style={styles.feedMeta}>{event.volunteerName}</div>
        )}
      </div>
      <div style={styles.feedTime}>just now</div>
    </div>
  );
}

function StatPill({ label, count, color }) {
  return (
    <div style={{ ...styles.statPill, borderColor: color }}>
      <span style={{ color, fontWeight: '700', fontSize: '18px' }}>{count}</span>
      <span style={{ fontSize: '11px', color: 'var(--color-text-muted)' }}>{label}</span>
    </div>
  );
}

const styles = {
  container: {
    display: 'grid', gridTemplateColumns: '1fr 1fr',
    gap: '0', height: '100%', overflow: 'hidden',
  },
  panel: {
    display: 'flex', flexDirection: 'column',
    borderRight: '1px solid var(--color-border)',
    overflow: 'hidden',
  },
  panelHeader: {
    padding: '14px 16px',
    borderBottom: '1px solid var(--color-border)',
    display: 'flex', alignItems: 'center',
    justifyContent: 'space-between',
    fontWeight: '600', fontSize: '14px',
    background: 'var(--color-surface)',
  },
  panelCount: {
    fontSize: '11px', color: 'var(--color-text-muted)',
    background: 'var(--color-bg)',
    padding: '2px 8px', borderRadius: '20px',
  },
  statRow: {
    display: 'flex', gap: '8px',
    padding: '12px 16px',
    borderBottom: '1px solid var(--color-border)',
  },
  statPill: {
    flex: 1, display: 'flex', flexDirection: 'column',
    alignItems: 'center', gap: '2px',
    padding: '8px', borderRadius: '8px',
    border: '1px solid',
    background: 'var(--color-bg)',
  },
  volList: { flex: 1, overflowY: 'auto', padding: '8px' },
  volRow: {
    display: 'flex', alignItems: 'center', gap: '10px',
    padding: '10px 12px', borderRadius: '8px',
    transition: 'background 0.15s',
    cursor: 'default',
    '&:hover': { background: 'var(--color-surface-2)' },
  },
  statusDot: {
    width: '8px', height: '8px',
    borderRadius: '50%', flexShrink: 0,
  },
  volInfo: { flex: 1, minWidth: 0 },
  volName: { fontSize: '13px', fontWeight: '600', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' },
  volMeta: { fontSize: '11px', color: 'var(--color-text-muted)', marginTop: '2px' },
  feedList: { flex: 1, overflowY: 'auto', padding: '8px', display: 'flex', flexDirection: 'column', gap: '6px' },
  feedEvent: {
    display: 'flex', alignItems: 'flex-start', gap: '10px',
    padding: '10px 12px', borderRadius: '8px',
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
  },
  feedIcon: { fontSize: '16px', flexShrink: 0, marginTop: '1px' },
  feedBody: { flex: 1, minWidth: 0 },
  feedTitle: { fontSize: '13px', fontWeight: '500' },
  feedMeta: { fontSize: '11px', color: 'var(--color-text-muted)', marginTop: '2px' },
  feedTime: { fontSize: '10px', color: 'var(--color-text-muted)', flexShrink: 0 },
  empty: { textAlign: 'center', color: 'var(--color-text-muted)', padding: '32px', fontSize: '13px' },
};
