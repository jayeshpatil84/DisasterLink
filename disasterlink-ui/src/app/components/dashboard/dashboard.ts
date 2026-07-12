import { Component, OnInit } from '@angular/core';
import { DisasterReportService } from '../../services/disaster-report';
import { DashboardStats } from '../../models/disaster-report.model';

// Dashboard page: shows simple counts of disaster reports by status.
@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  stats: DashboardStats = {
    totalReports: 0,
    pendingReports: 0,
    inProgressReports: 0,
    resolvedReports: 0,
  };
  errorMessage = '';
  isLoading = true;

  constructor(private reportService: DisasterReportService) {}

  ngOnInit(): void {
    this.reportService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load dashboard statistics.';
        this.isLoading = false;
      },
    });
  }
}
