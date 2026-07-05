# PayFlux

PayFlux is a full-stack fintech banking platform built with Spring Boot microservices, Apache Kafka, Redis, PostgreSQL, Docker, and React. The project models a production-style banking backend with authentication, account ownership, wallet transfers, beneficiaries, notifications, audit records, and event-driven service communication.

## Features

- JWT-based authentication and authorization
- One primary bank account per user with generated account numbers
- Wallet dashboard with deposits and internal transfers
- Redis-backed transfer OTP confirmation and rate limiting
- Beneficiary management for trusted recipients
- Transaction history and customer notifications
- Kafka-driven event flow between services
- Audit records for admin-level operational visibility
- PostgreSQL per service with Flyway database migrations
- Responsive React banking dashboard built with Vite

## Architecture

PayFlux follows a microservice architecture where each service owns its own database and exposes a focused API. Services communicate synchronously through HTTP for user-facing requests and asynchronously through Kafka events for cross-service workflows.

| Service | Port | Responsibility | Database |
| --- | ---: | --- | --- |
| `authservice` | 8080 | Registration, login, JWT issuing, user identity | `authdb` |
| `accountservice` | 8081 | Bank account creation and account lookup | `accountdb` |
| `notificationservice` | 8082 | Customer notifications from domain events | `notificationdb` |
| `walletservice` | 8083 | Wallet balance, deposits, transfer confirmation, outbox events | `walletdb` |
| `beneficiaryservice` | 8084 | Saved beneficiary accounts | `beneficiarydb` |
| `transactionservice` | 8086 | Transfer and transaction records | `transactiondb` |
| `auditservice` | 8087 | Admin audit records from Kafka events | `auditdb` |
| `payflux-frontend` | 5173 | React banking web app | N/A |

Infrastructure:

- Kafka: `localhost:9092`
- Kafka UI: `http://localhost:8085`
- Redis: `localhost:6379`
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

- Java 21 or the Java version configured by the services
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

The frontend stores the JWT after login and sends it as a Bearer token to protected APIs. Backend services validate the token before allowing access.

Admin-only screens, such as audit records, require a user with the `ADMIN` role.

To promote a local user during development:

```sql
UPDATE users
SET role = 'ADMIN'
WHERE email = 'your-email@example.com';
```

After changing the role, log out and log back in so the JWT contains the updated role.

## Development Notes

- Each microservice owns its own database.
- Do not create direct foreign keys across service databases.
- Use Kafka events for cross-service propagation.
- Use Redis for short-lived state such as OTP confirmation and rate limiting.
- Use Flyway for schema changes.
- Keep secrets and local overrides out of Git.

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
