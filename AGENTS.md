# AGENTS.md

## Purpose
This file defines how AI coding agents (including Codex) should work in this repository to produce production-grade, reviewable changes.

Project context:
- Stack: Java 17, Spring Boot, MongoDB, Spring Security OAuth2 (GitHub)
- Build tool: Gradle (`./gradlew`)
- Key flows: URL shortening, OAuth login, user analytics, anonymous shorten support, mobile-safe auth token fallback

## Core Working Rules
- Prefer small, focused changes over large rewrites.
- Preserve existing behavior unless explicitly changing it.
- Keep controller/service/repository separation clear.
- Do not hardcode credentials or secrets.
- Do not remove or weaken authentication/authorization checks without explicit reason.

## Repository Conventions
- Use `rg` for search and file discovery.
- Keep code ASCII unless file already uses Unicode.
- Follow existing package structure under:
  - `src/main/java/com/jb/urlShortner/urlShortner`
- Put config in `application.properties` using env-based placeholders.
- Keep endpoint contracts backward-compatible when possible.

## Security and Secrets
- Never commit real values for:
  - `MONGODB_PW`
  - `GITHUB_CLIENT_ID`
  - `GITHUB_CLIENT_SECRET`
  - `AUTH_TOKEN_SECRET`
- Use environment variables with safe defaults only for local/test when required.
- Treat `AUTH_TOKEN_SECRET` as mandatory for production; changing it invalidates existing tokens.

## Auth-Specific Guardrails
- Current auth supports both:
  - Session-based OAuth (`Authentication` / `OAuth2User`)
  - Bearer token fallback (`Authorization: Bearer <token>`)
- For endpoints that can use either mode, resolve identity consistently through shared auth resolver logic.
- `/shorten` must work for anonymous users and still attach ownership when authenticated.

## API Behavior Expectations
- Keep these endpoint expectations intact unless requested otherwise:
  - `POST /shorten`: public
  - `GET /{hashId}`: public redirect
  - `GET /auth/me`: session or bearer token
  - `GET /urls/my`: authenticated via session or bearer token
  - `GET /metrics/summary`: authenticated via session or bearer token

## Testing and Validation
- Before finishing, run the most relevant checks possible:
  - `./gradlew test`
- If sandbox/network limitations block testing, state exactly what could not be run and why.
- When changing auth/session behavior, validate:
  - unauthenticated shorten flow
  - authenticated shorten flow
  - `/auth/me` with session and bearer token

## Change Hygiene
- Do not revert unrelated user changes.
- Avoid destructive git commands unless explicitly requested.
- Update `SESSION_CHANGES_SUMMARY.md` when behavior/API/auth semantics change.
- Keep explanations concise and include file-level references in summaries.

## Preferred Delivery Format
- Provide:
  1. What changed
  2. Why it changed
  3. What was validated (or blocked)
  4. Any required environment/config updates
