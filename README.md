# URL Shortener

[![CI](https://github.com/faithabiodun/url-shortner/actions/workflows/ci.yml/badge.svg)](https://github.com/faithabiodun/url-shortner/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-blue)
![Spring Boot 3.3.5](https://img.shields.io/badge/Spring_Boot-3.3.5-green)
![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)

Turn long URLs into short links. Open `http://localhost:8080/` for the visual UI, or use the JSON API directly. Built with Java 17, Spring Boot, PostgreSQL, Docker.

## Features

- Web UI at `/` — paste a link, optional custom alias + expiry, copy/open result, live visit count
- `POST /shorten` → random 6-char code, or your own alias (`customCode`), optional `expiresAt`
- `GET /{code}` → `302` redirect to the original URL (visit counted in the background)
- `GET /shorten/{code}` → JSON lookup (counts +1)
- `GET /shorten/{code}/stats` → visit count (never increments)
- `PUT /shorten/{code}` → change destination, `DELETE /shorten/{code}` → remove
- Fast under load: Caffeine cache serves repeat redirects without DB reads, counting is async so the `302` never waits on a write
- Clean errors as JSON: bad URL → `400`, taken alias → `409`, expired link → `410`, missing code → `404`
- Swagger UI at `/swagger-ui.html`, health at `/actuator/health`

## Run it (easiest — Docker, no Java/Postgres install needed)

```powershell
docker compose up --build
```

Open `http://localhost:8080/` in the browser, or test the API in another terminal:

```powershell
# 1. create (random code)
curl.exe -X POST http://localhost:8080/shorten -H "Content-Type: application/json" -d '{"url":"https://www.example.com/"}'

# 2. create with custom alias + 7-day expiry
curl.exe -X POST http://localhost:8080/shorten -H "Content-Type: application/json" -d '{"url":"https://spring.io/","customCode":"my-link1","expiresAt":"2026-12-31T23:59:59Z"}'

# 3. redirect (see 302 + Location header)
curl.exe -v http://localhost:8080/PASTE_CODE_HERE

# 4. stats (waits a moment — counting is async)
curl.exe http://localhost:8080/shorten/PASTE_CODE_HERE/stats
```

Stop:

```powershell
docker compose down
docker compose down -v    # + delete DB data (fresh start)
docker compose logs -f    # watch app logs
```

## Run it without Docker (IntelliJ)

1. Create Postgres DB `urlshortener` with user `urluser` / password `urlpass`.
2. Open `pom.xml` in IntelliJ as a project, wait for Maven import.
3. Run `UrlShortenerApplication.java`.
4. Open `http://localhost:8080/` or test `POST http://localhost:8080/shorten`.

## API

| Method | URL | Body | Success |
|---|---|---|---|
| POST | `/shorten` | `{"url":"https://...","customCode?":"my-link1","expiresAt?":"2026-12-31T23:59:59Z"}` | 201 + `shortCode` |
| GET | `/{code}` | — | 302 redirect to `url` (counted in background) |
| GET | `/shorten/{code}` | — | 200 (counted in background) |
| PUT | `/shorten/{code}` | `{"url":"https://..."}` | 200 |
| GET | `/shorten/{code}/stats` | — | 200 + `accessCount` (never counted) |
| DELETE | `/shorten/{code}` | — | 204 |

`customCode`: 4–20 chars, letters/numbers/`_`/`-`. Taken → `409`. Past `expiresAt` → `410 Gone`.
Bad URL → `400`, missing code → `404`. Postman collection: `postman_collection.json` (turn OFF `Automatically follow redirects` to see the 302).

Example:

```json
// POST /shorten
{"url": "https://spring.io/guides/gs/rest-service/"}
// → 201 {"id":1,"url":"https://...","shortCode":"aB3x9Z",...}

// GET /aB3x9Z → 302 Found, Header: Location: https://spring.io/...
```

## How it fits together

```
Browser GET /{code}
  → RedirectController (302 + Location header)
  → ShortUrlService.get()
      → LinkResolver.resolve() (Caffeine cache, DB only on miss)
      → VisitCounter.countAsync() (atomic UPDATE on a background thread)
  → Postgres (short_urls table)

UI GET /
  → web/HomeController → static/index.html + app.js (calls the API above)

API POST/PUT/DELETE /shorten/...
  → ShortUrlController (/shorten)
  → ShortUrlService + ShortCodeGenerator (abc123)
  → Repository → Postgres
  → exceptions → GlobalExceptionHandler (clean JSON, no whitelabel page)
```

Project layout: `url/` (controllers, `Service`, `Repository`, `ShortUrl`, `ShortCodeGenerator`, `LinkResolver`, `VisitCounter`, `dto/`), `web/` (UI controller), `config/` (`OpenApiConfig`, `CacheConfig`), `exception/` (`GlobalExceptionHandler`, `ApiError`), `src/main/resources/static/` (the UI).

## Tests + CI

```powershell
./mvnw test   # 14 tests: service unit (Mockito) + API integration (MockMvc + H2)
```

GitHub Actions (`.github/workflows/ci.yml`) runs on every push to `main`:
1. `mvn -B test` on Java 17
2. `docker build` to prove the Dockerfile still builds
