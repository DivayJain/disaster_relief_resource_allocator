# Activity diagrams

## 1. Sign up

```mermaid
flowchart TD
    A([Visitor opens /register]) --> B[Enter full name, e-mail, account type, password, confirm password]
    B --> C{All fields filled and e-mail valid?}
    C -- no --> E1[Show message on the page] --> B
    C -- yes --> D{Password at least 8 characters?}
    D -- no --> E1
    D -- yes --> F{Both passwords equal?}
    F -- no --> E1
    F -- yes --> G[RegisterView calls POST /api/auth/register]
    G --> H{Role is CITIZEN or VOLUNTEER?}
    H -- no --> E2[400 VALIDATION_ERROR] --> E1
    H -- yes --> I{E-mail already used, ignoring case?}
    I -- yes --> E3[409 EMAIL_TAKEN] --> E1
    I -- no --> J[Hash password with BCrypt and save user, enabled]
    J --> K{Unique index violated by a parallel sign-up?}
    K -- yes --> E3
    K -- no --> L[Issue JWT, return token and user]
    L --> M[UserSession stores token on the server]
    M --> N([Navigate to home: AuthGuard sends non-staff to /home])
```

## 2. Log in and role routing

```mermaid
flowchart TD
    A([User opens any page]) --> B{Logged in?}
    B -- no --> C{Page is /login or /register?}
    C -- yes --> D[Show the page]
    C -- no --> L[Redirect to /login]
    D --> E[Submit e-mail and password]
    E --> F{Too many recent failures for this e-mail or address?}
    F -- yes --> G[429 TOO_MANY_ATTEMPTS]
    F -- no --> H{E-mail known, password correct, account enabled?}
    H -- no --> I[Same 401 message, failure counted]
    H -- yes --> J[Issue JWT, store in UserSession]
    J --> K{Role is staff?}
    K -- yes --> S[Dashboard and the full staff menu]
    K -- no --> T[Citizen home: open shelters with free beds]
    B -- yes --> R{Role is staff?}
    R -- yes --> S
    R -- no --> U{Page is /home?}
    U -- yes --> T
    U -- no --> T2[Redirect to /home]
```

## 3. Relief request lifecycle

State names and rules come from `docs/API_CONTRACT.md` section 1.

```mermaid
flowchart TD
    A([Officer, field worker or NGO submits request]) --> B[Status PENDING, history written]
    B --> C[Officer reviews]
    C --> D{Decision}
    D -- ask for clarification --> B
    D -- reject, reason required --> X1([REJECTED])
    D -- verify --> E[VERIFIED, priority score and category calculated with explanation]
    E --> F{Officer overrides priority?}
    F -- yes --> F1[Override saved with reason] --> G
    F -- no --> G{Officer approves request?}
    G -- reject, reason required --> X1
    G -- approve --> H[APPROVED, no stock reserved yet]
    H --> I[Officer asks for allocation recommendation]
    I --> J[Recommend warehouses: nearest first, earliest expiry first, fairness cap, shortage shown]
    J --> K{Officer approves allocation?}
    K -- reject --> J
    K -- approve --> L[Lock stock rows, re-check availability, reserve stock once]
    L --> M{Enough stock still free?}
    M -- no --> N[Allocation STALE, recommend again] --> J
    M -- yes --> O[ALLOCATED]
    O --> P[Warehouse manager creates dispatch for their own warehouse]
    P --> Q[DISPATCHED]
    Q --> R[IN_TRANSIT]
    R --> S[Delivered: stock on hand and reserved reduced, ledger entry written]
    S --> Y([DELIVERED])
    B -.-> Z[Cancel allowed while PENDING, VERIFIED or APPROVED, reason required]
    E -.-> Z
    H -.-> Z
    Z --> X2([CANCELLED])
```

Every step writes a row to `request_history` and an `audit_logs` entry in the same transaction. Cancelling after ALLOCATED is not supported in the MVP (open decision O3).

## 4. Citizen finds a shelter

```mermaid
flowchart TD
    A([Citizen signs in]) --> B[CitizenHomeView loads]
    B --> C[Frontend calls GET /api/shelters/open with the token]
    C --> D{Valid token?}
    D -- no --> E[401, session ended, back to login]
    D -- yes --> F[Backend reads OPEN shelters]
    F --> G[Keep shelters with free beds, drop occupancy, capacity and coordinates]
    G --> H[Table: name, address, area, free beds, accessible, facilities]
```
