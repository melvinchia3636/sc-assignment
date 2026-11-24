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
  image("diagrams/architecture.png", width: 100%),
  caption: [Layered architecture of the application.],
)

The system utilizes a five layer structure as shown in the diagram in Figure 1 above. The direction of dependencies is from top to bottom, and the implementation domain model does not rely on any Java framework.

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
  [Event processing, backend task coordination, updating view.],

  [Service Layer],
  [`AirQualityService`、`ParksService`、`ReportService`、`PollutionAlertService`],
  [Business logics, caching, and alert mechanism.],

  [Storage Layer],
  [`LocalStore`、`SettingsStore`],
  [JSON and properties persistence, typed user preferences.],

  [API Layer],
  [`AqiApiClient`、`JdkHttpTransport`、`AqiJsonParser`、`ApiException`],
  [HTTP、JSON、Error normalization],

  [Model Layer],
  [`StationReading`、`AqiLevel`、`District`、`Park`、`CitizenReport`、`AqiAlert`、`AlertThreshold`],
  [Immutable constant and categorization strategies.],
)

Out of the above layers, the view layer, controller layer. and model layer forms an MVC triangle, while the service layer, storage layer, and API layer take charge of the business logics, persistence, and aggregation responsibilities outside the MVC, allowing the model layer to be decoupled from Swing as well as the Internet.

The data flow of a refresh operation is shown in the sequence diagram below. Due to the limits of the report size, the diagram might be a little bit too small. If you need the full picture, it is available in the GitHub repository under the `docs/diagrams` folder.

#figure(
  image("diagrams/refresh-flow.png", width: 100%),
  caption: [Refresh data flow across the layers.],
)

As shown in the diagram above, When the user clicks the refresh button, `DashboardController` will reads the selected district, then `AirQualityService.nearest` will be called as a backend thread by `Async`. The `AqiApiClient` will then assemble the URL that will be used by `JdkHttpTransport` for the GET request. The result JSON will be converted in to `StationReading` by `AqiJsonParser` and written into `LocalStore` cache. The controller will then go back to the event delegation thread and call `DashboardPanel.showReading` while providing the AQI reading to `PollutionAlertService.evaluate`, where the subject notifies the observers only when the reading exceeds the threshold.

== UML Class Diagram

#figure(
  image("diagrams/class-diagram-core.png", width: 100%),
  caption: [Domain model and design-pattern class diagram.],
)

#figure(
  image("diagrams/class-diagram-services.png", width: 100%),
  caption: [Service, storage, and API layer class diagram.],
)

Figure 2 above shows the core diagram covering the domain model and the two design patterns implemented, while figure 3 presents the relationship between the service, storage, and API layers. The core diagram includes `District`、`AqiLevel`、`StationReading`、`Park`、`CitizenReport`、`AlertThreshold`、`AdviceStrategy` and its three implementations、`AlertObserver` and its three implementations, `PollutionAlertService`, as well as `AqiAlert`. Field types, method signatures including parameters and return types, as well as their visibilities are annotated in the diagram. Standard UML class diagram notation is adopted.

Some examples of the key relationships between classes is as follows:

1. The three advice classes implement `AdviceStrategy` being an IS-A relationship;
2. `PollutionAlertService` holds zero to many `AlertObserver` being a HAS-A relationship;
3. `StationReading` depends on `AqiLevel` to perform the categorization;
4. `Park` and `CitizenReport` each associates with a `District`;
5. `AirQualityService` aggregates `AqiApiClient` and `LocalStore` with the latter persisting the readings into the disk;
6. `DashboardController` depends on `AirQualityService`, `ParksService`, and `PollutionAlertService` in runtime.

== Design Pattern Justification

=== Strategy Pattern: Switching Advises According to User Group

*Before Refactoring*

If-else chain, method must be modified when a new user group is added.

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

*Open-Closed Principle (OCP)*. Adding new user group can be completed through adding a new class, and there is no need to make any changes to `DashboardPanel` and `DashboardController`.

Meanwhile, the *Single Responsibility Principle (SRP)* is also satisfied, where each class only describes the advice given to a specific user-group. In comparison to the if-else chain, the strategy pattern isolates "points of variation" at class boundaries, allowing the unit test to perform validation for each and every user group without having the need to construct a massive combination of conditional clauses.

=== Observer Pattern: Threshold Alerting

*Before Refactoring*

Direct invocation of GUI components etc. from the subject.

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

*Dependency Inversion Principle (DIP)* and *OCP*. The subject depends on abstract `AlertObserver` while having no dependency on Swing. For example, `AlertArchiveObserver` demonstrates this by persisting every alert to `alerts.json` without changing `PollutionAlertService`, and a future observer (e.g., sending email, etc.) can be added the same way.

Meanwhile, the *SRP* is also retained, where the subject is only responsible for checking whether the threshold is exceeded, while the three observers take charge of displaying, recording, and archiving respectively.


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
  [Http requires construction; Status code checking; Response parsing.],
  [`HttpTransport`、`AqiJsonParser`、`ApiException`],

  [`AlertArchiveObserver`],
  [Persist every alert to `alerts.json`, newest first, keeping at most 50 entries.],
  [`LocalStore`、`PollutionAlertService`],
)

= Implementation and Unit Testing

== Core Logic and Clean Code

== Unit Test Suite

== Coverage and Known Limitations

= Integration and Application

== GUI and MVC

== External API Integration

== System Integration Testing

= Reflection and SDG 11 Alignment

== SDG 11 Impact

== Architectural Trade-offs

== Scalability

== Teamwork

= References and Appendices

== References

#heading(numbering: none)[Appendix A: Unit Test Report]

#heading(numbering: none)[Appendix B: AI Use Declaration]
