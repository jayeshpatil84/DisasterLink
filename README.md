# 🚨 DisasterLink

**Real-time Disaster Coordination Platform**

> *"SACHET tells you a flood is coming. OCHA coordinates governments. DisasterLink is where a stranded victim sends an SOS, a nearby volunteer gets auto-assigned, and a district officer sees everything on a live map — in real time."*

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTS                              │
│   Victim App    Volunteer App    District Officer Dashboard  │
│   (React)       (React)          (React + Leaflet.js)       │
└──────────┬───────────┬───────────────────┬──────────────────┘
           │ REST/HTTP │ WebSocket (STOMP)  │
           ▼           ▼                   ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Backend (Port 8080)                │
│                                                             │
│  ┌──────────────┐  ┌────────────────┐  ┌────────────────┐  │
│  │ SosController│  │VolunteerCtrl   │  │ AuthController │  │
│  └──────┬───────┘  └───────┬────────┘  └────────────────┘  │
│         │                  │                                 │
│  ┌──────▼───────┐  ┌───────▼────────┐                       │
│  │SosBeaconSvc  │  │AssignmentSvc   │                       │
│  └──────┬───────┘  └───────┬────────┘                       │
│         │                  │                                 │
│  ┌──────▼───────────────────▼───────┐                       │
│  │         AI Triage Service        │ ←── Gemini API        │
│  └──────────────────────────────────┘                       │
│                                                             │
│  ┌──────────────────────────────────┐                       │
│  │      WebSocket Broadcaster       │ ←── STOMP/SockJS      │
│  └──────────────────────────────────┘                       │
│                                                             │
│  ┌──────────────────────────────────┐                       │
│  │   Message Broker (Redis Pub/Sub) │ ← Kafka-compatible    │
│  └──────────────────────────────────┘                       │
└─────────────────────────────────────────────────────────────┘
           │                   │
    ┌──────▼──────┐    ┌───────▼──────┐
    │  MySQL DB   │    │  Redis Cache  │
    │  (JPA/Hib.) │    │  (Pub/Sub)    │
    └─────────────┘    └──────────────┘
```

---

## ✅ Core Features (MVP)

| # | Feature | Technology |
|---|---------|------------|
| 1 | **SOS Beacon** | REST API → WebSocket broadcast → Leaflet.js map pin |
| 2 | **Live Disaster Map** | React-Leaflet, color-coded by AI priority |
| 3 | **AI Triage** | Google Gemini API → CRITICAL/HIGH/MEDIUM/LOW + score |
| 4 | **Auto Volunteer Assignment** | Haversine distance + skill matching |
| 5 | **SMS Fallback** | Twilio inbound/outbound (works on 2G) |

---

## 🛠️ Tech Stack

### Backend
| Layer | Technology | Why |
|-------|-----------|-----|
| Framework | Spring Boot 3.2 (Java 17) | Enterprise-grade, Infosys-standard |
| Real-time | WebSocket + STOMP | Bidirectional push, sub-500ms latency |
| Message Broker | Redis Pub/Sub | Kafka-compatible interface, easier local setup |
| Auth | JWT + Spring Security + RBAC | Stateless, 4 roles: VICTIM/VOLUNTEER/OFFICER/ADMIN |
| AI | Google Gemini API (direct HTTP) | LangChain intentionally skipped (overhead) |
| Database | MySQL + JPA/Hibernate | Relational, audit trail for every SOS |
| SMS | Twilio | Works on 2G, critical for India context |

### Frontend
| Layer | Technology |
|-------|-----------|
| UI | React 18 + React Router |
| Map | Leaflet.js + React-Leaflet |
| State | Zustand (lightweight, no Redux boilerplate) |
| WebSocket | STOMP.js + SockJS |
| HTTP | Axios (with JWT interceptor) |

---

## ⚙️ Kafka Architecture Note

> **Interview talking point:** *"I designed the SOS ingestion layer to be Kafka-compatible — same `publish(topic, payload)` interface. Redis Pub/Sub is used in the prototype because it requires zero broker configuration locally. In production with thousands of simultaneous SOS signals (like during a major cyclone), you'd swap in Kafka with identical topic names and no business logic changes."*

Topics:
- `sos-incoming` — new beacon events
- `sos-triage-complete` — after AI scoring
- `volunteer-assigned` — assignment events
- `sms-inbound` — parsed SMS events

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6+
- Maven 3.9+

### 1. Database Setup

```sql
CREATE DATABASE disasterlink_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Backend Setup

```bash
cd backend

# Configure credentials
cp src/main/resources/application.properties src/main/resources/application-local.properties
# Edit: MySQL password, Gemini API key, Twilio credentials

# Run
mvn spring-boot:run
# Server starts at http://localhost:8080
```

### 3. Frontend Setup

```bash
cd frontend
npm install
npm start
# App runs at http://localhost:3000
```

### 4. Redis (required for message broker)

```bash
# macOS
brew install redis && redis-server

# Ubuntu
sudo apt install redis-server && sudo service redis start

# Docker
docker run -d -p 6379:6379 redis:alpine
```

---

## 🔑 API Reference

### Authentication
```
POST /api/auth/register   — create account
POST /api/auth/login      — get JWT token
```

### SOS Beacons
```
POST   /api/sos              — create SOS (any auth)
GET    /api/sos/active       — get active beacons (VOLUNTEER+)
GET    /api/sos              — get all beacons (OFFICER+)
PATCH  /api/sos/{id}/status  — update status (OFFICER+)
POST   /api/sos/sms-webhook  — Twilio inbound (public)
```

### Volunteers
```
GET   /api/volunteer           — all volunteers (OFFICER+)
GET   /api/volunteer/available — available volunteers (OFFICER+)
POST  /api/volunteer/register  — self-register (VOLUNTEER)
PATCH /api/volunteer/{id}/location — GPS update (VOLUNTEER)
PATCH /api/volunteer/{id}/release  — mark available (VOLUNTEER+)
```

### WebSocket Subscriptions
```
CONNECT: ws://localhost:8080/ws (SockJS)

/topic/sos         — SOS_CREATED, SOS_UPDATED events
/topic/assignments — VOLUNTEER_ASSIGNED events
/topic/volunteers  — LOCATION_UPDATE events
/topic/stats       — STATS_UPDATE events
```

---

## 🏗️ Project Structure

```
DisasterLink/
├── backend/
│   └── src/main/java/com/disasterlink/
│       ├── config/          # Security, WebSocket, Redis config
│       ├── controller/      # REST controllers (SOS, Auth, Volunteer)
│       ├── dto/             # Request/Response DTOs
│       ├── model/           # JPA entities (SosBeacon, Volunteer, User, Zone)
│       ├── repository/      # Spring Data JPA repos
│       ├── service/         # Business logic (Triage, Assignment, SMS)
│       └── websocket/       # WebSocketBroadcaster
└── frontend/
    └── src/
        ├── components/
        │   ├── Dashboard/   # Sidebar, StatsBar, WsStatusBadge
        │   ├── Map/         # LiveMap (Leaflet.js)
        │   ├── SOS/         # SosPanel (list view)
        │   └── Volunteer/   # AssignmentFeed
        ├── pages/           # LoginPage, DashboardPage, SosSubmitPage, VolunteerPage
        ├── services/        # api.js (Axios), websocket.js (STOMP)
        └── store/           # useDisasterStore.js (Zustand)
```

---

## 🎯 Design Decisions (For Interviews)

### 1. Why Redis Pub/Sub instead of Kafka?
Kafka requires ZooKeeper + broker cluster config — complex for a solo prototype. Redis Pub/Sub is a drop-in for low-volume use. **The interface is identical** — upgrading to Kafka needs zero business logic changes.

### 2. Why Gemini directly instead of LangChain?
LangChain adds 200+ MB dependency and abstraction layers that complicate debugging. Gemini's REST API is clean and the prompt engineering is simpler to explain in interviews.

### 3. Why Zustand over Redux?
Redux requires actions/reducers/selectors boilerplate. Zustand is 1KB and works identically. Easier to maintain, easier to demo.

### 4. Why Haversine for distance?
PostGIS or geospatial indexing would be overkill for a prototype. Haversine is mathematically correct (great-circle distance), fast in Java, and demonstrates algorithm knowledge in interviews.

### 5. Rule-based fallback triage
If Gemini API is down (disaster = network outage), a rule-based triage runs instead. **Shows resilience thinking** — interviewers love this.

---

## 📊 Gap Analysis vs Existing Solutions

| Feature | SACHET (India) | UN OCHA | **DisasterLink** |
|---------|---------------|---------|-----------------|
| Disaster alerts | ✅ | ❌ | ✅ (via WebSocket) |
| SOS by victim | ❌ | ❌ | ✅ |
| AI triage | ❌ | ❌ | ✅ Gemini |
| Volunteer assignment | ❌ | ❌ | ✅ Auto |
| SMS fallback | ❌ | ❌ | ✅ Twilio |
| Live map | ❌ | ✅ (OSOCC) | ✅ Leaflet |
| Ground-level access | ❌ | ❌ | ✅ |

---

## 👤 Roles (RBAC)

| Role | Permissions |
|------|-------------|
| `VICTIM` | Send SOS, view own SOS status |
| `VOLUNTEER` | View active SOS, update location, receive assignments |
| `DISTRICT_OFFICER` | Full dashboard, update SOS status, view all data |
| `ADMIN` | Everything + user management |

---

## 📝 License

MIT — Built for interview demonstration purposes.
