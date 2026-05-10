import React from 'react';
import { useDisasterStore } from '../store/useDisasterStore';
import Sidebar from '../components/Dashboard/Sidebar';
import StatsBar from '../components/Dashboard/StatsBar';
import LiveMap from '../components/Map/LiveMap';
import SosPanel from '../components/SOS/SosPanel';
import AssignmentFeed from '../components/Volunteer/AssignmentFeed';
import WsStatusBadge from '../components/Dashboard/WsStatusBadge';

export default function DashboardPage() {
  const { activeTab, setActiveTab } = useDisasterStore();

  return (
    <div className="app-layout">
      <Sidebar />

      <div className="main-content">
        {/* Top bar */}
        <div style={styles.topbar}>
          <div style={styles.tabs}>
            {TABS.map((tab) => (
              <button
                key={tab.id}
                style={{
                  ...styles.tab,
                  ...(activeTab === tab.id ? styles.tabActive : {}),
                }}
                onClick={() => setActiveTab(tab.id)}
              >
                <span>{tab.icon}</span>
                <span>{tab.label}</span>
              </button>
            ))}
          </div>
          <WsStatusBadge />
        </div>

        {/* Stats strip */}
        <StatsBar />

        {/* Main view */}
        <div style={styles.viewArea}>
          {activeTab === 'map'        && <LiveMap />}
          {activeTab === 'sos'        && <SosPanel />}
          {activeTab === 'volunteers' && <AssignmentFeed />}
        </div>
      </div>
    </div>
  );
}

const TABS = [
  { id: 'map',        icon: '🗺️',  label: 'Live Map'    },
  { id: 'sos',        icon: '🚨',  label: 'SOS Beacons' },
  { id: 'volunteers', icon: '🙋',  label: 'Volunteers'  },
];

const styles = {
  topbar: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 16px',
    height: '52px',
    background: 'var(--color-surface)',
    borderBottom: '1px solid var(--color-border)',
    flexShrink: 0,
  },
  tabs: { display: 'flex', gap: '4px' },
  tab: {
    display: 'flex',
    alignItems: 'center',
    gap: '6px',
    padding: '6px 14px',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    background: 'transparent',
    color: 'var(--color-text-muted)',
    fontSize: '13px',
    fontWeight: '500',
    transition: 'all 0.15s ease',
  },
  tabActive: {
    background: 'rgba(30,64,175,0.2)',
    color: '#93c5fd',
  },
  viewArea: {
    flex: 1,
    overflow: 'hidden',
    position: 'relative',
  },
};
