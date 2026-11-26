// ===========================================================================
// Software Construction - Group Assignment report
// ===========================================================================

#import "setup.typ": apply-style, code-block, report-table
#import "cover.typ": cover
#import "toc.typ": toc

#show: apply-style

#cover()

#pagebreak()

#toc()

#pagebreak()

= Introduction and SDG 11 Context

== Urban Problem and Operating Organisation

== SDG 11 Target Alignment

== Stakeholders and Their Needs

== Scope and Expected Users

= Literature Review

== Existing Smart-City Solutions and the Gap

== Design Patterns and Prior Applications

== Java Technologies

== Evidence of the Urban Problem

= System Design and Modelling

== Architecture Overview

#figure(
  image("diagrams/architecture.png", width: 80%),
  caption: [Layered architecture of the application.],
)

The system utilizes a five-layer structure as shown in the diagram in Figure 1 above. The direction of dependencies is from top to bottom, and the implementation domain model does not rely on any Java framework.

#report-table(
  3,
  [Layer],
  [Key Classes],
  [Responsibility],
  [View Layer],
  [`MainFrame`、`DashboardPanel`、`StationsPanel`、`ReportsPanel`、`AlertsPanel`],
  [Only responsible for rendering and input gathering.],

  [Controller Layer],
  [`DashboardController`、`StationsController`、`ReportsController`、`AlertsController`、`Async`],
  [Event processing, backend task coordination, updating the view.],

  [Service Layer],
  [`AirQualityService`、`ParksService`、`ReportService`、`PollutionAlertService`],
  [Business logic, caching, and alert mechanism.],

  [Storage Layer],
  [`LocalStore`、`SettingsStore`],
  [JSON and properties persistence, typed user preferences],

  [API Layer],
  [`AqiApiClient`、`AqiJsonParser`、`ApiException`],
  [HTTP、JSON、Error normalization],

  [Model Layer],
  [`StationReading`、`AqiLevel`、`District`、`Park`、`CitizenReport`、`AqiAlert`、`AlertThreshold`],
  [Immutable constant and categorization strategies.],
)

Out of the above layers, the view layer and controller layer. and the model layer form an MVC triangle, while the service layer, storage layer, and API layer take charge of the business logic, persistence, and aggregation responsibilities outside the MVC, allowing the model layer to be decoupled from Swing as well as the Internet.

The data flow of a refresh operation is shown in the sequence diagram below. Due to the limits of the report size, the diagram might be a little bit too small. If you need the full picture, it is available in the GitHub repository under the `docs/diagrams` folder.

#figure(
  image("diagrams/refresh-flow.png", width: 100%),
  caption: [Refresh data flow across the layers.],
)

As shown in the diagram above, when the user clicks the refresh button, `DashboardController` will read the selected district, then `AirQualityService.nearest` will be called as a backend thread by `Async`. The `AqiApiClient` will then assemble the URL and perform the GET request with OkHttp. The result JSON will be converted into `StationReading` by `AqiJsonParser` and written into `LocalStore` cache. The controller will then go back to the event delegation thread and call `DashboardPanel.showReading` while providing the AQI reading to `PollutionAlertService.evaluate`, where the subject notifies the observers only when the reading exceeds the threshold.

#figure(
  image("diagrams/class-diagram-core.png", width: 100%),
  caption: [Domain model and design-pattern class diagram.],
)

#figure(
  image("diagrams/class-diagram-services.png", width: 100%),
  caption: [Service, storage, and API layer class diagram.],
)

Figure 2 above shows the core diagram covering the domain model and the two design patterns implemented, while Figure 3 presents the relationship between the service, storage, and API layers. The core diagram includes `District`、`AqiLevel`、`StationReading`、`Park`、`CitizenReport`、`AlertThreshold`、`AdviceStrategy` and its three implementations、`AlertObserver` and its three implementations, `PollutionAlertService`, as well as `AqiAlert`. Field types, method signatures including parameters and return types, as well as their visibilities are annotated in the diagram. Standard UML class diagram notation is adopted.

Some examples of the key relationships between classes are as follows:

1. The three advice classes implement `AdviceStrategy`, which is an IS-A relationship;
2. `PollutionAlertService` holds zero to many `AlertObserver` objects, being a HAS-A relationship;
3. `StationReading` depends on `AqiLevel` to perform the categorization;
4. `Park` and `CitizenReport` each associate with a `District`;
5. `AirQualityService` aggregates `AqiApiClient` and `LocalStore`, with the latter persisting the readings to disk;
6. `DashboardController` depends on `AirQualityService`, `ParksService`, and `PollutionAlertService` at runtime.

== Design Pattern Justification

=== Strategy Pattern: Switching Advice According to User Group

*Before Refactoring*

If-else chain; method must be modified when a new user group is added.

#code-block[
  ```java
  public String advice(String audience, double aqi) {
      if (audience.equals("general")) {
          if (aqi <= 50) return "Outdoor activity is safe.";
          if (aqi <= 100) return "Reduce long outdoor exertion.";
          return "Avoid outdoor exertion.";
      } else if (audience.equals("children")) {
          // Another set of threshold values and advices 
      }
      // The method keeps growing larger and larger for every newly added user group
  }
  ```
]

*After Refactoring*

Organized into strategies, method calling only requires the interface.

#code-block[
  ```java
  public interface AdviceStrategy {
      String advice(AqiLevel level);
      int outdoorMinutes(AqiLevel level);
  }

  public final class ChildrenAdvice implements AdviceStrategy {
      @Override
      public String advice(AqiLevel level) {
          return switch (level) {
              case GOOD -> "Good air for play.";
              case MODERATE -> "Outdoor play is fine; watch asthma.";
              case SENSITIVE -> "Move vigorous play indoors.";
              default -> "Keep children indoors.";
          };
      }
  }
  ```
]

*SOLID Principle Demonstrated*

*Open-Closed Principle (OCP)*. Adding a new user group can be completed by adding a new class, and there is no need to make any changes to `DashboardPanel` and `DashboardController`.

Meanwhile, the *Single Responsibility Principle (SRP)* is also satisfied, where each class only describes the advice given to a specific user group. In comparison to the if-else chain, the strategy pattern isolates "points of variation" at class boundaries, allowing the unit test to perform validation for each and every user group without having the need to construct a massive combination of conditional clauses.

#pagebreak()

=== Observer Pattern: Threshold Alerting

*Before Refactoring*

Direct invocation of GUI components, etc., from the subject.

#code-block[
  ```java
  public void evaluate(StationReading reading) {
      if (reading.aqi() > threshold) {
          // Dependency of the subject with GUI component
          statusBar.setText("Alert: " + reading.aqi()); 
          // Dependency of the subject with notification history
          notificationHistory.add(reading);             
      }
  }
  ```
]


*After Refactoring*

The subject only knows about the observer interface.

#code-block[
  ```java
  public void evaluate(StationReading reading) {
      AqiLevel level = reading.level();
      if (!level.isAtLeast(required)) { lastAnnounced = null; return; }
      if (lastAnnounced != null && !level.isWorseThan(lastAnnounced)) return;

      lastAnnounced = level;
      AqiAlert alert = new AqiAlert(level, reading.aqi(),
              reading.name(), System.currentTimeMillis());
      for (AlertObserver observer : observers) {
          observer.onThresholdExceeded(alert);
      }
  }
  ```
]

*SOLID Principle Demonstrated*

*Dependency Inversion Principle (DIP)* and *OCP*. The subject depends on the abstract `AlertObserver` while having no dependency on Swing. For example, `AlertArchiveObserver` demonstrates this by persisting every alert to `alerts.json` without changing `PollutionAlertService`, and a future observer (e.g., sending email, etc.) can be added the same way.

Meanwhile, the *SRP* is also retained, where the subject is only responsible for checking whether the threshold is exceeded, while the three observers take charge of displaying, recording, and archiving, respectively.


== CRC Cards

#report-table(
  3,
  [Class Name],
  [Responsibilities],
  [Collaborations],
  [`AqiLevel`],
  [Categorise AQI values into six levels; Provides level comparison.],
  [`StationReading`、`AdviceStrategy`、`PollutionAlertService`],

  [`AdviceStrategy` (and its three implementations)],
  [Generate advice and suggested outdoor duration according to the AQI level and user group.],
  [`DashboardController`、`DashboardPanel`],

  [`PollutionAlertService`],
  [Determine whether the AQI reading exceeds the threshold, and whether it is more severe than the last recorded value; Notify observers.],
  [`AlertObserver`、`SettingsStore`、`StationReading`],

  [`AirQualityService`],
  [Invoke the API to obtain the AQI readings; Write into cache; Read from cache.],
  [`AqiApiClient`、`LocalStore`、`DashboardController`],

  [`AqiApiClient`],
  [HTTP requires construction; Status code checking; Response parsing.],
  [`OkHttpClient`、`AqiJsonParser`、`ApiException`],

  [`AlertArchiveObserver`],
  [Persist every alert to `alerts.json`, newest first, keeping at most 50 entries.],
  [`LocalStore`、`PollutionAlertService`],
)

= Implementation and Unit Testing

== Core Logic and Clean Code

- *Naming and Code Structure: * Classes are domain-driven (e.g., `StationReading`, `NearbyPark`, etc.), method names are verbs (e.g., `nearest`, `evaluate`, `register`, etc.), boolean methods are prefixed with `is` (e.g., `isEnabled`, `isWorseThan`, etc.). All public APIs include Javadoc that describes the method usage, input parameters, and output value.

- *Method Length and Complexity: * Most methods in the service layer and API layer have their length controlled within 20 lines. Conditional branches are broken up using early returns to prevent deep nesting. For example, `AqiJsonParser.currentValues` returns early for `null`, filters missing or non-finite pollutant values, and returns an immutable copy; `PollutionAlertService.evaluate` handles disabled and unknown readings first, then de-duplication, and only then broadcasts.

- *Magic Number Elimination: * The lower bounds of the AQI levels are defined in the constructor parameters (e.g., `MODERATE("Moderate", 51)`, etc.). Other fixed values are all defined as named constants (e.g., `EARTH_RADIUS_KM`、`MAX_NEARBY_PARKS`、`MAX_NOTE_LENGTH`、`REQUEST_TIMEOUT`, etc.).

- *Single Responsibility Principle Example: * `AqiLevel` only handles the mapping between the value and the corresponding levels, as well as the comparison between levels, and it isn't responsible for the colors, the text, or the networking. Colors are located in `ui/AqiColors`; text content is in each strategy class; data are stored as `StationReading`. Hence, `AqiLevelText` is able to cover all the boundary values within milliseconds.

- *Open-Closed Principle: * `AdviceStrategies` is the only place that knows all the strategies, and `DashboardController` uses them through the `AdviceStrategy` interface. When a new strategy is added, only the newly created strategy class has to be added to the register table; no modification to controllers and views is needed.

- *Code Excerpts: * The registration and announcement mechanism of observers is located in `PollutionAlertService`, where the core mechanisms are less than 30 lines (excluding blank lines).

  #code-block[
    ```java
    public void register(AlertObserver observer) {
        observers.addIfAbsent(observer);
    }

    public void evaluate(StationReading reading) {
        AlertThreshold current = threshold.get();

        if (!current.isEnabled()) {
            lastAnnounced = null;
            return;
        }

        AqiLevel level = reading.level();
        AqiLevel required = current.level().orElseThrow();

        if (level == AqiLevel.UNKNOWN || !level.isAtLeast(required)) {
            lastAnnounced = null;
            return;
        }

        if (lastAnnounced != null && !level.isWorseThan(lastAnnounced)) {
            return;
        }

        lastAnnounced = level;

        announce(
            new AqiAlert(
                level,
                reading.aqi(),
                reading.name(),
                System.currentTimeMillis()
            )
        );
    }
    ```
  ]

== Unit Test Suite

The test suite contains *88 test methods* (excluding the two live API tests). The default build executes 105 test cases after parameterized tests are expanded. It covers every top-level package (`model`, `api`, `service`, `storage`, and `ui`) plus the `advice` and `alerts` subpackages:

#report-table(
  3,
  [Test Class],
  [Methods],
  [Focus],
  [`AqiLevelTest`],
  [4],
  [12 boundary values, invalid input, level comparison],

  [`AdviceStrategiesTest`],
  [7],
  [Full coverage of all three strategies, fallback, outdoor-time monotonicity],

  [`AqiJsonParserTest`],
  [15],
  [Valid and partial responses, missing fields, empty series, error envelope],

  [`AqiApiClientTest`],
  [10],
  [2xx, 404, 500, unreadable response, I/O failure, URL and bounding-box construction],

  [`ApiExceptionTest`],
  [1],
  [User-facing messages for 404, 429, 5xx, and unexpected statuses],

  [`AirQualityServiceTest`],
  [5],
  [Successful caching, failed fetch retaining the old cache, nearby stations and distance sorting],

  [`PollutionAlertServiceTest`],
  [8],
  [Register/unregister, multiple observers, de-duplication within the same band, re-alert on worsening, disabled state],

  [`AlertLogObserverTest`],
  [6],
  [Newest first, listener callback, read-only snapshot, history restoration],

  [`AlertArchiveObserverTest`],
  [3],
  [Persistence order, 50-entry cap, restoration after restart],

  [`BannerObserverTest`],
  [1],
  [Alert forwarded to the status bar callback],

  [`SettingsStoreTest`],
  [4],
  [Defaults, audience and threshold persistence, district change notification],

  [`ParksServiceTest`],
  [4],
  [Distance sorting, distance calculation, dataset integrity],

  [`ReportServiceTest`],
  [9],
  [Add and list, blank/null validation, over-length validation, persistence],

  [`LocalStoreTest`],
  [7],
  [Reading/report/station/settings/alert round-trips, corrupt-file fallback],

  [`AqiColorsTest`],
  [4],
  [Palette integrity, null fallback, notice colours, shared instances],
)

The test suites adopted three assertion types:

- 4 `@ParameterizedTest` (AQI boundaries, full coverage of strategies, outdoor duration, strategy key resolving);
- 18 `assertThrows`;
- 38 `assertAll`.

All the test cases are uniformly named as `methodName_scenario_expectedBehaviour` (e.g., `disabledThreshold_neverAlerts`、`unknownStation_reportsNotFound`, etc.).

== Coverage and Known Limitation

#figure(
  image("assets/jacoco.png", width: 100%),
  caption: [JaCoCo test coverage report generated by `mvn verify`.],
)

As shown in the figure above, the JaCoCo report shows a line coverage of 96% (Instructions 96%, Branches 95%). `ui`, `controller`, and `App` are excluded from the statistics, since these classes mainly focus on layout, color mapping, and event forwarding, out of which `AqiColors` has a dedicated unit test written. The remainder is covered in system integration testing in Section 5.3. Hence, the coverage in the business logic layer better reflects the quality of the testing.

The behaviour of the alert where "only one alert is announced for one AQI band" is only valid within a single process. The `AlertArchiveObserver` will persist the alert history into `alerts.json`, but the `lastAnnounced` field that is used to suppress repeated alerts only exists in memory. When the application is restarted and the data is refetched, the same AQI band will be determined as a new threshold excess, and hence a repeated record is produced in the alert history list. This is an intentional decision, since not persisting the suppression state prevents the user from missing out on the air quality that has deteriorated once again when reopening the app after a long time, with the cost being a repeated alert on every restart of the application.

The limitation is recreated in `PollutionAlertServiceTest.restart_sameBand_isNotAnnouncedAgain`. The test first make a service instance send out a Moderate alert and persist into the log, then a second instance is created to simulate the service restart. For the same reading value, an assertion is made that no alert is made again, but since the suppression status is not recovered in a new instance, the assertion fails.

The test case is marked with `@Tag("limitation")`, excluded through the `excludedGroups` in the `pom.xml` in the default build, allowing `mvn test` and `mvn verify` to still remain passing. To inspect the expected failure case, run:

#code-block[
  ```bash
  mvn test -Plimitation-tests
  ```
]

Failure output is as follows:

#code-block[
  ```text
  PollutionAlertServiceTest.restart_sameBand_isNotAnnouncedAgain:144
  The same band should stay suppressed after a restart
  ==> expected: <true> but was: <false>
  ```
]

= Integration and Application

== GUI and MVC

Three distinct roles are clearly separated in the system:

#report-table(
  3,
  [Role],
  [Classes],
  [Responsibility],
  [*Model*],
  [`StationReading`、`AqiLevel`、`District`、`Park`、`CitizenReport`、`AqiAlert`、`AlertThreshold`],
  [Doesn't import Swing or make any network requests],

  [*View*],
  [`MainFrame` (Four Panels + Status Bar) 、`DashboardPanel`、`StationsPanel`、`ReportsPanel`、`AlertsPanel`],
  [Only exposes methods like `showReading`、`showParks`、`showReports`; doesn't contain a business decision mechanism],

  [*Controller*],
  [`DashboardController`、`StationsController`、`ReportsController`、`AlertsController`],
  [Read input from view, invoke services, and write output back to view; Time-consuming operations are delegated to a separate thread through `Async` (a small wrapper of `SwingWorker`)],
)

Note that the `DefaultTableModel` and `DefaultListModel` from Swing are the inner state of the view despite a `Model` suffix; hence, they don't belong to the domain-specific model. All the models mentioned above are placed under the `model` package.

*Input Validation: * `ReportService.add` serves as the only validation entry point, where checks are made to ensure that the district, category, and description field cannot be empty, description payload cannot exceed 500 characters. If there is any violation found, an `IllegalArgumentException` will be thrown carrying human-readable message that will then be caught by `ReportsController` and displayed in the view through `reportsPanel.showMessage`. No stack trace message will be displayed in the user interface. `AqiApiClient` performs similar normalization for HTTP status codes and JSON structures.

The following sequence diagram traces a main use case from input to output.

#figure(
  image("diagrams/main-use-case.png", width: 98%),
  caption: [Sequence diagram of the main use case: refreshing the dashboard from user input to rendered reading and alert evaluation.],
)

#pagebreak()

Shown below are the dashboard, reports, and alerts screens. The stations screen (`StationsPanel` with `StationsController`, using `StationSummary` and `StationReading`) is not pictured since it's additional out-of-scope functionality.

#figure(
  image("assets/screenshot1.png", width: 85%),
  caption: [Dashboard view (`DashboardPanel`, driven by `DashboardController`, using `StationReading`, `AqiLevel`, `District`, and `NearbyPark`): the current AQI reading, audience-specific outdoor advice, and nearby green spaces ordered by distance.],
)

#figure(
  image("assets/screenshot2.png", width: 85%),
  caption: [Citizen reports view (`ReportsPanel`, driven by `ReportsController`, using `CitizenReport` and `District`): submit a pollution observation for a district and browse the locally stored report history.],
)

#figure(
  image("assets/screenshot3.png", width: 85%),
  caption: [Pollution alerts view (`AlertsPanel`, driven by `AlertsController`, using `AlertThreshold`, `AqiAlert`, `AqiLevel`, and `StationReading`): choose the threshold band and review the alert history recorded when a reading crosses it.],
)

== External API Integration

#report-table(
  (1fr, 3fr),
  [Item],
  [Details],
  [API name],
  [AQI API, a read-only wrapper around WAQI data],
  [Base URL],
  [`https://aqi-api-melvin.fly.dev`],
  [Endpoints],
  [`/nearest-station`, `/stations`, `/station/:idx`],
  [Success response],
  [200 + JSON],
  [Error response],
  [`{"error":{"code":"...", "message":"..."}}`],
)

*JSON Parsing: * The `AqiJsonParser` utilizes the `JsonObject` tree model provided by the Gson library to perform per-field reading instead of binding the entire response into a data transfer object (DTO). This allows default values to replace missing fields (e.g., empty name string, AQI reading being `NaN`, timestamp being 0), `null` time series points to be skipped, and prevents parsing failure if a new field is added in the upstream API.

*HTTP Status Code Processing: * For a non-2xx API response that is detected by `AqiApiClient.read` through `status / 100 != 2`, the JSON response will be parsed, and `ApiException.messageFor` processes a human-readable message for 404, 429, 5xx, and network failure. The `error.message` will be prioritized if the response body carries one; otherwise, it will fall back to the local error message content. The user interface will only show the human-readable text returned by `failure.getMessage()` and never the class name of stack traces of the error.

*Graceful Degradation: * Each successful request for the station reading will have the response persisted into `~/.airngreen/last-reading.json`. In the event of request failure, `DashboardController.onReadingFailed` will read and display from the cache while showing an orange-colored hint "Offline: showing the last saved reading" and the failure reason in the status bar. If the application fails to read from the cache, only the error message and park list will be shown. The station list is cached in the same way so that it can still be viewed when offline.

== System Integration Testing

#{
  set text(size: 9pt)
  report-table(
  (0.7fr, 2.2fr, 3fr, 1.8fr, 1.8fr, 0.8fr),
  [Test ID],
  [Input / Operation],
  [Expected Output],
  [Actual Result],
  [Category],
  [Status],
  [TC-01],
  [Select Kuala Lumpur in the dashboard and press Refresh],
  [The station name, reading value and AQI level are shown in the AQI card; The status bar shows loading complete.],
  [As Expected],
  [Normal API calling],
  [PASS],
  [TC-02],
  [Open stations page, load the station list, search Seremban, and double click the station row.],
  [The list is filtered to a single row; Shown below in the detail box is AQI 43, level Good, and the sample amount.],
  [As Expected],
  [GUI operations],
  [PASS],
  [TC-03],
  [Disconnect the device from the Internet and start the application.],
  [The message "The air-quality service is unavailable. Showing the last saved reading." is shown at the top; If there exists cache data, then the old readings will be displayed.],
  [As Expected],
  [Failed API Response],
  [PASS],
  [TC-04],
  [Set the threshold to "Moderate or worse" in the Alerts page; Go back to the dashboard page and select the district with AQI > 50.],
  ["ALERT: Moderate (AQI …)" are shown in the status bar; A new entry is added in the history list in the Alerts page; No repeated alerts are recorded for the same AQI band when refreshing the data.],
  [As Expected],
  [End-to-end logic],
  [PASS],
  [TC-05],
  [In the report page, submit the form without filling in the description; then submit the form with a 501 characters description.],
  [An alert message showing "Describe what you observed." in the first submission; "Keep the description under 500 characters." in the second; Both attempts are not written into the table],
  [As Expected],
  [Boundary condition],
  [PASS],
  )
}

= Reflection and SDG 11 Alignment

== SDG 11 Impact

== Architectural Trade-offs

== Scalability

== Teamwork


= References

#heading(numbering: none)[Appendix A: Unit Test Report]

Default build: *88 methods*, *105 executions*, all passing; line and instruction coverage *96%*, branch coverage *95%*. The table also records the two live tests (run separately) and the known-limitation test.

#{
  set text(size: 7pt)
  report-table(
    (0.7fr, 1.9fr, 1.7fr, 1.6fr, 1.8fr, 1.8fr, 1.1fr),
    [ID],
    [Class],
    [Method],
    [Input / Scenario],
    [Expected],
    [Actual],
    [Status],
    [UT-01],
    [`AqiLevel`],
    [`classify`],
    [AQI 0, 50, 51, 101, 151, 201, 301, 500],
    [Expected band per EPA boundary],
    [Expected band per EPA boundary],
    [PASS],
    [UT-02],
    [`AqiLevel`],
    [`classify`],
    [AQI -1, NaN, +infinity],
    [UNKNOWN for every value],
    [UNKNOWN for every value],
    [PASS],
    [UT-03],
    [`AqiLevel`],
    [`isWorseThan`],
    [Moderate vs unhealthy; equal bands],
    [True only for a worse band],
    [True only for a worse band],
    [PASS],
    [UT-04],
    [`AqiLevel`],
    [`isAtLeast`],
    [Sensitive vs sensitive; good vs moderate],
    [True when same or worse],
    [True when same or worse],
    [PASS],
    [UT-05],
    [`AdviceStrategy`],
    [`advice`],
    [3 strategies x 7 bands],
    [Non-blank advice for every band],
    [Non-blank advice for every band],
    [PASS],
    [UT-06],
    [`AdviceStrategy`],
    [`outdoorMinutes`],
    [3 strategies x ordered bands],
    [Non-negative, non-increasing; 0 for UNKNOWN],
    [Non-negative, non-increasing; 0 for UNKNOWN],
    [PASS],
    [UT-07],
    [`AdviceStrategies`],
    [`byKey`],
    [Keys general, children, elderly],
    [Matching strategy returned],
    [Matching strategy returned],
    [PASS],
    [UT-08],
    [`AdviceStrategies`],
    [`byKey`],
    [Unknown key],
    [GeneralAdvice fallback],
    [GeneralAdvice fallback],
    [PASS],
    [UT-09],
    [`GeneralAdvice`],
    [`outdoorMinutes`],
    [GOOD to VERY_UNHEALTHY],
    [120, 60, 30, 15, 0 minutes],
    [120, 60, 30, 15, 0 minutes],
    [PASS],
    [UT-10],
    [`ChildrenAdvice`],
    [`outdoorMinutes`],
    [Children vs general at GOOD],
    [Children get fewer minutes],
    [Children get fewer minutes],
    [PASS],
    [UT-11],
    [`ElderlyAdvice`],
    [`outdoorMinutes`],
    [UNHEALTHY],
    [0 minutes],
    [0 minutes],
    [PASS],
    [UT-12],
    [`AqiJsonParser`],
    [`parseStation`],
    [Full station JSON],
    [All fields mapped; 2 series points],
    [All fields mapped; 2 series points],
    [PASS],
    [UT-13],
    [`AqiJsonParser`],
    [`parseStation`],
    [Series point with null value],
    [Null point skipped],
    [Null point skipped],
    [PASS],
    [UT-14],
    [`AqiJsonParser`],
    [`parseStation`],
    [null point, null time, null value],
    [Only complete point kept],
    [Only complete point kept],
    [PASS],
    [UT-15],
    [`AqiJsonParser`],
    [`parseStation`],
    [pm25=null, o3=1e999],
    [Only finite pm10 kept],
    [Only finite pm10 kept],
    [PASS],
    [UT-16],
    [`AqiJsonParser`],
    [`parseStation`],
    [series = {}],
    [Empty series],
    [Empty series],
    [PASS],
    [UT-17],
    [`AqiJsonParser`],
    [`parseStation`],
    [Station with idx only],
    [Defaults: empty name, NaN AQI, no series],
    [Defaults: empty name, NaN AQI, no series],
    [PASS],
    [UT-18],
    [`AqiJsonParser`],
    [`parseStation`],
    [Body: null],
    [JsonParseException],
    [JsonParseException],
    [PASS],
    [UT-19],
    [`AqiJsonParser`],
    [`parseStation`],
    [Body: {}],
    [JsonParseException],
    [JsonParseException],
    [PASS],
    [UT-20],
    [`AqiJsonParser`],
    [`parseStations`],
    [Two stations, one without AQI],
    [Names trimmed; null AQI preserved],
    [Names trimmed; null AQI preserved],
    [PASS],
    [UT-21],
    [`AqiJsonParser`],
    [`parseStations`],
    [Body: {}],
    [JsonParseException],
    [JsonParseException],
    [PASS],
    [UT-22],
    [`AqiJsonParser`],
    [`parseStations`],
    [Body: null],
    [JsonParseException],
    [JsonParseException],
    [PASS],
    [UT-23],
    [`AqiJsonParser`],
    [`parseStations`],
    [Array [null, station]],
    [Null entry skipped],
    [Null entry skipped],
    [PASS],
    [UT-24],
    [`AqiJsonParser`],
    [`parseStations`],
    [Station without latitude/longitude],
    [Both coordinates default to 0],
    [Both coordinates default to 0],
    [PASS],
    [UT-25],
    [`AqiJsonParser`],
    [`errorMessage`],
    [Error envelope with message],
    [Server message returned],
    [Server message returned],
    [PASS],
    [UT-26],
    [`AqiJsonParser`],
    [`errorMessage`],
    [Non-JSON, {}, string error],
    [null],
    [null],
    [PASS],
    [UT-27],
    [`AqiApiClient`],
    [`nearestStation`],
    [lat 3.139, lon 101.6869],
    [Reading parsed; query has lat/lon],
    [Reading parsed; query has lat/lon],
    [PASS],
    [UT-28],
    [`AqiApiClient`],
    [`station`],
    [idx A2820325],
    [GET /station/A2820325],
    [GET /station/A2820325],
    [PASS],
    [UT-29],
    [`AqiApiClient`],
    [`constructor`],
    [airngreen.apiBase points to stub],
    [Requests use configured base URL],
    [Requests use configured base URL],
    [PASS],
    [UT-30],
    [`AqiApiClient`],
    [`stations`],
    [south, west, north, east bounds],
    [All four query parameters sent],
    [All four query parameters sent],
    [PASS],
    [UT-31],
    [`AqiApiClient`],
    [`station`],
    [404 with error.message],
    [ApiException uses server message],
    [ApiException uses server message],
    [PASS],
    [UT-32],
    [`AqiApiClient`],
    [`station`],
    [503 without message],
    [Friendly unavailable message; status 503],
    [Friendly unavailable message; status 503],
    [PASS],
    [UT-33],
    [`AqiApiClient`],
    [`nearestStation`],
    [Malformed JSON body],
    [ApiException],
    [ApiException],
    [PASS],
    [UT-34],
    [`AqiApiClient`],
    [`nearestStation`],
    [200 without station object],
    [ApiException],
    [ApiException],
    [PASS],
    [UT-35],
    [`AqiApiClient`],
    [`nearestStation`],
    [Server stopped],
    [ApiException status 0; service failure],
    [ApiException status 0; service failure],
    [PASS],
    [UT-36],
    [`AqiApiClient`],
    [`constructor`],
    [Base URL with trailing slash],
    [Path normalised to /station/2609],
    [Path normalised to /station/2609],
    [PASS],
    [UT-37],
    [`ApiException`],
    [`messageFor`],
    [404, 429, 500, 418],
    [Distinct user-facing messages],
    [Distinct user-facing messages],
    [PASS],
    [UT-38],
    [`AirQualityService`],
    [`nearest`],
    [Mock nearest-station success],
    [Reading returned and cached],
    [Reading returned and cached],
    [PASS],
    [UT-39],
    [`AirQualityService`],
    [`nearest`],
    [Success then 503],
    [ApiException; old cache retained],
    [ApiException; old cache retained],
    [PASS],
    [UT-40],
    [`AirQualityService`],
    [`byIdx`],
    [Station 2609],
    [Reading returned and cached],
    [Reading returned and cached],
    [PASS],
    [UT-41],
    [`AirQualityService`],
    [`stations`],
    [3 stations returned out of order],
    [Sorted local/middle/far; cache matches],
    [Sorted local/middle/far; cache matches],
    [PASS],
    [UT-42],
    [`AirQualityService`],
    [`getCachedReading`],
    [Fresh service],
    [Optional empty],
    [Optional empty],
    [PASS],
    [UT-43],
    [`ParksService`],
    [`nearest`],
    [3 parks around Kuala Lumpur],
    [Nearest first; all 3 returned],
    [Nearest first; all 3 returned],
    [PASS],
    [UT-44],
    [`GeoDistance`],
    [`kmBetween`],
    [Identical coordinates],
    [0 km],
    [0 km],
    [PASS],
    [UT-45],
    [`GeoDistance`],
    [`kmBetween`],
    [KL to Putrajaya, both directions],
    [Equal distance, about 22 km],
    [Equal distance, about 22 km],
    [PASS],
    [UT-46],
    [`ParksService`],
    [`fromClasspath`],
    [Bundled parks.json],
    [Data available for every district],
    [Data available for every district],
    [PASS],
    [UT-47],
    [`ReportService`],
    [`add`],
    [Valid district, category, note],
    [Report created with UUID and timestamp],
    [Report created with UUID and timestamp],
    [PASS],
    [UT-48],
    [`ReportService`],
    [`add`],
    [Padded category and note],
    [Values trimmed before storage],
    [Values trimmed before storage],
    [PASS],
    [UT-49],
    [`ReportService`],
    [`add`],
    [Note is only whitespace],
    [IllegalArgumentException],
    [IllegalArgumentException],
    [PASS],
    [UT-50],
    [`ReportService`],
    [`add`],
    [Category blank],
    [IllegalArgumentException],
    [IllegalArgumentException],
    [PASS],
    [UT-51],
    [`ReportService`],
    [`add`],
    [501-character note],
    [IllegalArgumentException],
    [IllegalArgumentException],
    [PASS],
    [UT-52],
    [`ReportService`],
    [`add`],
    [district = null],
    [IllegalArgumentException],
    [IllegalArgumentException],
    [PASS],
    [UT-53],
    [`ReportService`],
    [`add`],
    [category null; note null],
    [IllegalArgumentException for each],
    [IllegalArgumentException for each],
    [PASS],
    [UT-54],
    [`ReportService`],
    [`getAll`],
    [Two stored reports],
    [Newest first],
    [Newest first],
    [PASS],
    [UT-55],
    [`ReportService`],
    [`getAll`],
    [Add, then new service instance],
    [Report still listed],
    [Report still listed],
    [PASS],
    [UT-56],
    [`AlertArchiveObserver`],
    [`onThresholdExceeded`],
    [Three alerts],
    [Stored newest first],
    [Stored newest first],
    [PASS],
    [UT-57],
    [`AlertArchiveObserver`],
    [`onThresholdExceeded`],
    [51 alerts],
    [Archive capped at 50],
    [Archive capped at 50],
    [PASS],
    [UT-58],
    [`AlertArchiveObserver`],
    [`onThresholdExceeded`],
    [Persist, then new observer],
    [Archived alerts restored],
    [Archived alerts restored],
    [PASS],
    [UT-59],
    [`AlertLogObserver`],
    [`onThresholdExceeded`],
    [Two alerts],
    [Recorded newest first],
    [Recorded newest first],
    [PASS],
    [UT-60],
    [`AlertLogObserver`],
    [`onAlertRecorded`],
    [Listener registered],
    [Every alert delivered],
    [Every alert delivered],
    [PASS],
    [UT-61],
    [`AlertLogObserver`],
    [`onAlertRecorded`],
    [Two listeners],
    [Both receive the alert],
    [Both receive the alert],
    [PASS],
    [UT-62],
    [`AlertLogObserver`],
    [`getAlerts`],
    [Returned list, mutation attempted],
    [Immutable snapshot],
    [Immutable snapshot],
    [PASS],
    [UT-63],
    [`AlertLogObserver`],
    [`getAlerts`],
    [New observer],
    [Empty list],
    [Empty list],
    [PASS],
    [UT-64],
    [`AlertLogObserver`],
    [`constructor`],
    [Seeded archived alerts],
    [Seed order preserved],
    [Seed order preserved],
    [PASS],
    [UT-65],
    [`BannerObserver`],
    [`onThresholdExceeded`],
    [Moderate alert, AQI 90],
    [Same alert forwarded unchanged],
    [Same alert forwarded unchanged],
    [PASS],
    [UT-66],
    [`PollutionAlertService`],
    [`evaluate`],
    [Moderate 90 with one observer],
    [Observer notified],
    [Observer notified],
    [PASS],
    [UT-67],
    [`PollutionAlertService`],
    [`unregister`],
    [Unregister, then evaluate],
    [No notification],
    [No notification],
    [PASS],
    [UT-68],
    [`PollutionAlertService`],
    [`evaluate`],
    [Three registered observers],
    [All notified],
    [All notified],
    [PASS],
    [UT-69],
    [`PollutionAlertService`],
    [`evaluate`],
    [Two moderate readings],
    [One alert],
    [One alert],
    [PASS],
    [UT-70],
    [`PollutionAlertService`],
    [`evaluate`],
    [Moderate, then unhealthy],
    [Second alert],
    [Second alert],
    [PASS],
    [UT-71],
    [`PollutionAlertService`],
    [`evaluate`],
    [Threshold OFF, hazardous reading],
    [No alert],
    [No alert],
    [PASS],
    [UT-72],
    [`PollutionAlertService`],
    [`evaluate`],
    [Moderate, good, moderate],
    [Two alerts],
    [Two alerts],
    [PASS],
    [UT-73],
    [`PollutionAlertService`],
    [`evaluate`],
    [UNKNOWN reading],
    [No alert],
    [No alert],
    [PASS],
    [UT-74],
    [`LocalStore`],
    [`loadReading/saveReading`],
    [Save, then load],
    [Reading equal],
    [Reading equal],
    [PASS],
    [UT-75],
    [`LocalStore`],
    [`loadReports/saveReports`],
    [Save, then load],
    [Report list equal],
    [Report list equal],
    [PASS],
    [UT-76],
    [`LocalStore`],
    [`loadStations/saveStations`],
    [Save, then load],
    [Station list equal],
    [Station list equal],
    [PASS],
    [UT-77],
    [`LocalStore`],
    [`loadAlerts/saveAlerts`],
    [Save, then load],
    [Alert list equal],
    [Alert list equal],
    [PASS],
    [UT-78],
    [`LocalStore`],
    [`loadSettings/saveSettings`],
    [Save, then load properties],
    [Properties equal],
    [Properties equal],
    [PASS],
    [UT-79],
    [`LocalStore`],
    [`loadReports`],
    [Empty directory],
    [Empty lists and properties],
    [Empty lists and properties],
    [PASS],
    [UT-80],
    [`LocalStore`],
    [`loadReading`],
    [Corrupt reading JSON],
    [Optional empty],
    [Optional empty],
    [PASS],
    [UT-81],
    [`SettingsStore`],
    [`getters`],
    [New store],
    [KL, general audience, alerts OFF],
    [KL, general audience, alerts OFF],
    [PASS],
    [UT-82],
    [`SettingsStore`],
    [`setAudienceStrategy`],
    [Children, reopen store],
    [Children restored],
    [Children restored],
    [PASS],
    [UT-83],
    [`SettingsStore`],
    [`setDistrict/setThreshold`],
    [Klang, unhealthy; reopen],
    [Both restored],
    [Both restored],
    [PASS],
    [UT-84],
    [`SettingsStore`],
    [`setDistrict`],
    [Listener registered; set Klang],
    [Listener receives Klang],
    [Listener receives Klang],
    [PASS],
    [UT-85],
    [`AqiColors`],
    [`forLevel`],
    [All levels],
    [Non-null colour],
    [Non-null colour],
    [PASS],
    [UT-86],
    [`AqiColors`],
    [`forLevel`],
    [UNKNOWN and null],
    [Grey fallback],
    [Grey fallback],
    [PASS],
    [UT-87],
    [`AqiColors`],
    [`forMessage`],
    [warning true and false],
    [SENSITIVE and GOOD],
    [SENSITIVE and GOOD],
    [PASS],
    [UT-88],
    [`AqiColors`],
    [`forLevel`],
    [GOOD twice],
    [Same Color instance],
    [Same Color instance],
    [PASS],
    [UT-89],
    [`AqiApiClient`],
    [`station`],
    [Live: station 999999999],
    [404 ApiException],
    [404 ApiException],
    [PASS (live)],
    [UT-90],
    [`AqiApiClient`],
    [`station`],
    [Live: station 2609],
    [Non-empty; <=72 samples; finite; inside 72h window],
    [Non-empty; <=72 samples; finite; inside 72h window],
    [PASS (live)],
    [UT-91],
    [`PollutionAlertService`],
    [`evaluate`],
    [Moderate alert, restart, same reading],
    [No duplicate alert],
    [Alert repeated],
    [FAIL (expected)],
  )
}

#heading(numbering: none)[Appendix B: AI Use Declaration]

DeepSeek was used for coding assistance, including code refactoring, test generation, debugging suggestions, and documentation drafting. It was not used to fabricate experimental data or test results. The team reviewed, built, and tested every change before submission.

