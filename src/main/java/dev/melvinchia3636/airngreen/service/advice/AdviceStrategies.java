package dev.melvinchia3636.airngreen.service.advice;

import java.util.List;

/**
 * Registry of the available advice strategies.
 */
public final class AdviceStrategies {

    private static final List<AdviceStrategy> ALL = List.of(
            new GeneralAdvice(),
            new ChildrenAdvice(),
            new ElderlyAdvice());

    private AdviceStrategies() {
    }

    /**
     * Every strategy in display order.
     *
     * @return an immutable list
     */
    public static List<AdviceStrategy> getAll() {
        return ALL;
    }

    /**
     * Resolves a persisted strategy key.
     *
     * @param key stored key
     * @return the matching strategy, or general advice when unknown
     */
    public static AdviceStrategy byKey(String key) {
        return ALL.stream()
                .filter(strategy -> strategy.key().equalsIgnoreCase(key))
                .findFirst()
                .orElseGet(GeneralAdvice::new);
    }
}
