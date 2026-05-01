# SpringBootApps

Collection of Spring Boot applications and experiments.

## Projects

### `gateway3000`

Simple Spring API Gateway project.

### `LexiconLairMVC`

Original server-rendered Spring MVC version of LexiconLair. This project remains intact as the MVC/JSP reference implementation.

### `LexiconLair`

React SPA version of LexiconLair.

```text
LexiconLair/
  backend/   Spring Boot REST API
  frontend/  React/Vite SPA
```

The backend exposes `/api/**` JSON endpoints, uses DTOs at the REST boundary, and keeps JPA entities inside the persistence layer. The frontend is currently scaffolded and still uses mock data in several views while API integration is completed.

### `template`

Reference backend/frontend project used as a working pattern for the React SPA and Spring Boot REST API structure.
