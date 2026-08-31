import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { SosService } from '../../services/sos.service';
import { WebSocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth';
import {
  SosBeacon,
  SosStatusHistory,
  URGENCY_CLASS,
  URGENCY_EMOJI,
  DISASTER_EMOJI,
} from '../../models/sos-beacon.model';

@Component({
  selector: 'app-victim-my-sos',
  standalone: false,
  templateUrl: './victim-my-sos.html',
  styleUrl: './victim-my-sos.css',
})
export class VictimMySos implements OnInit, OnDestroy {
  myBeacons: SosBeacon[] = [];
  loading = false;
  error: string | null = null;
  notificationMessage: string | null = null;

  selectedBeaconTimeline: SosStatusHistory[] = [];
  selectedBeaconId: number | null = null;
  loadingTimeline = false;

  private wsSubscription?: Subscription;

  urgencyClass = URGENCY_CLASS;
  urgencyEmoji = URGENCY_EMOJI;
  disasterEmoji = DISASTER_EMOJI;

  constructor(
    private sosService: SosService,
    private wsService: WebSocketService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadMySos();
    this.setupWebSocket();
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
  }

  loadMySos(): void {
    this.loading = true;
    this.sosService.getVictimMySos().subscribe({
      next: (data) => {
        this.myBeacons = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load your SOS emergency requests';
        this.loading = false;
      },
    });
  }

  setupWebSocket(): void {
    const session = this.authService.getSession();
    if (!session || !session.userId) return;

    this.wsService.connect();
    this.wsSubscription = this.wsService
      .getVictimNotifications(session.userId)
      .subscribe({
        next: (notification) => {
          this.notificationMessage = notification.message;
          this.loadMySos();
          setTimeout(() => (this.notificationMessage = null), 6000);
        },
        error: (err) => console.warn('Victim WS error:', err),
      });
  }

  viewTimeline(beacon: SosBeacon): void {
    this.selectedBeaconId = beacon.id;
    this.loadingTimeline = true;
    this.sosService.getSosHistory(beacon.id).subscribe({
      next: (history) => {
        this.selectedBeaconTimeline = history ?? [];
        this.loadingTimeline = false;
      },
      error: () => {
        this.selectedBeaconTimeline = [];
        this.loadingTimeline = false;
      },
    });
  }

  closeTimeline(): void {
    this.selectedBeaconId = null;
    this.selectedBeaconTimeline = [];
  }
}
