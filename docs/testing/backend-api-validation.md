# Backend API Validation

## Purpose
Backend validation is required before frontend integration is considered complete.

## Minimum Validation Record
Every backend-affecting task should record the following for each validated endpoint or endpoint group:
- Endpoint
- Request Parameters
- Response Schema or observed response shape
- Success Case
- Error Case
- Database Verification when the endpoint writes data or changes state

## Required Workflow
1. Start from the task scope and identify changed backend behavior.
2. Run direct API validation or targeted backend tests against the changed endpoints.
3. Verify persistence state when the endpoint performs create, update, delete-like behavior, status transition, or operational toggle changes.
4. Only after backend validation passes may frontend integration be considered complete.

## Database Verification Rule
- Required for write operations and state transitions.
- Optional for read-only endpoints, but filtering and source correctness still need to be validated when relevant.

## Evidence Storage
If backend validation keeps AI-captured command output or service logs:
- store them under `docs/testing/ai-logs/`
- keep screenshots under `docs/testing/screenshots/`
- reference the stored paths in the task document

## Completion Gate
A backend-affecting task is not complete if:
- only build/test passed but endpoint behavior was not validated
- write operations were not checked against the database state
- frontend integration was declared complete before backend validation passed
