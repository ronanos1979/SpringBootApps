# Lexicon Lair Backend

Spring Boot backend for Lexicon Lair. It exposes a REST API under `/api/**`, uses Spring Security session-cookie authentication, serves a small static landing page at `/`, and can also serve a built React frontend from `src/main/resources/static`.

The React frontend currently lives at:

```text
c:\dev\git\ReactExamples\frontend
```

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL by default
- Gradle

## Running Locally

Start the backend:

```powershell
cd c:\dev\git\SpringBootApps\backend
.\gradlew.bat bootRun
```

The backend runs on:

```text
http://localhost:8080
```

Browser landing page:

```text
http://localhost:8080
```

REST API base:

```text
http://localhost:8080/api
```

## Authentication Flow

The app currently uses Spring Security session authentication, not JWT.

Login:

```http
POST /api/auth/login
Content-Type: application/x-www-form-urlencoded

username=admin&password=change-me
```

On successful login, Spring creates a server-side session and returns a `JSESSIONID` cookie. Postman and browsers then send that cookie on later requests.

Check the logged-in user:

```http
GET /api/auth/me
Cookie: JSESSIONID=...
```

Logout:

```http
POST /api/auth/logout
Cookie: JSESSIONID=...
```

## API Endpoints

Public:

```text
POST /api/auth/login
```

Authenticated:

```text
GET    /api/auth/me
POST   /api/auth/logout
GET    /api/users
POST   /api/users
PUT    /api/users/{id}
DELETE /api/users/{id}
```

Example create-user request:

```json
{
  "username": "testuser",
  "password": "password123",
  "email": "testuser@example.com",
  "firstName": "Test",
  "lastName": "User"
}
```

## Postman Test Flow

1. `POST http://localhost:8080/api/auth/login`

   Body type: `x-www-form-urlencoded`

   ```text
   username = admin
   password = change-me
   ```

2. Confirm Postman stored the `JSESSIONID` cookie.

3. Call:

   ```text
   GET http://localhost:8080/api/auth/me
   GET http://localhost:8080/api/users
   ```

4. Logout:

   ```text
   POST http://localhost:8080/api/auth/logout
   ```

5. Calling `GET /api/users` again should return `401 Unauthorized`.

## React Frontend Development

Run the React app separately during development:

```powershell
cd c:\dev\git\ReactExamples\frontend
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

The backend CORS config allows `http://localhost:5173` and allows credentials so the browser can send the `JSESSIONID` cookie.

The Vite frontend also has a development proxy for `/api`, so frontend code can call:

```text
/api/auth/login
/api/users
```

instead of hard-coding `http://localhost:8080`.

## Static Content

The backend serves:

```text
src/main/resources/static/index.html
```

at:

```text
http://localhost:8080
http://localhost:8080/index.html
```

`SpaController` forwards non-API browser routes to `index.html` for React Router support. It excludes `/api/**`, `/assets/**`, `/favicon*`, and `/index.html` to avoid forwarding API/static requests and to avoid an `index.html` forward loop.

## Production Frontend Build

To serve the React app from Spring Boot:

1. Build the React app:

   ```powershell
   cd c:\dev\git\ReactExamples\frontend
   npm run build
   ```

2. Copy the contents of the React `dist` folder into:

   ```text
   c:\dev\git\SpringBootApps\backend\src\main\resources\static
   ```

3. Build and run the backend jar:

   ```powershell
   cd c:\dev\git\SpringBootApps\backend
   .\gradlew.bat bootJar
   java -jar build\libs\backend-0.0.1-SNAPSHOT.jar
   ```

## Database

The default active profile is PostgreSQL:

```properties
spring.profiles.active=${SPRING_PROFILES_ACTIVE:postgresql}
```

Default connection settings:

```properties
spring.datasource.url=${BACKEND_PG_URL:jdbc:postgresql://localhost:5432/backend}
spring.datasource.username=${BACKEND_PG_USERNAME:backend-user}
spring.datasource.password=${BACKEND_PG_PASSWORD:}
```

Create the PostgreSQL database and user:

```sql
CREATE DATABASE backend;
CREATE USER "backend-user" WITH PASSWORD 'change-me';
GRANT ALL PRIVILEGES ON DATABASE backend TO "backend-user";

\c backend

GRANT USAGE ON SCHEMA public TO "backend-user";
GRANT CREATE ON SCHEMA public TO "backend-user";
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "backend-user";
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO "backend-user";
```

The app seeds an admin user from:

```text
src/main/resources/data.sql
```

Current development login:

```text
username: admin
password: change-me
```

The stored password must be a BCrypt hash. Do not store the plain-text password in the database.

## MySQL Notes

MySQL support is present through the MySQL profile and connector, but PostgreSQL is the default.

Example local setup:

```sql
CREATE DATABASE backend;
CREATE USER 'backend-user'@'localhost';
GRANT ALL PRIVILEGES ON backend.* TO 'backend-user'@'localhost';
ALTER USER 'backend-user'@'localhost' IDENTIFIED BY 'change-me';
```

Run with:

```powershell
$env:SPRING_PROFILES_ACTIVE='mysql'
.\gradlew.bat bootRun
```

## Tests

Run backend tests:

```powershell
cd c:\dev\git\SpringBootApps\backend
.\gradlew.bat test
```

The test suite covers:

- user controller unit behavior
- authenticated and unauthenticated `/api/users` behavior
- auth controller behavior
- user details loading
- Spring Security login/logout/API protection
- CORS configuration
- static landing page content
- SPA fallback exclusions

Run frontend tests:

```powershell
cd c:\dev\git\ReactExamples\frontend
npm run test
```

## Important Notes

- `/api/**` requires authentication except `/api/auth/login`.
- The app uses `JSESSIONID` cookies, so frontend calls must include credentials.
- Postman stores and resends cookies automatically unless disabled.
- CORS matters in the browser, not in Postman.
- JWT is not currently implemented.
