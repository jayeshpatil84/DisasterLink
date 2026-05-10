import { create } from 'zustand';

export const useDisasterStore = create((set, get) => ({
  // Auth
  user: null,
  token: null,
  setAuth: (user, token) => set({ user, token }),
  logout: () => set({ user: null, token: null }),

  // SOS Beacons
  beacons: [],
  setBeacons: (beacons) => set({ beacons }),
  addBeacon: (beacon) => set((state) => ({
    beacons: [beacon, ...state.beacons.filter(b => b.id !== beacon.id)]
  })),
  updateBeacon: (beacon) => set((state) => ({
    beacons: state.beacons.map(b => b.id === beacon.id ? beacon : b)
  })),

  // Volunteers
  volunteers: [],
  setVolunteers: (volunteers) => set({ volunteers }),
  updateVolunteerLocation: (volunteerId, lat, lng) => set((state) => ({
    volunteers: state.volunteers.map(v =>
      v.id === volunteerId ? { ...v, currentLatitude: lat, currentLongitude: lng } : v
    )
  })),

  // Assignments (real-time)
  assignments: [],
  addAssignment: (assignment) => set((state) => ({
    assignments: [assignment, ...state.assignments.slice(0, 49)]
  })),

  // Dashboard stats
  stats: {
    activeSos: 0,
    criticalSos: 0,
    availableVolunteers: 0,
    resolvedToday: 0,
  },
  setStats: (stats) => set({ stats }),

  // Map state
  mapCenter: [20.5937, 78.9629], // India center
  mapZoom: 5,
  setMapCenter: (center, zoom) => set({ mapCenter: center, mapZoom: zoom }),
  selectedBeacon: null,
  setSelectedBeacon: (beacon) => set({ selectedBeacon: beacon }),

  // WebSocket connection status
  wsConnected: false,
  setWsConnected: (status) => set({ wsConnected: status }),

  // UI
  activeTab: 'map',
  setActiveTab: (tab) => set({ activeTab: tab }),
  sidebarOpen: true,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
}));
