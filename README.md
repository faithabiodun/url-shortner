# URL Shortener

Convert a long URL to a short code. Built with Java 17, Spring Boot, PostgreSQL.

## Run it (easiest — Docker, no Java/Postgres install needed)

```powershell
cd C:\Users\User\Documents\url-shortener
docker compose up --build
```

Open another terminal to test:

```powershell
curl.exe -X POST http://localhost:8080/shorten -H "Content-Type: application/json" -d '{"url":"https://www.example.com/some/long/url"}'
```

Stop everything:

```powershell
docker compose down
```

Useful checks for a beginner:

```powershell
docker ps                 # see my running containers
docker compose logs -f    # watch my app logs (Ctrl+C to exit)
docker compose down -v    # stop + delete my DB data (fresh start)
```

## Run it without Docker (IntelliJ)

1. Create Postgres DB `urlshortener` with user `urluser` / password `urlpass`.
2. Open `pom.xml` in IntelliJ as a project, wait for Maven import.
3. Run `UrlShortenerApplication.java`.
4. Test `POST http://localhost:8080/shorten`.

## API

| Method | URL | Body | Success |
|---|---|---|---|
| POST | `/shorten` | `{"url":"https://..."}` | 201 + `shortCode` |
| GET | `/shorten/{code}` | — | 200 (visit counted +1) |
| PUT | `/shorten/{code}` | `{"url":"https://..."}` | 200 |
| GET | `/shorten/{code}/stats` | — | 200 + `accessCount` (not counted) |
| DELETE | `/shorten/{code}` | — | 204 |

Bad URL → 400, missing code → 404. Postman collection: `postman_collection.json`.

## How it fits together

`Controller (/shorten)` → `Service (rules + counting)` + `ShortCodeGenerator (abc123)` → `Repository` → `Postgres (short_urls table)`. Exceptions become clean JSON via `GlobalExceptionHandler`.
