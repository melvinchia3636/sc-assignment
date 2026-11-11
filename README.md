# sc-assignment

REST API built with Spring Boot and Maven.

- **Server:** `server/` — Spring Boot 4.1, Java 21, Maven
- **Client:** `client/` — empty for now

## Requirements

- JDK 17+ (project targets Java 21)
- No local Maven needed — the wrapper (`./mvnw`) handles it

## Run

```bash
cd server
./mvnw spring-boot:run
```

The server starts on <http://localhost:8080>.

```bash
./mvnw test        # run tests
./mvnw clean package
java -jar target/server-0.0.1-SNAPSHOT.jar
```

To use a different port:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
```

## Endpoints

| Method | Path      | Response                                            |
| ------ | --------- | --------------------------------------------------- |
| GET    | `/`       | `204 No Content`                                    |
| GET    | `/health` | `200` `{"status":"UP","timestamp":"..."}`           |

```console
$ curl http://localhost:8080/health
{"status":"UP","timestamp":"2026-11-11T23:04:41.864497Z"}
```

## Error responses

Errors are returned as JSON, including 404s and unsupported methods:

```console
$ curl http://localhost:8080/does-not-exist
{"timestamp":"2026-11-11T23:00:09.719847Z","status":404,"error":"Not Found",
 "message":"No endpoint found for this request","path":"/does-not-exist"}
```

| Status | Meaning                                        |
| ------ | ---------------------------------------------- |
| 404    | No endpoint found for this request             |
| 405    | HTTP method not supported for this endpoint    |
| 500    | Unexpected server error (details only logged)  |

## Project structure

```
sc-assignment/
├── client/                                  # empty
└── server/
    ├── pom.xml
    ├── mvnw, mvnw.cmd, .mvn/                # Maven wrapper
    └── src/
        ├── main/java/com/groupone/assignment/
        │   ├── ServerApplication.java
        │   ├── controllers/
        │   │   ├── HealthController.java
        │   │   └── RootController.java
        │   └── error/
        │       ├── ApiError.java
        │       └── GlobalExceptionHandler.java
        └── test/java/com/groupone/assignment/
```

## License

[MIT](LICENSE)
