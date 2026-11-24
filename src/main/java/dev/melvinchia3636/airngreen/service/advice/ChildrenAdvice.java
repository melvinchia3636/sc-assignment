package dev.melvinchia3636.airngreen.service.advice;

import dev.melvinchia3636.airngreen.model.AqiLevel;

/** Advice for children, whose airways are more sensitive to pollution. */
public final class ChildrenAdvice implements AdviceStrategy {

    @Override
    public String key() {
        return "children";
    }

    @Override
    public String name() {
        return "Children";
    }

    @Override
    public String advice(AqiLevel level) {
        return switch (level) {
            case GOOD -> "Good air for play. Outdoor games are fine.";
            case MODERATE ->
                "Outdoor play is fine, but watch children with asthma.";
            case SENSITIVE ->
                "Move vigorous play indoors or shorten outdoor sessions.";
            case UNHEALTHY -> "Keep children indoors and avoid outdoor sport.";
            case VERY_UNHEALTHY, HAZARDOUS ->
                "Keep children indoors with windows closed.";
            case UNKNOWN ->
                "No current reading. Refresh before planning outdoor play.";
        };
    }

    @Override
    public int outdoorMinutes(AqiLevel level) {
        return switch (level) {
            case GOOD -> 90;
            case MODERATE -> 45;
            case SENSITIVE -> 15;
            case UNHEALTHY, VERY_UNHEALTHY, HAZARDOUS, UNKNOWN -> 0;
        };
    }
}
