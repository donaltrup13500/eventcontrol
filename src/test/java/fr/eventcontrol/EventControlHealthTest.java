package fr.eventcontrol;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventControlHealthTest {
    @Test
    void sharedHealthIgnoresDeadOrDyingPlayers() {
        assertEquals(10.0F, SharedHealthLogic.calculateSharedHealthValue(List.of(20.0F, 0.0F, 10.0F)), 0.0001F);
    }

    @Test
    void lavaChunkBudgetStaysReasonable() {
        assertEquals(24, LavaLogic.computeSafeLavaRadiusChunks());
    }
}
