import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DisasterReportService } from '../../services/disaster-report';

// Used for both creating a new disaster report and editing an existing one.
// If the route contains an :id, we load that report and switch to edit mode.
@Component({
  selector: 'app-report-form',
  standalone: false,
  templateUrl: './report-form.html',
  styleUrl: './report-form.css',
})
export class ReportForm implements OnInit {
  reportForm: FormGroup;
  isEditMode = false;
  reportId: number | null = null;
  errorMessage = '';
  isSubmitting = false;

  disasterTypes = ['FLOOD', 'FIRE', 'EARTHQUAKE', 'CYCLONE', 'LANDSLIDE', 'OTHER'];

  constructor(
    private fb: FormBuilder,
    private reportService: DisasterReportService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.reportForm = this.fb.group({
      title: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.maxLength(1000)]],
      location: ['', [Validators.required]],
      disasterType: ['FLOOD', [Validators.required]],
    });
  }

  get f() {
    return this.reportForm.controls;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.reportId = Number(idParam);
      this.reportService.getReportById(this.reportId).subscribe({
        next: (report) => {
          this.reportForm.patchValue({
            title: report.title,
            description: report.description,
            location: report.location,
            disasterType: report.disasterType,
          });
        },
        error: () => {
          this.errorMessage = 'Unable to load the report.';
        },
      });
    }
  }

  onSubmit(): void {
    this.errorMessage = '';

    if (this.reportForm.invalid) {
      this.reportForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    const request = this.reportForm.value;

    const action = this.isEditMode
      ? this.reportService.updateReport(this.reportId!, request)
      : this.reportService.createReport(request);

    action.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.router.navigate(['/reports']);
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = err.error?.message || 'Unable to save the report.';
      },
    });
  }
}
