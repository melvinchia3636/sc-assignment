# Air & Green

A desktop monitor for urban air quality and nearby green space, built for
**SDG 11 (Sustainable Cities and Communities)** — targets 11.6 (urban air
quality) and 11.7 (safe, inclusive public spaces).

The application reads live station data from the
[AQI service](https://aqi-api-melvin.fly.dev), turns it into advice for a chosen
audience, recommends parks near the selected district, records citizen
pollution reports, and raises a threshold alert when air quality worsens.

## Features

- Current AQI, pollutant breakdown and the station's recent history
- Audience-specific advice and safe outdoor time (general public, children,
  older adults)
- Nearby parks ranked by distance
- Citizen pollution reports stored locally
- Threshold alerts with an in-window banner and a history list
- Offline fallback to the last saved reading
- Station directory with search and per-station lookup

## Design

| Layer | Package | Responsibility |
|---|---|---|
| Model | `model` | `AqiLevel`, `StationReading`, `District`, `Park`, `CitizenReport` |
| API | `api` | HTTP transport, JSON parsing, status handling |
| Service | `service` | Caching, settings, parks, reports, alerts |
| View | `ui` | Swing panels and the main window |
| Controller | `controller` | Event handling and background work |

Two design patterns carry the assignment requirements:

- **Strategy** — `AdviceStrategy` with `GeneralAdvice`, `ChildrenAdvice` and
  `ElderlyAdvice`. A new audience is added by writing another class; the
  dashboard does not change.
- **Observer** — `PollutionAlertService` is the subject. `BannerObserver` shows
  the alert, `AlertLogObserver` records it for the Alerts tab, and
  `AlertArchiveObserver` persists it to `~/.airngreen/alerts.json`. A reading is
  announced once per band, and cleaner air re-arms the alert.

## Data source

| Endpoint | Use |
|---|---|
| `GET /nearest-station?lat=&lon=` | nearest reporting station with current readings and a 72-hour series |
| `GET /stations` | public station directory; accepts optional `south` / `west` / `north` / `east` bounds |
| `GET /station/:idx` | one station by WAQI id |

Successful readings are cached in `~/.airngreen/last-reading.json`. If the
service is down, the dashboard shows that reading with an offline notice.

The station list queries a bounding box around the selected district and sorts
the results nearest-first. It does not attempt a worldwide directory, because
WAQI's global query is sampled and incomplete.

Green-space data in `parks.json` is derived from
[OpenStreetMap](https://www.openstreetmap.org/) (© OpenStreetMap contributors,
ODbL) and bundled offline.

## Build and run

Requires JDK 17 or newer and Maven 3.9+.

```bash
mvn verify
java -jar target/airngreen.jar
```

For development, `mvn exec:java` runs the application directly. To point the
client at a stub service, pass a system property:

```bash
java -Dairngreen.apiBase=http://127.0.0.1:8080 -jar target/airngreen.jar
```

## Formatting

Spotless formats Java sources with the Eclipse JDT formatter configured in
[`config/airngreen-formatter.xml`](config/airngreen-formatter.xml) and checks the
result during `mvn verify`, so CI rejects unformatted code.

The profile uses four-space indentation, an 80-column limit, and wraps a
wrapped argument list one argument per line, with the closing parenthesis on
its own line.

VS Code uses the same profile: `.vscode/settings.json` points the Java extension
at the XML file and enables format on save for Java files.

```bash
mvn spotless:apply   # rewrite sources
mvn spotless:check   # check only
```

## Tests

```bash
mvn test         # unit tests; 105 test cases, JaCoCo line coverage 96%
mvn clean verify # also writes target/site/jacoco/index.html
```

Coverage is measured on the model, API and service layers. The Swing views and
controllers are covered by the system integration tests in the report. Use
`clean` before the coverage screenshot: JaCoCo appends to its execution file, so
a previous live-test run would inflate the number.

Live tests call the deployed service and are excluded from the default build:

```bash
mvn test -Plive-tests
```

The live tests pass. The 72-hour series is sparse by design: WAQI returns
`null` for hours a station did not report, and the client drops those samples,
so the trend length varies. The test therefore asserts a non-empty series
contained in the 72-hour window instead of a fixed sample count.

## Layout

```
src/main/java/dev/melvinchia3636/airngreen/
├── App.java
├── api/          AqiApiClient, AqiJsonParser, ApiException
├── controller/   one controller per screen, plus Async
├── model/        domain records and enums
├── service/      AirQualityService, ParksService, ReportService
│   ├── advice/   AdviceStrategy and its three implementations
│   └── alerts/   PollutionAlertService and its observers
├── storage/      LocalStore, SettingsStore
└── ui/           MainFrame, four panels, StatusBar
src/main/resources/parks.json
src/test/java/...  JUnit 5 tests
```

## Licence

MIT. See [LICENSE](LICENSE).
