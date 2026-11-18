package dev.melvinchia3636.airngreen.repositories.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.melvinchia3636.airngreen.R;

import org.junit.jupiter.api.Test;

class AudienceTest {

    @Test
    void general_hasNoExtraNote() {
        assertEquals(0, Audience.GENERAL.noteRes(40));
        assertEquals(0, Audience.GENERAL.noteRes(120));
    }

    @Test
    void children_noteChangesWithAqi() {
        assertEquals(R.string.audience_note_children_good, Audience.CHILDREN.noteRes(30));
        assertEquals(R.string.audience_note_children_fair, Audience.CHILDREN.noteRes(80));
        assertEquals(R.string.audience_note_children_poor, Audience.CHILDREN.noteRes(120));
    }

    @Test
    void elderly_noteChangesWithAqi() {
        assertEquals(R.string.audience_note_elderly_good, Audience.ELDERLY.noteRes(30));
        assertEquals(R.string.audience_note_elderly_fair, Audience.ELDERLY.noteRes(80));
        assertEquals(R.string.audience_note_elderly_poor, Audience.ELDERLY.noteRes(120));
    }

    @Test
    void outdoorMinutes_shrinkAsAirQualityWorsens() {
        assertEquals(120, Audience.GENERAL.outdoorMinutes(40));
        assertEquals(60, Audience.GENERAL.outdoorMinutes(80));
        assertEquals(30, Audience.GENERAL.outdoorMinutes(120));
        assertEquals(0, Audience.GENERAL.outdoorMinutes(180));

        assertEquals(90, Audience.CHILDREN.outdoorMinutes(40));
        assertEquals(30, Audience.CHILDREN.outdoorMinutes(80));
        assertEquals(15, Audience.CHILDREN.outdoorMinutes(120));
        assertEquals(0, Audience.CHILDREN.outdoorMinutes(180));

        assertEquals(60, Audience.ELDERLY.outdoorMinutes(40));
        assertEquals(30, Audience.ELDERLY.outdoorMinutes(80));
        assertEquals(0, Audience.ELDERLY.outdoorMinutes(120));
    }
}
