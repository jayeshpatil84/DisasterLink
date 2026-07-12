import { Component } from '@angular/core';

// Root component. Only responsible for the navbar + router outlet shell.
@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  standalone: false,
  styleUrl: './app.css',
})
export class App {}
