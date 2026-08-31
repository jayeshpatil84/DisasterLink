import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, SessionUser } from '../models/user.model';

const SESSION_KEY = 'disasterlink_session';

/**
 * Manages JWT-based authentication.
 * The full session (token + user info + role) is stored in localStorage
 * so the user stays logged in across page refreshes.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private isBrowserPlatform: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowserPlatform = isPlatformBrowser(this.platformId);
  }

  private isBrowser(): boolean {
    return this.isBrowserPlatform && typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, request).pipe(
      tap(res => this.saveSession(res))
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(res => this.saveSession(res))
    );
  }

  logout(): void {
    if (this.isBrowser()) {
      localStorage.removeItem(SESSION_KEY);
    }
  }

  saveSession(response: AuthResponse): void {
    const session: SessionUser = {
      token:    response.token,
      userId:   response.userId,
      username: response.username,
      email:    response.email,
      role:     response.role,
    };
    if (this.isBrowser()) {
      localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    }
  }

  getSession(): SessionUser | null {
    if (!this.isBrowser()) {
      return null;
    }
    try {
      const raw = localStorage.getItem(SESSION_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  getToken(): string | null {
    return this.getSession()?.token ?? null;
  }

  getUsername(): string | null {
    return this.getSession()?.username ?? null;
  }

  getRole(): string | null {
    return this.getSession()?.role ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isOfficer(): boolean {
    return this.getRole() === 'OFFICER';
  }

  isVolunteer(): boolean {
    return this.getRole() === 'VOLUNTEER';
  }

  isVictim(): boolean {
    return this.getRole() === 'VICTIM';
  }
}
