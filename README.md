# UniGate

A smart university registration portal built with Spring Boot 3 and React 18, offering automated eligibility checks, state-machine-driven application workflows, grade management, internship tracking, skill exchange, and real-time notifications.

## Architecture

```
unigate/
├── backend/          # Spring Boot 3.2 REST API
│   └── src/main/java/com/unigate/
│       ├── common/         # security, config (JWT, WebSocket, OpenAPI), AOP, scheduling
│       ├── registration/   # application workflow (state machine)
│       ├── eligibility/    # eligibility rule engine
│       ├── grades/         # grade management & events
│       ├── internship/     # internship offers and applications
│       ├── skillswap/      # peer skill-exchange marketplace
│       ├── timetable/      # schedule management
│       └── notification/   # WebSocket + email notifications
├── frontend/         # React 18 SPA
│   └── src/
│       ├── pages/    # admin/, student/, auth/, grades/, internship/, skillswap/, timetable/
│       ├── components/, layouts/, hooks/, context/, services/
│       └── i18n/     # internationalisation
└── docker-compose.yml
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring State Machine 4 |
| Persistence | Spring Data JPA, PostgreSQL 15, Hibernate |
| API docs | SpringDoc OpenAPI 3 (Swagger UI) |
| Auth | JWT (JJWT 0.11), bcrypt |
| Real-time | Spring WebSocket + STOMP |
| Email | Spring Mail |
| Mapping | MapStruct 1.5 |
| Frontend | React 18, React Router 6, Tailwind CSS 3 |
| HTTP client | Axios, SockJS + @stomp/stompjs |
| Containerisation | Docker Compose |

## Features

### Student portal
- Submit registration applications with document upload
- Real-time application status tracking
- View grades and academic transcript
- Browse and apply for internship offers
- Peer skill-swap: offer and request skills from other students
- Personal timetable view

### Admin dashboard
- Review and process student applications (approve / reject / request documents)
- Configure eligibility rules (automated pre-screening)
- Manage grade entries and configurations
- Manage internship offers and applications
- Oversee skill-swap requests
- Broadcast notifications

### Super-admin
- Platform-wide user and role management
- System configuration

### Cross-cutting
- Email notifications on status changes
- Live WebSocket push notifications
- i18n support

## Prerequisites

- Docker & Docker Compose
- Java 17+ and Maven 3.8+ (for local backend dev)
- Node.js 18+ (for local frontend dev)

## Quick Start

### 1. Configure environment

Create a `.env` file at the project root:

```env
DB_NAME=unigate
DB_USER=unigate
DB_PASSWORD=unigate_pass
JWT_SECRET=change_me_in_production_long_random_string
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=noreply@example.com
MAIL_PASSWORD=mail_password
```

### 2. Start all services

```bash
docker compose up -d
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |

## Development (without Docker)

**Backend**

```bash
cd backend
mvn spring-boot:run
```

**Frontend**

```bash
cd frontend
npm install
npm start
```

The frontend proxies API requests to `http://localhost:8081` (configured in `package.json`).

## API Documentation

Swagger UI is available at `/swagger-ui.html` when the backend is running (configured via `OpenApiConfig`).

## Project Structure Notes

- Registration workflow is driven by **Spring State Machine** — states and transitions are in `registration/statemachine/`.
- Eligibility engine lives in `eligibility/service/` and is evaluated automatically when an application is submitted.
- Grade-related domain events are published via Spring's event system (`grades/event/`, `grades/listener/`).
- Async tasks (email sending, notifications) are handled via `AsyncConfig` and `@Async`.
