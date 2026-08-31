// The possible disaster types a user can report
export type DisasterType = 'FLOOD' | 'FIRE' | 'EARTHQUAKE' | 'CYCLONE' | 'LANDSLIDE' | 'OTHER';

// The status of a disaster report, updated by an admin/responder
export type ReportStatus = 'PENDING' | 'IN_PROGRESS' | 'RESOLVED';

// Full disaster report as returned by the backend
export interface DisasterReport {
  id: number;
  title: string;
  description: string;
  location: string;
  disasterType: DisasterType;
  status: ReportStatus;
  reportedByUsername: string;
  createdAt: string;
}

// Payload used to create or update a disaster report
export interface DisasterReportRequest {
  title: string;
  description: string;
  location: string;
  disasterType: DisasterType;
}

// Simple statistics shown on the dashboard
export interface DashboardStats {
  totalReports: number;
  pendingReports: number;
  inProgressReports: number;
  resolvedReports: number;
}
