# Scalable Job Application Tracking Backend

[![Backend CI](https://github.com/malapatisaisony/scalable-job-application-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/malapatisaisony/scalable-job-application-tracker/actions/workflows/ci.yml)

A production-style REST API for tracking job applications, built with **Java 21, Spring Boot, PostgreSQL, Redis, JWT authentication, Docker, GitHub Actions, Prometheus, and k6**.

The project focuses on backend engineering concepts such as authentication, authorization, caching, rate limiting, pagination, database indexing, observability, automated testing, load testing, and reproducible containerized deployment.

---

## Features

### Authentication and Security

- User registration and login
- BCrypt password hashing
- JWT access tokens
- Refresh token support
- Refresh token persistence and revocation
- Stateless Spring Security configuration
- User-specific resource authorization
- Input validation and centralized exception handling
- Environment-based configuration for sensitive values

### Job Application Management

Authenticated users can:

- Create job applications
- View individual applications
- View paginated applications
- Update applications
- Delete applications
- Filter applications by status
- Search applications
- Track application lifecycle states

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

- Redis caching for frequently accessed job applications
- 10-minute cache TTL
- Cache eviction after updates and deletes
- Per-user Redis rate limiting
- Maximum page-size validation
- PostgreSQL indexes for user and status-based queries
- Composite indexes for common query patterns

### Observability

- Spring Boot Actuator
- Health checks
- Application metrics
- Prometheus metrics endpoint
- Database connection pool metrics
- JVM and process metrics
- Cache metrics
- Request correlation IDs
- Request duration logging
- HTTP status logging

### API Documentation

Interactive API documentation is available through Swagger UI.

After starting the application:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend language |
| Spring Boot 4.1.1 | Application framework |
| Spring MVC | REST APIs |
| Spring Security | Authentication and authorization |
| PostgreSQL 17 | Primary relational database |
| Redis 7 | Caching and rate limiting |
| Spring Data JPA | Database access |
| Flyway | Database migrations |
| JWT | Stateless authentication |
| BCrypt | Password hashing |
| Swagger / OpenAPI | API documentation |
| Spring Boot Actuator | Health and operational metrics |
| Micrometer + Prometheus | Application metrics |
| Docker | Application containerization |
| Docker Compose | Local multi-container environment |
| Maven | Build and dependency management |
| JUnit / Mockito | Automated testing |
| k6 | Load testing |
| GitHub Actions | Continuous Integration |

---

## Architecture

```text
                         Client
                            |
                            v
                  +--------------------+
                  |   Spring Boot API  |
                  |      Port 8080     |
                  +--------------------+
                     |              |
                     |              |
             Authentication      Job APIs
                     |              |
                     v              v
              Spring Security   Service Layer
                     |              |
                     |         +----+----+
                     |         |         |
                     v         v         v
                    JWT    PostgreSQL   Redis
                             Port       Port
                             5432       6379
                               |
                               v
                            Flyway
                          Migrations
```

The application follows a layered backend structure:

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

## Database Schema

The project uses Flyway migrations to manage database changes.

### Users

Stores:

- user ID
- email
- BCrypt password hash
- role
- creation and update timestamps

### Refresh Tokens

Stores:

- user reference
- SHA-256 token hash
- expiration timestamp
- revoked status
- creation timestamp

Raw refresh tokens are not stored directly in the database.

### Job Applications

Stores:

- user ID
- company name
- job title
- job URL
- location
- application status
- applied timestamp
- creation timestamp
- update timestamp

Foreign keys enforce ownership relationships between users and their applications.

---

## Database Indexing

Indexes are included for frequently used access patterns:

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

Index usage was validated with:

```sql
EXPLAIN (ANALYZE, BUFFERS)
```

During local query-plan testing, approximately **50,000 temporary benchmark rows** were inserted inside a transaction.

The query:

```sql
SELECT *
FROM job_applications
WHERE user_id = 1
ORDER BY created_at DESC
LIMIT 10;
```

used:

```text
idx_job_applications_user_created_at
```

with an observed local PostgreSQL execution time of approximately:

```text
0.668 ms
```

The benchmark transaction was rolled back afterward, so the temporary rows were not retained.

> This result represents a local query-plan experiment and is not a production performance guarantee.

---

## Redis Caching

Individual job application reads are cached in Redis.

Cache behavior:

```text
GET request
    |
    v
Check Redis
    |
    +------ Cache Hit ------> Return cached response
    |
    +------ Cache Miss
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

Cache entries expire after:

```text
10 minutes
```

Update and delete operations evict stale cached values.

---

## Rate Limiting

Job application endpoints use Redis-backed per-user rate limiting.

Current limit:

```text
100 requests / minute / authenticated user
```

Requests above the configured limit return:

```text
HTTP 429 Too Many Requests
```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register a user |
| POST | `/api/users/login` | Authenticate and receive tokens |
| POST | `/api/users/refresh` | Refresh access token |
| POST | `/api/users/logout` | Revoke refresh token |
| GET | `/api/users/me` | Get authenticated user |

### Job Applications

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/job-applications` | Create application |
| GET | `/api/job-applications` | List applications |
| GET | `/api/job-applications/{id}` | Get application |
| PATCH | `/api/job-applications/{id}` | Update application |
| DELETE | `/api/job-applications/{id}` | Delete application |

Example paginated request:

```text
GET /api/job-applications?page=0&size=10
```

Filter by status:

```text
GET /api/job-applications?status=INTERVIEW&page=0&size=10
```

Search:

```text
GET /api/job-applications?search=Amazon&page=0&size=10
```

Pagination constraints:

```text
page >= 0
1 <= size <= 100
```

---

## Running Locally with Docker

### Prerequisites

Install:

- Docker Desktop
- Git

Clone the repository:

```bash
git clone https://github.com/malapatisaisony/scalable-job-application-tracker.git
cd scalable-job-application-tracker
```

Create your local environment file:

```bash
cp .env.example .env
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Update `.env` with your local values:

```env
DB_NAME=jobtracker
DB_USERNAME=jobtracker
DB_PASSWORD=your-local-password
JWT_SECRET=replace-with-a-long-random-secret
```

Start the complete stack:

```bash
docker compose up --build -d
```

Docker Compose starts:

```text
Spring Boot API     -> localhost:8080
PostgreSQL          -> localhost:5432
Redis               -> localhost:6379
```

Check running containers:

```bash
docker ps
```

Stop the application:

```bash
docker compose down
```

Database and Redis data are stored in Docker volumes and persist across normal container restarts.

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

Additional metric endpoints:

```text
http://localhost:8080/actuator/metrics
```

Observed metrics include:

- JVM memory
- JVM threads
- process CPU
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

Logs include fields such as:

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

Request bodies, passwords, and authorization tokens are not logged by the request logging filter.

---

## Automated Tests

The project contains controller, service, and application-context tests.

Current test suite:

```text
Tests run: 12
Failures: 0
Errors: 0
Skipped: 0
```

Run tests locally with:

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

- PostgreSQL 17
- Redis 7
- Java 21

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

The repository includes:

```text
load-test.js
```

The load test uses k6 and authenticates through the real login endpoint before testing the cached job lookup endpoint.

Credentials are supplied through environment variables instead of being committed to the repository.

Example:

```bash
docker run --rm -i \
  -e TEST_EMAIL="your-test-email" \
  -e TEST_PASSWORD="your-test-password" \
  grafana/k6 run - < load-test.js
```

For Windows PowerShell:

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
Average:             111.53 ms
Median:               63.66 ms
p90:                 271.21 ms
p95:                 333.37 ms
Maximum:             779.67 ms
HTTP request failures: 0%
```

> These numbers are from a local development benchmark and should not be interpreted as production latency measurements.

---

## Security Design

The project includes:

```text
BCrypt password hashing
JWT access tokens
refresh-token hashing
refresh-token revocation
stateless authentication
resource ownership checks
request validation
rate limiting
environment-based secrets
centralized error handling
```

The committed `.env.example` contains placeholders only.

The real:

```text
.env
```

file is excluded from Git through `.gitignore`.

---

## Project Structure

```text
src/main/java/com/saisony/jobtracker/
|
+-- config/
|   +-- CacheConfig.java
|   +-- OpenApiConfig.java
|   +-- RateLimitInterceptor.java
|   +-- RequestLoggingFilter.java
|   +-- WebConfig.java
|
+-- exception/
|   +-- GlobalExceptionHandler.java
|   +-- ConflictException.java
|   +-- InvalidCredentialsException.java
|   +-- InvalidRefreshTokenException.java
|   +-- ResourceNotFoundException.java
|
+-- job/
|   +-- controller/
|   +-- dto/
|   +-- entity/
|   +-- repository/
|   +-- service/
|
+-- security/
|   +-- JwtService.java
|   +-- RefreshToken.java
|   +-- RefreshTokenService.java
|   +-- SecurityConfig.java
|   +-- repository/
|
+-- service/
|   +-- RateLimitService.java
|
+-- user/
    +-- controller/
    +-- dto/
    +-- entity/
    +-- repository/
    +-- service/
```

Database migrations:

```text
src/main/resources/db/migration/
|
+-- V1__create_users_table.sql
+-- V2__create_refresh_tokens_table.sql
+-- V3__create_job_applications_table.sql
+-- V4__add_job_application_indexes.sql
```

---

## Engineering Highlights

This project demonstrates practical backend engineering beyond basic CRUD:

- Secure JWT authentication with persistent refresh tokens
- PostgreSQL schema management with Flyway
- Redis-backed caching
- Redis-backed rate limiting
- Ownership-safe data access
- Pagination, filtering, and search
- Database index design
- Query-plan validation with PostgreSQL
- Automated controller and service testing
- Dockerized multi-service environment
- Swagger/OpenAPI documentation
- Metrics through Actuator and Prometheus
- Request correlation and latency logging
- Local load testing with k6
- GitHub Actions CI

---

## Future Improvements

Potential extensions include:

- Application status history
- Analytics dashboard
- Response-rate analytics
- Average time-to-interview metrics
- Asynchronous event processing
- Email or notification support
- Advanced PostgreSQL full-text search
- Integration testing with Testcontainers
- Frontend client
- Cloud deployment

---

## Author

**Sai Sony Malapati**

GitHub: [malapatisaisony](https://github.com/malapatisaisony)