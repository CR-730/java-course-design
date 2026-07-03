# Java Course Design Plan

## Goal

Create a minimal, teacher-compliant Java training project for topic D: user behavior funnel analysis.

## Scope For Day 1

- Maven multi-module skeleton: parent, core, benchmark, web.
- External configuration files.
- Logback configuration.
- MySQL schema for topic D.
- Data dictionary.
- Editable ER diagram in draw.io format, plus PNG export if CLI works.

## Scope For Day 2

- Add configuration loading utility.
- Add HikariCP data source utility.
- Add DataFaker-based generator for 50,000 users and 100,000 behavior logs.
- Insert data with JDBC batch operations.
- Keep generated data compatible with topic D events and channels.

## Scope For Day 3 Core

- Add `parallelStream` statistics with no shared mutable state.
- Add a custom Collector for funnel conversion statistics.
- Add JMH benchmark comparing loop, stream, and parallel stream implementations.
- Generate `docs/jmh-report.md` from a real benchmark run.

## Scope For Day 4

- Add Mockito tests that isolate DAO, Redis cache, and async warmup boundaries.
- Enforce JaCoCo line coverage >= 60% for the `core` module.
- Add `docs/code-review.md`.
- Add runnable PowerShell scripts under `scripts/`.
- Rewrite README with complete run and verification instructions.

## Phases

| Phase | Status | Verification |
| --- | --- | --- |
| 1. Initialize planning files | complete | task_plan.md, findings.md, progress.md exist |
| 2. Create Maven multi-module skeleton | complete | `mvn test` passed |
| 3. Add Day 1 configuration and SQL | complete | files exist under resources/sql/docker |
| 4. Add Day 1 documents and ER diagram | complete | docs contain data dictionary and ER diagram |
| 5. Verify build and diagram export | complete | Maven build and draw.io export completed |
| 6. Start Docker services | complete | MySQL schema and Redis PONG verified |
| 7. Add Day 2 data generation | complete | generator inserted required scale |
| 8. Add Day 2 Stream report | complete | report generated under report/ |
| 9. Add Redis cache and async warmup | complete | Redis keys and TTL verified |
| 10. Add Javalin stats API | complete | `/api/stats` and sub-endpoints verified |
| 11. Add parallelStream and custom Collector | complete | `mvn -q -pl core test` passed |
| 12. Add JMH benchmark report | complete | `docs/jmh-output.txt` and `docs/jmh-report.md` generated from real JMH run |
| 13. Add Mockito tests and JaCoCo gate | complete | `mvn -pl core clean verify` passed, line coverage 61.76% |
| 14. Add Day 4 docs and run scripts | complete | `docs/code-review.md`, `scripts/*.ps1`, README updated |

## Decisions

- Use JDK 25 because it satisfies the Java 17+ requirement and is already installed.
- Use two core tables for the minimum viable ER model: `user` and `user_log`.
- Keep product category as a column in `user_log` instead of adding a separate product table on Day 1.
- Avoid implementing Day 2 business logic in this phase.

## Errors Encountered

| Error | Attempt | Resolution |
| --- | --- | --- |
| Workspace was not a Git repository | Initial status check | Continue without commit; project can be initialized later |
| JaCoCo 0.8.12 printed instrumentation warnings on JDK 25 | Initial `mvn test` with core JaCoCo execution | Removed automatic Day 1 JaCoCo execution; keep plugin managed for later coverage work |
| PowerShell split `-Dexec.mainClass=...` as Maven lifecycle phase | First exec run | Quote Maven property: `"-Dexec.mainClass=..."` |
| Javalin JSON returned 500 due missing mapper | First `/api/stats` request | Added `jackson-databind` to `web/pom.xml` |
| `mvn -pl web -am exec:java` executed on parent first | First Web startup attempt | Run `mvn install -DskipTests`, then `mvn -pl web exec:java` |
| JMH jar missed `META-INF/BenchmarkList` | First benchmark jar run | Added explicit JMH annotation processor configuration in `benchmark/pom.xml` |
| Mockito could not mock classes on JDK 25 with inline mock maker | First Mockito test run | Added `mock-maker-subclass` test resource |
| JaCoCo 0.8.12 could not instrument JDK 25 class file version 69 | First JaCoCo verify run | Upgraded JaCoCo Maven plugin to 0.8.14 |
| JaCoCo exec path broke under Chinese workspace path | JaCoCo report skipped execution data | Wrote exec data to ASCII user-home path and pointed report/check to it |
| PowerShell split Maven `-Doutput=...xml` | First effective-pom command | Quoted the Maven property |
