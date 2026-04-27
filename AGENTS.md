# AGENTS.md

## Project
This project designs and implements the given requirements for a course enrollment system using Spring Boot.

The main goal is to build a clear and maintainable backend that correctly handles:
- course management
- enrollment management
- status transitions
- capacity rules

## Scope
Core domains:
- users
- courses
- enrollments

Optional features may be added later

## Stack
- Java 17, Spring Boot, Data JPA
- MySQL
- Gradle

## Architecture
Use a layered architecture.

Prefer simple and explainable structure over unnecessary abstraction.

## Domain Rules
Important rules:
- course status: DRAFT, OPEN, CLOSED
- enrollment status: PENDING, CONFIRMED, CANCELLED
- only OPEN courses can be enrolled in
- active enrollment count includes PENDING and CONFIRMED
- CANCELLED does not count toward capacity
- payment confirmation is modeled as status change only
- cancellation must follow business conditions
- role check and ownership check are separate concerns

## Authorization
Authentication is intentionally simplified for this project.

Current user identification:
- identify user by request header

Still required:
- enforce role checks
- enforce ownership checks in the service layer

Examples:
- only CREATOR can create courses
- only the creator who owns the course can change that course
- only the student who owns the enrollment can cancel it

## Response Language
- Default to Korean in all responses.
- Switch language only if the user explicitly requests another language.

If reviewing code, follow:
- `skills/code-review.md`