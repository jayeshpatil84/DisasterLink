import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, of } from 'rxjs';
import { SosService } from '../../services/sos.service';
import { DisasterType, SosRequest } from '../../models/sos-beacon.model';

// FIX L4: Replaced native fetch() with HttpClient for reverse geocoding.
// Using raw fetch() in an Angular component:
//  1. Bypasses the AuthInterceptor (fine here since Nominatim needs no auth,
//     but establishes a bad pattern for future API calls in this component)
//  2. Runs outside Angular's zone — change detection won't fire automatically
//     after the promise resolves, potentially leaving the address field visually
//     blank until the next CD cycle
//  3. Cannot be easily unit-tested since Angular's HttpClientTestingModule doesn't
//     intercept fetch() calls
// HttpClient + catchError is idiomatic Angular and solves all three issues.

interface NominatimResponse {
  display_name?: string;
}

@Component({
  selector: 'app-sos-form',
  standalone: false,
  templateUrl: './sos-form.html',
  styleUrl: './sos-form.css',
})
export class SosForm {
  disasterTypes: DisasterType[] = [
    'FLOOD',
    'FIRE',
    'EARTHQUAKE',
    'CYCLONE',
    'LANDSLIDE',
    'TSUNAMI',
    'ACCIDENT',
    'MEDICAL',
    'OTHER',
  ];

  formData: SosRequest = {
    disasterType: 'FLOOD',
    description: '',
    latitude: 19.076,
    longitude: 72.8777,
    address: '',
  };

  loading = false;
  geoLocating = false;
  error: string | null = null;
  successMessage: string | null = null;

  // FIX L4: HttpClient injected (was missing before — fetch() used directly)
  constructor(
    private sosService: SosService,
    private router: Router,
    private http: HttpClient
  ) {}

  /** Uses the browser's Geolocation API to get high-accuracy GPS coordinates. */
  getCurrentLocation(): void {
    if (typeof window === 'undefined' || typeof navigator === 'undefined' || !navigator.geolocation) {
      this.error = 'Geolocation is not supported by your browser.';
      return;
    }

    this.geoLocating = true;
    this.error = null;

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.formData.latitude = parseFloat(position.coords.latitude.toFixed(6));
        this.formData.longitude = parseFloat(position.coords.longitude.toFixed(6));
        this.geoLocating = false;
        this.reverseGeocode(this.formData.latitude, this.formData.longitude);
      },
      () => {
        this.error = 'Could not fetch GPS location. Please enter coordinates manually.';
        this.geoLocating = false;
      },
      { enableHighAccuracy: true, timeout: 8000 }
    );
  }

  /**
   * Free reverse geocoding via Nominatim OpenStreetMap API.
   * FIX L4: Uses HttpClient instead of fetch() — stays in Angular zone,
   * supports testing, and is consistent with the rest of the app.
   */
  private reverseGeocode(lat: number, lng: number): void {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`;
    this.http
      .get<NominatimResponse>(url)
      .pipe(catchError(() => of(null))) // Non-critical — silently ignore errors
      .subscribe((data) => {
        if (data?.display_name) {
          this.formData.address = data.display_name;
        }
      });
  }

  onSubmit(): void {
    if (!this.formData.description || this.formData.description.length < 10) {
      this.error = 'Please provide a detailed description (at least 10 characters).';
      return;
    }

    this.loading = true;
    this.error = null;

    this.sosService.submitSos(this.formData).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = `SOS Beacon #${response.id} submitted! Gemini AI Urgency: ${response.urgencyLabel} (${response.urgencyScore}/100)`;
        setTimeout(() => {
          this.router.navigate(['/dashboard']);
        }, 2000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to submit SOS beacon. Please try again.';
      },
    });
  }
}