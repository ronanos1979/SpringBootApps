# LexiconLair Frontend

React/Vite frontend for the LexiconLair SPA.

## Stack

- React 19
- React Router
- Vite
- Bootstrap
- ESLint

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

## Lint

```powershell
npm run lint
```

## Backend

The Spring Boot REST backend lives in `../backend` and runs on `http://localhost:8080`.

The frontend should call backend APIs under `/api/**` with credentials enabled so the browser sends the session cookie:

```js
fetch('/api/auth/me', {
  credentials: 'include'
})
```

During local development, the backend CORS config allows `http://localhost:5173`.

## Current State

The SPA routes and pages are scaffolded. Some pages still use `src/data/mockData.js` while API integration is completed.

Primary routes:

```text
/
/login
/authors
/authors/add
/authors/update/:id
/books
/books/add
/books/update/:id
/words
/words/add
/words/update/:id
/users
/users/add
/users/update/:id
/definitions
```

## API Shape

The backend exposes DTO-based JSON APIs:

```text
/api/authors
/api/books
/api/words
/api/definitions
/api/users
/api/auth/login
/api/auth/me
/api/auth/logout
```
