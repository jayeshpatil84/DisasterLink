# DisasterLink API Documentation

Base URL: `http://localhost:8080/api`

All request/response bodies are JSON. Endpoints other than `/auth/**` require
the header:
```
Authorization: Bearer <token>
```
(the token is returned by the login/register endpoints).

---

## Auth

### Register
`POST /api/auth/register`

Request body:
```json
{
  "username": "john",
  "email": "john@example.com",
  "password": "secret123"
}
```

Response (201 Created):
```json
{
  "token": "eyJhbGciOi...",
  "userId": 1,
  "username": "john",
  "email": "john@example.com"
}
```

### Login
`POST /api/auth/login`

Request body:
```json
{
  "username": "john",
  "password": "secret123"
}
```

Response (200 OK): same shape as register.

---

## Disaster Reports
(All require `Authorization: Bearer <token>`)

### Get all reports
`GET /api/reports`

Response (200 OK):
```json
[
  {
    "id": 1,
    "title": "Flooding near Main Street",
    "description": "Water level rising fast",
    "location": "Main Street, Springfield",
    "disasterType": "FLOOD",
    "status": "PENDING",
    "reportedByUsername": "john",
    "createdAt": "2026-07-12T10:15:00"
  }
]
```

### Get a single report
`GET /api/reports/{id}`

### Create a report
`POST /api/reports`

Request body:
```json
{
  "title": "Flooding near Main Street",
  "description": "Water level rising fast",
  "location": "Main Street, Springfield",
  "disasterType": "FLOOD"
}
```
Response: 201 Created, returns the created report.

### Update a report
`PUT /api/reports/{id}`

Request body: same shape as create.

### Update only the status
`PATCH /api/reports/{id}/status`

Request body:
```json
{ "status": "IN_PROGRESS" }
```
Valid values: `PENDING`, `IN_PROGRESS`, `RESOLVED`.

### Delete a report
`DELETE /api/reports/{id}`

Response: 204 No Content.

---

## Dashboard

### Get statistics
`GET /api/dashboard/stats`

Response (200 OK):
```json
{
  "totalReports": 10,
  "pendingReports": 4,
  "inProgressReports": 3,
  "resolvedReports": 3
}
```

---

## Error Responses

All errors follow this shape:
```json
{
  "timestamp": "2026-07-12T10:20:00",
  "status": 404,
  "message": "Disaster report not found with id: 5",
  "fieldErrors": null
}
```

Validation errors additionally populate `fieldErrors`:
```json
{
  "timestamp": "2026-07-12T10:20:00",
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": {
    "title": "Title is required"
  }
}
```

| Status | Meaning                                |
|--------|-----------------------------------------|
| 400    | Validation error                        |
| 401    | Invalid username or password            |
| 404    | Resource not found                      |
| 409    | Duplicate username/email on register    |
| 500    | Unexpected server error                 |
