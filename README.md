# DisasterLink — College Mini Project

A simple disaster reporting and tracking system built as a college mini project.
Users can register, log in, report disasters, view all reports, update a
report's status, delete reports, and see basic statistics on a dashboard.

## Tech Stack

**Backend**
- Java 21
- Spring Boot 3.3.4
- Spring Web, Spring Data JPA (Hibernate), Spring Security, Bean Validation
- MySQL
- Maven
- JWT authentication (jjwt)

**Frontend**
- Angular 21 (NgModules, not standalone components)
- Angular Router
- Reactive Forms
- Bootstrap 5

## Project Structure

```
DisasterLink/
├── disasterlink-backend/     Spring Boot backend
│   ├── src/main/java/com/disasterlink/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── config/
│   │   ├── security/
│   │   └── exception/
│   ├── src/main/resources/application.properties
│   ├── sql/schema.sql
│   ├── API_DOCUMENTATION.md
│   └── pom.xml
│
└── disasterlink-ui/           Angular frontend
    └── src/app/
        ├── components/
        ├── services/
        ├── models/
        ├── guards/
        └── interceptors/
```

## Features

1. User Registration
2. Login (JWT-based)
3. Report a Disaster
4. View All Disaster Reports
5. Update Disaster Report Status
6. Delete a Report
7. Dashboard with simple statistics (total / pending / in progress / resolved)

## Prerequisites

- JDK 21
- Maven (or use the included setup instructions)
- Node.js 20+ and npm
- MySQL Server running locally

## 1. Database Setup

Create the database (Hibernate will create the tables automatically, but you
need the database itself to exist first):

```sql
CREATE DATABASE disasterlink_db;
```

The `sql/schema.sql` file inside `disasterlink-backend` shows the exact table
structure for reference — you don't need to run it manually since
`spring.jpa.hibernate.ddl-auto=update` creates/updates the tables for you.

Update your MySQL username/password in:
```
disasterlink-backend/src/main/resources/application.properties
```
```properties
spring.datasource.username=root
spring.datasource.password=root
```

## 2. Running the Backend

```bash
cd disasterlink-backend
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**.

To build a jar instead:
```bash
mvn clean package
java -jar target/disasterlink-backend-1.0.0.jar
```

## 3. Running the Frontend

```bash
cd disasterlink-ui
npm install
npm start
```

The frontend starts on **http://localhost:4200** and talks to the backend
at `http://localhost:8080/api` (configured in `src/environments/environment.ts`).

## 4. Using the App

1. Open http://localhost:4200
2. Register a new account
3. Log in
4. Go to "Report Disaster" to submit a report
5. View all reports under "Reports", update their status, or delete them
6. Check the "Dashboard" for a quick summary of report counts

## API Documentation

See `disasterlink-backend/API_DOCUMENTATION.md` for the full list of REST
endpoints, request/response bodies, and error formats.

## Notes

- This project intentionally avoids Docker, Kubernetes, microservices,
  message queues, and other production infrastructure — it is meant to be
  simple and easy to understand for a college mini project.
- JWT secret and DB credentials in `application.properties` are for local
  development only; in a real deployment these would come from environment
  variables.
