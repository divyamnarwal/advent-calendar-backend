# Advent Calendar Backend

Spring Boot backend for the Advent Calendar app, including auth-linked users, daily challenge assignment, profile and badge progression, photo uploads, time capsules, and monthly recap analytics.

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- Spring Security + OAuth2 Resource Server (JWT)
- PostgreSQL
- Spring Validation
- Springdoc OpenAPI (Swagger UI)
- Maven

## Core Features

- JWT-based auth flow (Clerk issuer/audience support)
- Mood-based challenge recommendation (LOW, NEUTRAL, HIGH)
- Culture-aware challenge rotation (GLOBAL, INDIA, RUSSIA)
- Daily challenge preview and confirm flow
- User progress tracking and completion metrics
- Profile management (name, avatar, theme preference)
- Badge and points system (streak and completion milestones)
- Time capsule create/reveal/pending APIs
- Photo upload signature generation for Cloudinary and photo history
- Monthly recap endpoint for engagement summary
- Pulse endpoint for daily aggregate stats

## Local Setup

### Prerequisites

- Java 17+
- PostgreSQL running locally

### Environment Variables

The app loads local overrides from `.env` via:

`spring.config.import=optional:file:.env[.properties]`

Set these values in `.env`:

| Variable | Required | Description |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | PostgreSQL password |
| `CLERK_JWT_ENABLED` | Yes | `true` to enable authenticated API usage |
| `CLERK_JWT_ISSUER` | If auth enabled | Clerk issuer URL |
| `CLERK_JWT_AUDIENCE` | If auth enabled | Expected JWT audience |
| `CLERK_JWT_VALIDATE_AUDIENCE` | Optional | `true` to enforce audience check |
| `CLOUDINARY_CLOUD_NAME` | For photo upload signatures | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | For photo upload signatures | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | For photo upload signatures | Cloudinary API secret |
| `CLOUDINARY_FOLDER` | Optional | Folder for upload signatures (default `advent-recap`) |

### Run

Windows (loads `.env` automatically):

```powershell
.\start-backend.ps1
```

Or with Maven:

```powershell
.\mvnw.cmd spring-boot:run
```

Default base URL: `http://localhost:8081`

Swagger UI: `http://localhost:8081/swagger-ui/index.html`

OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Authentication and Access

- Public endpoints: `GET /health`, `GET /challenges`, `GET /challenges/category/{category}`, and Swagger/OpenAPI docs.
- With `CLERK_JWT_ENABLED=true`, all other endpoints require `Authorization: Bearer <JWT>`.
- With `CLERK_JWT_ENABLED=false`, non-public endpoints are denied by security config.
- Most user-scoped endpoints also enforce ownership (`userId` in request must match authenticated user).

### Recommended first-login flow

1. Obtain JWT from Clerk on frontend.
2. Call `POST /auth/ensure-user` once to create or link backend user.
3. Use `GET /auth/me` and protected endpoints with the same JWT.

## API Summary

### Auth

- `GET /auth/me`
- `POST /auth/ensure-user`

### Profile

- `GET /api/profile`
- `PUT /api/profile`
- `PUT /api/profile/theme`
- `GET /api/profile/badges`

### Challenges

- `POST /challenges`
- `GET /challenges`
- `GET /challenges/category/{category}`
- `GET /challenges/today?userId={id}&mood={LOW|NEUTRAL|HIGH}`
- `GET /challenges/today/preview?userId={id}&mood={LOW|NEUTRAL|HIGH}`

### User Challenges

- `POST /user-challenges/join?userId={id}&challengeId={id}`
- `GET /user-challenges/user/{userId}`
- `GET /user-challenges/user/{userId}/status?status={ASSIGNED|COMPLETED}`
- `GET /user-challenges/user/{userId}/progress`
- `GET /user-challenges/daily?userId={id}`
- `POST /user-challenges/daily/confirm`
- `POST /user-challenges/start?userId={id}&challengeId={id}&mood={LOW|NEUTRAL|HIGH}`
- `GET /user-challenges/challenge/{challengeId}`
- `GET /user-challenges/{id}`
- `PUT /user-challenges/{id}/complete`
- `PUT /user-challenges/{id}/status?status={ASSIGNED|COMPLETED}`
- `DELETE /user-challenges/clear-pending?userId={id}`

### Users

- `POST /users`
- `GET /users/{id}`
- `GET /users`

### Photos

- `GET /photos/upload-signature`
- `POST /photos`
- `GET /photos?month=YYYY-MM` (optional month; defaults to current month)
- `DELETE /photos/{photoId}`

### Recap

- `GET /recap/monthly?month=YYYY-MM` (optional month; defaults to current month)

### Time Capsules

- `POST /time-capsules?userId={id}`
- `GET /time-capsules/revealed?userId={id}`
- `GET /time-capsules/pending?userId={id}`

### Pulse

- `GET /pulse/today`

## Database and Seeding

- `spring.jpa.hibernate.ddl-auto=update` keeps schema aligned with entities.
- `spring.sql.init.mode=always` runs `src/main/resources/data.sql` on startup.
- `data.sql` includes user table backfill/defaults and challenge seed entries.

## Testing

Run tests with:

```powershell
.\mvnw.cmd test
```

## Notes

- `.env` is intentionally ignored by Git.
- CORS is currently configured to allow all origins in `CorsConfig`.
