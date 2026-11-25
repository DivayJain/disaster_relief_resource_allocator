# DRRC diagrams

All diagrams are [Mermaid](https://mermaid.js.org/) code blocks. GitHub, VS Code (Markdown preview) and most Markdown viewers draw them directly; no export step is needed.

| Diagram | File | Shows |
|---|---|---|
| Use case | [USE_CASE_DIAGRAM.md](USE_CASE_DIAGRAM.md) | Who can do what, per role, including public sign-up and the citizen home page |
| Class | [CLASS_DIAGRAM.md](CLASS_DIAGRAM.md) | Backend layers, the auth and sign-up classes, the domain model and the Vaadin frontend classes |
| Activity | [ACTIVITY_DIAGRAM.md](ACTIVITY_DIAGRAM.md) | Sign-up, login and role routing, and the relief request lifecycle from request to delivery |
| ER | [ER_DIAGRAM.md](ER_DIAGRAM.md) | All 23 tables and their foreign keys (migrations V1 to V11) |
| Data flow | [DATA_FLOW_DIAGRAM.md](DATA_FLOW_DIAGRAM.md) | Context (level 0) and level 1 flows between users, the Vaadin app, the REST API and PostgreSQL/PostGIS |

Sources of truth: the Flyway migrations in `database/migrations`, the controllers' role rules (`@PreAuthorize`, `@StaffOnly`, `@AdminOrOfficer`), and `docs/API_CONTRACT.md`. If one of those changes, update the matching diagram in the same pull request.
