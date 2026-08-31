import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  DashboardStats,
  SosBeacon,
  SosRequest,
  SosStatusHistory,
  Volunteer,
  VolunteerInfo,
} from '../models/sos-beacon.model';

/**
 * Handles all SOS beacon and role-specific API calls.
 */
@Injectable({ providedIn: 'root' })
export class SosService {
  private apiUrl = `${environment.apiUrl}/sos`;

  constructor(private http: HttpClient) {}

  // -------------------------------------------------------------------------
  // General & Shared SOS calls
  // -------------------------------------------------------------------------

  /** Officer/Volunteer: get all beacons sorted by urgency. */
  getAllBeacons(): Observable<SosBeacon[]> {
    return this.http.get<SosBeacon[]>(this.apiUrl);
  }

  /** Victim: get own beacons via /api/sos/my (or /api/victim/my-sos). */
  getMyBeacons(): Observable<SosBeacon[]> {
    return this.http.get<SosBeacon[]>(`${environment.apiUrl}/victim/my-sos`);
  }

  /** Volunteer: get beacons assigned to self. */
  getAssignedBeacons(): Observable<SosBeacon[]> {
    return this.http.get<SosBeacon[]>(`${environment.apiUrl}/volunteer/tasks`);
  }

  getBeaconById(id: number): Observable<SosBeacon> {
    return this.http.get<SosBeacon>(`${this.apiUrl}/${id}`);
  }

  getSosHistory(id: number): Observable<SosStatusHistory[]> {
    return this.http.get<SosStatusHistory[]>(`${this.apiUrl}/${id}/history`);
  }

  submitSos(request: SosRequest): Observable<SosBeacon> {
    return this.http.post<SosBeacon>(this.apiUrl, request);
  }

  assignVolunteer(beaconId: number, volunteerId: number): Observable<SosBeacon> {
    return this.http.patch<SosBeacon>(`${this.apiUrl}/${beaconId}/assign`, { volunteerId });
  }

  reassignVolunteer(beaconId: number, volunteerId: number): Observable<SosBeacon> {
    return this.http.patch<SosBeacon>(`${this.apiUrl}/${beaconId}/reassign`, { volunteerId });
  }

  updateStatus(beaconId: number, status: string): Observable<SosBeacon> {
    return this.http.patch<SosBeacon>(`${this.apiUrl}/${beaconId}/status`, { status });
  }

  deleteBeacon(beaconId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${beaconId}`);
  }

  // -------------------------------------------------------------------------
  // Officer APIs
  // -------------------------------------------------------------------------

  getOfficerStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${environment.apiUrl}/officer/stats`);
  }

  getOfficerVolunteers(): Observable<VolunteerInfo[]> {
    return this.http.get<VolunteerInfo[]>(`${environment.apiUrl}/officer/volunteers`);
  }

  getOfficerSos(urgencyLevel?: string, status?: string): Observable<SosBeacon[]> {
    let params = new HttpParams();
    if (urgencyLevel && urgencyLevel !== 'ALL') {
      params = params.set('urgencyLevel', urgencyLevel);
    }
    if (status && status !== 'ALL') {
      params = params.set('status', status);
    }
    return this.http.get<SosBeacon[]>(`${environment.apiUrl}/officer/sos`, { params });
  }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${environment.apiUrl}/officer/stats`);
  }

  getVolunteers(): Observable<Volunteer[]> {
    return this.http.get<Volunteer[]>(`${environment.apiUrl}/volunteers`);
  }

  // -------------------------------------------------------------------------
  // Volunteer APIs
  // -------------------------------------------------------------------------

  getVolunteerTasks(): Observable<SosBeacon[]> {
    return this.http.get<SosBeacon[]>(`${environment.apiUrl}/volunteer/tasks`);
  }

  // -------------------------------------------------------------------------
  // Victim APIs
  // -------------------------------------------------------------------------

  getVictimMySos(): Observable<SosBeacon[]> {
    return this.http.get<SosBeacon[]>(`${environment.apiUrl}/victim/my-sos`);
  }
}
