# AIChatBot

This repository contains a two-project starter setup for an AI chatbot application:

- `frontend/`: a Vite + React client with a chat-oriented layout and a backend connectivity check.
- `backend/`: a Spring Boot 3 application using Java 17+, Spring Web, Spring Validation, and Spring AI.

## Project structure

```text
.
├── backend/
└── frontend/
```

## Frontend

### Prerequisites

- Node.js 18+
- npm 9+

### Environment variables

Create `frontend/.env` from `frontend/.env.example` and adjust as needed:

```bash
VITE_API_BASE_URL=http://localhost:8080
```

### Run locally

```bash
cd frontend
npm install
npm run dev
```

The app starts on `http://localhost:5173` by default and calls `GET /api/health` on the backend during startup.

### Build

```bash
cd frontend
npm install
npm run build
```

## Backend

### Prerequisites

- Java 17+
- Maven 3.9+

### Environment variables

The backend supports these environment variables:

```bash
GEMINI_API_KEY=your_gemini_api_key
GEMINI_MODEL=gemini-1.5-flash
GEMINI_BASE_URL=https://generativelanguage.googleapis.com
APP_CORS_ALLOWED_ORIGIN=http://localhost:5173
SERVER_PORT=8080
```

`GEMINI_API_KEY` is required for `POST /api/chat`. The application reads it from Spring configuration or the `GEMINI_API_KEY` environment variable only, so secrets are never hardcoded in the controller.

### Configure Gemini locally

Set the API key before starting the backend:

```bash
export GEMINI_API_KEY=your_gemini_api_key
export GEMINI_MODEL=gemini-1.5-flash
cd backend
mvn spring-boot:run
```

You can also override the values in `backend/src/main/resources/application.yml` with environment variables when running from your IDE.

### Configure Gemini in deployment

Provide `GEMINI_API_KEY` as an environment variable or secret in your deployment platform, plus optional `GEMINI_MODEL` and `GEMINI_BASE_URL` overrides. Examples include:

- Docker / Compose: set `GEMINI_API_KEY` in the container environment.
- Kubernetes: mount `GEMINI_API_KEY` from a `Secret` and expose it as an environment variable.
- PaaS platforms such as Render, Railway, or Heroku: add `GEMINI_API_KEY` in the service's secret or environment variable settings.

### Run locally

```bash
cd backend
mvn spring-boot:run
```

The backend listens on `http://localhost:8080` by default and exposes `GET /api/health` plus `POST /api/chat` for Gemini-backed chat responses.

### Test

```bash
cd backend
mvn test
```

## Connectivity flow

1. Start the backend.
2. Start the frontend.
3. Open the frontend in a browser.
4. The status pill in the UI updates based on the backend health response.


### Chat endpoint

Send a request like:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Hello from Gemini"}'
```

Successful and failed responses both return structured JSON with the assistant reply, request metadata, and error details when applicable.
