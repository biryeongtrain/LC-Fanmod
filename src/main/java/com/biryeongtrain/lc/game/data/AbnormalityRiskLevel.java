package com.biryeongtrain.lc.game.data;

public enum AbnormalityRiskLevel {
    ZAYIN(1),
    TETH(2),
    HE(3),
    WAW(4),
    ALEPH(5);

    private final int severity;

    AbnormalityRiskLevel(int severity) {
        this.severity = severity;
    }

    public boolean isAtMost(AbnormalityRiskLevel other) {
        return this.severity <= other.severity;
    }
}
