import React, { useState } from 'react';
import { useDisasterStore } from '../../store/useDisasterStore';
import { sosApi } from '../../services/api';
import { formatDistanceToNow } from 'date-fns';

const PRIORITY_ORDER = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };

export default function SosPanel() {
  const { beacons, updateBeacon } = useDisasterStore();
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');

  const filtered = beacons
    .filter(b => filter === 'ALL' || b.status === filter || b.triagePriority === filter)
    .filter(b =>
      !search ||
      b.disasterType?.toLowerCase().includes(search.toLowerCase()) ||
      b.situationDetails?.toLowerCase().includes(search.toLowerCase()) ||
      b.victimName?.toLowerCase().includes(search.toLowerCase())
    )
    .sort((a, b) => (PRIORITY_ORDER[a.triagePriority] ?? 9) - (PRIORITY_ORDER[b.triagePriority] ?? 9));

  const handleStatusChange = async (beacon, status) => {
    try {
      const res = await sosApi.updateStatus(beacon.id, status);
      updateBeacon(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div style={styles.container}>
      {/* Controls */}
      <div style={styles.controls}>
        <input
          className="form-input"
          style={{ maxWidth: '240px' }}
          placeholder="🔍 Search beacons..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />
        <div style={styles.filterRow}>
          {FILTERS.map(f => (
            <button
              key={f.value}
              style={{
                ...styles.filterBtn,
                ...(filter === f.value ? styles.filterBtnActive : {})
              }}
              onClick={() => setFilter(f.value)}
            >
              {f.label}
            </button>
          ))}
        </div>
        <div style={styles.count}>{filtered.length} beacons</div>
      </div>

      {/* List */}
      <div style={styles.list}>
        {filtered.length === 0 && (
          <div style={styles.empty}>
            No beacons match current filters.
          </div>
        )}
        {filtered.map(beacon => (
          <BeaconCard
            key={beacon.id}
            beacon={beacon}
            onStatusChange={handleStatusChange}
          />
        ))}
      </div>
    </div>
  );
}

function BeaconCard({ beacon, onStatusChange }) {
  const [expanded, setExpanded] = useState(false);

  const priorityClass = `badge badge-${beacon.triagePriority?.toLowerCase()}`;
  const timeAgo = beacon.createdAt
    ? formatDistanceToNow(new Date(beacon.createdAt), { addSuffix: true })
    : '';

  return (
    <div
      className="slide-in"
      style={{
        ...styles.card,
        borderLeft: `3px solid ${PRIORITY_COLORS[beacon.triagePriority] || '#475569'}`,
      }}
    >
      {/* Header row */}
      <div style={styles.cardHeader} onClick={() => setExpanded(!expanded)}>
        <div style={styles.cardLeft}>
          <span style={styles.beaconId}>#{beacon.id}</span>
          <div>
            <div style={styles.disasterType}>{beacon.disasterType}</div>
            <div style={styles.meta}>
              {beacon.affectedCount || '?'} people · {beacon.source} · {timeAgo}
            </div>
          </div>
        </div>

        <div style={styles.cardRight}>
          <span className={priorityClass}>
            {beacon.triagePriority}
          </span>
          <span style={styles.scoreChip}>
            {beacon.triageScore}/100
          </span>
          <StatusBadge status={beacon.status} />
          <span style={{ color: 'var(--color-text-muted)', fontSize: '12px' }}>
            {expanded ? '▲' : '▼'}
          </span>
        </div>
      </div>

      {/* Expanded details */}
      {expanded && (
        <div style={styles.expandedBody}>
          {beacon.situationDetails && (
            <div style={styles.detailRow}>
              <span style={styles.detailLabel}>Situation</span>
              <span>{beacon.situationDetails}</span>
            </div>
          )}
          {beacon.triageReason && (
            <div style={styles.detailRow}>
              <span style={styles.detailLabel}>🤖 AI Triage</span>
              <span style={{ color: '#93c5fd', fontStyle: 'italic' }}>
                {beacon.triageReason}
              </span>
            </div>
          )}
          {beacon.victimName && (
            <div style={styles.detailRow}>
              <span style={styles.detailLabel}>Contact</span>
              <span>{beacon.victimName} · {beacon.contactPhone || 'N/A'}</span>
            </div>
          )}
          <div style={styles.detailRow}>
            <span style={styles.detailLabel}>Location</span>
            <a
              href={`https://maps.google.com/?q=${beacon.latitude},${beacon.longitude}`}
              target="_blank"
              rel="noreferrer"
              style={{ color: '#60a5fa' }}
            >
              {beacon.latitude?.toFixed(4)}, {beacon.longitude?.toFixed(4)} ↗
            </a>
          </div>
          {beacon.assignedVolunteerId && (
            <div style={styles.detailRow}>
              <span style={styles.detailLabel}>Assigned To</span>
              <span>Volunteer #{beacon.assignedVolunteerId}</span>
            </div>
          )}

          {/* Actions */}
          <div style={styles.actions}>
            {beacon.status === 'ACTIVE' && (
              <button
                className="btn btn-primary"
                style={{ fontSize: '12px', padding: '6px 12px' }}
                onClick={() => onStatusChange(beacon, 'IN_PROGRESS')}
              >
                Mark In Progress
              </button>
            )}
            {beacon.status !== 'RESOLVED' && (
              <button
                className="btn"
                style={{ fontSize: '12px', padding: '6px 12px',
                         background: '#16a34a', color: 'white' }}
                onClick={() => onStatusChange(beacon, 'RESOLVED')}
              >
                ✓ Resolve
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function StatusBadge({ status }) {
  const colors = {
    ACTIVE: '#dc2626',
    ASSIGNED: '#3b82f6',
    IN_PROGRESS: '#8b5cf6',
    RESOLVED: '#16a34a',
    CLOSED: '#6b7280',
  };
  return (
    <span style={{
      fontSize: '10px',
      padding: '2px 7px',
      borderRadius: '20px',
      border: `1px solid ${colors[status] || '#475569'}`,
      color: colors[status] || '#94a3b8',
      fontWeight: 600,
    }}>
      {status}
    </span>
  );
}

const PRIORITY_COLORS = {
  CRITICAL: '#dc2626',
  HIGH: '#ea580c',
  MEDIUM: '#d97706',
  LOW: '#16a34a',
};

const FILTERS = [
  { value: 'ALL', label: 'All' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'CRITICAL', label: '🔴 Critical' },
  { value: 'HIGH', label: '🟠 High' },
  { value: 'ASSIGNED', label: 'Assigned' },
  { value: 'RESOLVED', label: 'Resolved' },
];

const styles = {
  container: {
    height: '100%', display: 'flex', flexDirection: 'column',
    overflow: 'hidden',
  },
  controls: {
    padding: '12px 16px',
    borderBottom: '1px solid var(--color-border)',
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    flexWrap: 'wrap',
    background: 'var(--color-surface)',
  },
  filterRow: { display: 'flex', gap: '4px', flexWrap: 'wrap' },
  filterBtn: {
    padding: '4px 10px', borderRadius: '20px',
    border: '1px solid var(--color-border)',
    background: 'transparent',
    color: 'var(--color-text-muted)',
    cursor: 'pointer', fontSize: '12px',
  },
  filterBtnActive: {
    background: 'rgba(30,64,175,0.2)',
    borderColor: '#3b82f6',
    color: '#93c5fd',
  },
  count: { marginLeft: 'auto', fontSize: '12px', color: 'var(--color-text-muted)' },
  list: { flex: 1, overflowY: 'auto', padding: '12px 16px', display: 'flex', flexDirection: 'column', gap: '8px' },
  empty: { textAlign: 'center', color: 'var(--color-text-muted)', padding: '40px', fontSize: '14px' },
  card: {
    background: 'var(--color-surface)',
    border: '1px solid var(--color-border)',
    borderRadius: '10px',
    overflow: 'hidden',
  },
  cardHeader: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '12px 14px', cursor: 'pointer',
  },
  cardLeft: { display: 'flex', alignItems: 'center', gap: '10px' },
  cardRight: { display: 'flex', alignItems: 'center', gap: '8px' },
  beaconId: { fontSize: '11px', color: 'var(--color-text-muted)', fontFamily: 'monospace' },
  disasterType: { fontSize: '14px', fontWeight: '600' },
  meta: { fontSize: '11px', color: 'var(--color-text-muted)', marginTop: '2px' },
  scoreChip: {
    fontSize: '11px', padding: '2px 7px', borderRadius: '4px',
    background: 'var(--color-surface-2)', color: 'var(--color-text-muted)',
    fontFamily: 'monospace',
  },
  expandedBody: {
    padding: '0 14px 14px', display: 'flex', flexDirection: 'column', gap: '8px',
    borderTop: '1px solid var(--color-border)', paddingTop: '12px',
  },
  detailRow: {
    display: 'flex', gap: '12px', fontSize: '13px',
    alignItems: 'flex-start',
  },
  detailLabel: {
    color: 'var(--color-text-muted)', minWidth: '80px',
    fontWeight: '500', fontSize: '12px', flexShrink: 0,
  },
  actions: { display: 'flex', gap: '8px', marginTop: '4px' },
};
