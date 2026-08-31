import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { SosService } from '../../services/sos.service';
import { WebSocketService } from '../../services/websocket.service';
import { AuthService } from '../../services/auth';
import {
  SosBeacon,
  URGENCY_CLASS,
  URGENCY_EMOJI,
  DISASTER_EMOJI,
} from '../../models/sos-beacon.model';

@Component({
  selector: 'app-volunteer-dashboard',
  standalone: false,
  templateUrl: './volunteer-dashboard.html',
  styleUrl: './volunteer-dashboard.css',
})
export class VolunteerDashboard implements OnInit, OnDestroy {
  tasks: SosBeacon[] = [];
  volunteerStatus: string = 'AVAILABLE';
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

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
    this.loadTasks();
    this.setupWebSocket();
  }

  ngOnDestroy(): void {
    if (this.wsSubscription) {
      this.wsSubscription.unsubscribe();
    }
  }

  loadTasks(): void {
    this.loading = true;
    this.sosService.getVolunteerTasks().subscribe({
      next: (data) => {
        this.tasks = data ?? [];
        // Update volunteer status based on active tasks
        const hasActiveTask = this.tasks.some(
          (t) => t.status !== 'RESOLVED' && t.status !== 'CANCELLED'
        );
        this.volunteerStatus = hasActiveTask ? 'BUSY' : 'AVAILABLE';
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load assigned tasks';
        this.loading = false;
      },
    });
  }

  setupWebSocket(): void {
    const session = this.authService.getSession();
    if (!session || !session.userId) return;

    this.wsService.connect();
    this.wsSubscription = this.wsService
      .getVolunteerNotifications(session.userId)
      .subscribe({
        next: (notification) => {
          this.successMessage = notification.message;
          this.loadTasks();
          setTimeout(() => (this.successMessage = null), 6000);
        },
        error: (err) => console.warn('Volunteer WS error:', err),
      });
  }

  updateTaskStatus(task: SosBeacon, newStatus: string): void {
    this.error = null;
    this.successMessage = null;

    this.sosService.updateStatus(task.id, newStatus).subscribe({
      next: (updated) => {
        this.successMessage = `Task #${task.id} updated to ${newStatus}`;
        this.loadTasks();
        setTimeout(() => (this.successMessage = null), 4000);
      },
      error: (err) => {
        this.error = err.error?.message || `Failed to update status to ${newStatus}`;
        setTimeout(() => (this.error = null), 5000);
      },
    });
  }

  getNextAction(status: string): { label: string; nextStatus: string; btnClass: string } | null {
    switch (status) {
      case 'ASSIGNED':
        return { label: 'Accept & En Route', nextStatus: 'EN_ROUTE', btnClass: 'btn-info' };
      case 'EN_ROUTE':
        return { label: 'Mark Arrived', nextStatus: 'ARRIVED', btnClass: 'btn-primary' };
      case 'ARRIVED':
        return { label: 'Start Response', nextStatus: 'IN_PROGRESS', btnClass: 'btn-warning' };
      case 'IN_PROGRESS':
        return { label: 'Mark Completed', nextStatus: 'RESOLVED', btnClass: 'btn-success' };
      default:
        return null;
    }
  }
}
