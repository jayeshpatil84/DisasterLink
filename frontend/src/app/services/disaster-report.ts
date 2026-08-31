// @deprecated — Legacy service stub. Replaced by SosService in v2.0.
// Kept to avoid import errors in old component stubs that are being redirected.

import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class DisasterReportService {
  // All methods return empty observables — this service is no longer used.
  getAllReports(): Observable<any[]> { return of([]); }
  getReportById(id: number): Observable<any> { return of(null); }
  createReport(request: any): Observable<any> { return of(null); }
  updateReport(id: number, request: any): Observable<any> { return of(null); }
  updateStatus(id: number, status: string): Observable<any> { return of(null); }
  deleteReport(id: number): Observable<void> { return of(); }
}
