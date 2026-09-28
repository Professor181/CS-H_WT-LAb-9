# Scalable Real-Time Cricket Score Management System

A Spring Boot application for maintaining cricket teams and players, scheduling matches, recording ball-by-ball events, and viewing a live scoreboard. The browser dashboard polls the score API every three seconds while a match is live. All score and player statistics are calculated from saved deliveries.

## Features

- Team and player CRUD with roster filtering
- Fixture creation, start, next-innings, completion, and match summaries
- Ball-by-ball scoring for runs, wides, no-balls, byes, leg-byes, and wickets
- Persistent innings totals and player batting/bowling statistics
- Legal-ball over calculation and 120-ball innings limit
- Responsive dashboard, match centre, scorer, and management pages
- JSON validation and API error responses
- Seeded sample teams, players, and a live match
- Postman collection and H2-backed integration tests

## Technologies

Java 17, Spring Boot 3.5, Spring Web, Spring Data JPA, Hibernate, MySQL, Maven, HTML, CSS, JavaScript, Bootstrap 5, JUnit 5, H2 (tests), and Postman.

## Architecture

```text
Browser (HTML/CSS/JavaScript)
        ↓ Fetch API
REST controllers and DTOs
        ↓
Service layer (match and scoring rules)
        ↓
Spring Data repositories / Hibernate
        ↓
MySQL
```

## Project Structure

```text
src/main/java/com/example/cricket/
  config/       CORS and demo-data initialization
  controller/   REST endpoints
  dto/          Validated requests and API views
  entity/       JPA entities and enums
  exception/    JSON API error handling
  repository/   Spring Data repositories
  service/      Match, roster, innings, and scoring logic
src/main/resources/
  application.properties
  static/       Dashboard, scorer, management pages, CSS, and JavaScript
src/test/java/  Spring Boot scoring and lifecycle tests
postman/        Importable API collection
```

## Database Setup

1. Install and start MySQL Server.
2. Create the database (or allow the configured MySQL URL to create it):

   ```sql
   CREATE DATABASE cricket_score_db;
   ```

3. Set credentials in `src/main/resources/application.properties`, or provide environment variables:

   ```powershell
   $env:DB_USERNAME = 'root'
   $env:DB_PASSWORD = 'your-local-password'
   ```

   The default password is the placeholder `YOUR_PASSWORD`; change it before running against your local MySQL installation. Alternatively, set `DB_URL` to a different JDBC URL. Do not commit local credentials.

Hibernate creates/updates the tables on startup. The sample data is inserted only when no teams exist.

## Run

Requirements: JDK 17 or later and Maven 3.6 or later.

```powershell
mvn clean test
mvn spring-boot:run
```

Or package and run the jar:

```powershell
mvn clean package
java -jar target/cricket-score-management-system-1.0.0.jar
```

The automated tests use an isolated in-memory H2 database. The application itself uses MySQL.

## Dashboard

Open the application at [http://localhost:8080](http://localhost:8080). Use the navigation to open Matches, Teams, Players, or Scorer. The sample India vs Australia match starts live with persisted demo deliveries. The match details page polls the score endpoint every three seconds while the match is live.

## REST API

Base URL: `http://localhost:8080/api`

| Method | Endpoint | Description |
| --- | --- | --- |
| GET, POST | `/teams` | List or create teams |
| GET, PUT, DELETE | `/teams/{id}` | Read, update, or delete a team |
| GET, POST | `/players` | List or create players |
| GET, PUT, DELETE | `/players/{id}` | Read, update, or delete a player |
| GET | `/teams/{teamId}/players` | List one team's players |
| GET, POST | `/matches` | List or create matches |
| GET, PUT, DELETE | `/matches/{id}` | Read, update, or delete an upcoming match |
| POST | `/matches/{id}/start` | Start a match and create its first innings |
| GET | `/matches/{id}/score` | Current innings score, players, and recent balls |
| GET, POST | `/matches/{id}/innings` | List or create an innings |
| POST | `/matches/{id}/next-innings` | Begin the second innings (`{"battingTeamId": 2}`) |
| POST | `/innings/{inningsId}/ball` | Record one delivery |
| GET | `/innings/{inningsId}/balls` | Read ball history |
| POST | `/matches/{id}/complete` | Complete a live match |
| GET | `/matches/{id}/summary` | Read final score and player performances |

Example delivery:

```json
{
  "strikerId": 1,
  "nonStrikerId": 2,
  "bowlerId": 8,
  "runs": 4,
  "extras": 0,
  "extraType": "NONE",
  "wicket": false,
  "wicketType": "NONE",
  "dismissedPlayerId": null
}
```

Overs are calculated from legal balls (six balls per over), not decimal arithmetic. Wide and no-ball deliveries do not advance legal balls. Run-out wickets are not credited to the bowler. Batting and bowling assignments are checked against the active innings' teams.

## Postman

Import `postman/Cricket-Score-Management.postman_collection.json` into Postman. The collection defines `baseUrl`, `teamId`, `playerId`, `matchId`, and `inningsId` variables. Change IDs to the records in your database as needed. Start with team/player reads, then create or start a match, and record deliveries against its innings.

## Authentication

No authentication is required because this is an academic demonstration project.

## Future Enhancements

- WebSocket score updates
- JWT authentication
- Player analytics and historical match search
- Cloud deployment
- Mobile application