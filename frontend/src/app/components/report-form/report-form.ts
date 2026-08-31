import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

/**
 * @deprecated Replaced by SosForm in v2.0.
 * This stub immediately redirects to the new SOS submission form.
 */
@Component({
  selector: 'app-report-form',
  standalone: false,
  template: `<div class="container py-5 text-center text-muted">Redirecting...</div>`,
})
export class ReportForm implements OnInit {
  constructor(private router: Router) {}
  ngOnInit(): void {
    this.router.navigate(['/reports/new']);
  }
}
