# Westminster Health Centre — Backend API

REST API for managing doctors and receptionists at the Westminster Health Centre.
Built with Spring Boot 3, PostgreSQL, and JUnit 5.

**Live API:** `https://your-app.railway.app`
**Swagger UI:** `https://your-app.railway.app/swagger-ui`

---

## Tech stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL (production) / H2 (tests) |
| Validation | Jakarta Bean Validation |
| API docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito + MockMvc |
| Deployment | Railway |

---

## Running locally

### Prerequisites
- JDK 17+
- Maven 3.8+
- PostgreSQL running locally (or use Docker)

### 1 — Start a local PostgreSQL database
```bash
# Docker (easiest)
docker run --name healthcentre-db \
  -e POSTGRES_DB=healthcentre \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 -d postgres:16
```

### 2 — Run the application
```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.
Swagger UI is at `http://localhost:8080/swagger-ui`.

### 3 — Run the tests
```bash
mvn test
```

Tests use an H2 in-memory database — no PostgreSQL needed.

---

## API reference

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/staff` | List all staff (optional `?search=`) |
| `GET` | `/api/staff/stats` | Staff counts and remaining capacity |
| `GET` | `/api/staff/{staffId}` | Get a single staff member |
| `POST` | `/api/staff/doctors` | Add a new doctor |
| `POST` | `/api/staff/receptionists` | Add a new receptionist |
| `PUT` | `/api/staff/doctors/{staffId}` | Update an existing doctor |
| `PUT` | `/api/staff/receptionists/{staffId}` | Update an existing receptionist |
| `DELETE` | `/api/staff/{staffId}` | Remove a staff member |

Full request/response schemas are in the Swagger UI.

### Staff ID format
Staff IDs follow the pattern `XX0000` — two uppercase letters followed by four digits.
By convention, doctors use `DR` and receptionists use `RC`, but the API does not enforce this.

### Error responses
All errors return a consistent envelope:
```json
{
  "status": 404,
  "message": "No staff member found with ID 'DR9999'.",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

---

## Project structure

```
src/
├── main/java/com/westminster/healthcentre/
│   ├── HealthCentreApplication.java     Entry point
│   ├── config/
│   │   ├── AppConfig.java               CORS + OpenAPI metadata
│   │   └── DataSeeder.java              Sample data on first startup
│   ├── controller/
│   │   └── StaffController.java         REST endpoints (no business logic)
│   ├── dto/
│   │   └── StaffDtos.java               Request/response records with validation
│   ├── exception/
│   │   ├── HealthCentreExceptions.java  Typed domain exceptions
│   │   └── GlobalExceptionHandler.java  Maps exceptions → HTTP responses
│   ├── model/
│   │   ├── StaffMember.java             Abstract JPA entity (SINGLE_TABLE base)
│   │   ├── Doctor.java                  Doctor subtype
│   │   └── Receptionist.java            Receptionist subtype
│   ├── repository/
│   │   └── StaffMemberRepository.java   Spring Data JPA + custom JPQL queries
│   └── service/
│       ├── StaffService.java            Service interface
│       └── StaffServiceImpl.java        Business logic + entity↔DTO mapping
└── test/java/com/westminster/healthcentre/
    ├── controller/
    │   └── StaffControllerIntegrationTest.java   Full-stack MockMvc tests
    └── service/
        └── StaffServiceImplTest.java              Mockito unit tests
```

---

## Architecture decisions

### Why DTOs instead of returning entities directly?
Entities are JPA-managed objects with database concerns (IDs, lazy-loading proxies, discriminator columns). Exposing them directly from the API leaks implementation details and can cause serialisation issues. DTOs are plain records that contain exactly what the API consumer needs.

### Why SINGLE_TABLE inheritance?
Both Doctor and Receptionist share most fields, the total row count is small (≤50), and there are no complex polymorphic queries. SINGLE_TABLE avoids joins entirely, which keeps queries fast and the schema simple.

### Why an interface for StaffService?
The controller depends on `StaffService` (the abstraction), not `StaffServiceImpl` (the detail). This means unit tests can mock the interface without needing Spring or a database, keeping them fast and focused.

### Why typed exceptions instead of generic RuntimeException?
Each exception (`StaffNotFoundException`, `DuplicateStaffIdException`, etc.) maps to a specific HTTP status in `GlobalExceptionHandler`. This keeps error handling centralised and controllers free of try/catch blocks.

---

## Deploying to Railway

1. Push this folder to a GitHub repository.
2. Create a new Railway project → **Deploy from GitHub repo**.
3. Add a PostgreSQL plugin — Railway injects `DATABASE_URL` automatically.
4. Set these environment variables in Railway:
   ```
   DB_USERNAME=postgres
   DB_PASSWORD=<from Railway PostgreSQL plugin>
   CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
   ```
5. Railway detects the `pom.xml` and builds automatically.

---

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | Set automatically by Railway |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/healthcentre` | Full JDBC URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `password` | Database password |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins |
| `app.staff.limit` | `50` | Maximum number of staff members |
