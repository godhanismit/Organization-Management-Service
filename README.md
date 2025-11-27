## Organization Management Service

Spring Boot + MongoDB backend that provisions organizations in a multi-tenant style. Each organization receives a dedicated Mongo collection (e.g. `org_[organization_name]`). Admins authenticate via JWT and can manage their organization lifecycle.

### Tech Stack

- Spring Boot 3 (Web, Validation, Security)
- MongoDB with dynamic collection creation
- JWT for stateless auth

### How to Run

1. Install Java 17+ and MongoDB (default port 27017).
2. Update `application.properties` if your Mongo URI or JWT secret differs.
3. Run locally:
   ```bash
   ./mvnw spring-boot:run
   ```

### Key Endpoints

| Method | Path               | Auth | Description                           |
| ------ | ------------------ | ---- | ------------------------------------- |
| POST   | `/org/create`      | ❌   | Provision organization + admin        |
| GET    | `/org/get`         | ❌   | Fetch org metadata by name            |
| PUT    | `/org/update`      | ✅   | Update org/admin + rename collection  |
| DELETE | `/org/delete`      | ✅   | Cascade delete org, admin, collection |
| POST   | `/org/admin/login` | ❌   | Admin login, returns JWT              |

All protected endpoints require `Authorization: Bearer <token>` from `/admin/login`.

### Request Examples

```http
POST /org/create
{
  "organizationName": "Acme Inc",
  "email": "admin@acme.com",
  "password": "Str0ngPass!"
}
```

```http
POST /org/admin/login
{
  "email": "admin@acme.com",
  "password": "Str0ngPass!"
}
```

### Module Layout (Class-based design)

| Layer       | Key Classes                                                                               | Responsibility                                                                                                       |
| ----------- | ----------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| API         | `OrganizationController`, `AuthController`                                                | Expose REST endpoints, trigger validation, translate HTTP ↔ service results                                          |
| Service     | `OrganizationService`, `AuthService`                                                      | Enforce business rules (unique names/emails), manage admin/org lifecycle, orchestrate Mongo collections, create JWTs |
| Domain/Repo | `Organization`, `AdminUser`, `OrganizationRepository`, `AdminUserRepository`              | Persist master metadata in Mongo (`organizations`, `admin_users`)                                                    |
| Security    | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtTokenService`, `AdminUserDetailsService` | Stateless JWT auth, BCrypt hashing, request filtering                                                                |
| Support     | `ValidationUtils`, DTOs, exceptions                                                       | Manual request validation, typed responses, consistent error payloads                                                |

### Design Notes

- **Master database + tenant collections:** Creating an organization always adds a row to the master `organizations` collection and spins up a private collection named `org_<organization>`. The admin user lives in `admin_users` with a link back to the org. This gives us one place to track metadata while each tenant still gets isolated data.
- **JWT-based security:** The login endpoint issues a signed token that stores the admin ID and org ID. Every protected call passes through the JWT filter, so we never keep server-side sessions and can scale the API easily.
- **Simple validation helpers:** `ValidationUtils` does the basic checks (non-empty names, valid emails, minimum password length). This keeps the code easy to follow for beginners.
- **Renaming organizations:** When the org name changes we create a fresh collection, copy the existing documents, and drop the old one. It’s safe for small datasets; on huge datasets you’d switch to Mongo’s `renameCollection` command or separate databases per tenant.
- **Easy to extend:** To add more tenant features, reuse the stored metadata (collection name + connection details) and point new repositories or services at the right `org_<name>` collection.
- **Scalability + trade-offs:** This layout scales well for dozens or even hundreds of tenants because the API is stateless and each org has its own collection. The trade-off is that Mongo will still share one cluster, so very large tenants or heavy workloads might compete for I/O. At that point you could shard Mongo, move each org to its own database, or add a queue layer for slow operations like collection copies.
