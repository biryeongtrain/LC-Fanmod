package dev.qf.player;

public interface Employees {
    EmployeeType lc$getType();
    float lc$getCurrentSP();
    int getFortitude();
    int getPrudence();
    int getTemperance();
    void lc$applyAttribute();

    default float getMaxSP() {
        return getPrudence();
    }

    int lc$getStat(StatType stat);

    default int getAdditionalSuccessRate() {
        return getTemperance() * 2;
    }
    default float getAdditionalWorkSpeedRatio() {
        return getTemperance() * 0.1f;
    }
    int getJustice();
    default float getAdditionalAttackSpeed() {
        return getJustice() * 0.87f;
    }
    default float getAdditionalMoveSpeed() {
        return getJustice() * 0.01f;
    }

    default int getEmployeeTier() {
        int totalTier = getStatTier(getFortitude()) + getStatTier(getPrudence()) + getStatTier(getTemperance()) + getStatTier(getJustice());
        if (totalTier >= 16) {
            return 5;
        } else if (totalTier >= 12) {
            return 4;
        } else if (totalTier >= 9) {
            return 3;
        } else if (totalTier >= 6) {
            return 2;
        } else {
            return 1;
        }
    }
    static int getStatTier(int value) {
        if (value >= 100) {
            return 6;
        } else if (value >= 85) {
            return 5;
        } else if (value >= 65) {
            return 4;
        } else if (value >= 45) {
            return 3;
        } else if (value >= 30) {
            return 2;
        } else {
            return 1;
        }
    }
}
