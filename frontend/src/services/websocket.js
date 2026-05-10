import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useDisasterStore } from '../store/useDisasterStore';

let stompClient = null;

export const connectWebSocket = () => {
  const store = useDisasterStore.getState();
  const token = localStorage.getItem('dl_token');

  stompClient = new Client({
    webSocketFactory: () =>
      new SockJS(`${process.env.REACT_APP_API_URL || 'http://localhost:8080'}/ws`),

    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },

    reconnectDelay: 5000,

    onConnect: () => {
      console.log('[WebSocket] Connected to DisasterLink server');
      store.setWsConnected(true);

      // Subscribe: new & updated SOS beacons
      stompClient.subscribe('/topic/sos', (message) => {
        const payload = JSON.parse(message.body);
        const { event, data } = payload;

        if (event === 'SOS_CREATED') {
          store.addBeacon(data);
          showBrowserNotification(data);
        } else if (event === 'SOS_UPDATED') {
          store.updateBeacon(data);
        }

        refreshStats();
      });

      // Subscribe: volunteer assignments
      stompClient.subscribe('/topic/assignments', (message) => {
        const payload = JSON.parse(message.body);
        store.addAssignment(payload);
        store.updateBeacon({ id: payload.beaconId, status: 'ASSIGNED' });
      });

      // Subscribe: volunteer location updates
      stompClient.subscribe('/topic/volunteers', (message) => {
        const payload = JSON.parse(message.body);
        if (payload.event === 'LOCATION_UPDATE') {
          store.updateVolunteerLocation(payload.volunteerId, payload.lat, payload.lng);
        }
      });

      // Subscribe: dashboard stats
      stompClient.subscribe('/topic/stats', (message) => {
        const payload = JSON.parse(message.body);
        store.setStats(payload.data);
      });
    },

    onDisconnect: () => {
      console.log('[WebSocket] Disconnected');
      store.setWsConnected(false);
    },

    onStompError: (frame) => {
      console.error('[WebSocket] STOMP error:', frame.headers['message']);
      store.setWsConnected(false);
    },
  });

  stompClient.activate();
};

export const disconnectWebSocket = () => {
  if (stompClient?.active) {
    stompClient.deactivate();
  }
};

// Send volunteer location update via WebSocket (more efficient than REST polling)
export const sendLocationUpdate = (volunteerId, lat, lng) => {
  if (stompClient?.active) {
    stompClient.publish({
      destination: '/app/volunteer/location',
      body: JSON.stringify({ volunteerId, lat, lng }),
    });
  }
};

const refreshStats = async () => {
  // Recalculate stats from current beacon list
  const { beacons, setStats } = useDisasterStore.getState();
  const today = new Date().toDateString();

  setStats({
    activeSos: beacons.filter(b => b.status === 'ACTIVE' || b.status === 'ASSIGNED').length,
    criticalSos: beacons.filter(b => b.triagePriority === 'CRITICAL' && b.status === 'ACTIVE').length,
    resolvedToday: beacons.filter(b =>
      b.status === 'RESOLVED' && new Date(b.resolvedAt).toDateString() === today
    ).length,
  });
};

const showBrowserNotification = (beacon) => {
  if (!('Notification' in window) || Notification.permission !== 'granted') return;
  new Notification(`🚨 New SOS: ${beacon.disasterType}`, {
    body: `Priority: ${beacon.triagePriority} | People: ${beacon.affectedCount || '?'}`,
    icon: '/logo192.png',
  });
};
