import React from 'react';
import { useDisasterStore } from '../../store/useDisasterStore';

export default function StatsBar() {
  const { stats, beacons, volunteers } = useDisasterStore();

  const activeSos = beacons.filter(b =>
    b.status === 'ACTIVE' || b.status === 'ASSIGNED').length;
  const criticalCount = beacons.filter(b =>
    b.triagePriority === 'CRITICAL' && b.status === 'ACTIVE').length;
  const availableVols = volunteers.filter(v => v.status === 'AVAILABLE').length;
  const resolvedToday = beacons.filter(b => {
    if (b.status !== 'RESOLVED') return false;
    const d = new Date(b.resolvedAt);
    const today = new Date();
    return d.toDateString() === today.toDateString();
  }).length;

  return (
    <div className="stat-grid">
      <div className="stat-card stat-critical">
        <div className="stat-value">{activeSos}</div>
        <div className="stat-label">Active SOS</div>
      </div>
      <div className="stat-card stat-high">
        <div className="stat-value">{criticalCount}</div>
        <div className="stat-label">Critical</div>
      </div>
      <div className="stat-card stat-info">
        <div className="stat-value">{availableVols}</div>
        <div className="stat-label">Available Volunteers</div>
      </div>
      <div className="stat-card stat-success">
        <div className="stat-value">{resolvedToday}</div>
        <div className="stat-label">Resolved Today</div>
      </div>
    </div>
  );
}
