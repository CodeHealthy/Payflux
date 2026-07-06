# PayFlux

PayFlux is a full-stack fintech banking platform built with Spring Boot microservices, Apache Kafka, Redis, PostgreSQL, Docker, and React. The project models a production-style banking backend with authentication, account ownership, wallet transfers, beneficiaries, notifications, audit records, and event-driven service communication.

## Features

- JWT-based authentication and authorization with Redis-backed refresh sessions
- One primary bank account per user with generated account numbers
- Ledger-backed wallet dashboard with deposits and internal transfers
- Redis-backed transfer OTP confirmation, rate limiting, login protection, and refresh tokens
- Beneficiary management for trusted recipients
- Transaction history and customer notifications
- Kafka-driven event flow between services
- Audit records for admin-level operational visibility
- Actuator health endpoints and request correlation IDs for debugging
- PostgreSQL per service with Flyway database migrations
- Responsive React banking dashboard built with Vite

## Architecture

PayFlux follows a microservice architecture where each service owns its own database and exposes a focused API. Services communicate synchronously through HTTP for user-facing requests and asynchronously through Kafka events for cross-service workflows.

| Service | Port | Responsibility | Database |
| --- | ---: | --- | --- |
| `authservice` | 8080 | Registration, login, JWT issuing, refresh sessions, user identity | `authdb` |
| `accountservice` | 8081 | Bank account creation and account lookup | `accountdb` |
| `notificationservice` | 8082 | Customer notifications from domain events | `notificationdb` |
| `walletservice` | 8083 | Wallet balance projection, append-only ledger, deposits, transfer confirmation, outbox events | `walletdb` |
| `beneficiaryservice` | 8084 | Saved beneficiary accounts | `beneficiarydb` |
| `transactionservice` | 8086 | Transfer and transaction records | `transactiondb` |
| `auditservice` | 8087 | Admin audit records from Kafka events | `auditdb` |
| `gatewayservice` | 8088 | Single API entry point, JWT enforcement, request routing | N/A |
| `payflux-frontend` | 5173 | React banking web app | N/A |

Infrastructure:

- Kafka: `localhost:9092`
- Kafka UI: `http://localhost:8085`
- Redis: `localhost:6379`
- API Gateway: `http://localhost:8088`
- PostgreSQL containers exposed on ports `5543` through `5549`

## Tech Stack

Backend:

- Java
- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Spring Kafka
- Flyway
- PostgreSQL
- Redis
- Maven

Frontend:

- React
- Vite
- Fetch API
- Lucide React icons
- CSS modules/global app styling

Infrastructure:

- Docker Compose
- Apache Kafka
- Redis
- PostgreSQL
- Kafka UI

## Project Structure

```text
Payflux/
|-- accountservice/
|-- auditservice/
|-- authservice/
|-- beneficiaryservice/
|-- gatewayservice/
|-- notificationservice/
|-- transactionservice/
|-- walletservice/
|-- payflux-frontend/
|-- scripts/
|-- docker-compose.yml
|-- start-payflux.cmd
|-- start-backend.cmd
|-- start-frontend.cmd
`-- README.md
```

## Prerequisites

Install these before running the project:

- Java 17 or newer
- Node.js and npm
- Docker Desktop
- Git

Make sure Docker Desktop is running before starting the infrastructure.

## Getting Started

Clone the repository:

```powershell
git clone <repository-url>
cd Payflux
```

Create a local environment file when you need to override defaults:

```powershell
Copy-Item .env.example .env
```

The root `.env` file is ignored by Git. Docker Compose reads it automatically, and the provided startup scripts load it before launching backend or frontend processes.

Start infrastructure:

```powershell
docker compose up -d
```

Start all backend services:

```powershell
.\start-backend.cmd
```

Start the frontend:

```powershell
.\start-frontend.cmd
```

Or start everything together:

```powershell
.\start-payflux.cmd
```

Open the app:

```text
http://127.0.0.1:5173
```

Open Kafka UI:

```text
http://localhost:8085
```

## Manual Service Commands

Run a backend service manually:

```powershell
cd authservice
.\mvnw.cmd spring-boot:run
```

Run the frontend manually:

```powershell
cd payflux-frontend
npm install
npm run dev -- --host 127.0.0.1
```

Build the frontend:

```powershell
cd payflux-frontend
npm run build
```

Run tests for a service:

```powershell
cd walletservice
.\mvnw.cmd test
```

## Database Migrations

PayFlux uses Flyway migrations in each service under:

```text
src/main/resources/db/migration
```

Migration files are versioned with names like:

```text
V1__create_users_table.sql
V2__add_account_number_to_accounts.sql
```

For normal development, prefer adding a new migration instead of editing a migration that has already been applied to a database.

## Authentication and Roles

The frontend stores the access token and refresh token after login. Protected API calls send the access token as a Bearer token through `gatewayservice`. If the access token expires, the frontend calls `POST /auth/refresh` once, receives a rotated refresh token, and retries the original request. Logout calls `POST /auth/logout` to revoke the Redis-backed refresh token before clearing local session state.

`gatewayservice` is the first authorization layer. Registration, login, refresh, logout, and health endpoints are public. Banking APIs require a valid JWT. Audit APIs require the `ADMIN` role at the gateway and again inside `auditservice`.

Admin-only screens, such as audit records, require a user with the `ADMIN` role.

## Wallet Ledger

Wallet money movement is recorded in `wallet_ledger_entries` before customer-facing transaction rows are saved. The `wallets.available_balance` column is kept as a current-balance projection for fast dashboard reads, while the ledger remains the durable accounting trail for deposits, transfer debits, transfer credits, future reversals, and reconciliation.

Transfers are tracked separately in `wallet_transfers` as a lifecycle aggregate. A transfer starts as `PENDING_CONFIRMATION`, moves to `PROCESSING` during OTP confirmation, and ends as `COMPLETED` or `FAILED`. This keeps transfer orchestration state separate from immutable ledger entries and customer-facing statement rows.

To promote a local user during development:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'your-email@example.com';
```

After changing the role, log out and log back in so the JWT contains the updated role.

## Observability

Every backend service includes Spring Boot Actuator with local health and info endpoints:

```text
http://localhost:<service-port>/actuator/health
http://localhost:<service-port>/actuator/info
```

The frontend sends an `X-Correlation-Id` header with every API request. The gateway and backend services preserve or generate that ID, return it in response headers, include it in structured API errors, and add it to log context so a failed request can be traced across services.

## Development Notes

- Each microservice owns its own database.
- Do not create direct foreign keys across service databases.
- Use Kafka events for cross-service propagation.
- Use Redis for short-lived state such as OTP confirmation, rate limiting, login protection, and refresh-token sessions.
- Use Flyway for schema changes.
- Keep secrets and local overrides in `.env`, never in committed source files.
- Use environment variables for deployment-specific values such as database URLs, passwords, JWT secrets, CORS origins, service URLs, Kafka brokers, Redis host, and frontend gateway targets.

## Useful Docker Commands

Check running containers:

```powershell
docker compose ps
```

View logs:

```powershell
docker compose logs -f kafka
```

Stop infrastructure:

```powershell
docker compose down
```

Reset all local Docker volumes:

```powershell
docker compose down -v
```

Use volume reset carefully because it deletes local database, Kafka, and Redis data.

## Repository Status

This project is actively evolving as a hands-on fintech microservices system. The current focus is building professional banking workflows while keeping the implementation understandable for learning and experimentation.
