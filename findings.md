# Findings

## Workspace

- Original workspace contains only course Word documents and no Java source project.
- Java environment now resolves to JDK 25 for `java`, `javac`, and Maven.
- draw.io desktop CLI exists at `C:\Program Files\draw.io\draw.io.exe`.
- Maven multi-module project compiles with `mvn test`.

## Course Requirements Relevant To Topic D

- Topic D: user behavior funnel analysis.
- Minimum data scale: 100,000 logs and 50,000 distinct users.
- Core events: `view`, `cart`, `order`, `pay`.
- Day 1 expected outputs: project skeleton, external configuration, ER diagram, data dictionary, SQL schema, Logback configuration.
- Final project later needs Stream API, Redis, JMH, JUnit/Mockito, JaCoCo, and Javalin API.

## Day 1 Data Model

- `user`: stores basic user identity and channel metadata.
- `user_log`: stores behavior event logs and references `user.id`.
- Relationship: one user has many behavior logs.
- ER diagram exported successfully to `docs/er-diagram.png` and `docs/er-diagram.drawio.png`.

## Follow-Up Notes

- JaCoCo automatic execution was not enabled on Day 1 because JDK 25 runtime classes trigger warnings with the configured JaCoCo version. Coverage can be reintroduced later when implementing real tests.
- Docker MySQL ports 3306 and 3307 were already occupied on the host, so this project uses host port 3308.
- The first Docker run accidentally attached the old `docker_mysql_data` volume. The compose file now uses project name `java-course-design` and volume `java_course_design_mysql_data` to keep this project isolated.
- Verified Docker services: `java-mysql` contains `course_design.user` and `course_design.user_log`; `java-redis` responds to `PING`.
- Data generation verification: `user` has 50,000 rows; `user_log` has 100,000 rows; distinct `user_id` in logs is 50,000.
- Event distribution after generation: view 55,000; cart 25,000; order 13,000; pay 7,000.
- Channel distribution after generation: app 33,333; web 33,334; miniprogram 33,333.
- Redis cache keys verified: `stats:eventType`, `stats:channel`, `stats:dailyPv`, `stats:dailyUv`, `stats:funnel`, `stats:topCategory`.
- `/api/stats` returns JSON with `fromCache=true` after async warmup.
- Verified API endpoints: `/api/health`, `/api/stats`, `/api/stats/event-type`, `/api/stats/channel`, `/api/stats/funnel`.
