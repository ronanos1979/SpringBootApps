# LexiconLair Backend

Spring Boot REST API backend for the LexiconLair React SPA.

## Stack

- Java 21
- Spring Boot 4
- Spring Web MVC REST controllers
- Spring Security session authentication
- Spring Data JPA
- PostgreSQL or MySQL
- Gradle

## Run

```powershell
.\gradlew.bat bootRun
```

The backend runs on `http://localhost:8080`.

## Test

```powershell
.\gradlew.bat test
```

The test suite includes MockMvc tests for REST controllers and security, plus Mockito unit tests for auth/config/service behavior.

## Profiles

The active profile defaults to PostgreSQL:

```properties
spring.profiles.active=${SPRING_PROFILES_ACTIVE:postgresql}
```

Database connection values are read from environment variables:

```text
LEXICONLAIR_PG_URL
LEXICONLAIR_PG_USERNAME
LEXICONLAIR_PG_PASSWORD

LEXICONLAIR_MYSQL_URL
LEXICONLAIR_MYSQL_USERNAME
LEXICONLAIR_MYSQL_PASSWORD
```

## API Boundary

Controllers use request and response DTOs. JPA entities remain inside the backend persistence layer and are not returned directly from API endpoints.

DTO packages are feature-local:

```text
author/dto
book/dto
definition/dto
user/dto
word/dto
```

## Authentication

Authentication uses Spring Security form-login processing over REST endpoints and stores authentication in the HTTP session.

```text
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
```

Login expects form parameters:

```text
username=admin
password=change-me
```

The frontend must send requests with credentials enabled so the browser includes the `JSESSIONID` cookie.

## Entity APIs

All entity APIs require authentication.

```text
GET    /api/authors
GET    /api/authors/{id}
POST   /api/authors
PUT    /api/authors/{id}
DELETE /api/authors/{id}

GET    /api/books
GET    /api/books/{id}
POST   /api/books
PUT    /api/books/{id}
DELETE /api/books/{id}

GET    /api/words
GET    /api/words/{id}
POST   /api/words
PUT    /api/words/{id}
DELETE /api/words/{id}

GET    /api/definitions
GET    /api/definitions/{id}
POST   /api/definitions
PUT    /api/definitions/{id}
DELETE /api/definitions/{id}

GET    /api/users
GET    /api/users/{id}
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

## Request DTOs

`AuthorRequest`

```json
{
  "firstName": "Jane",
  "lastName": "Austen"
}
```

`BookRequest`

```json
{
  "title": "Emma",
  "authorId": 1
}
```

`WordRequest`

```json
{
  "text": "lexicon",
  "language": "en"
}
```

`DefinitionRequest`

```json
{
  "wordId": 1,
  "definitionText": "A vocabulary.",
  "partOfSpeech": "noun",
  "example": "The lexicon is broad.",
  "sourceApi": "dictionary-api",
  "cachedAt": "2026-05-01T10:00:00"
}
```

`UserCreateRequest`

```json
{
  "username": "newuser",
  "password": "password123",
  "email": "new@example.com",
  "firstName": "New",
  "lastName": "User"
}
```

`UserUpdateRequest`

```json
{
  "username": "newuser",
  "password": "",
  "email": "new@example.com",
  "firstName": "New",
  "lastName": "User"
}
```

Blank or missing password on update keeps the existing stored password.

## Frontend Integration

The React app lives in `../frontend`. During local development, CORS allows `http://localhost:5173` to call `/api/**` with credentials.

## Static Page

The backend serves `src/main/resources/static/index.html` as a lightweight API status page. Browser routes outside `/api/**` are forwarded to `index.html` for SPA routing.
