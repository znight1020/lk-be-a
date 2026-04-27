# Code Review Guide

## 1. Review Principles
- Prefer correctness over cleverness.
- Prefer simple and explainable code over unnecessary abstraction.
- Review code based on current requirements, not hypothetical future extensions.
- The most important review target is whether the business rules are implemented correctly.

---

## 2. Formatting and Style
### Formatter
- Follow `style/code_formatter.xml`.
- Do not manually reformat code against the formatter result.
- Use LF line separator.
- Use 4 spaces for indentation.
- Do not use tabs.
- Keep line length within 120 characters when possible.

### Imports
- Do not use wildcard imports.
- Remove unused imports.
- Follow the formatter-defined import order.

### Naming
- Use meaningful names.
- Avoid ambiguous abbreviations.
- Class names should be noun-based.
- Method names should describe behavior clearly.
- DTO names must reflect request/response purpose.
- Exception names must describe the failure case clearly.

### Comments
- Write comments only when they explain intent, reason, or constraint.
- Do not write comments that merely restate the code.
- Remove outdated comments.

---

## 3. Architecture and Layering
### General
- Use a layered architecture.
- Prefer simple and explainable structure over unnecessary abstraction.

### Controller
- Controllers must not contain business logic.
- Controllers should handle request mapping, validation, current user identification, and response generation only.

### Service
- Business rules must be implemented in the service layer.
- State transitions, capacity validation, ownership checks, and cancellation rules belong in services.

### Repository
- Repositories are responsible for persistence concerns only.
- Do not place business rules in repositories.
- Complex cross-table reads may be separated into dedicated query methods or query classes.

### DTO
- Do not expose entities directly through API request/response.
- Separate request DTOs and response DTOs from entities.
- Use validation annotations on request DTOs when applicable.

### Entity
- Use enums for status and role values.
- Avoid unnecessary bidirectional relationships.
- Keep entity responsibilities minimal and focused.

---

## 4. Domain Rules Review
Review whether the implementation matches these rules.

### Course
- course status must be one of: `DRAFT`, `OPEN`, `CLOSED`
- only `OPEN` courses can be enrolled in

### Enrollment
- enrollment status must be one of: `PENDING`, `CONFIRMED`, `CANCELLED`
- payment confirmation is modeled as status change only
- cancellation must follow business conditions

### Capacity
- active enrollment count includes `PENDING` and `CONFIRMED`
- `CANCELLED` does not count toward capacity
- enrollment must be rejected if capacity is exceeded

### Authorization
- authentication is simplified by request header
- role checks are required
- ownership checks must be enforced in the service layer

Examples:
- only `CREATOR` can create courses
- only the creator who owns the course can change that course
- only the student who owns the enrollment can cancel it

---

## 5. Exception Handling
- Use exceptions with clear intent.
- Prefer domain-specific exceptions over generic exceptions.
- Keep error response format consistent.
- Check whether exception messages are understandable and appropriate.

---

## 6. Database and Query Review
- Use snake_case for table and column names.
- Review whether indexes match actual query patterns.
- Check whether database conditions match business rules.

Examples:
- active enrollment count should match `PENDING`, `CONFIRMED`
- `CANCELLED` should be excluded from capacity-related queries

---

## 7. Swagger Review
- Swagger should be kept minimal for review convenience.
- Avoid excessive documentation annotations in controllers.
- Prefer concise summaries over verbose API annotations.

---

## 8. Test Review
Review whether important business rules are covered by tests.

Priority targets:
- status transitions
- capacity checks
- duplicate enrollment prevention
- cancellation rules
- ownership checks
- concurrency-sensitive enrollment logic

---

## 9. Final Review Checklist
Before approving or accepting a change, check:

- does it match the current requirements?
- is business logic placed in the service layer?
- are role and ownership checks both handled correctly?
- are exception cases handled clearly?
- does the implementation match the current domain rules?
- are repository queries consistent with business conditions?
- are relevant tests added or updated?
- is the code easy to understand and explain?