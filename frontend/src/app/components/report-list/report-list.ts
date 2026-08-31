import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

/**
 * @deprecated Replaced by SosList in v2.0.
 * This stub immediately redirects to the new incidents list.
 */
@Component({
  selector: 'app-report-list',
  standalone: false,
  template: `<div class="container py-5 text-center text-muted">Redirecting...</div>`,
})
export class ReportList implements OnInit {
  constructor(private router: Router) {}
  ngOnInit(): void {
    this.router.navigate(['/reports']);
  }
}
