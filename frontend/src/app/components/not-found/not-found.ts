import { Component } from '@angular/core';

// Simple 404 page shown for any unmatched route.
@Component({
  selector: 'app-not-found',
  standalone: false,
  templateUrl: './not-found.html',
  styleUrl: './not-found.css',
})
export class NotFound {}
