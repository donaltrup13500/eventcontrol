package fr.eventcontrol;

import java.util.List;

public final class SharedHealthLogic {
    private SharedHealthLogic() {
    }

    public static float calculateSharedHealthValue(List<Float> playerHealthValues) {
        if (playerHealthValues == null || playerHealthValues.isEmpty()) {
            return 20.0F;
        }

        float minHealth = Float.MAX_VALUE;
        for (float health : playerHealthValues) {
            if (health <= 0.0F) {
                continue;
            }
            minHealth = Math.min(minHealth, health);
        }

        if (minHealth == Float.MAX_VALUE) {
            return 20.0F;
        }

        return Math.max(0.0F, minHealth);
    }
}