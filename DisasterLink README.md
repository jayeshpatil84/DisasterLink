# DisasterLink — Real-Time Disaster Response Platform

DisasterLink is a full-stack disaster response and coordination platform designed to help victims, volunteers, and officers manage emergency situations efficiently.

The platform allows users to submit **geo-tagged SOS requests**, view incidents on a **live map**, automatically classify the urgency of an SOS using **Google Gemini AI**, and receive real-time updates through **WebSocket communication** and push notifications.

---

## 🚀 Key Features

- 📍 **Geo-Tagged SOS** — Submit emergency requests with location and description.
- 🤖 **AI-Powered SOS Triage** — Gemini AI analyzes SOS requests and assigns an urgency score from 0–100.
- 🔄 **Rule-Based Fallback** — Automatically uses rule-based triage when Gemini AI is unavailable.
- 🗺️ **Live Disaster Map** — View SOS incidents using Leaflet and OpenStreetMap.
- ⚡ **Real-Time Updates** — STOMP over WebSocket provides live SOS updates without refreshing the page.
- 👥 **Volunteer Management** — Officers can assign volunteers to emergency requests.
- 🔐 **Role-Based Authentication** — Separate access for Victims, Volunteers, and Officers using JWT authentication.
- 📊 **Dashboard** — View disaster and volunteer statistics.
- 📱 **Push Notifications** — CRITICAL and HIGH priority alerts can be sent through ntfy.sh.

---

## 🏗️ Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | Angular 21, TypeScript, Leaflet.js, Bootstrap 5 |
| **Real-Time Communication** | STOMP, SockJS, WebSocket |
| **Backend** | Java 21, Spring Boot 3.4.3 |
| **Security** | Spring Security, JWT |
| **Database** | MySQL 8.0 |
| **ORM** | Spring Data JPA, Hibernate |
| **AI Integration** | Google Gemini 1.5 Flash |
| **Notifications** | ntfy.sh |

---

## 👥 User Roles

### Victim

Victims can:

- Create an account
- Submit an SOS request
- Provide emergency details and location
- View their submitted SOS requests
- Cancel their own pending requests

### Volunteer

Volunteers can:

- View available incidents
- View assigned SOS requests
- Update the status of assigned incidents
- Help coordinate emergency response activities

### Officer

Officers act as the command center and can:

- View all SOS incidents
- Assign volunteers to incidents
- Update incident status
- View volunteers
- Monitor overall disaster statistics

---

## 🔄 How the System Works

### 1. User Authentication

Users can register and log in using the application.

After successful authentication, the backend generates a JWT containing information such as:

- User ID
- Username
- Role

The JWT is then used to authenticate protected API requests.

---

### 2. SOS Submission

When a victim submits an SOS request:

1. The SOS request is stored in MySQL.
2. The request contains the user's location and emergency description.
3. Gemini AI analyzes the description and generates an urgency score.
4. The score is categorized as:
   - **CRITICAL**
   - **HIGH**
   - **MEDIUM**
   - **LOW**
5. If Gemini AI is unavailable, the application uses the rule-based triage system.
6. The new SOS is broadcast through WebSocket.
7. CRITICAL and HIGH priority requests can trigger an ntfy.sh notification.

---

### 3. Live Map

The dashboard uses **Leaflet.js** with **OpenStreetMap** to display SOS incidents.

When a new SOS is created or an existing incident is updated, the dashboard receives the change through WebSocket communication without requiring a page refresh.

---

### 4. Volunteer Assignment

Officers can view registered SOS incidents and assign available volunteers to them.

The assigned volunteer can then view the incident and update its status as the response progresses.

---

## ⚡ Real-Time Communication

DisasterLink uses **STOMP over WebSocket** for real-time communication.

### WebSocket Endpoint

```text
/ws
```

### SOS Feed

```text
/topic/sos-feed
```

This allows connected users to receive SOS updates in real time.

---

## 🧠 AI-Based SOS Triage

DisasterLink uses Google Gemini to analyze the emergency description submitted by a victim.

The system produces an urgency score between **0 and 100** and maps it to an appropriate priority level.

| Score Category | Priority |
|---|---|
| Highest urgency | CRITICAL |
| High urgency | HIGH |
| Moderate urgency | MEDIUM |
| Lower urgency | LOW |

A rule-based fallback system is also included so that SOS triage can continue when the Gemini API is unavailable.

---

## 🗄️ Database Setup

DisasterLink uses MySQL 8.0.

Create the database before starting the backend:

```sql
CREATE DATABASE disasterlink_db;
```

Make sure the database username and password configured in the backend match your local MySQL setup.

---

## ⚙️ Running the Backend

### Prerequisites

- JDK 21
- Maven
- MySQL 8.0

Navigate to the backend directory:

```bash
cd backend
```

Start the Spring Boot application:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

## 🌐 Running the Frontend

### Prerequisites

- Node.js 20+
- npm

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the Angular application:

```bash
npm start
```

The frontend runs on:

```text
http://localhost:4200
```

The frontend communicates with the Spring Boot backend running on port `8080`.

---

## 🔑 Environment Variables

The application supports the following environment variables:

| Variable | Description |
|---|---|
| `JWT_SECRET` | Secret key used for JWT authentication |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins |
| `GEMINI_API_KEY` | Google Gemini API key |
| `NTFY_TOPIC` | ntfy.sh topic used for emergency notifications |

### Example

```text
JWT_SECRET=your_secure_jwt_secret
GEMINI_API_KEY=your_gemini_api_key
NTFY_TOPIC=disasterlink-sos-alerts
```

> **Important:** Never commit your actual API keys, passwords, or JWT secrets to GitHub.

---

## 👮 Officer Account

Public registration is available for:

- VICTIM
- VOLUNTEER

An officer account can be created directly in the MySQL database.

Alternatively, you can register a user normally and update the user's role to `OFFICER` in the database.

Example:

```sql
UPDATE users
SET role = 'OFFICER'
WHERE username = 'your_username';
```

---

## 🔌 API Overview

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| POST | `/api/sos` | Authenticated Users |
| GET | `/api/sos` | Volunteer, Officer |
| GET | `/api/sos/my` | Victim |
| GET | `/api/sos/assigned` | Volunteer, Officer |
| PATCH | `/api/sos/{id}/assign` | Officer |
| PATCH | `/api/sos/{id}/status` | Authenticated Users |
| DELETE | `/api/sos/{id}` | Victim, Officer |
| GET | `/api/dashboard/stats` | Authenticated Users |
| GET | `/api/volunteers` | Officer |

---

## 📱 Push Notifications

DisasterLink can use **ntfy.sh** to send emergency notifications.

To use this feature:

1. Install the ntfy application.
2. Subscribe to the configured DisasterLink notification topic.
3. Submit a CRITICAL or HIGH priority SOS.
4. The system sends a notification to the configured topic.

---

## 📁 Project Structure

```text
DisasterLink/
│
├── backend/
│   └── Spring Boot REST API
│
├── frontend/
│   └── Angular Application
│
└── README.md
```

---

## 🎯 Project Objective

The main objective of DisasterLink is to demonstrate how modern web technologies can be combined to build a real-time emergency coordination system.

The project focuses on:

- Full-stack application development
- REST API development
- Real-time WebSocket communication
- JWT-based authentication
- Role-based authorization
- Database management
- AI API integration
- Location-based incident visualization
- Real-time emergency notifications

---

## 📌 Future Improvements

Possible future improvements include:

- Online deployment
- Advanced volunteer tracking
- Improved disaster analytics
- Additional notification channels
- Mobile application
- Enhanced AI-based emergency classification
- Automated volunteer recommendations

---

## 📄 License

This project was developed as a student project for learning and demonstration purposes.