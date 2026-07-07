# PayFlux

PayFlux is a full-stack fintech banking platform built as a production-style learning project. It combines a Spring Boot microservice backend, Kafka event flows, Redis-backed security workflows, PostgreSQL databases, Flyway migrations, an API gateway, and a Vite React banking dashboard.

The product models a modern digital banking workspace where users can register, receive a generated PayFlux account number, manage a wallet, save beneficiaries, confirm transfers with OTP, review transactions, receive notifications, and export statements. Admin users get operational visibility through audit records, user review, and wallet suspension controls.

## System Design

PayFlux follows a database-per-service microservice architecture. Each service owns its domain model and persistence boundary. Synchronous user-facing requests enter through `gatewayservice`, while cross-service state changes are propagated with Kafka events.

Key backend patterns used:

- API Gateway for centralized routing and JWT enforcement
- JWT authentication with role-based authorization
- Redis for refresh sessions, login protection, OTP confirmation, idempotency, and rate limiting
- Kafka for event-driven communication between services
- Outbox pattern for reliable wallet event publishing
- PostgreSQL database per service
- Flyway-managed schema migrations
- Ledger-based wallet accounting
- Actuator health endpoints and correlation IDs for debugging

## Services

| Service | Responsibility |
| --- | --- |
| `authservice` | Registration, login, JWTs, refresh sessions, admin user directory |
| `accountservice` | Account provisioning and account-number lookup |
| `walletservice` | Wallets, deposits, OTP transfer confirmation, ledger entries, reversals, admin wallet controls |
| `beneficiaryservice` | Saved recipients and recipient verification |
| `transactionservice` | Transaction history and transaction details |
| `notificationservice` | User notifications from Kafka events |
| `auditservice` | Admin audit records from domain events |
| `gatewayservice` | Single backend entry point and route-level authorization |
| `payflux-frontend` | Responsive React banking web app |

## Technology Stack

Backend:

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- Redis
- Flyway
- Maven

Frontend:

- React
- Vite
- Fetch API
- CSS
- Lucide React icons

Infrastructure:

- Docker Compose
- Apache Kafka
- Kafka UI
- Redis
- PostgreSQL

## Repository Layout

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
`-- README.md
```

## Current Focus

PayFlux is evolving toward a more realistic fintech-grade system. The current implementation emphasizes secure user flows, reliable money movement, event-driven consistency, auditability, admin operations, and a professional banking dashboard experience.
