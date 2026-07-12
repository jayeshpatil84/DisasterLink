// Represents the currently logged-in user (decoded from the JWT / login response)
export interface User {
  id: number;
  username: string;
  email: string;
}

// Payload sent to the backend when registering a new user
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

// Payload sent to the backend when logging in
export interface LoginRequest {
  username: string;
  password: string;
}

// Response returned by the backend after a successful login
export interface AuthResponse {
  token: string;
  username: string;
  email: string;
  userId: number;
}
