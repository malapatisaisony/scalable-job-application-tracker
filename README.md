# Scalable Job Application Tracker

[![Backend CI](https://github.com/malapatisaisony/scalable-job-application-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/malapatisaisony/scalable-job-application-tracker/actions/workflows/ci.yml)

A production-style full-stack job application tracking system built with **React, Vite, Java 21, Spring Boot, PostgreSQL, Redis, JWT authentication, Docker, GitHub Actions, Prometheus, and k6**.

The application provides a responsive React frontend for registering users, signing in, and managing job applications, backed by a secure Spring Boot REST API.

The project emphasizes backend engineering concepts including **authentication and authorization, refresh-token management, Redis caching, rate limiting, pagination, database indexing, observability, automated testing, load testing, and containerized infrastructure**, while providing a complete frontend for demonstrating the system end to end.

---

## Features

### Frontend Experience

- Responsive React user interface
- User registration and login
- Persistent authenticated sessions
- Automatic JWT access-token refresh
- Secure logout with refresh-token revocation
- Job application dashboard
- Create job applications
- Edit existing applications
- Delete applications
- Search by company or job title
- Filter applications by status
- Paginated results
- User-specific application isolation
- Loading states
- Validation feedback
- Empty-state handling
- API error handling

### Authentication and Security

- User registration and login
- BCrypt password hashing
- JWT access tokens
- Refresh-token support
- Refresh-token persistence
- Refresh-token revocation during logout
- Automatic access-token renewal from the frontend
- Stateless Spring Security
- User-specific resource authorization
- Input validation
- Centralized exception handling
- Environment-based secret configuration
- CORS configuration for the frontend

### Job Application Management

Authenticated users can:

- Create job applications
- View applications
- View individual applications
- Update applications
- Delete applications
- Search applications
- Filter applications by status
- Navigate paginated results
- Track the application lifecycle

Supported statuses:

```text
SAVED
APPLIED
ONLINE_ASSESSMENT
PHONE_SCREEN
INTERVIEW
OFFER
REJECTED
WITHDRAWN
```

### Performance

- Redis caching for frequently accessed applications
- 10-minute cache TTL
- Cache eviction after updates and deletes
- Redis-backed per-user rate limiting
- Maximum page-size validation
- PostgreSQL indexes for common access patterns
- Composite indexes for user/status queries
- Query-plan validation with PostgreSQL `EXPLAIN ANALYZE`

### Observability

- Spring Boot Actuator
- Application health endpoint
- Prometheus metrics
- JVM metrics
- Process metrics
- Database connection-pool metrics
- HTTP request metrics
- Request correlation IDs
- Request duration logging
- HTTP status logging

### Developer Experience

- Dockerized backend
- PostgreSQL and Redis through Docker Compose
- Flyway database migrations
- Swagger/OpenAPI documentation
- Maven build
- React production build using Vite
- Automated backend tests
- GitHub Actions continuous integration
- k6 load-testing script

---

## Tech Stack

| Technology | Purpose |
|---|---|
| React | Frontend user interface |
| Vite | Frontend development and production build |
| JavaScript | Frontend application logic |
| Java 21 | Backend programming language |
| Spring Boot 4.1.1 | Backend framework |
| Spring MVC | REST API implementation |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database access |
| PostgreSQL 17 | Primary relational database |
| Redis 7 | Caching and rate limiting |
| Flyway | Database migrations |
| JWT | Stateless authentication |
| BCrypt | Password hashing |
| Swagger / OpenAPI | API documentation |
| Spring Boot Actuator | Health and operational endpoints |
| Micrometer + Prometheus | Application metrics |
| Docker | Backend containerization |
| Docker Compose | Local service orchestration |
| Maven | Backend build and dependency management |
| JUnit / Mockito | Automated backend testing |
| k6 | Load testing |
| GitHub Actions | Continuous Integration |

---

## Architecture

```text
                         User
                           |
                           v
                  +-----------------+
                  |  React Frontend |
                  |   Vite :5173    |
                  +--------+--------+
                           |
                           | HTTP / JSON
                           | JWT Bearer Token
                           v
                  +------------------+
                  | Spring Boot API  |
                  |     :8080        |
                  +--------+---------+
                           |
            +--------------+--------------+
            |                             |
            v                             v
     Spring Security                Service Layer
      JWT Validation                     |
                                          |
                              +-----------+-----------+
                              |                       |
                              v                       v
                       PostgreSQL :5432          Redis :6379
                              |                       |
                              |                 Cache +
                              |                Rate Limiting
                              v
                       Flyway Migrations
```

Backend request flow:

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
PostgreSQL
```

Redis operates alongside the persistence layer for caching and rate limiting.

---

## Frontend

The React frontend is located in:

```text
frontend/
```

Main frontend functionality includes:

```text
Registration
Login
Dashboard
Create Application
Edit Application
Delete Application
Search
Status Filtering
Pagination
Logout
Automatic Access-Token Refresh
Session Expiration Handling
```

The frontend communicates with the Spring Boot REST API running on:

```text
http://localhost:8080
```

During development, Vite runs the frontend on:

```text
http://localhost:5173
```

---

## Authentication Flow

### Login

```text
User
 |
 v
React Login Form
 |
 v
POST /api/users/login
 |
 v
Spring Security / UserService
 |
 +--> Validate BCrypt password
 |
 +--> Generate JWT access token
 |
 +--> Generate refresh token
 |
 v
React stores authentication tokens
```

### Protected Request

```text
React
 |
 | Authorization: Bearer <access-token>
 v
Spring Boot API
 |
 v
JWT Validation
 |
 v
Authenticated Request
```

### Automatic Token Refresh

When an access token expires:

```text
Protected API Request
 |
 v
HTTP 401
 |
 v
Frontend sends refresh token
 |
 v
POST /api/users/refresh
 |
 v
New JWT access token
 |
 v
Original request retried
```

If the refresh token is invalid, revoked, or expired, the user is returned to the login screen.

### Logout

```text
React
 |
 v
POST /api/users/logout
 |
 v
Refresh token revoked
 |
 v
Tokens removed from browser
 |
 v
Login screen
```

---

## User Data Isolation

Every job application is associated with its owning user.

An authenticated user can only access applications belonging to that user.

This prevents one user from:

```text
viewing
editing
deleting
```

another user's applications.

The behavior was also verified through the frontend using separate test accounts.

---

## Database Schema

Database schema changes are managed with Flyway migrations.

### Users

Stores:

- User ID
- Email
- BCrypt password hash
- Role
- Creation timestamp
- Update timestamp

### Refresh Tokens

Stores:

- User reference
- SHA-256 refresh-token hash
- Expiration timestamp
- Revoked status
- Creation timestamp

Raw refresh tokens are not stored directly in PostgreSQL.

### Job Applications

Stores:

- User ID
- Company name
- Job title
- Job URL
- Location
- Application status
- Applied timestamp
- Creation timestamp
- Update timestamp

Foreign keys enforce relationships between users and their applications.

---

## Database Indexing

Indexes are included for frequently used access patterns.

Examples:

```sql
CREATE INDEX idx_job_applications_user_id
ON job_applications(user_id);

CREATE INDEX idx_job_applications_status
ON job_applications(status);

CREATE INDEX idx_job_applications_user_created_at
ON job_applications(user_id, created_at DESC);

CREATE INDEX idx_job_applications_user_status_created_at
ON job_applications(user_id, status, created_at DESC);
```

Index usage was validated using:

```sql
EXPLAIN (ANALYZE, BUFFERS)
```

During local query-plan testing, approximately **50,000 temporary benchmark rows** were inserted inside a transaction.

Example query:

```sql
SELECT *
FROM job_applications
WHERE user_id = 1
ORDER BY created_at DESC
LIMIT 10;
```

PostgreSQL used:

```text
idx_job_applications_user_created_at
```

Observed local execution time:

```text
approximately 0.668 ms
```

The benchmark transaction was rolled back afterward.

> This was a local query-plan experiment and should not be interpreted as a production performance guarantee.

---

## Redis Caching

Individual job application reads are cached in Redis.

```text
GET Request
    |
    v
Check Redis
    |
    +---- Cache Hit ----> Return cached response
    |
    +---- Cache Miss
              |
              v
          PostgreSQL
              |
              v
         Store in Redis
              |
              v
           Response
```

Current cache TTL:

```text
10 minutes
```

Update and delete operations evict stale cache entries.

---

## Rate Limiting

Job application endpoints use Redis-backed per-user rate limiting.

Current limit:

```text
100 requests / minute / authenticated user
```

Requests exceeding the configured limit receive:

```text
HTTP 429 Too Many Requests
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Authenticate and receive tokens |
| GET | `/api/users/me` | Get authenticated user |
| POST | `/api/users/refresh` | Refresh an access token |
| POST | `/api/users/logout` | Revoke a refresh token |

### Job Applications

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/job-applications` | Create an application |
| GET | `/api/job-applications` | List applications |
| GET | `/api/job-applications/{id}` | Get an application |
| PATCH | `/api/job-applications/{id}` | Update an application |
| DELETE | `/api/job-applications/{id}` | Delete an application |

---

## Pagination

Example:

```text
GET /api/job-applications?page=0&size=10
```

Pagination constraints:

```text
page >= 0
1 <= size <= 100
```

The React frontend provides Previous/Next pagination controls using the paginated response returned by Spring Boot.

---

## Search

Search applications by company or job title:

```text
GET /api/job-applications?search=Amazon&page=0&size=10
```

---

## Status Filtering

Example:

```text
GET /api/job-applications?status=INTERVIEW&page=0&size=10
```

The frontend provides a dropdown for:

```text
All Statuses
Saved
Applied
Online Assessment
Phone Screen
Interview
Offer
Rejected
Withdrawn
```

---

## Swagger / OpenAPI

Interactive API documentation is available through Swagger UI.

After starting the backend:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

---

## Running the Project Locally

### Prerequisites

Install:

- Git
- Docker Desktop
- Node.js
- npm

Clone the repository:

```bash
git clone https://github.com/malapatisaisony/scalable-job-application-tracker.git
cd scalable-job-application-tracker
```

---

## Backend Environment Configuration

Create a local `.env` file from:

```text
.env.example
```

Linux/macOS:

```bash
cp .env.example .env
```

Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Example:

```env
DB_NAME=jobtracker
DB_USERNAME=jobtracker
DB_PASSWORD=your-local-password
JWT_SECRET=replace-with-a-long-random-secret
```

The actual `.env` file is ignored by Git.

---

## Start the Backend

Run:

```bash
docker compose up --build -d
```

Docker Compose starts:

```text
Spring Boot API  -> localhost:8080
PostgreSQL       -> localhost:5432
Redis            -> localhost:6379
```

Check the containers:

```bash
docker ps
```

Stop the backend:

```bash
docker compose down
```

PostgreSQL and Redis data are stored in Docker volumes.

---

## Start the Frontend

Open a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Then open:

```text
http://localhost:5173
```

The frontend communicates with:

```text
http://localhost:8080
```

---

## Build the Frontend

Create an optimized production build:

```bash
cd frontend
npm run build
```

The generated production files are written to:

```text
frontend/dist/
```

The `dist` directory is excluded from Git.

---

## Health and Metrics

Application health:

```text
http://localhost:8080/actuator/health
```

Prometheus metrics:

```text
http://localhost:8080/actuator/prometheus
```

Additional metrics:

```text
http://localhost:8080/actuator/metrics
```

Available metrics include:

- JVM memory
- JVM threads
- Process CPU
- PostgreSQL connection pool statistics
- Redis/cache metrics
- HTTP request metrics

---

## Request Logging

Each request receives a generated request ID.

The API returns:

```text
X-Request-Id
```

Logs contain fields such as:

```text
method
path
status
durationMs
requestId
```

Example:

```text
request completed method=GET path=/actuator/health status=200 durationMs=13 requestId=<uuid>
```

Passwords, request bodies, authorization headers, and JWT values are not logged by the request logging filter.

---

## Automated Tests

The backend contains:

- Controller tests
- Service tests
- Application-context tests

Current backend test suite:

```text
Tests run: 12
Failures: 0
Errors: 0
Skipped: 0
```

Run on Linux/macOS:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

---

## Continuous Integration

GitHub Actions runs the backend test suite automatically on:

```text
push
pull_request
```

The CI environment starts:

```text
PostgreSQL 17
Redis 7
Java 21
```

and executes:

```bash
./mvnw test
```

Workflow:

```text
.github/workflows/ci.yml
```

---

## Load Testing

The repository contains:

```text
load-test.js
```

The k6 test authenticates through the real login endpoint before exercising an authenticated cached job lookup.

Credentials are passed through environment variables and are not hard-coded in the repository.

Example:

```bash
docker run --rm -i \
  -e TEST_EMAIL="your-test-email" \
  -e TEST_PASSWORD="your-test-password" \
  grafana/k6 run - < load-test.js
```

Windows PowerShell:

```powershell
Get-Content .\load-test.js -Raw |
docker run --rm -i `
  -e TEST_EMAIL="your-test-email" `
  -e TEST_PASSWORD="your-test-password" `
  grafana/k6 run -
```

A local benchmark using:

```text
4 virtual users
20 second duration
cached GET endpoint
```

produced:

```text
Average:              111.53 ms
Median:                63.66 ms
p90:                  271.21 ms
p95:                  333.37 ms
Maximum:              779.67 ms
HTTP request failures: 0%
```

> These values were collected from a local development benchmark and should not be interpreted as production latency measurements.

---

## Security Design

The project currently includes:

```text
BCrypt password hashing
JWT access tokens
refresh-token hashing
refresh-token revocation
stateless authentication
resource ownership validation
input validation
rate limiting
environment-based secrets
centralized error handling
CORS configuration
```

Refresh tokens are hashed before being stored in PostgreSQL.

Secrets are supplied through environment variables.

The committed:

```text
.env.example
```

contains placeholders only.

The actual:

```text
.env
```

is excluded through `.gitignore`.

---

## Project Structure

```text
scalable-job-application-tracker/
|
+-- frontend/
|   |
|   +-- public/
|   |
|   +-- src/
|   |   +-- assets/
|   |   +-- App.jsx
|   |   +-- api.js
|   |   +-- index.css
|   |   +-- main.jsx
|   |
|   +-- package.json
|   +-- package-lock.json
|   +-- vite.config.js
|
+-- src/
|   |
|   +-- main/
|   |   |
|   |   +-- java/com/saisony/jobtracker/
|   |   |   |
|   |   |   +-- config/
|   |   |   +-- exception/
|   |   |   +-- job/
|   |   |   +-- security/
|   |   |   +-- user/
|   |   |
|   |   +-- resources/
|   |       |
|   |       +-- db/migration/
|   |       +-- application.yaml
|   |
|   +-- test/
|
+-- .github/
|   +-- workflows/
|       +-- ci.yml
|
+-- Dockerfile
+-- docker-compose.yml
+-- load-test.js
+-- pom.xml
+-- .env.example
+-- README.md
```

---

## Database Migrations

Flyway manages database schema evolution.

Migrations currently cover:

```text
V1 - Users
V2 - Refresh tokens
V3 - Job applications
V4 - Composite job-application indexes
```

This allows database changes to remain version controlled and reproducible.

---

## Error Handling

The backend provides centralized exception handling for cases including:

```text
Validation errors
Invalid login credentials
Duplicate email registration
Unauthorized requests
Missing resources
Invalid request parameters
```

HTTP status codes are returned consistently to the frontend.

Examples:

```text
400 Bad Request
401 Unauthorized
404 Not Found
409 Conflict
429 Too Many Requests
```

---

## Engineering Highlights

This project demonstrates:

- Designing REST APIs with Spring Boot
- Building authentication using JWT access and refresh tokens
- Secure password storage with BCrypt
- Refresh-token hashing and revocation
- User-level resource authorization
- Building a React client for a REST API
- Automatic token refresh after JWT expiration
- PostgreSQL schema design
- Database migration management with Flyway
- Composite-index design
- Query-plan analysis
- Redis caching
- Redis-based rate limiting
- Pagination and filtering
- Structured request logging
- Application metrics
- Dockerized development infrastructure
- Automated backend testing
- GitHub Actions CI
- API documentation with OpenAPI
- Load testing with k6
- Production frontend builds with Vite

---

## Local Development Ports

| Service | Port |
|---|---:|
| React / Vite | 5173 |
| Spring Boot API | 8080 |
| PostgreSQL | 5432 |
| Redis | 6379 |

---

## Future Improvements

Possible future enhancements include:

- Frontend component refactoring
- Environment-based frontend API configuration
- Frontend automated tests
- Frontend build validation in GitHub Actions
- Dockerizing the React frontend
- Deployment configuration
- Application analytics dashboard
- Status-history tracking
- Email reminders
- Advanced search
- Additional observability dashboards
- Distributed rate-limiting improvements
- HTTP-only secure-cookie authentication for production deployments

---

## Disclaimer

Performance measurements in this repository were collected in a local development environment.

They should not be interpreted as production benchmarks or guaranteed throughput/latency results.

---

## Author

**Sai Sony Malapati**

Project developed as a full-stack software engineering portfolio project focused on scalable backend architecture, security, performance, observability, and practical end-to-end application development.