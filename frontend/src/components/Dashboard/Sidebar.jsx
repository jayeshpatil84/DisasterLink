import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useDisasterStore } from '../../store/useDisasterStore';

export default function Sidebar() {
  const navigate = useNavigate();
  const { user, logout, sidebarOpen, toggleSidebar } = useDisasterStore();

  const handleLogout = () => {
    localStorage.removeItem('dl_token');
    logout();
    navigate('/login');
  };

  return (
    <div className={`sidebar ${sidebarOpen ? '' : 'collapsed'}`}>
      {/* Logo */}
      <div style={styles.logo}>
        <span style={styles.logoIcon}>🚨</span>
        {sidebarOpen && (
          <div>
            <div style={styles.logoTitle}>DisasterLink</div>
            <div style={styles.logoSub}>v1.0 · MVP</div>
          </div>
        )}
        <button style={styles.collapseBtn} onClick={toggleSidebar}>
          {sidebarOpen ? '◀' : '▶'}
        </button>
      </div>

      {/* User info */}
      {sidebarOpen && user && (
        <div style={styles.userCard}>
          <div style={styles.userAvatar}>{user.username?.[0]?.toUpperCase()}</div>
          <div>
            <div style={styles.userName}>{user.username}</div>
            <div style={styles.userRole}>{formatRole(user.role)}</div>
          </div>
        </div>
      )}

      {/* Nav items */}
      <nav style={styles.nav}>
        {NAV_ITEMS.map((item) => (
          <button
            key={item.path}
            style={styles.navItem}
            onClick={() => navigate(item.path)}
          >
            <span style={styles.navIcon}>{item.icon}</span>
            {sidebarOpen && <span style={styles.navLabel}>{item.label}</span>}
          </button>
        ))}
      </nav>

      {/* Bottom: SOS quick-send */}
      {sidebarOpen && (
        <div style={styles.bottomSection}>
          <button
            className="btn btn-danger"
            style={{ width: '100%', justifyContent: 'center' }}
            onClick={() => navigate('/sos')}
          >
            🆘 Send SOS
          </button>
          <button
            className="btn btn-ghost"
            style={{ width: '100%', justifyContent: 'center', marginTop: 8 }}
            onClick={handleLogout}
          >
            ↩ Sign Out
          </button>
        </div>
      )}
    </div>
  );
}

const NAV_ITEMS = [
  { icon: '🗺️',  label: 'Dashboard',  path: '/dashboard' },
  { icon: '🚨',  label: 'Send SOS',   path: '/sos'       },
  { icon: '🙋',  label: 'Volunteers', path: '/volunteer' },
];

const formatRole = (role) => {
  const map = {
    DISTRICT_OFFICER: 'District Officer',
    VOLUNTEER: 'Volunteer',
    VICTIM: 'Citizen',
    ADMIN: 'Admin',
  };
  return map[role] || role;
};

const styles = {
  logo: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    padding: '16px',
    borderBottom: '1px solid var(--color-border)',
  },
  logoIcon: { fontSize: '22px', flexShrink: 0 },
  logoTitle: { fontSize: '15px', fontWeight: '700' },
  logoSub: { fontSize: '10px', color: 'var(--color-text-muted)' },
  collapseBtn: {
    marginLeft: 'auto',
    background: 'transparent',
    border: 'none',
    color: 'var(--color-text-muted)',
    cursor: 'pointer',
    fontSize: '11px',
    padding: '4px',
  },
  userCard: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    padding: '12px 16px',
    borderBottom: '1px solid var(--color-border)',
  },
  userAvatar: {
    width: '32px', height: '32px',
    borderRadius: '50%',
    background: 'var(--color-primary)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontWeight: '700', fontSize: '14px', flexShrink: 0,
  },
  userName: { fontSize: '13px', fontWeight: '600' },
  userRole: { fontSize: '11px', color: 'var(--color-text-muted)' },
  nav: { flex: 1, padding: '8px', display: 'flex', flexDirection: 'column', gap: '2px' },
  navItem: {
    display: 'flex', alignItems: 'center', gap: '10px',
    padding: '10px', borderRadius: '8px',
    background: 'transparent', border: 'none',
    color: 'var(--color-text-muted)',
    cursor: 'pointer', width: '100%', textAlign: 'left',
    fontSize: '13px', fontWeight: '500',
    transition: 'all 0.15s ease',
  },
  navIcon: { fontSize: '16px', flexShrink: 0 },
  navLabel: {},
  bottomSection: { padding: '16px', borderTop: '1px solid var(--color-border)' },
};
