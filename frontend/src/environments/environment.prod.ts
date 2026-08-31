// Production environment configuration
//
// FIX H5: Added missing wsUrl property.
// The production environment was missing wsUrl, causing environment.wsUrl
// to be undefined in WebSocketService — WebSocket connections silently failed
// in the Docker deployment because the SockJS URL became "undefined/ws".
//
// In Docker Compose, the Nginx container proxies /ws/ to the backend, so
// we use a relative path that resolves to the current host + port.
// This works correctly whether deployed on localhost:4200, an IP, or a domain.
export const environment = {
  production: true,
  apiUrl: '/api',           // Nginx proxies /api/ → backend:8080/api/
  wsUrl: '/ws',             // Nginx proxies /ws/  → backend:8080/ws/ (WebSocket)
};