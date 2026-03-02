package com.biryeongtrain.lc.game.data;

public enum DepartmentType {
    CONTROL("Control Team", AbnormalityRiskLevel.HE),
    INFORMATION("Information Team", AbnormalityRiskLevel.WAW),
    TRAINING("Training Team", AbnormalityRiskLevel.HE),
    SAFETY("Safety Team", AbnormalityRiskLevel.WAW),
    WELFARE("Welfare Team", AbnormalityRiskLevel.HE),
    DISCIPLINE("Discipline Team", AbnormalityRiskLevel.ALEPH);

    private final String displayName;
    private final AbnormalityRiskLevel maxRiskLevel;

    DepartmentType(String displayName, AbnormalityRiskLevel maxRiskLevel) {
        this.displayName = displayName;
        this.maxRiskLevel = maxRiskLevel;
    }

    public String displayName() {
        return this.displayName;
    }

    public AbnormalityRiskLevel maxRiskLevel() {
        return this.maxRiskLevel;
    }

    public boolean accepts(AbnormalityRiskLevel riskLevel) {
        return riskLevel.isAtMost(this.maxRiskLevel);
    }
}
