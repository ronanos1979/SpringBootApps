# LexiconLair Backend Design

LexiconLair backend is a Spring Boot REST API for a React single-page application. It manages books, authors, words and their definitions, a quiz game, and administrative settings.

## Package Layout

The backend uses feature-oriented packages:

```text
com.ronanos.lexiconlair
  admin/
    domain/         AdminSettings JPA entity
    dto/            AdminSettingsRequest, AdminSettingsResponse
    persistence/    AdminSettingsRepository
    service/        AdminSettingsService, ExternalApiThrottleService
    web/            AdminSettingsController
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
  bookword/
    domain/         BookWord JPA entity (book ↔ word join with audit fields)
    dto/            AddWordToBookRequest, BulkAddWordsRequest/Response,
                    BookWordResponse, WordSearchResult, DefinitionSummary
    persistence/    BookWordRepository
    web/            BookWordController
  definition/
    domain/
    dto/
    persistence/
    web/
  dictionary/
    client/         DictionaryApiClient, DictionaryLookupResult
    dto/            DictionaryWordDTO
    mapper/         DefinitionMapper
  game/
    dto/            GameQuestionResponse, GameOptionResponse
    service/        GameService
    web/            GameController
  user/
    domain/
    dto/
    persistence/
    service/        UserSchemaInitializer
    web/            AuthController, UserController
  word/
    domain/
    dto/
    persistence/
    service/        WordDefinitionService
    web/            WordController
  config/           ApiExceptionHandler, SpaController, WebConfig
  security/         SpringSecurityConfiguration, UserDetailsServiceImpl
```

## Layers

- `domain`: JPA entities and persistence model.
- `dto`: REST request/response contracts.
- `persistence`: Spring Data JPA repositories.
- `service`: business workflows that go beyond simple CRUD.
- `web`: REST controllers under `/api/**`.
- `security`: Spring Security session authentication and database-backed user lookup.
- `config`: global exception handling, CORS configuration, and SPA route forwarding.

## REST Boundary

REST controllers do not expose JPA entities directly. Controllers accept request DTOs, map them to domain entities internally, and return response DTOs.

This keeps:

- API responses stable when persistence changes.
- Lazy JPA relationships out of the JSON contract.
- Sensitive fields (password hashes) out of responses.
- Request validation specific to each operation.

Error responses are normalised by `ApiExceptionHandler`, which translates `ResponseStatusException` and `MethodArgumentNotValidException` into a consistent JSON envelope:

```json
{
  "timestamp": "2026-05-26T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Word not found",
  "path": "/api/words/99"
}
```

## Authentication and Authorisation

Authentication is session based:

```text
POST /api/auth/login   — form parameters: username, password
GET  /api/auth/me      — returns the current authenticated user
POST /api/auth/logout  — invalidates the session
```

`UserDetailsServiceImpl` loads users from `UserRepository`. Spring Security stores successful authentication in the HTTP session. The React frontend must include credentials on API calls.

Two roles control access: `USER` and `ADMIN`. The security filter chain in `SpringSecurityConfiguration` defines the rule set:

- All authenticated users can read books and authors, add books and authors, manage their own book-word associations, and play the game.
- Only `ADMIN` users can access word, definition, user, and admin settings endpoints; update or delete books and authors; and view book details.

## BookWord Module

`BookWord` is the join entity between a `Book` and a `Word`. It records which user added the word (`createdBy`) and when. This drives the `easy` game mode, which limits question candidates to words the current user has personally added.

The `BookWordController` at `/api/books/{bookId}/words` supports:

- Listing a book's words (filtered to the current user by default via `mine=true`).
- Adding a single word, delegating to `WordDefinitionService` to find or create the word and fetch its definitions.
- Bulk-adding up to 500 words in one request, with per-word error isolation; the response reports `added`, `duplicates`, and `errors` separately.
- Removing a word from a book.

## Word and Definition Lifecycle

Creating or bulk-adding a word goes through `WordDefinitionService.findOrCreateWordWithDefinitions`, which:

1. Looks up the word by normalised text and language.
2. If found, returns the existing entity without calling the dictionary API.
3. If not found, saves the new `Word`, then calls `refreshDefinitions`.

`refreshDefinitions`:

1. Calls `ExternalApiThrottleService.beforeExternalApiCall()` to apply any configured delay.
2. Calls `DictionaryApiClient.lookupEntries(text, language)`.
3. Maps results to `Definition` entities via `DefinitionMapper`.
4. Deletes existing definitions for the word and saves the new set.
5. Updates `definitionLookupStatus`, `definitionLookupHttpStatus`, `definitionLookupMessage`, and `definitionLookupAt` on the `Word` entity.

Possible lookup statuses: `SUCCESS`, `NO_DEFINITIONS`, `NO_RESULTS`, `FAILED`.

Admins can trigger individual or bulk definition refreshes via:

- `POST /api/words/{id}/definitions/refresh`
- `POST /api/words/definitions/refresh-missing`

## External API Throttling

`ExternalApiThrottleService` prevents the application from hammering the external dictionary API. Before every external call, it reads the current `AdminSettings` and applies a `Thread.sleep(externalApiDelayMs)` once every `externalApiBatchSize` calls. Both values are configurable at runtime via `PUT /api/admin/settings` without restarting the application.

## Game Module

`GameService.nextQuestion(mode, userId)` produces a quiz question:

- `easy`: candidate words are those the current user has added to any book via `BookWordRepository.findDistinctWordsByCreatedBy(userId)`.
- `difficult`: candidate words are all words in the system that have at least one definition via `WordRepository.findWordsWithDefinitions()`.

From the candidate pool, words with no definitions are excluded. One word is chosen at random, one of its definitions is the correct answer, and decoy definitions from other words fill the remaining option slots. The number of options is read from `AdminSettings.gameOptionCount` (minimum 2).

The response includes `correctDefinitionId` so the frontend can verify the user's selection client-side without a round trip.

## Admin Settings Module

`AdminSettings` is a singleton entity (id = 1). `AdminSettingsService.getSettings()` creates the record with defaults on first access if it does not yet exist. Settings control:

- `externalApiDelayMs` — delay in ms applied between batches of external API calls.
- `externalApiBatchSize` — batch size before the delay fires.
- `gameOptionCount` — multiple-choice options per quiz question.
- `gameQuestionCount` — questions per game session.

## SPA Support

The backend provides:

- CORS for `http://localhost:5173` during local Vite development (`WebConfig`).
- `src/main/resources/static/index.html` as a backend status page.
- SPA route fallback via `SpaController`: browser routes outside `/api/**` forward to `index.html`.

## Testing

Tests use:

- `@SpringBootTest` + `@AutoConfigureMockMvc` for full-stack integration tests (auth session flow, static page).
- `@WebMvcTest` + MockMvc for REST controllers and Spring Security rules; each controller slice imports `SpringSecurityConfiguration` to test real authorization rules.
- Mockito for unit-level service tests (`GameService`, `WordDefinitionService`, `AdminSettingsService`, `ExternalApiThrottleService`, `UserDetailsServiceImpl`).
- `@DataJpaTest` with H2 for repository tests.
- Focused DTO tests for mapping correctness.
