import React from 'react';
import { useDisasterStore } from '../../store/useDisasterStore';

export default function WsStatusBadge() {
  const wsConnected = useDisasterStore((s) => s.wsConnected);

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: '6px',
      fontSize: '12px',
      color: wsConnected ? '#86efac' : '#fca5a5',
    }}>
      <span style={{
        width: '7px', height: '7px',
        borderRadius: '50%',
        background: wsConnected ? '#16a34a' : '#dc2626',
        boxShadow: wsConnected
          ? '0 0 6px #16a34a'
          : '0 0 6px #dc2626',
        flexShrink: 0,
      }} />
      {wsConnected ? 'Live' : 'Reconnecting...'}
    </div>
  );
}
