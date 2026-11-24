package dev.melvinchia3636.airngreen.service.advice;

import dev.melvinchia3636.airngreen.model.AqiLevel;

/**
 * Advice for the general public.
 */
public final class GeneralAdvice implements AdviceStrategy {

    @Override
    public String key() {
        return "general";
    }

    @Override
    public String name() {
        return "General public";
    }

    @Override
    public String advice(AqiLevel level) {
        return switch (level) {
            case GOOD -> "Air quality is good. Outdoor activity is safe for everyone.";
            case MODERATE -> "Air quality is acceptable. Unusually sensitive people "
                    + "should consider reducing long outdoor exertion.";
            case SENSITIVE -> "Sensitive groups should reduce prolonged outdoor exertion.";
            case UNHEALTHY -> "Everyone should reduce prolonged outdoor exertion.";
            case VERY_UNHEALTHY -> "Avoid outdoor exertion and stay indoors where possible.";
            case HAZARDOUS -> "Stay indoors and keep windows closed.";
            case UNKNOWN -> "No current reading. Refresh to try again.";
        };
    }

    @Override
    public int outdoorMinutes(AqiLevel level) {
        return switch (level) {
            case GOOD -> 120;
            case MODERATE -> 60;
            case SENSITIVE -> 30;
            case UNHEALTHY -> 15;
            case VERY_UNHEALTHY, HAZARDOUS, UNKNOWN -> 0;
        };
    }
}
