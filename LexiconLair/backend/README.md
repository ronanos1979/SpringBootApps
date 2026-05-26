# LexiconLair Backend

Spring Boot REST API backend for the LexiconLair React SPA.

## Stack

- Java 21
- Spring Boot 4
- Spring Web MVC REST controllers
- Spring Security — session-based authentication, role-based access control
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

The test suite includes full-stack `@SpringBootTest` integration tests for the auth session flow, `@WebMvcTest` MockMvc tests for every REST controller including security rules, Mockito unit tests for services, and `@DataJpaTest` repository tests against an H2 in-memory database.

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

Controllers use request and response DTOs. JPA entities remain inside the backend persistence layer and are never returned directly from API endpoints.

DTO packages are feature-local:

```text
admin/dto
author/dto
book/dto
bookword/dto
definition/dto
game/dto
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

## Role-Based Access Control

Two roles are supported: `USER` and `ADMIN`.

| Endpoint                                        | Minimum role |
|-------------------------------------------------|--------------|
| `GET /api/books`                                | USER         |
| `GET /api/books/search`                         | USER         |
| `POST /api/books`                               | USER         |
| `GET /api/authors`                              | USER         |
| `GET /api/authors/search`                       | USER         |
| `POST /api/authors`                             | USER         |
| `GET /api/books/{id}/words`                     | USER         |
| `POST /api/books/{id}/words`                    | USER         |
| `POST /api/books/{id}/words/bulk`               | USER         |
| `DELETE /api/books/{id}/words/{bookWordId}`     | USER         |
| `GET /api/game/question`                        | USER         |
| `GET /api/auth/me`                              | USER         |
| `POST /api/auth/logout`                         | USER         |
| `GET /api/books/{id}`                           | ADMIN        |
| `PUT /api/books/**`                             | ADMIN        |
| `DELETE /api/books/**`                          | ADMIN        |
| `PUT /api/authors/**`                           | ADMIN        |
| `DELETE /api/authors/**`                        | ADMIN        |
| `GET /api/words/**`                             | ADMIN        |
| `POST /api/words/**`                            | ADMIN        |
| `PUT /api/words/**`                             | ADMIN        |
| `DELETE /api/words/**`                          | ADMIN        |
| `GET /api/definitions/**`                       | ADMIN        |
| `POST /api/definitions/**`                      | ADMIN        |
| `PUT /api/definitions/**`                       | ADMIN        |
| `DELETE /api/definitions/**`                    | ADMIN        |
| `GET /api/users/**`                             | ADMIN        |
| `POST /api/users/**`                            | ADMIN        |
| `PUT /api/users/**`                             | ADMIN        |
| `DELETE /api/users/**`                          | ADMIN        |
| `GET /api/admin/settings`                       | ADMIN        |
| `PUT /api/admin/settings`                       | ADMIN        |

## Entity APIs

### Authors

```text
GET    /api/authors
GET    /api/authors/{id}
POST   /api/authors
PUT    /api/authors/{id}         (ADMIN)
DELETE /api/authors/{id}         (ADMIN)
```

### Books

```text
GET    /api/books
GET    /api/books/{id}           (ADMIN)
POST   /api/books
PUT    /api/books/{id}           (ADMIN)
DELETE /api/books/{id}           (ADMIN)
```

### Book Words — words associated with a specific book

```text
GET    /api/books/{bookId}/words
GET    /api/books/{bookId}/words?mine=false   (all users' words for this book)
POST   /api/books/{bookId}/words
POST   /api/books/{bookId}/words/bulk
DELETE /api/books/{bookId}/words/{bookWordId}
```

`GET` defaults to `mine=true`, returning only words added by the authenticated user.

### Words

```text
GET    /api/words                              (ADMIN)
GET    /api/words/{id}                         (ADMIN)
GET    /api/words/search?q=<text>              (ADMIN)
GET    /api/words/without-definitions          (ADMIN)
POST   /api/words                              (ADMIN)
POST   /api/words/{id}/definitions/refresh     (ADMIN)
POST   /api/words/definitions/refresh-missing  (ADMIN)
PUT    /api/words/{id}                         (ADMIN)
DELETE /api/words/{id}                         (ADMIN)
```

`/search` queries both `BookWord` entries (with book/author context) and standalone `Word` records, deduplicating words that appear in both.

`/without-definitions` returns words that have no associated `Definition` records.

`/{id}/definitions/refresh` re-fetches definitions from the external dictionary API and replaces stored definitions for that word.

`/definitions/refresh-missing` runs the refresh for every word that currently has no definitions.

### Definitions

```text
GET    /api/definitions          (ADMIN)
GET    /api/definitions/{id}     (ADMIN)
POST   /api/definitions          (ADMIN)
PUT    /api/definitions/{id}     (ADMIN)
DELETE /api/definitions/{id}     (ADMIN)
```

### Users

```text
GET    /api/users                (ADMIN)
GET    /api/users/{id}           (ADMIN)
POST   /api/users                (ADMIN)
PUT    /api/users/{id}           (ADMIN)
DELETE /api/users/{id}           (ADMIN)
```

### Game

```text
GET /api/game/question?mode=easy        (USER)
GET /api/game/question?mode=difficult   (USER)
```

`easy` mode draws the question word from words the authenticated user has added to their books.  
`difficult` mode draws from all words in the system that have definitions.

The response includes multiple choice options; the number of options is controlled by the `gameOptionCount` admin setting.

### Admin Settings

```text
GET /api/admin/settings    (ADMIN)
PUT /api/admin/settings    (ADMIN)
```

Controls external dictionary API throttling and game configuration.

---

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

`AddWordToBookRequest`

```json
{
  "text": "ephemeral",
  "language": "en"
}
```

`BulkAddWordsRequest`

```json
{
  "words": ["ephemeral", "serendipity", "stoic"],
  "language": "en"
}
```

The bulk endpoint deduplicates the input list, skips words already in the book (reported as `duplicates`), and reports any individual failures in `errors`.

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
  "lastName": "User",
  "role": "USER"
}
```

`UserUpdateRequest`

```json
{
  "username": "newuser",
  "password": "",
  "email": "new@example.com",
  "firstName": "New",
  "lastName": "User",
  "role": "ADMIN"
}
```

Blank or missing password on update keeps the existing stored password.

`AdminSettingsRequest`

```json
{
  "externalApiDelayMs": 50,
  "externalApiBatchSize": 10,
  "gameOptionCount": 4,
  "gameQuestionCount": 10
}
```

`externalApiDelayMs` — milliseconds to pause between batches of external API calls (0 = no throttling).  
`externalApiBatchSize` — number of external API calls per batch before the delay is inserted.  
`gameOptionCount` — number of multiple-choice options per quiz question (minimum 2).  
`gameQuestionCount` — number of questions per game session.

---

## Game Response

`GameQuestionResponse`

```json
{
  "mode": "easy",
  "wordId": 12,
  "wordText": "stoic",
  "language": "en",
  "correctDefinitionId": 34,
  "options": [
    { "definitionId": 34, "definitionText": "Enduring pain without complaint.", "partOfSpeech": "adjective" },
    { "definitionId": 21, "definitionText": "Having a dashing appearance.", "partOfSpeech": "adjective" }
  ]
}
```

The frontend must determine correctness by comparing the user's selected `definitionId` against `correctDefinitionId`.

---

## Dictionary Lookup

When a word is created or its definitions are refreshed, `WordDefinitionService` calls the external dictionary API via `DictionaryApiClient`. Responses are mapped to `Definition` entities by `DefinitionMapper`. The lookup result status (`SUCCESS`, `NO_DEFINITIONS`, `NO_RESULTS`, `FAILED`) and HTTP status code are stored on the `Word` entity for visibility.

`ExternalApiThrottleService` applies a configurable delay between API calls to stay within external rate limits. The delay fires every `externalApiBatchSize` calls rather than on every single call.

---

## Default Admin Account

`data.sql` seeds an admin account on first startup:

```text
username: admin
password: change-me
role: ADMIN
```

Change the password immediately after first login.

---

## Frontend Integration

The React app lives in `../frontend`. During local development, CORS allows `http://localhost:5173` to call `/api/**` with credentials.

## Static Page

The backend serves `src/main/resources/static/index.html` as a lightweight API status page. Browser routes outside `/api/**` are forwarded to `index.html` for SPA routing.
