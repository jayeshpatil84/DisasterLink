// User authentication models

export type UserRole = 'VICTIM' | 'VOLUNTEER' | 'OFFICER';

export type VolunteerStatus = 'AVAILABLE' | 'BUSY' | 'OFFLINE';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
  email: string;
  role: UserRole;
}

/** Stored in localStorage after login. */
export interface SessionUser {
  token: string;
  userId: number;
  username: string;
  email: string;
  role: UserRole;
}
