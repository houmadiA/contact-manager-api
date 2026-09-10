# Contact Manager

Contact management: list with sorting/filtering/paging, view, create, update, delete with
confirmation, CSV import/export, and sign in/out.

- `contact-manager-api` — Spring Boot 4 / Java 21, PostgreSQL
- `contact-manager-frontend` — Angular 21, Angular Material, NgRx

## Running

```bash
cd contact-manager-api
docker compose up --build
```

- App: http://localhost:4200
- API: http://localhost:8080/api
- Swagger: http://localhost:8080/swagger-ui.html

Log in with **demo / demo1234**. Flyway creates the schema and seeds 50 contacts on first start.

To work on one side at a time:

```bash
in contact-manager-api => docker compose up -d postgres

in contact-manager-api => mvn spring-boot:run
in contact-manager-frontend => npm install && npm start
```

## Sessions

Sign-in returns no token in its body: the API sets the access and refresh tokens as `HttpOnly`,
`Secure`, `SameSite=Strict` cookies, so no script can read them. Browsers accept `Secure` cookies
over `http://localhost`; behind a plain-HTTP host, set `CONTACT_MANAGER_COOKIE_SECURE=false`, and
serve the app and the API from the same site to keep `SameSite=Strict`.
