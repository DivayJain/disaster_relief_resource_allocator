# Disaster Relief Resource Coordinator
Java full-stack project: Spring Boot, Vaadin Flow, PostgreSQL/PostGIS.
See docs/ for architecture and API contract.

## What it does
Staff (administrators, relief officers, warehouse managers, field workers, NGO coordinators) record disasters and affected areas, raise and approve relief requests, get explainable priority scores and stock allocation recommendations, dispatch and track deliveries, and manage shelters, volunteers and partner contributions. Citizens and volunteers can **sign up** and see a home page of open shelters with free beds.

## Project layout
| Folder | Content |
|---|---|
| `backend/` | Spring Boot REST API (port 8080), JWT security, Flyway migrations at start-up |
| `frontend/` | Vaadin Flow UI (port 8081); talks to the backend over REST, keeps the token in the server session |
| `database/` | Flyway migrations (`migrations/`) and demo seed data (`seed/`, dev profile only) |
| `deployment/` | Dockerfiles and Docker Compose |
| `docs/` | Architecture, API contract, database design, security review, demo script, and `docs/diagrams/` |

## Diagrams
[Use case](docs/diagrams/USE_CASE_DIAGRAM.md), [class](docs/diagrams/CLASS_DIAGRAM.md), [activity](docs/diagrams/ACTIVITY_DIAGRAM.md), [ER](docs/diagrams/ER_DIAGRAM.md) and [data flow](docs/diagrams/DATA_FLOW_DIAGRAM.md) diagrams (Mermaid, rendered by GitHub). Index: [docs/diagrams/README.md](docs/diagrams/README.md).

## Run it locally
Follow [docs/INTEGRATION_GUIDE.md](docs/INTEGRATION_GUIDE.md) (database in Docker, then backend, then frontend). Demo logins and the sign-up page are described there; `docs/DEMO_SCRIPT.md` is the walk-through.

## Deploying
Two long-running Java services plus PostgreSQL/PostGIS, so use a container host (Docker Compose on a VPS, Render, Railway, Fly.io). Serverless hosts such as Vercel cannot run it. Always use `SPRING_PROFILES_ACTIVE=prod`.
