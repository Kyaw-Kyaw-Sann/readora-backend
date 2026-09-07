# AGENTS.md — Readora Backend Debugging Context


## Interaction Rules

- The user may ask questions in English, but always respond in Myanmar language.

- Never add, update, remove, rename, move, fix, or modify any code, file, folder, configuration, dependency, or project structure without explicit permission.

- Do not make any project changes unless the user explicitly says the exact phrase:

  "Build Now"

- Before the phrase "Build Now" is given, only:
  - discuss
  - explain
  - review
  - plan
  - suggest
  - provide commands or code snippets without applying them

- When the user says "Build Now", you may make only the changes that were discussed or explicitly requested.

- Do not interpret similar phrases such as "go ahead", "continue", "start", "do it", or "proceed" as permission to modify the project.

- If "Build Now" has not been explicitly provided, do not modify the project.


## Purpose

You are working inside the Readora backend repository.

Your immediate task is to diagnose and fix a recurring Spring Boot + Hibernate + PostgreSQL error affecting simple GET endpoints.

Do not make broad refactors. Preserve the existing architecture, naming, DTO style, package structure, endpoint contracts, and business rules unless a change is clearly required to fix the bug.

The user wants a simple, maintainable, junior-friendly solution that can be explained in an interview.

---

# 1. Current Critical Problem

Several GET APIs unexpectedly return HTTP 500 even though they previously worked.

Affected endpoints currently include:

```text
GET /api/books
GET /api/books/new
GET /api/admin/books?page=0&size=10
GET /api/admin/subscriptions
```

Postman response:

```json
{
  "success": false,
  "message": "Something went wrong",
  "data": null
}
```

Spring Boot / Hibernate log:

```text
SQLState: 42883
ERROR: function lower(bytea) does not exist
Hint: No function matches the given name and argument types.
```

Example positions seen in logs:

```text
Position: 451
Position: 479
```

The same project previously had a very similar PostgreSQL/Hibernate parameter-binding problem in Admin User search queries.

The previous successful fix was to stop using a single JPQL query with a nullable String search parameter such as:

```java
:search IS NULL
OR LOWER(u.name) LIKE ...
```

and instead use two repository methods:

```text
no search   -> query with no String search parameter at all
with search -> query with a guaranteed non-null searchPattern
```

Do not assume that is definitely the only current root cause. Inspect the exact SQL Hibernate executes.

---

# 2. Debugging Goal

Find the exact JPQL/HQL/repository method causing PostgreSQL to receive:

```sql
LOWER(bytea)
```

or an equivalent invalid expression.

The correct fix should eliminate the root cause, not just hide the error.

Before editing code:

1. Identify which repository method actually executes for each failing endpoint.
2. Turn on SQL/bind logging if needed.
3. Inspect the generated SQL immediately before the exception.
4. Trace each bound parameter and its Java type.
5. Search the entire project for risky nullable String search patterns.
6. Verify database column types for text fields used inside `LOWER(...)`.

Useful temporary logging:

```properties
spring.jpa.show-sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE
```

Remove or reduce noisy TRACE logging after debugging.

Useful global searches:

```text
LOWER(
LOWER(CONCAT
:search IS NULL
LIKE :search
LIKE :searchPattern
CONCAT('%'
```

Also inspect native queries if any exist.

---

# 3. Important Existing Fix Pattern

For optional text search, prefer this pattern in this project.

Bad / risky pattern:

```java
@Query("""
    SELECT x
    FROM Entity x
    WHERE (
        :search IS NULL
        OR LOWER(x.name) LIKE LOWER(CONCAT('%', :search, '%'))
    )
""")
Page<Entity> search(@Param("search") String search, Pageable pageable);
```

Preferred pattern:

```java
@Query("""
    SELECT x
    FROM Entity x
    WHERE ...
""")
Page<Entity> findWithoutSearch(..., Pageable pageable);
```

and:

```java
@Query("""
    SELECT x
    FROM Entity x
    WHERE LOWER(x.name) LIKE :searchPattern
    AND ...
""")
Page<Entity> findWithSearch(
    @Param("searchPattern") String searchPattern,
    ...,
    Pageable pageable
);
```

Service:

```java
String normalizedSearch = normalizeSearch(search);

if (normalizedSearch == null) {
    return repository.findWithoutSearch(...);
}

String searchPattern = "%" + normalizedSearch + "%";
return repository.findWithSearch(searchPattern, ...);
```

Normalization:

```java
private String normalizeSearch(String search) {
    if (search == null || search.isBlank()) {
        return null;
    }

    return search.trim().toLowerCase();
}
```

This pattern already solved a PostgreSQL `bytea` search-binding problem earlier in this project.

---

# 4. Current BookRepository Context

Package:

```text
com.readora.backend.repository
```

Current `BookRepository` is intended to contain:

```java
public interface BookRepository extends JpaRepository<Book, Long>
```

Existing methods include:

```java
boolean existsByIsbn(String isbn);
boolean existsByIsbnAndIdNot(String isbn, Long id);
Optional<Book> findByIdAndStatus(Long id, BookStatus status);
List<Book> findAllByStatus(BookStatus status);
List<Book> findTop10ByStatusOrderByViewCountDescCreatedAtDesc(BookStatus status);
long countByStatus(BookStatus status);
long countByAccessType(BookAccessType accessType);
```

Public discovery was already changed to split search/no-search:

```java
Page<Book> findPublishedBooks(
    BookAccessType accessType,
    Long categoryId,
    Pageable pageable
);
```

and:

```java
Page<Book> findPublishedBooksBySearch(
    String searchPattern,
    BookAccessType accessType,
    Long categoryId,
    Pageable pageable
);
```

The public search query should only have:

```java
LOWER(b.title) LIKE :searchPattern
OR LOWER(b.author) LIKE :searchPattern
```

The no-search public query should contain no String search parameter.

Admin books originally still used the risky single-query pattern:

```java
WHERE (
    :search IS NULL
    OR LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%'))
)
```

That has now been changed/intended to be changed to:

```java
findAdminBooks(...)
```

with no search String parameter, and:

```java
findAdminBooksBySearch(...)
```

with a non-null `searchPattern`.

Verify the actual repository on disk matches this intended design.

---

# 5. Current BookService Context

Package:

```text
com.readora.backend.service
```

Admin list method:

```java
@Transactional(readOnly = true)
public PageResponse<BookSummaryResponse> getAdminBooks(
    String search,
    BookStatus status,
    BookAccessType accessType,
    Long categoryId,
    int page,
    int size
)
```

Desired behavior:

```java
String normalizedSearch = normalizeSearch(search);

if (normalizedSearch == null) {
    bookPage = bookRepository.findAdminBooks(
        status,
        accessType,
        categoryId,
        pageable
    );
} else {
    String searchPattern = "%" + normalizedSearch + "%";

    bookPage = bookRepository.findAdminBooksBySearch(
        searchPattern,
        status,
        accessType,
        categoryId,
        pageable
    );
}
```

Keep `normalizeText(...)` for normal fields such as ISBN/description/language.

Use a separate `normalizeSearch(...)` helper for search.

Public book discovery service already follows the same split-search approach and should be inspected to ensure the source on disk is actually the updated version.

---

# 6. Current SubscriptionRepository Context

Package:

```text
com.readora.backend.repository
```

Current repository intentionally has separate search/no-search admin methods.

Existing core methods:

```java
Optional<Subscription> findTopByUser_IdOrderByStartedAtDesc(Long userId);
Optional<Subscription> findTopByUser_IdAndStatusOrderByStartedAtDesc(
    Long userId,
    SubscriptionStatus status
);
List<Subscription> findAllByUser_IdOrderByStartedAtDesc(Long userId);
```

Admin no-search method:

```java
Page<Subscription> findAdminSubscriptions(
    SubscriptionStatus status,
    SubscriptionPlan plan,
    LocalDateTime now,
    Pageable pageable
);
```

Admin search method:

```java
Page<Subscription> findAdminSubscriptionsBySearch(
    String searchPattern,
    SubscriptionStatus status,
    SubscriptionPlan plan,
    LocalDateTime now,
    Pageable pageable
);
```

The no-search query contains no `LOWER()` call.

The search query contains:

```java
LOWER(s.user.name) LIKE :searchPattern
OR LOWER(s.user.email) LIKE :searchPattern
```

and `searchPattern` should always be non-null.

Subscription repository also includes dashboard count methods:

```java
long countByPlan(SubscriptionPlan plan);
long countByStatus(SubscriptionStatus status);
long countEffectiveActive(LocalDateTime now);
long countEffectiveExpired(LocalDateTime now);
```

If `GET /api/admin/subscriptions` without a search term still produces `lower(bytea)`, do not blindly rewrite these methods.

Instead determine why a query containing `LOWER(...)` is executing on the no-search path.

Possible causes to investigate:

```text
- stale compiled code
- wrong method overload being called
- another repository query triggered during mapping
- lazy-loading side effect
- custom mapper/service invoking another query
- outdated source vs running build
- duplicate classes/files
- query generated from another endpoint/filter
```

---

# 7. Current AdminSubscriptionService Context

Package:

```text
com.readora.backend.service
```

Current behavior:

```java
String normalizedSearch = normalizeSearch(search);
LocalDateTime now = LocalDateTime.now();
Pageable pageable = PageRequest.of(
    page,
    size,
    Sort.by(Sort.Direction.DESC, "startedAt")
);

if (normalizedSearch == null) {
    subscriptionPage = subscriptionRepository.findAdminSubscriptions(
        status,
        plan,
        now,
        pageable
    );
} else {
    String searchPattern = "%" + normalizedSearch + "%";

    subscriptionPage = subscriptionRepository.findAdminSubscriptionsBySearch(
        searchPattern,
        status,
        plan,
        now,
        pageable
    );
}
```

This is already the preferred split-query approach.

Again, inspect actual runtime behavior instead of assuming this file must be changed.

---

# 8. PostgreSQL Checks

Verify text columns are actually text/varchar, especially fields used by `LOWER(...)`.

Books:

```sql
SELECT
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'books'
AND column_name IN ('title', 'author');
```

Expected:

```text
title  -> character varying or text
author -> character varying or text
```

Users:

```sql
SELECT
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'users'
AND column_name IN ('name', 'email');
```

Expected:

```text
name  -> character varying or text
email -> character varying or text
```

Also inspect any other fields passed into `LOWER(...)`.

If a column is actually `bytea`, find out why before changing the database.

Do not apply arbitrary SQL casts unless the model/schema is genuinely intended to store textual data and the cast is the correct long-term fix.

---

# 9. Build / Stale Class Checks

The project previously worked after source changes, then similar failures appeared again.

Confirm the application is running freshly compiled classes.

Recommended Windows flow:

```bash
mvnw.cmd clean
mvnw.cmd compile
mvnw.cmd spring-boot:run
```

If necessary, PowerShell:

```powershell
Remove-Item -Recurse -Force target
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

Check for:

```text
- multiple backend processes running on port 8080
- IDE launching a different module
- duplicate repository source files
- stale generated/build output
- wrong active Spring profile
- wrong database instance
```

If useful, identify the PID listening on port 8080 before restart.

---

# 10. Readora Project Overview

Readora is a personalized digital-library portfolio project.

Main products:

```text
React Native mobile app
Next.js landing/admin app
Spring Boot REST API
PostgreSQL database
Cloudinary media storage
```

Core content:

```text
PDF books
Audiobooks
Books may have PDF, audio, or both
```

Main user features:

```text
local authentication
Google sign-in
email verification
password reset
profile
interests
FREE / PREMIUM book access
mock monthly/yearly subscriptions
favorites
reading progress
listening progress
reviews
search/filter/pagination
recommendations
personal library
```

Admin features:

```text
category management
book management
user management
review moderation
subscription viewing/filtering
dashboard statistics
```

Project intentionally excludes:

```text
real payments
refunds
ML recommendation models
DRM
offline mode
advanced background audio
social/chat
microservices
Kubernetes
```

---

# 11. Backend Technology and Conventions

Backend:

```text
Java 21
Spring Boot
Maven
PostgreSQL
Spring Data JPA / Hibernate
Spring Security
JWT
Lombok
SpringDoc/OpenAPI
Cloudinary
```

Base package:

```text
com.readora.backend
```

DTO convention:

```text
All DTOs are Java Records.
```

Controller response convention:

```java
ApiResponse<T>
```

Pagination response:

```java
PageResponse<T>
```

Exceptions currently used:

```text
BadRequestException
UnauthorizedException
ResourceNotFoundException
ForbiddenException
```

Mappers:

```text
static mapper classes
no MapStruct
```

Controller documentation:

```text
@Tag on every controller
@Operation(summary = "...") on every endpoint
```

Validation:

```text
@Valid on request bodies
```

Transactions:

```text
@Transactional for writes
@Transactional(readOnly = true) for reads
```

Exception handling note:

The current generic exception handler may turn unexpected exceptions into:

```json
{
  "success": false,
  "message": "Something went wrong",
  "data": null
}
```

That is why the full terminal stack trace is important for debugging.

---

# 12. Security Context

Security is stateless JWT.

General rules:

```text
/api/auth/** -> public
OpenAPI endpoints -> public
/api/admin/** -> ADMIN role
other protected endpoints -> authenticated
```

Do not change security configuration unless debugging clearly proves it is related to the issue.

The current `500 lower(bytea)` error is expected to be a persistence/query problem, not an authorization problem.

---

# 13. Important Enums

```java
AuthProvider { LOCAL, GOOGLE }
Role { USER, ADMIN }
BookAccessType { FREE, PREMIUM }
BookStatus { DRAFT, PUBLISHED, ARCHIVED }
SubscriptionPlan { MONTHLY, YEARLY }
SubscriptionStatus { ACTIVE, EXPIRED, CANCELLED }
```

There is no `LIFETIME` plan.

---

# 14. Important Entity Behavior

## User

Important fields include:

```text
id
name
email
password
provider
emailVerified
role
profileImageUrl
interests
```

## Book

Important fields include:

```text
id
title
description
isbn
language
publicationDate
author
coverUrl
coverPublicId
pdfUrl
pdfPublicId
audioUrl
audioPublicId
pageCount
audioDurationSeconds
accessType
status
viewCount
categories
createdAt
updatedAt
```

## Subscription

Important fields include:

```text
id
user
plan
status
startedAt
expiresAt
cancelledAt
createdAt
updatedAt
```

Effective premium rule:

```text
status == ACTIVE
AND expiresAt > now
```

A stale database row may still have:

```text
status = ACTIVE
expiresAt <= now
```

but should be treated effectively as expired.

## Review

Important fields:

```text
id
user
book
rating
comment
createdAt
updatedAt
```

One review per user/book.

---

# 15. Relevant Completed Backend Phases

The backend is already far along.

Completed areas include:

```text
Authentication
Categories
Profile
Interests
Cloudinary
Mock subscriptions
Admin book management
Book discovery
Book access control
Favorites
Reading progress
Listening progress
Reviews
Recommendations
Personal library
Admin users
Admin reviews/subscriptions
Admin dashboard
```

Do not rewrite working modules while fixing this persistence issue.

---

# 16. Book Discovery Behavior

Public list endpoint:

```text
GET /api/books
```

Supports:

```text
search
categoryId
accessType
sort
page
size
```

Sort enum:

```java
NEWEST
POPULAR
TITLE_ASC
TITLE_DESC
```

Convenience endpoints include:

```text
GET /api/books/free
GET /api/books/premium
GET /api/books/new
GET /api/books/popular
```

These may internally reuse the same `getBooks(...)` service path.

That means one broken repository query can make several convenience endpoints fail together.

Public discovery only returns:

```text
BookStatus.PUBLISHED
```

---

# 17. Admin Book Behavior

Admin APIs include list/detail/create/update/publish/archive.

Admin list supports:

```text
search
status
accessType
categoryId
page
size
```

Admin list should include DRAFT/PUBLISHED/ARCHIVED depending on filters.

Do not hard-delete books.

---

# 18. Subscription Behavior

Subscriptions are mock subscriptions only.

Plans:

```text
MONTHLY
YEARLY
```

Statuses:

```text
ACTIVE
EXPIRED
CANCELLED
```

Cancellation is immediate.

Only effective active subscriptions grant premium access.

Admin subscription management is read-only:

```text
GET /api/admin/subscriptions
```

Supports:

```text
search user name/email
status
plan
page
size
```

No admin mutation actions for subscription are intended.

---

# 19. Admin Dashboard

Dashboard endpoint:

```text
GET /api/admin/dashboard
```

Aggregates counts for:

```text
users
books
reviews
subscriptions
```

Subscription stats use effective active/expired semantics.

Do not break dashboard repository methods while fixing admin subscriptions.

---

# 20. Search Bug History

This project has already experienced PostgreSQL errors such as:

```text
function lower(bytea) does not exist
```

and:

```text
operator does not exist: text ~~ bytea
```

The previous confirmed working solution for Admin User Management was:

```text
do not use nullable :search in one LIKE query

instead:
findAdminUsers(...)
findAdminUsersBySearch(...)
```

The user confirmed that approach removed the error.

Use this history as a strong clue, but still inspect actual SQL because the current failures include an endpoint whose no-search repository method appears not to contain `LOWER()`.

---

# 21. What the Agent Should Do Now

Perform the debugging work directly in the repository.

Recommended sequence:

```text
1. Inspect current source files on disk.
2. Search the entire project for LOWER / LIKE / nullable String search JPQL.
3. Confirm BookRepository and BookService actually contain the latest split-query changes.
4. Confirm BookDiscoveryService actually uses split search/no-search methods.
5. Confirm SubscriptionRepository/AdminSubscriptionService match the intended code.
6. Enable Hibernate SQL + bind logging temporarily.
7. Reproduce exactly one failing endpoint at a time.
8. Capture the SQL generated immediately before PostgreSQL throws 42883.
9. Identify which expression is being treated as bytea.
10. Verify DB column types.
11. Check for stale classes/processes/modules.
12. Make the smallest correct fix.
13. Compile.
14. Restart from a clean build.
15. Retest all affected endpoints.
16. Remove temporary noisy debugging configuration if added.
```

Retest:

```text
GET /api/books
GET /api/books?search=clean
GET /api/books/new
GET /api/books/popular

GET /api/admin/books?page=0&size=10
GET /api/admin/books?search=java&page=0&size=10

GET /api/admin/subscriptions
GET /api/admin/subscriptions?search=john
```

Also verify representative unaffected endpoints still work.

---

# 22. Expected Agent Output

After fixing the issue, report:

```text
Root cause
Exact repository/service/query involved
Why PostgreSQL inferred bytea
Files changed
What was changed
Why the fix is safe
How it was tested
Which endpoints now return 200
Any remaining risk
```

If you cannot reproduce the issue, do not invent a fix.

Instead report:

```text
what was inspected
what SQL was observed
what could not be reproduced
what additional runtime evidence is needed
```

---

# 23. Implementation Style Rules

Keep changes simple.

Do not:

```text
introduce QueryDSL
introduce Specifications unless truly necessary
add MapStruct
rewrite repositories wholesale
change entity relationships unnecessarily
change endpoint URLs
change API response format
add new architecture layers
change unrelated business rules
upgrade framework versions just to attempt a fix
```

Prefer a small repository/service correction that is easy to explain.

When modifying an existing file, preserve unrelated methods exactly unless a change is required.

---

# 24. Final Success Criteria

The fix is complete when all of these are true:

```text
GET /api/books -> 200
GET /api/books/new -> 200
GET /api/admin/books?page=0&size=10 -> 200
GET /api/admin/subscriptions -> 200

search variants also work
pagination still works
filters still work
no PostgreSQL lower(bytea) error remains
project compiles cleanly
no unrelated API regressions
```

The root cause must be understood and documented, not merely bypassed.
