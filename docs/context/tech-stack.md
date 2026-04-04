# Tech Stack

## Backend
- Java 21 in `novel/pom.xml`
- Spring Boot 4.0.0
- Spring Web
- Spring Data JPA
- Spring Actuator
- MariaDB JDBC driver
- Maven build workflow
- Jsoup, OkHttp, Apache HttpClient, AWS S3 SDK, PDFBox, Guava, Gson

## Web Frontend
- Vue 3.4.x
- Vue Router 4.x
- Vue CLI 5 build workflow
- Tailwind CSS 3.x via local PostCSS pipeline
- Node 20 LTS recommended for maintenance work
- Element Plus 2.x
- Axios
- CryptoJS

## Database And Data
- MariaDB / MySQL-style runtime
- Large SQL imports under `sql/`
- Single-database and dual-database runtime modes

## Validation Tooling
- Maven package/build checks for backend
- Vue CLI build checks for frontend
- Local browser MCP / Playwright for browser acceptance when frontend behavior changes

## Environment Baseline
- Backend: `http://localhost:8081`
- Web: `http://localhost:8080`

## Known Drift To Verify Before Editing
- Local compose files build from the repository `Dockerfile`, while external compose files still deploy the published image `mattgideon/freenovel:v1.0.11-prod`.
- Historical helper notes and older setup guides may still exist outside the canonical docs under `docs/`.
- Some frontend dependencies emit engine warnings on Node 24 even though the build still completes.
