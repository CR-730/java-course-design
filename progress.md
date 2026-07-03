# Progress

## 2026-07-02

- Confirmed scope: implement方案 A, a fresh Maven multi-module project.
- Created project directory `java-course-design`.
- Confirmed draw.io CLI is installed.
- Created file-based planning records.
- Created Maven parent project with `core`, `benchmark`, and `web` modules.
- Added Day 1 configuration: `application.properties`, `logback.xml`, `schema.sql`, `docker-compose.yml`.
- Added Day 1 documents: `data-dictionary.md`, `README.md`, `er-diagram.drawio`.
- Exported ER diagram PNG files.
- Ran `mvn test`: build succeeded, 1 test passed.
- Updated Docker MySQL host port to 3308 because 3306 and 3307 were already in use.
- Started Docker services with an isolated MySQL volume.
- Verified MySQL schema and Redis connectivity.
- Added data generator using HikariCP, JDBC batch insert, and DataFaker dependency.
- Generated 50,000 users and 100,000 user behavior logs.
- Verified database counts and event/channel distributions.
- Added Stream-based `StatsService`.
- Generated `report/user-behavior-report.txt`.
- Updated README with Docker, data generation, report, test, and API commands.
- Added Redis cache service with TTL.
- Added CompletableFuture async cache warmup.
- Added Javalin stats endpoints.
- Added Jackson JSON mapper dependency for Javalin.
- Verified `/api/stats` JSON output and Redis cache keys.
- Stopped local Web API before publishing the current version.
- Preparing first Git commit and private GitHub repository upload.
