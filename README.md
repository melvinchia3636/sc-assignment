# sc-assignment

**Urban Air Quality and Green Space Monitor** -- an Android app (Java) built for
Scenario 5 of the Software Construction group assignment (SDG 11: Sustainable
Cities and Communities).

The app asks for location permission, finds the nearest WAQI monitoring station,
explains the health impact of its US AQI reading, and shows a 24-hour AQI trend
with weather. Data is cached so the last good reading stays visible when the
network is unavailable. The green spaces tab is a placeholder for now.

## Features

- Nearest reporting WAQI station via a runtime location permission request
- US AQI reading with colour-coded category, contrast-aware text and health advice
- Pollutant sub-indices: PM2.5, PM10, PM1, O₃, NO₂, SO₂, CO
- 72-hour WAQI series with a 24-hour AQI sparkline
- Weather from the station's CWOP feed (temperature, humidity, pressure, wind)
- Green spaces tab placeholder
- Pull-to-refresh and a manual refresh button
- Graceful degradation: cached data plus an "offline" banner on failure
- Human-readable error messages for 404, 429, 5xx and network failures
- No stack traces are ever shown to the user

## Stack

| Layer | Choice |
| --- | --- |
| Language | Java 17 |
| UI | Android Views + Material 3 |
| Typeface | DM Sans (bundled, SIL Open Font License) |
| Icons | Android-Iconics + Google Material typeface |
| Networking | OkHttp + Gson |
| Charts | MPAndroidChart |
| Location | Google Play Services Fused Location Provider |
| Tests | JUnit 5 (Jupiter) + JaCoCo |
| Build | Gradle 8.14.3, Android Gradle Plugin 8.11 |
| SDK | compileSdk 36, minSdk 24, targetSdk 36 |

## Requirements

- JDK 17
- Android SDK with platform 36 and build-tools 36
- `local.properties` pointing at the SDK:

```properties
sdk.dir=/Users/<you>/Library/Android/sdk
```

No `local.properties` is committed; Android Studio generates it, or create it
manually as above.

## Build and run

```bash
# debug APK
./gradlew assembleDebug

# install on a connected device or emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Or open the project root in Android Studio and press Run.

First build downloads the Gradle wrapper and Android dependencies.

## Localization

- English is the default (`values/strings.xml`); Malay (`values-ms`) and Simplified Chinese
  (`values-zh-rCN`) are provided.
- Settings → Language opens a single-choice dialog (System default, English, Bahasa Melayu,
  简体中文) and applies the choice via `AppCompatDelegate.setApplicationLocales(...)`.
- `res/xml/locales_config.xml` is wired through `android:localeConfig`, so Android 13+ also
  lists the app under Settings → System → Languages → App languages.
- To add a language, copy `values/strings.xml` to `values-<code>/`, add the code to
  `locales_config.xml`, and add an entry to the language dialog in `SettingsTab`.

## Tests and coverage

```bash
./gradlew testDebugUnitTest     # 33 JUnit 5 tests
./gradlew jacocoTestReport      # HTML report
open app/build/reports/jacoco/jacocoTestReport/html/index.html
open app/build/reports/tests/testDebugUnitTest/index.html
```

## Data sources

- **Air quality** -- the app scrapes WAQI / [aqicn.org](https://aqicn.org)
  directly, finding the nearest reporting station with its current sub-indices
  and 72-hour series:
  - `https://mapq.waqi.info/mapq2/bounds` for nearby stations
  - `https://api2.waqi.info/api/token/{idx}` and `/api/feed/@{idx}/aqi.json` for
    numeric station ids
  - `https://airnet.waqi.info/airnet/feed/hourly/{id}` and
    `/feed/cwop/{id}` for `A`-prefixed stations

The WAQI endpoints are unofficial and can change.

## Project structure

```
sc-assignment/
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/groupone/assignment/
│       │   │   ├── services/     # location resolution + tab navigation
│       │   │   ├── repositories/# models, SnapshotCache, AirQualityRepository
│       │   │   ├── scrapers/    # WaqiClient, parsers, sources and lib helpers
│       │   │   ├── ui/
│       │   │   │   ├── MainActivity.java
│       │   │   │   ├── MainViewModel.java
│       │   │   │   ├── aqi/     # AqiFragment, AqiScale, PollutantCard, WeatherItem
│       │   │   │   ├── green/   # green space placeholder screen
│       │   │   │   └── common/  # Base fragments, icon helpers, chart setup
│       │   └── res/             # layouts, colours, theme, strings
│       └── test/java/com/groupone/assignment/
├── docs/                        # Typst report, cover, TOC
├── build.gradle
├── settings.gradle
└── gradlew
```

## Report

The written report source is in `docs/` (`report.typ`, `cover.typ`, `toc.typ`,
`setup.typ`) and compiles to `docs/report.pdf` with Typst.

## License

App code: [MIT](LICENSE). Bundled **DM Sans** typeface: SIL Open Font License 1.1,
see [`licenses/DM-Sans-OFL.txt`](licenses/DM-Sans-OFL.txt). Icons are from
**Google Material Icons** (Apache 2.0), see
[`licenses/Material-Icons-Apache-2.0.txt`](licenses/Material-Icons-Apache-2.0.txt).
