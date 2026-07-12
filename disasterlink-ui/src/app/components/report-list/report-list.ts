import { Component, OnInit } from '@angular/core';
import { DisasterReportService } from '../../services/disaster-report';
import { DisasterReport } from '../../models/disaster-report.model';

// Lists all disaster reports and allows updating status or deleting a report.
@Component({
  selector: 'app-report-list',
  standalone: false,
  templateUrl: './report-list.html',
  styleUrl: './report-list.css',
})
export class ReportList implements OnInit {
  reports: DisasterReport[] = [];
  errorMessage = '';
  isLoading = true;

  constructor(private reportService: DisasterReportService) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.isLoading = true;
    this.reportService.getAllReports().subscribe({
      next: (data) => {
        this.reports = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load disaster reports.';
        this.isLoading = false;
      },
    });
  }

  onStatusChange(report: DisasterReport, newStatus: string): void {
    this.reportService.updateStatus(report.id, newStatus).subscribe({
      next: (updated) => {
        report.status = updated.status;
      },
      error: () => {
        this.errorMessage = 'Unable to update status.';
      },
    });
  }

  onDelete(report: DisasterReport): void {
    if (!confirm(`Delete report "${report.title}"?`)) {
      return;
    }
    this.reportService.deleteReport(report.id).subscribe({
      next: () => {
        this.reports = this.reports.filter((r) => r.id !== report.id);
      },
      error: () => {
        this.errorMessage = 'Unable to delete report.';
      },
    });
  }

  statusBadgeClass(status: string): string {
    switch (status) {
      case 'PENDING':
        return 'bg-warning text-dark';
      case 'IN_PROGRESS':
        return 'bg-info text-dark';
      case 'RESOLVED':
        return 'bg-success';
      default:
        return 'bg-secondary';
    }
  }
}
