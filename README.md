# DisasterLink v2.0 — Real-Time Disaster Response Platform

Full-stack emergency coordination platform: geo-tagged SOS beacons, a live Leaflet command map, Gemini AI urgency triage (with a rule-based fallback), STOMP WebSockets, and ntfy.sh push alerts.

---

## Architecture

| Layer | Stack |
|---|---|
| **Frontend** (`frontend/`) | Angular 21, TypeScript, Leaflet.js, Bootstrap 5, STOMP over SockJS |
| **Backend** (`backend/`) | Java 21, Spring Boot 3.4.3, Spring Security (stateless JWT), JPA/Hibernate, WebSocket |
| **Database** | MySQL 8.0 |
| **Integrations** | Google Gemini 1.5 Flash (optional), ntfy.sh push notifications |

### Roles

- **VICTIM** — submit geo-tagged SOS beacons, view/cancel own pending reports
- **VOLUNTEER** — view incidents, update status on assigned beacons
- **OFFICER** — command center: assign volunteers, update any incident, view all beacons

---

## Quick start (Docker Compose)

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)

### 1. Environment

```bash
cp .env.example .env
```

Edit `.env` if you want a custom JWT secret, Gemini key, or ntfy topic.

### 2. Start

From the **DisasterLink** root (this folder):

```bash
docker compose up --build
```

### 3. Open the app

- Web UI: [http://localhost:4200](http://localhost:4200)
- REST API: [http://localhost:8080/api](http://localhost:8080/api)
- Health: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

Register as a **Victim** or **Volunteer**, then submit an SOS from **Submit SOS**.

---

## Local development (without Docker)

### 1. MySQL

Create a database (password must match `DB_PASSWORD` / `.env`):

```sql
CREATE DATABASE disasterlink_db;
```

### 2. Backend

Requires **JDK 21** and **Maven**.

```bash
cd backend
mvn spring-boot:run
```

Runs at `http://localhost:8080`.

Optional env vars (PowerShell):

```powershell
$env:JWT_SECRET="cosmosjayeshspaceastroidcometput233241"
$env:GEMINI_API_KEY=""
mvn spring-boot:run
```

### 3. Frontend

Requires **Node.js 20+**.

```bash
cd frontend
npm install
npm start
```

Runs at `http://localhost:4200` and talks to `http://localhost:8080`.

---

## Environment variables

Copy `.env.example` → `.env`. Docker Compose reads these automatically.

| Variable | Default | Description |
|---|---|---|
| `MYSQL_ROOT_PASSWORD` | `123456` | MySQL root password |
| `JWT_SECRET` | (see `.env.example`) | HS256 signing key, **minimum 32 characters** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Comma-separated browser origins |
| `GEMINI_API_KEY` | empty | Optional; empty = rule-based triage |
| `NTFY_TOPIC` | `disasterlink-sos-alerts` | ntfy.sh topic for CRITICAL/HIGH alerts |

---

## Officer / admin account

Self-registration allows **VICTIM** and **VOLUNTEER** only. Create an officer in MySQL after first startup:

```sql
USE disasterlink_db;

-- username: officer1   password: officer123
INSERT INTO users (username, email, password, role, created_at)
VALUES (
  'officer1',
  'officer@disasterlink.com',
  '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym502LNnnX508w021jB256',
  'OFFICER',
  NOW()
);
```

If that hash does not match your BCrypt version, register a volunteer, then:

```sql
UPDATE users SET role = 'OFFICER' WHERE username = 'your_username';
```

---

## How it works

### Auth

Login/register returns a JWT with `userId`, `username`, and `role`. The Angular interceptor sends `Authorization: Bearer <token>` only to `/api` — never to third-party APIs such as Nominatim.

### SOS + triage

`POST /api/sos`:

1. Persist a `PENDING` beacon with GPS + description
2. **Gemini** scores 0–100 (`CRITICAL` / `HIGH` / `MEDIUM` / `LOW`), or the **rule-based engine** if the key is missing or the call fails
3. Broadcast `SosResponse` on STOMP topic `/topic/sos-feed`
4. Async ntfy push for **CRITICAL** and **HIGH**

### Live map

The dashboard uses Leaflet + OpenStreetMap. Color-coded pins update from the WebSocket feed without a page refresh.

---

## API (overview)

| Method | Path | Who |
|---|---|---|
| POST | `/api/auth/register` | Public (VICTIM / VOLUNTEER) |
| POST | `/api/auth/login` | Public |
| POST | `/api/sos` | Authenticated |
| GET | `/api/sos` | VOLUNTEER, OFFICER |
| GET | `/api/sos/my` | VICTIM |
| GET | `/api/sos/assigned` | VOLUNTEER, OFFICER |
| PATCH | `/api/sos/{id}/assign` | OFFICER |
| PATCH | `/api/sos/{id}/status` | Authenticated (volunteers: assigned only) |
| DELETE | `/api/sos/{id}` | VICTIM (own PENDING) / OFFICER |
| GET | `/api/dashboard/stats` | Authenticated |
| GET | `/api/volunteers` | OFFICER |

WebSocket: connect to `/ws` (SockJS), subscribe to `/topic/sos-feed`.

---

## Mobile push (ntfy.sh)

1. Install the free **ntfy** app
2. Subscribe to your topic (default: `disasterlink-sos-alerts`)
3. CRITICAL / HIGH SOS submissions send a push to that topic

---

## Project layout

```
DisasterLink/
├── docker-compose.yml
├── .env.example
├── backend/          Spring Boot API
└── frontend/         Angular UI
```

---

## License

Student project — use and extend as you need.
