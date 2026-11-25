# Security review (Day 9) - what was checked and what is still open

Scope: the Spring Boot backend as of Day 9. This is a code review plus automated tests, not a penetration test.

## In place and tested
| Area | What the code does | Test |
|---|---|---|
| Authentication | Stateless JWT (HS256), 1-hour life, secret only from `JWT_SECRET` | `JwtServiceTest`, `ApiSecurityIntegrationTest` |
| Authorization | Every endpoint carries a role rule enforced on the server (`@PreAuthorize`/annotations); a missing token gives 401, a wrong role 403 | `SecurityConfigTest`, integration tests per module |
| Ownership | A warehouse manager can only receive or dispatch for their own warehouse; a volunteer only touches their own record | `InventoryServiceTest`, `DispatchServiceTest`, `VolunteerServiceTest` |
| SQL injection | All SQL uses bound parameters; the only text added to SQL is `ORDER BY`, chosen from a fixed whitelist (unknown sort gives 400) | `PageQueryTest`, `inventoryIsRoleRestricted` |
| Brute force | 5 failed sign-ins per e-mail or 30 per address in 15 minutes give 429 | `LoginThrottleTest`, `loginIsThrottledAfterRepeatedFailures` |
| Concurrency | Two officers cannot reserve the same stock (row locks, guarded updates, database CHECK) | `twoOfficersCannotOverAllocateTheSameStock` |
| Audit | `audit_logs` is append-only (database trigger); who/what/when is written in the same transaction as the change | `day7_checks.sql` |
| Personal data and AI | Phone, e-mail, Aadhaar-style and PAN numbers are removed before storage and before the text goes to the model; the key is read from `AI_API_KEY`; output must match a strict schema; a result never changes a request without a person | `PiiScrubberTest`, `AnalysisParserTest`, `AnalysisServiceTest` |
| Error leakage | Unexpected errors return a generic 500 problem document; the stack trace stays in the server log | `GlobalExceptionHandler` |
| Secrets | No password, key or secret in the repository; `.env` is git-ignored | manual review |

## Done since the first review
- Demo data cannot reach production: profile `prod` loads migrations only, and `DemoDataGuard` stops the server if the `dev` profile is also active or if demo users exist (`DemoDataGuardTest`).
- Dependency and code scanning: `.github/dependabot.yml` (weekly updates for backend, frontend and workflows) and `.github/workflows/codeql.yml` (static analysis of the Java code). One manual step remains: switch on "Dependabot alerts" and "Dependabot security updates" in the repository's Settings, Code security page. Review the first scan results; no penetration test has been run.

## Open items (not done, owner in brackets)
1. Login throttle and the analyzer rate limit are in memory: they reset on restart and do not work across several servers. **Accepted for the prototype** (one server). Before running more than one instance, store them in the database (backend/Divay).
2. No token revocation or refresh: a stolen token works until it expires (1 hour), and a disabled account keeps working until then. **Accepted for the prototype.** Before public use, add a per-user token version checked on each request, a logout that bumps it, and refresh tokens (backend). This changes sign-in code, so it needs a decision on token lifetime first.
3. Demo users share a published password. The `dev` profile seeds them; `prod` now refuses them (done above).
4. HTTPS, CORS origins, database backups and secret storage belong to deployment (Divay). Nothing in the backend forces HTTPS.
5. Names and addresses typed into free text cannot be removed by the scrubber; staff must not paste them into the analyzer (training/UI warning).
6. The Helvetica PDF font prints only Latin characters; any non-Latin text appears as `?`. The user interface is English only, so this matters only if someone types non-Latin text into a field (backend).
7. The proposed rules in `PROJECT_STATUS.md` (priority weights, fairness cap, who may approve) are assumptions until Neha and Divay approve them (`APPROVALS.md`).
8. **Public sign-up has no rate limit, CAPTCHA or e-mail verification.** Anyone can create unlimited CITIZEN or VOLUNTEER accounts and can type any e-mail address. Role is restricted server-side to those two roles (`AuthServiceTest`), so this cannot be used to gain staff access, but it can fill the `users` table. Before public use: throttle `POST /api/auth/register` per address (reuse the `LoginThrottle` approach, stored in the database), add e-mail verification, and consider a CAPTCHA (backend).
9. Sign-up reveals whether an e-mail is registered (409 `EMAIL_TAKEN`), unlike login, which hides it. Accepted because a sign-up form has to say so; the throttle in item 8 limits harvesting.
10. `GET /api/shelters/open` is readable by any signed-in user, including self-registered ones. It returns only name, address, area, free beds, accessibility and facilities; occupancy, capacity and coordinates stay staff-only. Confirm with Divay that shelter addresses may be shown to the public (`APPROVALS.md` E2).
