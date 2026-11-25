# Data flow diagrams

Notation: rectangles are external entities, rounded boxes are processes, open-ended boxes are data stores.

## Level 0: context diagram

```mermaid
flowchart LR
    VIS[Visitor, citizen, volunteer]
    STAFF[Staff: admin, officer, warehouse manager, field worker, NGO]
    AIP[External AI model provider]
    SYS((DRRC system))

    VIS -- "sign-up details, credentials" --> SYS
    SYS -- "token session, home page, open shelters" --> VIS
    STAFF -- "credentials, requests, decisions, stock, dispatch, occupancy, reports" --> SYS
    SYS -- "dashboards, map, priorities, allocations, alerts, PDF report" --> STAFF
    SYS -- "scrubbed report text" --> AIP
    AIP -- "structured suggestion" --> SYS
```

The AI provider is optional. Without `AI_API_KEY` the analyzer answers 503 and nothing else is affected.

## Level 1: main processes and data stores

```mermaid
flowchart TB
    VIS[Visitor, citizen, volunteer]
    STAFF[Staff roles]
    AIP[AI model provider]

    P1(1.0 Vaadin UI: pages, route guard, role menu)
    P2(2.0 Authenticate and register)
    P3(3.0 Authorize: JWT check and role rules)
    P4(4.0 Disasters and relief requests)
    P5(5.0 Prioritize and allocate)
    P6(6.0 Inventory and dispatch)
    P7(7.0 Shelters, volunteers, partners)
    P8(8.0 Alerts, audit, analytics, reports)
    P9(9.0 Report analyzer)

    D1[(Users)]
    D2[(Disasters, areas, requests, history)]
    D3[(Allocations and reservations)]
    D4[(Inventory, ledger, dispatches)]
    D5[(Shelters, volunteers, partners)]
    D6[(Audit log and alerts)]
    D7[(AI analyses)]

    VIS -- "form input" --> P1
    STAFF -- "form input" --> P1
    P1 -- "pages and tables" --> VIS
    P1 -- "pages and tables" --> STAFF
    P1 -- "REST calls with bearer token held in UserSession" --> P3
    P1 -- "login and register calls" --> P2

    P2 -- "look up e-mail, store new BCrypt hash" --> D1
    D1 -- "user record" --> P2
    P2 -- "JWT" --> P1
    P3 -- "allowed calls, user id and role" --> P4
    P3 -- "allowed calls" --> P5
    P3 -- "allowed calls" --> P6
    P3 -- "allowed calls, open shelters for any signed-in user" --> P7
    P3 -- "allowed calls" --> P8
    P3 -- "allowed calls" --> P9

    P4 <--> D2
    P5 -- "read requests and stock" --> D2
    P5 <--> D3
    P5 -- "lock and reserve stock" --> D4
    P6 <--> D4
    P6 -- "status derived from delivery" --> D2
    P7 <--> D5
    P8 -- "read all stores" --> D2
    P8 -- "read" --> D4
    P8 -- "read" --> D5
    P8 <--> D6
    P4 -- "history and audit entries" --> D6
    P5 -- "audit entries" --> D6
    P6 -- "audit entries" --> D6
    P7 -- "overcrowding alert" --> D6

    P9 -- "scrubbed text" --> AIP
    AIP -- "suggestion" --> P9
    P9 <--> D7
```

## Level 2: sign-up and sign-in data flow

```mermaid
flowchart LR
    U[Visitor]
    V1(RegisterView / LoginView)
    C(AuthClient)
    API(AuthController)
    TH(LoginThrottle)
    SV(AuthService)
    JW(JwtService)
    DB[(users table)]
    SS[UserSession]

    U -- "name, e-mail, role, password" --> V1
    V1 -- "client checks" --> V1
    V1 --> C
    C -- "POST /api/auth/register or /login (JSON)" --> API
    API -- "login only: blocked?" --> TH
    API --> SV
    SV -- "find by lower(e-mail); insert on register" --> DB
    DB -- "user row" --> SV
    SV -- "compare BCrypt hash, or encode new password" --> SV
    SV --> JW
    JW -- "signed token, 1 hour" --> SV
    SV -- "token and user summary (no hash)" --> API
    API -- "201 on register, 200 on login" --> C
    C --> SS
    SS -- "token kept on the server, never sent to the browser" --> V1
    V1 -- "redirect by role" --> U
```

## Level 2: citizen shelter lookup

```mermaid
flowchart LR
    CZ[Citizen]
    HV(CitizenHomeView)
    AP(Api client)
    SC(ShelterController GET /api/shelters/open)
    SV(ShelterService)
    RP(ShelterRepository)
    DB[(shelters table)]

    CZ -- "opens /home" --> HV
    HV --> AP
    AP -- "bearer token" --> SC
    SC -- "any signed-in user" --> SV
    SV --> RP
    RP -- "status OPEN" --> DB
    DB -- "rows" --> RP
    RP --> SV
    SV -- "free beds above zero, public fields only" --> SC
    SC -- "page of name, address, area, free beds, accessible, facilities" --> AP
    AP --> HV
    HV -- "table" --> CZ
```
