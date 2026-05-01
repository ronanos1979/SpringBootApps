# LexiconLair Backend Design

LexiconLair backend is a Spring Boot REST API for a React single-page application.

## Package Layout

The backend uses feature-oriented packages:

```text
com.ronanos.lexiconlair
  author/
    domain/
    dto/
    persistence/
    web/
  book/
    domain/
    dto/
    persistence/
    web/
  definition/
    domain/
    dto/
    persistence/
    web/
  dictionary/
    client/
    dto/
    mapper/
  user/
    domain/
    dto/
    persistence/
    web/
  word/
    domain/
    dto/
    persistence/
    service/
    web/
  config/
  security/
```

## Layers

- `domain`: JPA entities and persistence model.
- `dto`: REST request/response contracts.
- `persistence`: Spring Data JPA repositories.
- `service`: business workflows that go beyond simple CRUD.
- `web`: REST controllers under `/api/**`.
- `security`: Spring Security session authentication and database-backed user lookup.
- `config`: CORS configuration and SPA route forwarding.

## REST Boundary

REST controllers do not expose JPA entities directly. Controllers accept request DTOs, map them to domain entities internally, and return response DTOs.

This keeps:

- API responses stable when persistence changes.
- Lazy JPA relationships out of the JSON contract.
- sensitive fields, such as password hashes, out of responses.
- request validation specific to each operation.

## Authentication

Authentication is session based:

```text
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
```

`UserDetailsServiceImpl` loads users from `UserRepository`. Spring Security stores successful authentication in the HTTP session. The React frontend must include credentials on API calls.

## API Surface

```text
/api/authors
/api/books
/api/words
/api/definitions
/api/users
```

Each entity API supports:

```text
GET collection
GET item by id
POST create
PUT update
DELETE item by id
```

## Dictionary Lookup

Creating a word goes through `WordDefinitionService`, which saves the word and fetches definitions from the external dictionary API. Dictionary API responses are represented by `dictionary/dto/DictionaryWordDTO` and mapped into `Definition` entities.

## SPA Support

The backend provides:

- CORS for `http://localhost:5173` during local Vite development.
- `src/main/resources/static/index.html` as a backend status page.
- SPA route fallback for browser routes outside `/api/**`.

## Testing

Tests use:

- MockMvc for REST controllers and Spring Security behavior.
- Mockito for unit-level service/config/auth behavior.
- focused domain tests for entity behavior.
