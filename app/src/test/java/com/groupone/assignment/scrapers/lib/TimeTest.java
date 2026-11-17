package com.groupone.assignment.scrapers.lib;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TimeTest {

    @Test
    void epochSeconds_appliesTheStationOffset() {
        assertEquals(
                Time.epochSeconds("2026-11-17T02:00:00Z"),
                Time.epochSeconds("2026-11-17 10:00:00", "+08:00"));
    }

    @Test
    void epochSeconds_convertsIsoTimestamps() {
        assertEquals(1_794_880_800L,
                Time.epochSeconds("2026-11-17T10:00:00+08:00"));
    }

    @Test
    void epochSeconds_returnsZeroForBlankOrInvalidInput() {
        assertEquals(0, Time.epochSeconds("", "+08:00"));
        assertEquals(0, Time.epochSeconds("not-a-date", "+08:00"));
    }
}
