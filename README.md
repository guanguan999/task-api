# Task Management API

A RESTful task management API built with Spring Boot, featuring JWT authentication and PostgreSQL persistence.

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Database | PostgreSQL + Spring Data JPA |
| Build Tool | Gradle (Kotlin DSL) |
| Utilities | Lombok |

## Project Structure

```
src/main/java/com/guanyiping/task/management/
├── Application.java
├── config/
│   └── SecurityConfig.java           # Security & JWT filter chain
├── controller/
│   ├── AuthController.java           # Register / Login
│   ├── TaskController.java           # Task CRUD
│   └── CategoryController.java       # Category CRUD
├── dto/
│   ├── AuthRequest.java
│   ├── AuthResponse.java
│   ├── TaskRequest.java
│   ├── TaskResponse.java
│   ├── CategoryRequest.java
│   ├── CategoryResponse.java
│   └── ErrorResponse.java
├── entity/
│   ├── User.java
│   ├── Task.java                     # ManyToOne → User, Category
│   └── Category.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── DuplicateResourceException.java
├── repository/
│   ├── UserRepository.java
│   ├── TaskRepository.java           # @EntityGraph to prevent N+1
│   └── CategoryRepository.java
├── security/
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   ├── CustomUserDetails.java
│   └── UserDetailsServiceImpl.java
└── service/
    ├── AuthService.java
    ├── TaskService.java
    └── CategoryService.java
```

## Prerequisites

- Java 21+
- PostgreSQL 12+
- Gradle 8+

## Getting Started

### 1. Database Setup

```sql
CREATE DATABASE mydb;
```

### 2. Configure Application

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your-32-character-minimum-secret-key
jwt.expiration=86400000
```

> **Note:** In production, use environment variables instead of storing secrets in `application.properties`.

### 3. Run the Application

```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080`.

### 4. Build

```bash
./gradlew build
```

### 5. Run Tests

```bash
./gradlew test
```

---

## API Reference

### Authentication

No token required for these endpoints.

#### Register

```
POST /auth/register
```

Request body:
```json
{
  "email": "user@example.com",
  "username": "username",
  "password": "password123"
}
```

Response `201 Created`:
```json
{
  "token": "<JWT token>"
}
```

#### Login

```
POST /auth/login
```

Request body:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

Response `200 OK`:
```json
{
  "token": "<JWT token>"
}
```

---

### Tasks

All task endpoints require the `Authorization` header:

```
Authorization: Bearer <JWT token>
```

#### Get All Tasks

```
GET /tasks
```

Response `200 OK`:
```json
[
  {
    "id": 1,
    "title": "My Task",
    "description": "Task description here",
    "priority": "High",
    "completed": false
  }
]
```

#### Get Task by ID

```
GET /tasks/{id}
```

Response `200 OK` / `404 Not Found`

#### Get Tasks by Priority

```
GET /tasks/priority/{priority}
```

`priority` must be one of: `Low`, `Medium`, `High`

#### Create Task

```
POST /tasks
```

Request body:
```json
{
  "title": "New Task",
  "description": "At least 10 characters",
  "priority": "High"
}
```

Response `201 Created`

Validation rules:
- `title`: required
- `description`: minimum 10 characters
- `priority`: `Low` | `Medium` | `High` (defaults to `Low`)

#### Update Task

```
PUT /tasks/{id}
```

Partial update supported — only non-null fields are applied.

Response `200 OK` / `404 Not Found`

#### Complete Task

```
PATCH /tasks/{id}/complete
```

Marks the task as completed. Response `200 OK` / `404 Not Found`

#### Delete Task

```
DELETE /tasks/{id}
```

Response `204 No Content` / `404 Not Found`

---

### Categories

All category endpoints require the `Authorization` header:

```
Authorization: Bearer <JWT token>
```

#### Get All Categories

```
GET /categories
```

Response `200 OK`:
```json
[
  {
    "id": 1,
    "name": "Work",
    "description": "Work-related tasks"
  }
]
```

#### Get Category by ID

```
GET /categories/{id}
```

Response `200 OK` / `404 Not Found`

#### Create Category

```
POST /categories
```

Request body:
```json
{
  "name": "Work",
  "description": "Work-related tasks"
}
```

Response `201 Created`

Validation rules:
- `name`: required, unique

#### Update Category

```
PUT /categories/{id}
```

Partial update supported — only non-null fields are applied.

Response `200 OK` / `404 Not Found`

#### Delete Category

```
DELETE /categories/{id}
```

Response `204 No Content` / `404 Not Found`

---

## Error Responses

All errors follow this format:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 1",
  "timestamp": "2026-03-27T10:00:00"
}
```

| Status | Cause |
|--------|-------|
| 400 | Validation error (invalid input) |
| 401 | Missing or invalid JWT token |
| 404 | Resource not found |
| 409 | Duplicate email on registration |
| 500 | Internal server error |

---

## Security Design

- **JWT** tokens are signed with HMAC-SHA and expire after **24 hours**
- **BCrypt** is used for password hashing
- **Stateless** session management (no CSRF required)
- `/auth/**` endpoints are publicly accessible; all others require a valid token
- Exception handler prevents stack trace leakage in responses

---

## Testing

Test coverage includes unit and integration tests across all layers:

| Test File | Coverage |
|-----------|----------|
| `TaskServiceTest` | Task CRUD logic, partial updates, 404 handling |
| `AuthServiceTest` | Register, login, duplicate email, bad credentials |
| `TaskControllerTest` | HTTP layer, input validation, status codes |
| `AuthControllerTest` | Auth endpoints, error scenarios |
| `JwtUtilTest` | Token generation, validation, expiration, tamper detection |
| `GlobalExceptionHandlerTest` | Exception-to-HTTP status mapping |
| `TaskRepositoryTest` | N+1 prevention via `@EntityGraph` (integration test against real DB) |

```bash
./gradlew test
```

---

## License

MIT