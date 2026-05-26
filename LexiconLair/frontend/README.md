# LexiconLair Frontend

React/Vite SPA for LexiconLair — a vocabulary management and quiz application.

## Stack

- React 19
- React Router v7
- Bootstrap 5
- Vite
- ESLint
- Vitest + React Testing Library

## Run

```powershell
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173`.

## Build

```powershell
npm run build
```

The production build outputs to `dist/`. The Spring Boot backend serves the built assets.

## Test

```powershell
npm run test
```

## Lint

```powershell
npm run lint
```

## Backend

The Spring Boot REST backend lives in `../backend` and runs on `http://localhost:8080`.

All API calls go through `src/api/client.js` with `credentials: 'include'` so the browser sends the session cookie:

```js
fetch('/api/auth/me', { credentials: 'include' })
```

During local development, the backend CORS config allows `http://localhost:5173`.

## Authentication

`AuthContext` (`src/auth/AuthContext.jsx`) holds the current user and exposes `login` / `logout` actions. It checks `GET /api/auth/me` on app load to restore session state.

`RequireAuth` wraps protected routes and redirects unauthenticated users to `/login`.

## Routes

```text
/                         Welcome / landing page
/login                    Login form

/authors                  List all authors
/authors/add              Add a new author

/books                    List all books
/books/add                Add a new book
/books/:id                Book detail — words in this book, add/remove words

/words                    List all words (ADMIN)
/words/add                Add a word (ADMIN)
/words/search             Search words across all books and standalone words (ADMIN)

/definitions              List all definitions (ADMIN)

/users                    User management (ADMIN)
/users/add                Add a new user (ADMIN)

/admin/settings           Admin settings — throttling and game configuration (ADMIN)

/game                     Vocabulary quiz game
```

## API Shape

The backend exposes DTO-based JSON APIs:

```text
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout

GET  /api/authors
GET  /api/authors/{id}
POST /api/authors
PUT  /api/authors/{id}
DELETE /api/authors/{id}

GET  /api/books
GET  /api/books/{id}
POST /api/books
PUT  /api/books/{id}
DELETE /api/books/{id}

GET    /api/books/{bookId}/words
POST   /api/books/{bookId}/words
POST   /api/books/{bookId}/words/bulk
DELETE /api/books/{bookId}/words/{bookWordId}

GET  /api/words
GET  /api/words/{id}
GET  /api/words/search?q=<text>
GET  /api/words/without-definitions
POST /api/words
POST /api/words/{id}/definitions/refresh
POST /api/words/definitions/refresh-missing
PUT  /api/words/{id}
DELETE /api/words/{id}

GET  /api/definitions
GET  /api/definitions/{id}
POST /api/definitions
PUT  /api/definitions/{id}
DELETE /api/definitions/{id}

GET  /api/users
GET  /api/users/{id}
POST /api/users
PUT  /api/users/{id}
DELETE /api/users/{id}

GET /api/game/question?mode=easy
GET /api/game/question?mode=difficult

GET /api/admin/settings
PUT /api/admin/settings
```

## Game

The `/game` page calls `GET /api/game/question?mode=<easy|difficult>` to receive a multiple-choice vocabulary question. The response includes `correctDefinitionId` and an `options` array. The frontend evaluates the user's selection locally without a further API call.

- **Easy mode**: question words come from words the current user has added to their books.
- **Difficult mode**: question words come from all words in the system that have definitions.

## Admin Settings

The `/admin/settings` page allows admins to configure:

- `externalApiDelayMs` — throttle delay between batches of external dictionary API calls.
- `externalApiBatchSize` — number of calls per batch before the delay fires.
- `gameOptionCount` — number of multiple-choice options per question.
- `gameQuestionCount` — number of questions per game session.
