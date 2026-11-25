# Use case diagram

Roles come from `Role.java`: ADMIN, OFFICER, WAREHOUSE_MANAGER, FIELD_WORKER, NGO, VOLUNTEER, CITIZEN. "Staff" means the first five (see `StaffOnly.java`). VOLUNTEER and CITIZEN are the two roles anyone can pick at public sign-up; the other roles are issued by an administrator.

## 1. Public access, citizens and volunteers

```mermaid
flowchart LR
    visitor([Visitor])
    citizen([Citizen])
    volunteer([Volunteer])

    subgraph PUBLIC["Public pages"]
        UC1(Sign up)
        UC2(Log in)
    end
    subgraph HOME["Citizen and volunteer home"]
        UC3(View home page)
        UC4(Browse open shelters with free beds)
        UC5(Log out)
    end
    BLOCK[["Server refuses every staff screen and staff API with 403"]]

    visitor --> UC1
    visitor --> UC2
    UC1 -. "creates account, signs in" .-> UC3
    UC2 -. "non-staff role" .-> UC3
    citizen --> UC3
    volunteer --> UC3
    citizen --> UC4
    volunteer --> UC4
    citizen --> UC5
    volunteer --> UC5
    citizen -.-> BLOCK
    volunteer -.-> BLOCK
```

Sign-up creates only a CITIZEN or VOLUNTEER account (default CITIZEN). Asking for any other role returns 400. An already used e-mail returns 409 `EMAIL_TAKEN`.

## 2. Staff roles

```mermaid
flowchart LR
    admin([Administrator])
    officer([Relief officer])
    wh([Warehouse manager])
    field([Field worker])
    ngo([NGO coordinator])

    subgraph DISASTER["Disasters and requests"]
        D1(Create disaster or affected area)
        D2(View disasters, map, dashboard)
        D3(Submit need request)
        D4(Verify, approve, reject, cancel request)
        D5(Override priority score)
    end
    subgraph LOGISTICS["Stock and delivery"]
        L1(View inventory and warehouses)
        L2(Receive stock)
        L3(Recommend allocation)
        L4(Approve or reject allocation)
        L5(Create dispatch)
        L6(Update delivery status)
    end
    subgraph PEOPLE["Shelters, people and partners"]
        S1(View shelters)
        S2(Add shelter)
        S3(Update shelter occupancy)
        V1(Manage volunteers and assignments)
        P1(Offer partner contribution)
        P2(Accept or decline contribution)
    end
    subgraph INSIGHT["Insight and control"]
        I1(View and acknowledge alerts)
        I2(Run report analyzer)
        I3(Review analyzer result)
        I4(Analytics, shortages, situation PDF)
        I5(View audit log)
        I6(System status)
    end

    admin --> D1 & D2 & L1 & L2 & L5 & L6 & S1 & S2 & S3 & V1 & P1 & P2 & I1 & I2 & I3 & I4 & I5 & I6
    officer --> D1 & D2 & D3 & D4 & D5 & L1 & L3 & L4 & L6 & S1 & S2 & S3 & V1 & P2 & I1 & I2 & I3 & I4 & I5
    wh --> D2 & L1 & L2 & L5 & L6 & S1 & I1 & I2
    field --> D2 & D3 & L6 & S1 & S3 & I1 & I2
    ngo --> D2 & D3 & S1 & V1 & P1 & I1 & I2
```

Notes from the code:
- Request approval and allocation approval are different actions, by OFFICER only (ADMIN excluded, open decision O1).
- `POST /api/requests` is limited to OFFICER, FIELD_WORKER and NGO. Public citizen reporting is open decision O4 and is not built.
- Dispatch creation is ADMIN and WAREHOUSE_MANAGER; delivery status changes also allow OFFICER and FIELD_WORKER.
- A warehouse manager acts only on their own warehouse, and a volunteer only on their own record (`SECURITY_REVIEW.md`).
- Individual role lists were read from the controllers; confirm any single cell against the controller before relying on it.
