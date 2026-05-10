import React, { useEffect, useRef } from 'react';
import { MapContainer, TileLayer, CircleMarker, Circle,
         Popup, Tooltip, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { useDisasterStore } from '../../store/useDisasterStore';
import { sosApi } from '../../services/api';

// Priority colour config
const PRIORITY_COLORS = {
  CRITICAL: { color: '#dc2626', fill: '#dc2626', fillOpacity: 0.7, radius: 14 },
  HIGH:     { color: '#ea580c', fill: '#ea580c', fillOpacity: 0.6, radius: 11 },
  MEDIUM:   { color: '#d97706', fill: '#d97706', fillOpacity: 0.5, radius: 9  },
  LOW:      { color: '#16a34a', fill: '#16a34a', fillOpacity: 0.4, radius: 7  },
};

const STATUS_COLORS = {
  ACTIVE:      null,   // use priority colour
  ASSIGNED:    '#3b82f6',
  IN_PROGRESS: '#8b5cf6',
  RESOLVED:    '#6b7280',
};

// Helper: auto-pan to first critical beacon
function MapController({ beacons }) {
  const map = useMap();

  useEffect(() => {
    const critical = beacons.find(b => b.triagePriority === 'CRITICAL' && b.status === 'ACTIVE');
    if (critical) {
      map.flyTo([critical.latitude, critical.longitude], 10, { duration: 1.5 });
    }
  }, [beacons.length]);

  return null;
}

export default function LiveMap() {
  const { beacons, setSelectedBeacon, updateBeacon } = useDisasterStore();

  const handleResolve = async (beacon) => {
    try {
      const res = await sosApi.updateStatus(beacon.id, 'RESOLVED');
      updateBeacon(res.data);
    } catch (err) {
      console.error('Failed to resolve:', err);
    }
  };

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative' }}>
      <MapContainer
        center={[20.5937, 78.9629]}
        zoom={5}
        style={{ width: '100%', height: '100%' }}
        zoomControl={true}
      >
        {/* Dark tile layer - OpenStreetMap */}
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          className="map-tiles-dark"
        />

        <MapController beacons={beacons} />

        {/* Render SOS beacons as colored circle markers */}
        {beacons.map((beacon) => {
          const pConfig = PRIORITY_COLORS[beacon.triagePriority] || PRIORITY_COLORS.MEDIUM;
          const statusColor = STATUS_COLORS[beacon.status];
          const color = statusColor || pConfig.color;

          return (
            <CircleMarker
              key={beacon.id}
              center={[beacon.latitude, beacon.longitude]}
              radius={pConfig.radius}
              pathOptions={{
                color,
                fillColor: color,
                fillOpacity: pConfig.fillOpacity,
                weight: beacon.triagePriority === 'CRITICAL' ? 3 : 2,
              }}
              eventHandlers={{
                click: () => setSelectedBeacon(beacon),
              }}
            >
              <Tooltip direction="top" offset={[0, -10]}>
                <strong>{beacon.disasterType}</strong> — {beacon.triagePriority}
                <br />
                {beacon.affectedCount} people | {beacon.status}
              </Tooltip>

              <Popup>
                <div style={{ minWidth: '200px', fontFamily: 'sans-serif' }}>
                  <div style={{ fontWeight: '700', marginBottom: '6px', fontSize: '14px' }}>
                    🚨 SOS #{beacon.id} — {beacon.disasterType}
                  </div>

                  <table style={{ fontSize: '12px', width: '100%', borderCollapse: 'collapse' }}>
                    <tbody>
                      <tr>
                        <td style={tdStyle}>Priority</td>
                        <td style={{ ...tdStyle, fontWeight: 600, color: pConfig.color }}>
                          {beacon.triagePriority}
                        </td>
                      </tr>
                      <tr>
                        <td style={tdStyle}>Score</td>
                        <td style={tdStyle}>{beacon.triageScore}/100</td>
                      </tr>
                      <tr>
                        <td style={tdStyle}>Status</td>
                        <td style={tdStyle}>{beacon.status}</td>
                      </tr>
                      <tr>
                        <td style={tdStyle}>People</td>
                        <td style={tdStyle}>{beacon.affectedCount || '?'}</td>
                      </tr>
                      {beacon.victimName && (
                        <tr>
                          <td style={tdStyle}>Victim</td>
                          <td style={tdStyle}>{beacon.victimName}</td>
                        </tr>
                      )}
                    </tbody>
                  </table>

                  {beacon.situationDetails && (
                    <div style={{ fontSize: '12px', marginTop: '6px',
                                  padding: '6px', background: '#f1f5f9',
                                  borderRadius: '4px', color: '#334155' }}>
                      {beacon.situationDetails}
                    </div>
                  )}

                  {beacon.triageReason && (
                    <div style={{ fontSize: '11px', marginTop: '6px',
                                  color: '#64748b', fontStyle: 'italic' }}>
                      AI: {beacon.triageReason}
                    </div>
                  )}

                  <div style={{ marginTop: '8px', display: 'flex', gap: '6px' }}>
                    <a
                      href={`https://maps.google.com/?q=${beacon.latitude},${beacon.longitude}`}
                      target="_blank"
                      rel="noreferrer"
                      style={{ fontSize: '11px', color: '#1e40af' }}
                    >
                      📍 Open in Maps
                    </a>
                    {beacon.status !== 'RESOLVED' && (
                      <button
                        onClick={() => handleResolve(beacon)}
                        style={{ fontSize: '11px', background: '#16a34a', color: 'white',
                                 border: 'none', borderRadius: '4px', padding: '2px 8px',
                                 cursor: 'pointer', marginLeft: 'auto' }}
                      >
                        ✓ Resolve
                      </button>
                    )}
                  </div>
                </div>
              </Popup>
            </CircleMarker>
          );
        })}

        {/* Pulse ring for CRITICAL beacons */}
        {beacons
          .filter(b => b.triagePriority === 'CRITICAL' && b.status === 'ACTIVE')
          .map(b => (
            <Circle
              key={`pulse-${b.id}`}
              center={[b.latitude, b.longitude]}
              radius={5000}
              pathOptions={{
                color: '#dc2626',
                fillOpacity: 0.06,
                weight: 1,
                dashArray: '6 4',
              }}
            />
          ))
        }
      </MapContainer>

      {/* Legend */}
      <div style={styles.legend}>
        <div style={styles.legendTitle}>Priority Legend</div>
        {Object.entries(PRIORITY_COLORS).map(([p, c]) => (
          <div key={p} style={styles.legendItem}>
            <span style={{ ...styles.dot, background: c.color }} />
            {p}
          </div>
        ))}
        <div style={styles.legendItem}>
          <span style={{ ...styles.dot, background: '#3b82f6' }} />
          ASSIGNED
        </div>
      </div>

      {/* Count badge */}
      <div style={styles.countBadge}>
        {beacons.filter(b => b.status === 'ACTIVE').length} active SOS
      </div>
    </div>
  );
}

const tdStyle = {
  padding: '3px 8px 3px 0',
  color: '#475569',
  verticalAlign: 'top',
};

const styles = {
  legend: {
    position: 'absolute',
    bottom: '24px',
    right: '12px',
    background: 'rgba(15,23,42,0.9)',
    border: '1px solid #334155',
    borderRadius: '10px',
    padding: '12px',
    zIndex: 1000,
    backdropFilter: 'blur(8px)',
    minWidth: '130px',
  },
  legendTitle: {
    fontSize: '11px',
    fontWeight: '700',
    textTransform: 'uppercase',
    color: '#94a3b8',
    letterSpacing: '0.5px',
    marginBottom: '8px',
  },
  legendItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    fontSize: '12px',
    color: '#f1f5f9',
    marginBottom: '4px',
  },
  dot: {
    width: '10px', height: '10px',
    borderRadius: '50%', flexShrink: 0,
  },
  countBadge: {
    position: 'absolute',
    top: '12px',
    left: '12px',
    background: 'rgba(220,38,38,0.9)',
    color: 'white',
    padding: '6px 12px',
    borderRadius: '20px',
    fontSize: '12px',
    fontWeight: '600',
    zIndex: 1000,
    backdropFilter: 'blur(8px)',
  },
};
