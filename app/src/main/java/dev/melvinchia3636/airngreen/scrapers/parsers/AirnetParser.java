package dev.melvinchia3636.airngreen.scrapers.parsers;

import dev.melvinchia3636.airngreen.repositories.model.ObservationSeries;
import dev.melvinchia3636.airngreen.repositories.model.Point;
import dev.melvinchia3636.airngreen.repositories.model.SeriesPoint;
import dev.melvinchia3636.airngreen.repositories.model.StationDetail;
import dev.melvinchia3636.airngreen.scrapers.lib.Series;
import dev.melvinchia3636.airngreen.scrapers.lib.Time;
import dev.melvinchia3636.airngreen.repositories.remote.AirnetRepository;
import dev.melvinchia3636.airngreen.repositories.remote.CwopRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses AirNet hourly feeds and optional CWOP weather.
 */
public class AirnetParser {

    /** AQI returned when a concentration exceeds the highest breakpoint. */
    private static final int FALLBACK_INDEX = 500;

    /** Pollutants combined into the derived AQI series, in priority order. */
    private static final String[] AQI_SERIES_KEYS = {"pm25", "pm10", "pm1"};

    /** EPA PM2.5 breakpoints (µg/m³ → AQI). */
    private static final double[][] PM25_BREAKPOINTS = {
            {0, 12.0, 0, 50},
            {12.1, 35.4, 51, 100},
            {35.5, 55.4, 101, 150},
            {55.5, 150.4, 151, 200},
            {150.5, 250.4, 201, 300},
            {250.5, 350.4, 301, 400},
            {350.5, 500.4, 401, 500},
    };

    /** EPA PM10 breakpoints (µg/m³ → AQI). */
    private static final double[][] PM10_BREAKPOINTS = {
            {0, 54, 0, 50},
            {55, 154, 51, 100},
            {155, 254, 101, 150},
            {255, 354, 151, 200},
            {355, 424, 201, 300},
            {425, 504, 301, 400},
            {505, 604, 401, 500},
    };

    private interface AqiConverter {
        double toAqi(double mean);
    }

    private interface WeatherField {
        String key();

        double read(CwopRepository.Reading reading);
    }

    private static final WeatherField[] WEATHER_FIELDS = {
            new WeatherField() {
                @Override
                public String key() {
                    return "h";
                }

                @Override
                public double read(CwopRepository.Reading reading) {
                    return reading.humidity == null ? 0 : reading.humidity;
                }
            },
            new WeatherField() {
                @Override
                public String key() {
                    return "p";
                }

                @Override
                public double read(CwopRepository.Reading reading) {
                    return reading.pressure == null ? 0 : reading.pressure;
                }
            },
            new WeatherField() {
                @Override
                public String key() {
                    return "t";
                }

                @Override
                public double read(CwopRepository.Reading reading) {
                    return reading.temperature == null ? 0 : reading.temperature;
                }
            },
            new WeatherField() {
                @Override
                public String key() {
                    return "w";
                }

                @Override
                public double read(CwopRepository.Reading reading) {
                    return reading.windSpeed == null ? 0 : reading.windSpeed;
                }
            },
    };

    /**
     * Converts a PM2.5 concentration to an AQI.
     */
    public static double pm25ToAqi(double concentration) {
        return interpolateAqi(PM25_BREAKPOINTS, concentration);
    }

    /**
     * Converts a PM10 concentration to an AQI.
     */
    public static double pm10ToAqi(double concentration) {
        return interpolateAqi(PM10_BREAKPOINTS, concentration);
    }

    /**
     * Parses an AirNet hourly feed plus optional CWOP weather into a station
     * report. Requires both PM2.5 and PM10 hourly series; PM1 and weather are
     * optional.
     */
    public StationDetail parse(AirnetRepository.Response root,
                               CwopRepository.Response cwopRoot,
                               String idx) {
        AirnetRepository.Data readings = root.data;
        if (readings == null || readings.pm25 == null || readings.pm25.isEmpty()
                || readings.pm10 == null || readings.pm10.isEmpty()) {
            return null;
        }

        Map<String, CwopRepository.Reading> cwop = cwopRoot == null || cwopRoot.data == null
                ? null : cwopRoot.data.data;
        List<ObservationSeries> decoded = decodeAirnetSeries(readings, cwop);
        Map<String, List<SeriesPoint>> aligned = Series.alignSeries(decoded);
        double[] indexes = feedIndexes(root.feed);

        double aqi = Double.NaN;
        for (int index = 0; index < AQI_SERIES_KEYS.length; index++) {
            double value = indexes[index];
            if (Double.isNaN(value)) {
                continue;
            }
            aqi = Double.isNaN(aqi) ? value : Math.max(aqi, value);
        }

        Map<String, List<SeriesPoint>> series = new LinkedHashMap<>();
        series.put("aqi", Series.combineMax(aligned, AQI_SERIES_KEYS));
        series.putAll(aligned);

        return new StationDetail(
                idx,
                root.meta == null || root.meta.name == null ? "" : root.meta.name,
                aqi,
                root.meta == null || root.meta.utime == null ? 0 : root.meta.utime,
                currentValues(indexes, aqi, cwop),
                series);
    }

    private static double interpolateAqi(double[][] breakpoints, double concentration) {
        for (double[] band : breakpoints) {
            if (concentration <= band[1]) {
                return Math.round(((band[3] - band[2]) / (band[1] - band[0]))
                        * (concentration - band[0]) + band[2]);
            }
        }
        return FALLBACK_INDEX;
    }

    private List<ObservationSeries> decodeAirnetSeries(
            AirnetRepository.Data readings, Map<String, CwopRepository.Reading> cwop) {
        List<ObservationSeries> series = new ArrayList<>();
        series.add(new ObservationSeries("pm25",
                toIndexPoints(readings.pm25, AirnetParser::pm25ToAqi)));
        series.add(new ObservationSeries("pm10",
                toIndexPoints(readings.pm10, AirnetParser::pm10ToAqi)));

        if (readings.pm1 != null && !readings.pm1.isEmpty()) {
            series.add(new ObservationSeries("pm1",
                    toIndexPoints(readings.pm1, AirnetParser::pm25ToAqi)));
        }

        series.addAll(weatherSeries(cwop));
        return series;
    }

    private List<ObservationSeries> weatherSeries(Map<String, CwopRepository.Reading> cwop) {
        if (cwop == null) {
            return Collections.emptyList();
        }

        List<String> times = new ArrayList<>(cwop.keySet());
        Collections.sort(times);

        List<ObservationSeries> series = new ArrayList<>();
        for (WeatherField field : WEATHER_FIELDS) {
            List<Point> points = new ArrayList<>();
            for (String time : times) {
                CwopRepository.Reading reading = cwop.get(time);
                points.add(new Point(Time.epochSeconds(time),
                        reading == null ? 0 : field.read(reading)));
            }
            series.add(new ObservationSeries(field.key(), points));
        }
        return series;
    }

    private List<Point> toIndexPoints(List<AirnetRepository.Entry> entries,
                                      AqiConverter toAqi) {
        List<Point> points = new ArrayList<>();
        for (AirnetRepository.Entry entry : entries) {
            if (entry.mean == null || entry.mean.isNaN()) {
                continue;
            }
            points.add(new Point(Time.epochSeconds(entry.time), toAqi.toAqi(entry.mean)));
        }
        return points;
    }

    private double[] feedIndexes(Map<String, List<Double>> feed) {
        return new double[]{
                feedIndex(feed == null ? null : feed.get("pm25"), AirnetParser::pm25ToAqi),
                feedIndex(feed == null ? null : feed.get("pm10"), AirnetParser::pm10ToAqi),
                feedIndex(feed == null ? null : feed.get("pm1"), AirnetParser::pm25ToAqi),
        };
    }

    private double feedIndex(List<Double> pair, AqiConverter toAqi) {
        if (pair == null || pair.size() < 2 || pair.get(1) == null) {
            return Double.NaN;
        }
        double concentration = pair.get(1) / 100;
        return Double.isNaN(concentration) ? Double.NaN : toAqi.toAqi(concentration);
    }

    private Map<String, Double> currentValues(double[] indexes, double aqi,
                                              Map<String, CwopRepository.Reading> cwop) {
        Map<String, Double> current = new LinkedHashMap<>();
        for (int index = 0; index < AQI_SERIES_KEYS.length; index++) {
            if (!Double.isNaN(indexes[index])) {
                current.put(AQI_SERIES_KEYS[index], indexes[index]);
            }
        }
        if (Double.isFinite(aqi) && aqi > 0) {
            current.put("aqi", aqi);
        }

        for (ObservationSeries series : weatherSeries(cwop)) {
            List<Point> points = series.getPoints();
            double value = points.isEmpty() ? 0 : points.get(points.size() - 1).getV();
            current.put(series.getKey(), value);
        }
        return current;
    }
}
