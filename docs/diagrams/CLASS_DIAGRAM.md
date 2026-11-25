# Class diagram

Packages are `com.disasterrelief.backend` (Spring Boot REST API, port 8080) and `com.disasterrelief.frontend` (Vaadin Flow UI, port 8081). Only the main classes are drawn; each backend feature package follows the same layering: Controller, Service, Repository.

## 1. Backend layering (shared pattern)

```mermaid
classDiagram
    direction LR
    class Controller {
        +REST endpoint methods
        role rule via PreAuthorize
    }
    class Service {
        +business rules
        +Transactional methods
    }
    class Rules {
        +static validation and policy
    }
    class Repository {
        +SQL via JdbcClient or JPA
    }
    class ApiException {
        +HttpStatus status
        +String code
        +validation(...)
        +conflict(code, message)
        +unauthenticated(message)
    }
    class GlobalExceptionHandler {
        +maps ApiException to problem+json
    }
    class PageResponse~T~ {
        +List~T~ content
        +int page
        +int size
        +long totalElements
        +int totalPages
    }
    Controller --> Service : calls
    Service --> Rules : uses
    Service --> Repository : calls
    Service ..> ApiException : throws
    GlobalExceptionHandler ..> ApiException : handles
    Service ..> PageResponse : returns
```

Feature packages using this pattern: `disaster`, `affectedarea`, `request`, `priority`, `allocation`, `dispatch`, `inventory`, `warehouse`, `resource`, `shelter`, `volunteer`, `partner`, `ai`, `alert`, `audit`, `analytics`, `report`, `map`, `dashboard`, `system`.

## 2. Authentication, sign-up and security (backend)

```mermaid
classDiagram
    direction LR
    class AuthController {
        +login(LoginRequest, HttpServletRequest) LoginResponse
        +register(RegisterRequest) LoginResponse
        +me(Jwt) UserSummary
    }
    class AuthService {
        -Set~Role~ SELF_SERVICE_ROLES
        +login(LoginRequest) LoginResponse
        +register(RegisterRequest) LoginResponse
        +currentUser(long) UserSummary
    }
    class UserRepository {
        <<interface>>
        +findByEmailIgnoreCase(String) Optional~AppUser~
    }
    class AppUser {
        -Long id
        -String fullName
        -String email
        -String passwordHash
        -Role role
        -boolean enabled
        -String preferredLanguage
    }
    class Role {
        <<enumeration>>
        ADMIN
        OFFICER
        WAREHOUSE_MANAGER
        FIELD_WORKER
        NGO
        VOLUNTEER
        CITIZEN
    }
    class LoginRequest {
        +String email
        +String password
    }
    class RegisterRequest {
        +String fullName
        +String email
        +String password
        +Role role
    }
    class LoginResponse {
        +String accessToken
        +String tokenType
        +long expiresIn
        +UserSummary user
    }
    class UserSummary {
        +Long id
        +String fullName
        +String email
        +Role role
    }
    class JwtService {
        +issue(AppUser) IssuedToken
    }
    class LoginThrottle {
        +blocked(email, address, now) boolean
        +recordFailure(...)
        +recordSuccess(email)
    }
    class SecurityConfig {
        +PasswordEncoder passwordEncoder()
        +SecurityFilterChain securityFilterChain()
        permits login, register, health
    }
    class StaffOnly {
        <<annotation>>
        ADMIN OFFICER WAREHOUSE_MANAGER FIELD_WORKER NGO
    }
    class AdminOrOfficer {
        <<annotation>>
    }
    AuthController --> AuthService
    AuthController --> LoginThrottle : login only
    AuthService --> UserRepository
    AuthService --> JwtService
    AuthService ..> LoginRequest
    AuthService ..> RegisterRequest
    AuthService ..> LoginResponse
    UserRepository --> AppUser
    AppUser --> Role
    LoginResponse --> UserSummary
    SecurityConfig ..> StaffOnly : method security
    SecurityConfig ..> AdminOrOfficer : method security
```

`register` rejects any role outside `SELF_SERVICE_ROLES` (CITIZEN, VOLUNTEER), rejects a duplicate e-mail with 409 `EMAIL_TAKEN`, stores a BCrypt hash, and returns the same token response as `login`.

## 3. Relief domain model

```mermaid
classDiagram
    direction LR
    class Disaster {
        +name
        +disasterType
        +severity
        +status
    }
    class AffectedArea {
        +name
        +district
        +populationEstimate
        +impactLevel
    }
    class Resource {
        +name
        +category
        +unit
        +perishable
    }
    class NeedRequest {
        +quantity
        +severity
        +RequestStatus status
        +priorityScore
        +priorityCategory
    }
    class RequestStatus {
        <<enumeration>>
        PENDING
        VERIFIED
        APPROVED
        ALLOCATED
        DISPATCHED
        IN_TRANSIT
        DELIVERED
        REJECTED
        CANCELLED
    }
    class RequestHistory {
        +action
        +fromStatus
        +toStatus
        +reason
    }
    class Warehouse {
        +code
        +name
        +status
    }
    class Inventory {
        +batchNumber
        +expiryDate
        +quantityOnHand
        +quantityReserved
    }
    class InventoryTransaction {
        +transactionType
        +quantityDeltaOnHand
        +quantityDeltaReserved
    }
    class Allocation {
        +status
        +requestedQty
        +allocatedQty
        +version
        +explanation
    }
    class AllocationItem {
        +quantity
        +distanceKm
        +reason
    }
    class Dispatch {
        +status
        +quantity
        +vehicle
        +deliveredAt
    }
    class DeliveryUpdate {
        +status
        +note
    }
    class Shelter {
        +name
        +capacityPeople
        +currentOccupancy
        +status
        +isAccessible
    }
    class Volunteer {
        +skills
        +availability
    }
    class VolunteerAssignment {
        +task
        +status
    }
    class PartnerContribution {
        +quantity
        +status
    }
    class AiAnalysis {
        +reportText
        +resultJson
        +reviewStatus
    }
    class Alert {
        +type
        +severity
        +status
    }
    class AuditLog {
        +action
        +entityType
        +details
    }
    class AppUser
    class Organization

    Disaster "1" --> "*" AffectedArea
    AffectedArea "1" --> "*" NeedRequest
    Resource "1" --> "*" NeedRequest
    AppUser "1" --> "*" NeedRequest : requests
    NeedRequest --> RequestStatus
    NeedRequest "1" --> "*" RequestHistory
    NeedRequest "1" --> "*" Allocation
    Allocation "1" --> "*" AllocationItem
    AllocationItem "*" --> "1" Warehouse
    Warehouse "1" --> "*" Inventory
    Resource "1" --> "*" Inventory
    Inventory "1" --> "*" InventoryTransaction
    Allocation "1" --> "*" Dispatch
    Dispatch "1" --> "*" DeliveryUpdate
    Organization "1" --> "*" AppUser
    Organization "1" --> "*" Shelter
    Organization "1" --> "*" Warehouse
    Organization "1" --> "*" PartnerContribution
    Volunteer "1" --> "*" VolunteerAssignment
    Disaster "1" --> "*" VolunteerAssignment
    AppUser "1" --> "*" AuditLog
    AppUser "1" --> "*" AiAnalysis
```

These are persistence concepts (tables, see `ER_DIAGRAM.md`). The backend uses JDBC repositories and DTO records for most of them; only `AppUser` is a JPA entity.

## 4. Frontend (Vaadin)

```mermaid
classDiagram
    direction LR
    class LoginView {
        +route login
        -attemptLogin()
    }
    class RegisterView {
        +route register
        -attemptRegister()
    }
    class CitizenHomeView {
        +route home
        lists open shelters
    }
    class MainLayout {
        -sidebar(role)
        -topbar(user, role)
        staff menu or Home only
    }
    class AppView {
        <<abstract>>
        #allowed(role) boolean
        #enter(event)
        #token() String
    }
    class DashboardView
    class SheltersView
    class RequestListView
    class MapGrid {
        +text(...)
        +col(...)
        +loader(...)
        +reload(page)
    }
    class AuthGuard {
        +beforeEnter(event)
        login and register are public
        non-staff go to CitizenHomeView
    }
    class Roles {
        +STAFF
        +DECIDERS
        +LOGISTICS
        +in(role, allowed) boolean
    }
    class UserSession {
        +login(LoginResult)
        +current() Optional~Login~
        +token() String
        +user() AuthUser
        +logout()
    }
    class AuthClient {
        +login(email, password) LoginResult
        +register(name, email, password, role) LoginResult
    }
    class Api {
        +get(token, path, ...)
        +page(token, path, page, size, ...)
        +patch(token, path, body)
    }
    class BackendException {
        +isUnauthenticated()
        +isForbidden()
    }
    class LoginResult {
        +accessToken
        +AuthUser user
    }
    class AuthUser {
        +id
        +fullName
        +email
        +role
    }
    AppView <|-- CitizenHomeView
    AppView <|-- DashboardView
    AppView <|-- SheltersView
    CitizenHomeView --> MapGrid
    SheltersView --> MapGrid
    LoginView --> AuthClient
    RegisterView --> AuthClient
    LoginView --> UserSession
    RegisterView --> UserSession
    AuthClient --> LoginResult
    LoginResult --> AuthUser
    AuthGuard --> UserSession
    AuthGuard --> Roles
    MainLayout --> Roles
    MainLayout --> UserSession
    AppView --> UserSession
    AppView --> Api
    Api ..> BackendException : throws
    AuthClient ..> BackendException : throws
```

The JWT is held in `UserSession` on the server side and is never sent to the browser. `Roles` only decides what the UI shows; the backend enforces the real permission on every call.
