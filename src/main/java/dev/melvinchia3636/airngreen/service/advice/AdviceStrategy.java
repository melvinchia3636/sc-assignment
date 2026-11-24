package dev.melvinchia3636.airngreen.service.advice;

import dev.melvinchia3636.airngreen.model.AqiLevel;

/**
 * Strategy for turning an AQI band into advice for one audience group.
 *
 * <p>
 * Each audience supplies its own thresholds and wording, so a new group can be
 * added without editing the advice screen.
 */
public interface AdviceStrategy {

    /**
     * Stable key used for persistence.
     *
     * @return a short lowercase identifier
     */
    String key();

    /**
     * Name shown in the audience selector.
     *
     * @return the audience name
     */
    String name();

    /**
     * Guidance for the current air quality.
     *
     * @param level current AQI band
     * @return one or two sentences of advice
     */
    String advice(AqiLevel level);

    /**
     * Suggested safe time outdoors.
     *
     * @param level current AQI band
     * @return minutes, or 0 when outdoor activity should be avoided
     */
    int outdoorMinutes(AqiLevel level);
}
