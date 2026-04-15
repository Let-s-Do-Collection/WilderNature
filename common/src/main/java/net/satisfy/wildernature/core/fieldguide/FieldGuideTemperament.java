package net.satisfy.wildernature.core.fieldguide;

public enum FieldGuideTemperament {
    FRIENDLY,
    NEUTRAL,
    HOSTILE;

    public static FieldGuideTemperament fromName(String name) {
        if (name == null) {
            return NEUTRAL;
        }

        return switch (name.toLowerCase()) {
            case "friendly" -> FRIENDLY;
            case "hostile" -> HOSTILE;
            default -> NEUTRAL;
        };
    }
}