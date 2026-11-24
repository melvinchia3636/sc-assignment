package dev.melvinchia3636.airngreen.service.advice;

import dev.melvinchia3636.airngreen.model.AqiLevel;

/**
 * Advice for older adults, who face a higher risk from particulate pollution.
 */
public final class ElderlyAdvice implements AdviceStrategy {

    @Override
    public String key() {
        return "elderly";
    }

    @Override
    public String name() {
        return "Older adults";
    }

    @Override
    public String advice(AqiLevel level) {
        return switch (level) {
            case GOOD -> "Air quality is good. A walk outdoors is fine.";
            case MODERATE -> "Light outdoor activity is fine; rest often.";
            case SENSITIVE -> "Shorten outdoor walks and avoid busy roads.";
            case UNHEALTHY -> "Stay indoors and keep medication nearby.";
            case VERY_UNHEALTHY, HAZARDOUS -> "Remain indoors and close windows.";
            case UNKNOWN -> "No current reading. Refresh before going out.";
        };
    }

    @Override
    public int outdoorMinutes(AqiLevel level) {
        return switch (level) {
            case GOOD -> 60;
            case MODERATE -> 30;
            case SENSITIVE -> 15;
            case UNHEALTHY, VERY_UNHEALTHY, HAZARDOUS, UNKNOWN -> 0;
        };
    }
}
