# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot backend application for a chess game, written in Kotlin. It uses Spring Boot 3.5.4 with Kotlin 1.9.25, running on Java 21.

**Package name**: `com.example.chess_backend.chess_backend` (note: uses underscores, not hyphens)

## Build System

This project uses Gradle with Kotlin DSL for build configuration.

### Common Commands

**Build the project:**
```bash
./gradlew build
```

**Run the application:**
```bash
./gradlew bootRun
```

**Run tests:**
```bash
./gradlew test
```

**Run a single test:**
```bash
./gradlew test --tests "com.example.chess_backend.chess_backend.ChessBackendApplicationTests"
```

**Clean build artifacts:**
```bash
./gradlew clean
```

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.5.4
- **Language**: Kotlin 1.9.25
- **Java Version**: 21
- **Database**: H2 in-memory database (JDBC)
- **Testing**: JUnit 5 with Kotlin test support

### Database Configuration
The application uses an H2 in-memory database in PostgreSQL compatibility mode (see `src/main/resources/application.properties`):
- **JDBC URL**: `jdbc:h2:mem:chess;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1`
- **H2 Console**: Enabled at `http://localhost:8080/h2-console` (username: `sa`, password: empty)
- **Dialect**: PostgreSQL (for production compatibility)
- **JPA**: Using Spring Data JPA with Hibernate
- **DDL Auto**: `create-drop` (fresh database on each restart - development only)

### Project Structure
- **Package path**: `src/main/kotlin/com/example/chess_backend/`
- **Test path**: `src/test/kotlin/com/example/chess_backend/`

**Implemented packages:**
- `user/` - User management (CRUD operations)
  - User entity, repository, service, controller, DTOs
- `match/` - Chess match management (CRUD operations)
  - Match entity with relationships to users
  - Repository, service, controller, DTOs
  - Tracks white/black players, status, and result

### Current Implementation Status
- **JPA Entities**: Using regular classes (not data classes) with `@field:` annotations
- **Validation**: Jakarta validation enabled with `@Valid` in controllers
- **JPA Auditing**: Enabled with `@CreatedDate` and `@LastModifiedDate`
- **REST APIs**: Full CRUD for users and matches
- **DTOs**: Separate request/response objects to decouple API from entities
- **Authentication**: Auth0 OAuth2 JWT validation with Spring Security
- **Error Handling**: Global exception handler with standardized error responses using ErrorCode enum

## Authentication & Security

### Auth0 Integration

The application uses **Auth0** for authentication with **Spring Security OAuth2 Resource Server** for JWT validation.

**Auth0 Configuration:**
- **Domain**: `dev-s85tbpsmrojxdyg0.us.auth0.com`
- **Audience**: `https://chess-api`
- **Config file**: `src/main/resources/application.properties`

### Security Architecture

**Decoupling Strategy:**
```
JWT (Auth0) → AuthenticationService → User (internal ID) → Application Logic
```

**Key Components:**
1. **SecurityConfig** (`config/SecurityConfig.kt`) - Production JWT validation
   - Validates JWT signature, expiration, issuer, and audience
   - All `/api/**` endpoints require authentication
   - Custom `AudienceValidator` for Auth0 audience checking

2. **DevSecurityConfig** (`config/DevSecurityConfig.kt`) - Development mode
   - Permits all requests (no authentication required)
   - Active when `SPRING_PROFILES_ACTIVE=dev`

3. **AuthenticationService** (`auth/AuthenticationService.kt`) - Abstraction layer
   - `registerUser(jwt: Jwt?, customName: String?): User` - Explicitly registers a new user
     - Throws `UserAlreadyRegisteredException` if user already exists
     - Accepts optional custom name (uses JWT claim if not provided)
   - `getCurrentUser(jwt: Jwt?): User` - Extracts auth0Id from JWT and returns User entity
     - **Does NOT auto-create users** - Users must register first
     - Throws `RuntimeException` if user is not registered
   - **Accepts nullable JWT** - Handles both dev mode (JWT null) and production mode (JWT present)
   - **Dev mode behavior**: Returns default test user (Alice) when JWT is null
   - Decouples Auth0 from business logic (easy to swap auth providers)

### User Model with Auth0

**User entity fields:**
- `id: Int` - Internal primary key (auto-increment)
- `name: String` - User display name
- `email: String` - User email (unique)
- `auth0Id: String` - Auth0 user ID (unique, indexed, immutable)
- `createdAt: LocalDateTime`
- `updatedAt: LocalDateTime`

**Why both `id` and `auth0Id`?**
- `id`: Internal database relationships (faster integer joins for Match table)
- `auth0Id`: Authentication lookups (maps to external Auth0 identity)

### API Endpoints

**Authentication-required endpoints:**
- `GET /api/users/me` - Returns current authenticated user
- `GET /api/matches/me` - Returns matches for authenticated user
- All other `/api/**` endpoints (in production mode)

**How it works:**
```kotlin
@GetMapping("/me")
fun getCurrentUser(@AuthenticationPrincipal jwt: Jwt?): ResponseEntity<UserResponse> {
    val user = authenticationService.getCurrentUser(jwt)
    return ResponseEntity.ok(user.toResponse())
}
```
**Note:** JWT parameter is nullable (`Jwt?`) to support dev mode where no authentication is required.

### Running the Application

**Development mode (no authentication):**
```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

**Production mode (requires JWT):**
```bash
./gradlew bootRun
```

**Running tests (uses dev profile):**
```bash
./gradlew test  # Tests use @ActiveProfiles("dev")
```

### Android App Integration

**Request JWT token with audience:**
```kotlin
Auth0.webAuth(account)
    .withAudience("https://chess-api")
    .start(...)
```

**Include JWT in all API requests:**
```
Authorization: Bearer <jwt-token>
```

### User Registration Flow

Users must explicitly register before using the app:

**1. First-time user:**
```
User authenticates with Auth0 → receives JWT token
Mobile app calls POST /api/users/register with JWT
AuthenticationService creates user from JWT claims (name, email)
Returns user data to app
```

**2. Returning user:**
```
User authenticates with Auth0 → receives JWT token
Mobile app calls GET /api/users/me with JWT
AuthenticationService finds existing user by auth0Id
Returns user data to app
```

**Registration endpoint:**
```kotlin
POST /api/users/register
Body: { "name": "Custom Name" }  // Optional: uses JWT claim if not provided
Headers: Authorization: Bearer <jwt-token>

Returns: 201 Created with UserResponse
Throws: 409 Conflict if user already registered
```

**Client flow:**
1. User authenticates with Auth0
2. Call `GET /api/users/me`
3. If 404 → call `POST /api/users/register`
4. If 200 → user already registered, proceed

### Spring Security Documentation

**Key resources:**
- OAuth2 Resource Server: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
- Authorization: https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html
- Method Security: https://docs.spring.io/spring-security/reference/servlet/integrations/mvc.html#mvc-authentication-principal
- Auth0 Quick Start: https://auth0.com/docs/quickstart/backend/java-spring-security5

## Important Notes

- The JVM level is set to 21 (originally 24, but downgraded for Kotlin compatibility)
- The package name uses underscores instead of hyphens due to Kotlin naming constraints
- Use `@field:` prefix for JPA/validation annotations in Kotlin to avoid IDE warnings
- Entity classes should use regular `class`, not `data class` for proper JPA behavior

## Future WebSocket Implementation (Planned)

**Game State Management Strategy:**
When implementing WebSocket support for real-time chess gameplay, use **Option 4: WebSocket + DB**:

1. **In-Memory Game Sessions**: Create a `GameSessionManager` component to hold active games in memory using `ConcurrentHashMap<Int, GameState>`
2. **GameState Model**: Create a `GameState` data class containing:
   - Match metadata (ID, players, status)
   - Current board position (FEN notation)
   - Move history (list of moves)
   - Connected players set
   - Helper methods: `applyMove()`, `isPlayerInGame()`, `getOpponentId()`
3. **Persistence Strategy**:
   - Save to database every N moves (e.g., every 10 moves)
   - Save on player disconnect
   - Save when game ends
   - Load from DB when player reconnects
4. **WebSocket Flow**:
   - Player connects → load game from DB into memory (if not already loaded)
   - Player makes move → update in-memory state, broadcast via WebSocket
   - Periodically persist to database
   - Player disconnects → save to DB, remove from memory if no players connected
5. **Database Schema**: Add to Match entity:
   - `currentPosition: String?` - FEN notation for board state
   - `moves: String?` - Comma-separated or JSON move history
   - `lastMoveAt: LocalDateTime?` - Timestamp of last move

**Benefits of this approach:**
- Fast real-time updates (no DB hit per move)
- Survives disconnects and server restarts
- Memory only used for active games
- Works well with WebSocket broadcasting

**Implementation files to create:**
- `GameState.kt` - In-memory game representation
- `GameSessionManager.kt` - Manages active sessions
- `GameWebSocketHandler.kt` - WebSocket endpoint for moves
- Update `Match` entity to include `currentPosition` and `moves` fields

## AWS ECS Deployment (Future)

**Deployment target:** AWS Elastic Container Service (ECS)

### Docker Configuration

**Dockerfile** (create when ready to deploy):
```dockerfile
# Multi-stage build for smaller image
FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Use production profile
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**docker-compose.yml** (for local testing):
```yaml
version: '3.8'
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://your-rds-host:5432/chess
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
```

### Production Configuration

**application-prod.properties** (create when deploying):
```properties
# Database - AWS RDS PostgreSQL
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Production settings
server.port=8080
logging.level.root=INFO
```

### ECS Deployment Checklist

1. **Build and push Docker image to ECR** (Elastic Container Registry)
2. **Create RDS PostgreSQL database** in same VPC as ECS
3. **Create ECS Task Definition** with environment variables for DB credentials
4. **Configure security groups** - allow ECS to connect to RDS
5. **Set up Application Load Balancer** (ALB) for HTTPS
6. **Store secrets in AWS Secrets Manager** or Parameter Store
7. **Run database migrations** using Flyway or Liquibase
8. **Deploy ECS Service** with desired task count

### Android App Configuration

**Local development:**
- Emulator: `http://10.0.2.2:8080/`
- Physical device: `http://YOUR_LOCAL_IP:8080/`

**Production (AWS):**
- `https://your-alb-domain.amazonaws.com/`
- Or use custom domain with Route 53