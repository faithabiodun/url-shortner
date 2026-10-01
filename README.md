# URL Shortener

[![CI](https://github.com/faithabiodun/url-shortner/actions/workflows/ci.yml/badge.svg)](https://github.com/faithabiodun/url-shortner/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-blue)
![Spring Boot 3.3.5](https://img.shields.io/badge/Spring_Boot-3.3.5-green)
![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)

Convert a long URL to a short code, redirect with `302`, and track visits. Built with Java 17, Spring Boot, PostgreSQL, Docker.

## Features

- `POST /shorten` → create 6-char code (`abc123`)
- `GET /{code}` → `302` redirect to original URL (counts +1 visit)
- `GET /shorten/{code}` → JSON lookup (counts +1)
- `GET /shorten/{code}/stats` → visit count (no increment)
- `PUT /shorten/{code}` → update destination, `DELETE` → remove
- Validation → `400`, missing code → `404` JSON via `GlobalExceptionHandler`

## Run it (easiest — Docker, no Java/Postgres install needed)

```powershell
docker compose up --build
```

Test in another terminal:

```powershell
# 1. create
curl.exe -X POST http://localhost:8080/shorten -H "Content-Type: application/json" -d '{"url":"https://www.example.com/"}'

# 2. redirect (see 302 + Location header)
curl.exe -v http://localhost:8080/PASTE_CODE_HERE

# 3. stats
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
4. Test `POST http://localhost:8080/shorten`.

## API

| Method | URL | Body | Success |
|---|---|---|---|
| POST | `/shorten` | `{"url":"https://..."}` | 201 + `shortCode` |
| GET | `/{code}` | — | 302 redirect to `url` (visit counted +1) |
| GET | `/shorten/{code}` | — | 200 (visit counted +1) |
| PUT | `/shorten/{code}` | `{"url":"https://..."}` | 200 |
| GET | `/shorten/{code}/stats` | — | 200 + `accessCount` (not counted) |
| DELETE | `/shorten/{code}` | — | 204 |

Bad URL → 400, missing code → 404. Postman collection: `postman_collection.json` (turn OFF `Automatically follow redirects` to see the 302).

Example create → redirect:

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
  → ShortUrlService.get() (rules + accessCount++)
  → ShortUrlRepository.findByShortCode()
  → Postgres (short_urls table)

API POST/PUT/DELETE /shorten/...
  → ShortUrlController (/shorten)
  → ShortUrlService + ShortCodeGenerator (abc123)
  → Repository → Postgres
  → exceptions → GlobalExceptionHandler (clean JSON, no whitelabel page)
```

Project layout: `url/` (`Controller`, `Service`, `Repository`, `ShortUrl`, `ShortCodeGenerator`, `dto/`), `exception/` (`GlobalExceptionHandler`, `ApiError`), `RedirectController.java` for public redirects.

## Tests + CI

```powershell
./mvnw test   # 6 tests: service unit (Mockito) + Spring context (H2)
```

GitHub Actions (`.github/workflows/ci.yml`) runs on every push to `main`:
1. `mvn -B test` on Java 17
2. `docker build` to prove the Dockerfile still builds
