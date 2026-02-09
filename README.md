# Mini Translation App - Spring Boot + Next.js
A small full-stack project that models a translation workflow end-to-end:
- create a translation request
- translate it using a real provider (DeepL)
- track status and history
- simulate “delivery” of the result to a partner system

The goal is to show how you build a clean workflow around translation: persistence, state transitions, validation, and consistent API behavior - not just “translate a string”.

---

## Tech stack

**Backend**
- Java 17
- Spring Boot 3
- Spring Web + Spring Data JPA
- H2 (in-memory database)

**Frontend**
- Next.js (App Router) + TypeScript
- Client-side `fetch`
- `NEXT_PUBLIC_API_URL` for backend URL

**Translation provider**
- DeepL API (Free endpoint supported)

---

## How it works (high level)

1. The UI sends text + language pair to the backend.
2. The backend stores it as a record with status `CREATED`.
3. The backend calls DeepL to translate and updates the record:
   - success → `TRANSLATED` + `translatedText`
   - failure → `FAILED`
4. The UI shows translation result and keeps a history list.
5. “Deliver” simulates sending the final payload to a partner system.

---

## API overview

### Health
- `GET /health`
- Response: `{ "status": "ok" }`

### Requests (stored in DB)
- `POST /tickets`
  - Create a translation request
- `GET /tickets`
  - List all requests
  - Optional filter: `?status=CREATED|TRANSLATED|FAILED`
- `GET /tickets/{id}`
  - Get a request by id

### Translation
- `POST /tickets/{id}/translate`
  - Calls DeepL and updates the record
  - On success:
    - `status = TRANSLATED`
    - `translatedText` set
    - `translatedAt` set
  - On provider failure:
    - `status = FAILED`
    - returns `502 Bad Gateway`
- `GET /tickets/{id}/translation`
  - Minimal status view:
  ```json
  { "id": 1, "status": "TRANSLATED", "translatedText": "..." }
  ```

### Delivery simulation
- `POST /tickets/{id}/deliver`
  - Only allowed when `status == TRANSLATED`
  - Returns:
  ```json
  {
    "delivered": true,
    "ticketId": 1,
    "deliveredAt": "2026-02-06T12:34:56",
    "payload": {
      "originalText": "...",
      "translatedText": "...",
      "sourceLang": "...",
      "targetLang": "..."
    }
  }
  ```

---

## Error format

The backend returns consistent JSON errors:
```json
{
  "error": "BAD_REQUEST | NOT_FOUND | CONFLICT | BAD_GATEWAY",
  "message": "Human-readable explanation",
  "timestamp": "..."
}
```

Common cases:
400 - validation / malformed JSON  
404 - id not found  
409 - invalid workflow state (e.g. deliver before translated)  
502 - translation provider error (DeepL)

---

## Run locally

### Prerequisites
- Java 17
- Maven
- Node.js 18+ recommended

This repo has two parts:
- Backend (Spring Boot)
- Frontend (Next.js)

If your frontend lives in a folder like `translation-ui/`, use that folder name in the commands below.

---

## 1) Backend (Spring Boot)

From the backend folder (where `pom.xml` is):
```
mvn spring-boot:run
```

Backend URL:
```
http://localhost:8080
```

H2 console (optional):
```
http://localhost:8080/h2-console
```

---

## 2) Configure real translation (DeepL)

### Important: who needs a DeepL key?
- When running locally: you need your own `DEEPL_API_KEY`.
- When this app is deployed: users won’t need keys (the backend holds the key server-side).

### Environment variables (backend)
Set these before running Spring Boot (or via IntelliJ Run Configuration):
```
DEEPL_API_KEY=your_key_here
DEEPL_BASE_URL=https://api-free.deepl.com/v2
DEEPL_TIMEOUT_MS=8000
```

Notes:
- The Free endpoint uses `https://api-free.deepl.com/v2`.
- If the provider fails (invalid key, rate limit, quota, etc.), `/translate` returns 502 and the request is stored as `FAILED`.

---

## 3) Frontend (Next.js)

Go into the frontend folder and install dependencies:
```
cd translation-ui
npm install
```

Create `.env.local` in the frontend folder:
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

Run the dev server:
```
npm run dev
```

Frontend URL:
```
http://localhost:3000
```

---

## Quick test checklist (local)

Open the UI:
```
http://localhost:3000
```

Translate something:
- Source: `en`
- Target: `pt`
- Text: `Hello, how are you?`

Confirm it’s real:
- The output should look like real Portuguese (not `[pt] ...`)

Open history:
- You should see the entry in the list

Click Deliver:
- You should see a JSON payload showing the delivered translation

---

## Deployment (coming soon)

A hosted demo is planned so anyone can try it via a public URL without running it locally.

Expected setup:
- Frontend hosted on Vercel
- Backend hosted on a Java-friendly platform (e.g. Render)
- DeepL key stored server-side (so users don’t need accounts)

When deployed, this README will be updated with:
- live demo link
- exact environment variables used for hosting
- CORS configuration notes

---

## Screenshots (placeholders)

-will add soon

## Future improvements

- Async translation (queue + worker)
- Retries for provider failures
- Caching / translation memory
- Pagination and search for history
- Authentication (per-user history)
- Postgres for production persistence
- Webhook callbacks for delivery
- Better observability (structured logs, metrics)

---

## Credits

Translation powered by DeepL API (https://www.deepl.com/)
