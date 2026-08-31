import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { Subscription } from 'rxjs';
import * as L from 'leaflet';
import { SosService } from '../../services/sos.service';
import { WebSocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth';
import {
  DashboardStats,
  SosBeacon,
  SosStatusHistory,
  VolunteerInfo,
  URGENCY_CLASS,
  URGENCY_EMOJI,
  DISASTER_EMOJI,
} from '../../models/sos-beacon.model';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit, OnDestroy, AfterViewInit {
  // Section 1: Stats
  stats: DashboardStats = {
    totalBeacons: 0,
    criticalBeacons: 0,
    highBeacons: 0,
    pendingBeacons: 0,
    assignedBeacons: 0,
    inProgressBeacons: 0,
    resolvedBeacons: 0,
    totalVolunteers: 0,
    activeVolunteers: 0,
    availableVolunteers: 0,
    busyVolunteers: 0,
  };

  // Section 2 & 3: SOS Beacons & Map
  beacons: SosBeacon[] = [];
  filteredBeacons: SosBeacon[] = [];
  selectedBeacon: SosBeacon | null = null;
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  // Filters
  urgencyFilter: string = 'ALL';
  statusFilter: string = 'ALL';

  // Section 4: Volunteers
  volunteers: VolunteerInfo[] = [];
  loadingVolunteers = false;

  // Assign / Reassign form state
  selectedVolunteerId: { [beaconId: number]: number } = {};

  // Timeline / History state
  selectedTimeline: SosStatusHistory[] = [];
  selectedTimelineBeaconId: number | null = null;
  loadingTimeline = false;

  private map: L.Map | null = null;
  private markersMap: Map<number, L.Marker> = new Map();
  private subscriptions: Subscription[] = [];

  urgencyClass = URGENCY_CLASS;
  urgencyEmoji = URGENCY_EMOJI;
  disasterEmoji = DISASTER_EMOJI;

  constructor(
    private sosService: SosService,
    private wsService: WebSocketService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadBeacons();
    this.loadVolunteers();
    this.setupWebSocket();
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.initMap();
    }, 150);
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  // Section 1: Stats
  loadStats(): void {
    this.sosService.getOfficerStats().subscribe({
      next: (data) => {
        if (data) this.stats = data;
      },
      error: (err) => console.warn('Could not load officer stats', err),
    });
  }

  // Section 2 & 3: Beacons & Filters
  loadBeacons(): void {
    this.loading = true;
    this.sosService.getOfficerSos(this.urgencyFilter, this.statusFilter).subscribe({
      next: (data) => {
        this.beacons = data ?? [];
        this.applyLocalFilters();
        this.updateMapMarkers();
        this.loading = false;
      },
      error: () => {
        this.beacons = [];
        this.filteredBeacons = [];
        this.updateMapMarkers();
        this.loading = false;
      },
    });
  }

  // Section 4: Volunteers
  loadVolunteers(): void {
    this.loadingVolunteers = true;
    this.sosService.getOfficerVolunteers().subscribe({
      next: (data) => {
        this.volunteers = data ?? [];
        this.loadingVolunteers = false;
      },
      error: () => {
        this.volunteers = [];
        this.loadingVolunteers = false;
      },
    });
  }

  get availableVolunteers(): VolunteerInfo[] {
    return this.volunteers.filter((v) => v.volunteerStatus === 'AVAILABLE');
  }

  setUrgencyFilter(urgency: string): void {
    this.urgencyFilter = urgency;
    this.loadBeacons();
  }

  setStatusFilter(status: string): void {
    this.statusFilter = status;
    this.loadBeacons();
  }

  applyLocalFilters(): void {
    this.filteredBeacons = this.beacons.filter((b) => {
      const matchUrgency =
        this.urgencyFilter === 'ALL' || b.urgencyLabel === this.urgencyFilter;
      const matchStatus =
        this.statusFilter === 'ALL' || b.status === this.statusFilter;
      return matchUrgency && matchStatus;
    });
  }

  // Assign volunteer
  assignVolunteer(beaconId: number): void {
    const volId = this.selectedVolunteerId[beaconId];
    if (!volId) {
      this.error = 'Please select a volunteer from the dropdown';
      return;
    }

    this.sosService.assignVolunteer(beaconId, volId).subscribe({
      next: () => {
        this.successMessage = `Volunteer assigned successfully to SOS #${beaconId}`;
        this.loadBeacons();
        this.loadVolunteers();
        this.loadStats();
        setTimeout(() => (this.successMessage = null), 4000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to assign volunteer';
        setTimeout(() => (this.error = null), 5000);
      },
    });
  }

  // Reassign volunteer
  reassignVolunteer(beaconId: number): void {
    const volId = this.selectedVolunteerId[beaconId];
    if (!volId) {
      this.error = 'Please select a volunteer from the dropdown';
      return;
    }

    this.sosService.reassignVolunteer(beaconId, volId).subscribe({
      next: () => {
        this.successMessage = `Volunteer reassigned successfully for SOS #${beaconId}`;
        this.loadBeacons();
        this.loadVolunteers();
        this.loadStats();
        setTimeout(() => (this.successMessage = null), 4000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to reassign volunteer';
        setTimeout(() => (this.error = null), 5000);
      },
    });
  }

  // View SOS Timeline / History
  viewTimeline(beacon: SosBeacon): void {
    this.selectedTimelineBeaconId = beacon.id;
    this.loadingTimeline = true;
    this.sosService.getSosHistory(beacon.id).subscribe({
      next: (history) => {
        this.selectedTimeline = history ?? [];
        this.loadingTimeline = false;
      },
      error: () => {
        this.selectedTimeline = [];
        this.loadingTimeline = false;
      },
    });
  }

  closeTimeline(): void {
    this.selectedTimelineBeaconId = null;
    this.selectedTimeline = [];
  }

  // Real-time WebSocket
  setupWebSocket(): void {
    try {
      this.wsService.connect();

      // Subscribe to new SOS notifications
      this.subscriptions.push(
        this.wsService.getNewSosNotifications().subscribe({
          next: (notif) => {
            this.successMessage = notif.message;
            this.loadStats();
            this.loadBeacons();
            this.loadVolunteers();
            setTimeout(() => (this.successMessage = null), 5000);
          },
          error: (err) => console.warn('WS new-sos error:', err),
        })
      );

      // Subscribe to resolved SOS notifications
      this.subscriptions.push(
        this.wsService.getResolvedSosNotifications().subscribe({
          next: (notif) => {
            this.successMessage = notif.message;
            this.loadStats();
            this.loadBeacons();
            this.loadVolunteers();
            setTimeout(() => (this.successMessage = null), 5000);
          },
          error: (err) => console.warn('WS resolved error:', err),
        })
      );

      // Fallback feed
      this.subscriptions.push(
        this.wsService.getBeaconFeed().subscribe({
          next: (beacon) => {
            const index = this.beacons.findIndex((b) => b.id === beacon.id);
            if (index !== -1) {
              this.beacons[index] = beacon;
            } else {
              this.beacons.unshift(beacon);
            }
            this.applyLocalFilters();
            this.addOrUpdateMarker(beacon);
            this.loadStats();
          },
        })
      );
    } catch (e) {
      console.warn('WebSocket connect skipped', e);
    }
  }

  // Section 2: Leaflet Map
  private initMap(): void {
    if (typeof window === 'undefined' || typeof document === 'undefined') return;
    const container = document.getElementById('leaflet-map');
    if (!container) return;

    if (this.map) {
      this.map.remove();
      this.map = null;
    }

    try {
      this.map = L.map('leaflet-map', {
        center: [20.5937, 78.9629],
        zoom: 5,
        zoomControl: true,
      });

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors | DisasterLink Command',
      }).addTo(this.map);

      this.updateMapMarkers();
    } catch (e) {
      console.warn('Leaflet init error handled:', e);
    }
  }

  private updateMapMarkers(): void {
    if (!this.map) return;

    this.markersMap.forEach((marker) => marker.remove());
    this.markersMap.clear();

    const bounds = L.latLngBounds([]);

    this.filteredBeacons.forEach((beacon) => {
      this.addOrUpdateMarker(beacon);
      if (beacon.latitude && beacon.longitude) {
        bounds.extend([beacon.latitude, beacon.longitude]);
      }
    });

    if (this.filteredBeacons.length > 0 && bounds.isValid()) {
      this.map.fitBounds(bounds, { padding: [40, 40], maxZoom: 10 });
    }
  }

  private addOrUpdateMarker(beacon: SosBeacon): void {
    if (!this.map || !beacon.latitude || !beacon.longitude) return;

    if (this.markersMap.has(beacon.id)) {
      this.markersMap.get(beacon.id)?.remove();
    }

    const iconHtml = `
      <div class="custom-map-pin ${this.urgencyClass[beacon.urgencyLabel] || 'urgency-low'}">
        <span class="pin-icon">${this.disasterEmoji[beacon.disasterType] || '🚨'}</span>
      </div>
    `;

    const customIcon = L.divIcon({
      html: iconHtml,
      className: 'map-pin-container',
      iconSize: [36, 36],
      iconAnchor: [18, 18],
    });

    const desc = beacon.description ?? '';
    const assignedVol = beacon.volunteerUsername || 'Unassigned';
    const popupContent = `
      <div class="p-2" style="min-width: 220px;">
        <div class="d-flex align-items-center justify-content-between gap-2 mb-1">
          <strong class="text-uppercase" style="font-size: 0.8rem; color: #60a5fa;">
            ${beacon.disasterType} #${beacon.id}
          </strong>
          <span class="badge-urgency ${this.urgencyClass[beacon.urgencyLabel]}">
            ${beacon.urgencyLabel} (${beacon.urgencyScore})
          </span>
        </div>
        <p class="mb-1" style="font-size: 0.85rem; color: #e2e8f0;">
          ${desc.substring(0, 100)}${desc.length > 100 ? '...' : ''}
        </p>
        <div class="text-muted" style="font-size: 0.75rem;">
          📍 ${beacon.address ?? `${beacon.latitude.toFixed(3)}, ${beacon.longitude.toFixed(3)}`}
        </div>
        <div class="mt-2 pt-1 border-top border-secondary-subtle d-flex justify-content-between align-items-center" style="font-size: 0.75rem;">
          <span class="text-muted">Volunteer: <strong class="text-info">${assignedVol}</strong></span>
          <span class="badge bg-secondary">${beacon.status}</span>
        </div>
      </div>
    `;

    const marker = L.marker([beacon.latitude, beacon.longitude], { icon: customIcon })
      .addTo(this.map)
      .bindPopup(popupContent);

    marker.on('click', () => {
      this.selectedBeacon = beacon;
    });

    this.markersMap.set(beacon.id, marker);
  }

  focusBeaconOnMap(beacon: SosBeacon): void {
    this.selectedBeacon = beacon;
    if (this.map && beacon.latitude && beacon.longitude) {
      this.map.flyTo([beacon.latitude, beacon.longitude], 12, { duration: 1.2 });
      const marker = this.markersMap.get(beacon.id);
      if (marker) {
        marker.openPopup();
      }
    }
  }
}