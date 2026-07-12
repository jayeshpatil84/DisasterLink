import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DashboardStats, DisasterReport, DisasterReportRequest } from '../models/disaster-report.model';

// Handles all CRUD operations for disaster reports, plus fetching dashboard statistics.
@Injectable({
  providedIn: 'root',
})
export class DisasterReportService {
  private apiUrl = `${environment.apiUrl}/reports`;

  constructor(private http: HttpClient) {}

  getAllReports(): Observable<DisasterReport[]> {
    return this.http.get<DisasterReport[]>(this.apiUrl);
  }

  getReportById(id: number): Observable<DisasterReport> {
    return this.http.get<DisasterReport>(`${this.apiUrl}/${id}`);
  }

  createReport(request: DisasterReportRequest): Observable<DisasterReport> {
    return this.http.post<DisasterReport>(this.apiUrl, request);
  }

  updateReport(id: number, request: DisasterReportRequest): Observable<DisasterReport> {
    return this.http.put<DisasterReport>(`${this.apiUrl}/${id}`, request);
  }

  updateStatus(id: number, status: string): Observable<DisasterReport> {
    return this.http.patch<DisasterReport>(`${this.apiUrl}/${id}/status`, { status });
  }

  deleteReport(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${environment.apiUrl}/dashboard/stats`);
  }
}
