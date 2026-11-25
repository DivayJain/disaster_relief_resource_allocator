# Entity relationship diagram

Source: `database/migrations/V1` to `V11` (PostgreSQL + PostGIS, managed by Flyway). 23 tables. Types are shortened; every table has a `BIGSERIAL id` primary key. PostGIS `geography` columns: `disasters.epicentre`, `affected_areas.location` and `boundary`, `warehouses.location`, `shelters.location`, `volunteers.location`.

## 1. Whole schema (relationships)

```mermaid
erDiagram
    ORGANIZATIONS ||--o{ USERS : employs
    ORGANIZATIONS ||--o{ WAREHOUSES : runs
    ORGANIZATIONS ||--o{ SHELTERS : runs
    ORGANIZATIONS ||--o{ VOLUNTEERS : fields
    ORGANIZATIONS ||--o{ PARTNER_CONTRIBUTIONS : offers
    USERS ||--o{ WAREHOUSES : manages
    USERS ||--o{ SHELTERS : manages
    USERS ||--o| VOLUNTEERS : "may be"
    USERS ||--o{ DISASTERS : creates
    USERS ||--o{ NEED_REQUESTS : "requests or approves"
    USERS ||--o{ AUDIT_LOGS : acts
    USERS ||--o{ AI_ANALYSES : "creates or reviews"
    USERS ||--o{ ALERTS : "marks read"

    DISASTERS ||--o{ AFFECTED_AREAS : contains
    DISASTERS ||--o{ NEED_REQUESTS : raises
    DISASTERS ||--o{ VOLUNTEER_ASSIGNMENTS : needs
    AFFECTED_AREAS ||--o{ NEED_REQUESTS : "located in"
    RESOURCES ||--o{ NEED_REQUESTS : "asked for"
    RESOURCES ||--o{ INVENTORY : "stocked as"
    RESOURCES ||--o{ PARTNER_CONTRIBUTIONS : "offered as"
    NEED_REQUESTS ||--o{ REQUEST_HISTORY : logs
    NEED_REQUESTS ||--o{ ALLOCATIONS : "served by"
    NEED_REQUESTS ||--o{ DISPATCHES : "delivered by"
    NEED_REQUESTS ||--o{ INVENTORY_TRANSACTIONS : "referenced by"
    NEED_REQUESTS ||--o{ VOLUNTEER_ASSIGNMENTS : "linked to"

    WAREHOUSES ||--o{ INVENTORY : holds
    INVENTORY ||--o{ INVENTORY_TRANSACTIONS : ledger
    INVENTORY ||--o{ ALLOCATION_RESERVATIONS : "reserved by"
    ALLOCATIONS ||--o{ ALLOCATION_ITEMS : picks
    ALLOCATIONS ||--o{ ALLOCATION_NOT_SELECTED : skips
    ALLOCATIONS ||--o{ ALLOCATION_RESERVATIONS : locks
    ALLOCATIONS ||--o{ DISPATCHES : "dispatched as"
    WAREHOUSES ||--o{ ALLOCATION_ITEMS : supplies
    WAREHOUSES ||--o{ ALLOCATION_NOT_SELECTED : considered
    WAREHOUSES ||--o{ DISPATCHES : "ships from"
    DISPATCHES ||--o{ DELIVERY_UPDATES : tracks
    VOLUNTEERS ||--o{ VOLUNTEER_ASSIGNMENTS : assigned
```

## 2. Core tables with columns

```mermaid
erDiagram
    ORGANIZATIONS {
        bigint id PK
        varchar name UK
        varchar type "GOVERNMENT, NGO, OTHER"
    }
    USERS {
        bigint id PK
        varchar full_name
        varchar email UK "unique on lower(email)"
        varchar password_hash "BCrypt"
        varchar role "7 roles, CHECK"
        bigint organization_id FK
        boolean enabled
        varchar preferred_language "en, hi, pa"
    }
    DISASTERS {
        bigint id PK
        varchar name
        varchar disaster_type
        varchar severity
        varchar status
        timestamptz started_at
        bigint created_by FK
        boolean is_demo
    }
    AFFECTED_AREAS {
        bigint id PK
        bigint disaster_id FK
        varchar name
        varchar district
        int population_estimate
        varchar impact_level
    }
    RESOURCES {
        bigint id PK
        varchar name
        varchar category
        varchar unit
        boolean perishable
        int shelf_life_days
    }
    NEED_REQUESTS {
        bigint id PK
        bigint disaster_id FK
        bigint affected_area_id FK
        bigint resource_id FK
        bigint requested_by FK
        int quantity
        varchar severity
        varchar status "9 states"
        numeric priority_score
        varchar priority_category
        bigint approved_by FK
    }
    WAREHOUSES {
        bigint id PK
        varchar code
        varchar name
        bigint organization_id FK
        bigint manager_user_id FK
        varchar status
    }
    INVENTORY {
        bigint id PK
        bigint warehouse_id FK
        bigint resource_id FK
        varchar batch_number
        date expiry_date
        int quantity_on_hand
        int quantity_reserved "CHECK 0 to on_hand"
    }
    SHELTERS {
        bigint id PK
        varchar code
        varchar name
        int capacity_people
        int current_occupancy
        varchar status "OPEN, CLOSED, INACTIVE"
        boolean is_accessible
    }
    ALLOCATIONS {
        bigint id PK
        bigint request_id FK
        varchar status "RECOMMENDED, APPROVED, REJECTED, STALE"
        int requested_qty
        int allocated_qty
        int version
        bigint created_by FK
        bigint approved_by FK
    }
    DISPATCHES {
        bigint id PK
        bigint allocation_id FK
        bigint request_id FK
        bigint warehouse_id FK
        varchar status "DISPATCHED, IN_TRANSIT, DELIVERED"
        int quantity
        timestamptz delivered_at
    }

    ORGANIZATIONS ||--o{ USERS : employs
    DISASTERS ||--o{ AFFECTED_AREAS : contains
    AFFECTED_AREAS ||--o{ NEED_REQUESTS : "located in"
    RESOURCES ||--o{ NEED_REQUESTS : "asked for"
    USERS ||--o{ NEED_REQUESTS : requests
    WAREHOUSES ||--o{ INVENTORY : holds
    RESOURCES ||--o{ INVENTORY : "stocked as"
    NEED_REQUESTS ||--o{ ALLOCATIONS : "served by"
    ALLOCATIONS ||--o{ DISPATCHES : "dispatched as"
```

## 3. Supporting tables

```mermaid
erDiagram
    REQUEST_HISTORY {
        bigint id PK
        bigint request_id FK
        varchar action
        varchar from_status
        varchar to_status
        bigint actor_user_id FK
        text reason
    }
    INVENTORY_TRANSACTIONS {
        bigint id PK
        bigint inventory_id FK
        varchar transaction_type
        int quantity_delta_on_hand
        int quantity_delta_reserved
        bigint request_id FK
        bigint actor_user_id FK
    }
    ALLOCATION_ITEMS {
        bigint id PK
        bigint allocation_id FK
        bigint warehouse_id FK
        int quantity
        numeric distance_km
        text reason
    }
    ALLOCATION_NOT_SELECTED {
        bigint id PK
        bigint allocation_id FK
        bigint warehouse_id FK
        text reason
    }
    ALLOCATION_RESERVATIONS {
        bigint id PK
        bigint allocation_id FK
        bigint inventory_id FK
        int quantity
        boolean dispatched
    }
    DELIVERY_UPDATES {
        bigint id PK
        bigint dispatch_id FK
        varchar status
        bigint actor_user_id FK
    }
    AUDIT_LOGS {
        bigint id PK
        bigint actor_user_id FK
        varchar actor_role
        varchar action
        varchar entity_type
        bigint entity_id
        text details
    }
    ALERTS {
        bigint id PK
        varchar type
        varchar severity
        varchar message
        varchar entity_type
        bigint entity_id
        varchar status "NEW or READ"
        bigint read_by FK
    }
    VOLUNTEERS {
        bigint id PK
        bigint user_id FK
        bigint organization_id FK
        varchar full_name
        varchar skills
        varchar availability
    }
    VOLUNTEER_ASSIGNMENTS {
        bigint id PK
        bigint volunteer_id FK
        bigint disaster_id FK
        bigint request_id FK
        varchar task
        varchar status
        bigint assigned_by FK
    }
    PARTNER_CONTRIBUTIONS {
        bigint id PK
        bigint organization_id FK
        bigint resource_id FK
        int quantity
        varchar status
        bigint decided_by FK
    }
    AI_ANALYSES {
        bigint id PK
        text report_text "PII scrubbed"
        varchar model
        varchar status
        text result_json
        varchar review_status "UNREVIEWED until a person reviews"
        bigint reviewed_by FK
        bigint created_by FK
    }
```

## Rules the schema enforces
- `users.email` is unique case-insensitively (index on `lower(email)`). This is what makes a duplicate sign-up fail with 409, even when two sign-ups race.
- `inventory`: `quantity_reserved` is never negative and never above `quantity_on_hand` (CHECK), the backstop for concurrent allocation approvals.
- `audit_logs` is append-only (database trigger); `inventory_transactions` is an append-only ledger.
- `need_requests` references `affected_areas(id, disaster_id)`, so a request cannot point at an area of a different disaster.
- Foreign keys are `ON DELETE RESTRICT`: history is never silently deleted.
- Self-registered users are plain `users` rows with role CITIZEN or VOLUNTEER and no organization; sign-up does not create a `volunteers` row.
