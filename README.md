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
OPENAI_API_KEY=your_openai_api_key
APP_CORS_ALLOWED_ORIGIN=http://localhost:5173
SERVER_PORT=8080
```

`OPENAI_API_KEY` is optional for the included health endpoint, but it is expected once you add Spring AI powered features.

### Run locally

```bash
cd backend
mvn spring-boot:run
```

The backend listens on `http://localhost:8080` by default and exposes a health endpoint at `GET /api/health`.

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
