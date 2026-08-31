import { Component, OnInit } from '@angular/core';
import { SosService } from '../../services/sos.service';
import { AuthService } from '../../services/auth';
import {
  SosBeacon,
  URGENCY_CLASS,
  URGENCY_EMOJI,
  DISASTER_EMOJI,
  Volunteer,
} from '../../models/sos-beacon.model';

@Component({
  selector: 'app-sos-list',
  standalone: false,
  templateUrl: './sos-list.html',
  styleUrl: './sos-list.css',
})
export class SosList implements OnInit {
  beacons: SosBeacon[] = [];
  filteredBeacons: SosBeacon[] = [];
  volunteers: Volunteer[] = [];
  loading = false;
  error: string | null = null;
  activeFilter: string = 'ALL';

  selectedVolunteerMap: { [beaconId: number]: number } = {};

  urgencyClass = URGENCY_CLASS;
  urgencyEmoji = URGENCY_EMOJI;
  disasterEmoji = DISASTER_EMOJI;

  constructor(
    private sosService: SosService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadBeacons();
    if (this.authService.isOfficer()) {
      this.loadVolunteers();
    }
  }

  loadBeacons(): void {
    this.loading = true;
    const request = this.authService.isVictim()
      ? this.sosService.getMyBeacons()
      : this.sosService.getAllBeacons();

    request.subscribe({
      next: (data) => {
        this.beacons = data ?? [];
        this.applyFilter(this.activeFilter);
        this.loading = false;
      },
      error: () => {
        this.beacons = [];
        this.applyFilter(this.activeFilter);
        this.loading = false;
        this.error = 'Could not load SOS beacons. Please try again.';
      },
    });
  }

  loadVolunteers(): void {
    this.sosService.getVolunteers().subscribe({
      next: (data) => {
        if (data && data.length > 0) this.volunteers = data;
      },
      error: (err) => console.error('Failed to load volunteers', err),
    });
  }

  applyFilter(filter: string): void {
    this.activeFilter = filter;
    const username = this.authService.getUsername();

    if (filter === 'ALL') {
      this.filteredBeacons = [...this.beacons];
    } else if (filter === 'PENDING') {
      this.filteredBeacons = this.beacons.filter((b) => b.status === 'PENDING');
    } else if (filter === 'IN_PROGRESS') {
      this.filteredBeacons = this.beacons.filter((b) => b.status === 'IN_PROGRESS');
    } else if (filter === 'RESOLVED') {
      this.filteredBeacons = this.beacons.filter((b) => b.status === 'RESOLVED');
    } else if (filter === 'MY_SOS') {
      this.filteredBeacons = this.beacons.filter((b) => b.reporterUsername === username);
    } else if (filter === 'ASSIGNED_TO_ME') {
      this.filteredBeacons = this.beacons.filter((b) => b.volunteerUsername === username);
    }
  }

  assignVolunteer(beaconId: number): void {
    const volunteerId = this.selectedVolunteerMap[beaconId];
    if (!volunteerId) return;

    this.sosService.assignVolunteer(beaconId, volunteerId).subscribe({
      next: (updated) => {
        const index = this.beacons.findIndex((b) => b.id === beaconId);
        if (index !== -1) {
          this.beacons[index] = updated;
          this.applyFilter(this.activeFilter);
        }
      },
      error: (err) => alert(err.error?.message || 'Failed to assign volunteer'),
    });
  }

  updateStatus(beaconId: number, status: string): void {
    this.sosService.updateStatus(beaconId, status).subscribe({
      next: (updated) => {
        const index = this.beacons.findIndex((b) => b.id === beaconId);
        if (index !== -1) {
          this.beacons[index] = updated;
          this.applyFilter(this.activeFilter);
        }
      },
      error: (err) => alert(err.error?.message || 'Failed to update status'),
    });
  }

  cancelSos(beaconId: number): void {
    if (!confirm('Are you sure you want to cancel this SOS beacon?')) return;

    this.sosService.deleteBeacon(beaconId).subscribe({
      next: () => {
        this.beacons = this.beacons.filter((b) => b.id !== beaconId);
        this.applyFilter(this.activeFilter);
      },
      error: (err) => alert(err.error?.message || 'Failed to cancel SOS'),
    });
  }
}