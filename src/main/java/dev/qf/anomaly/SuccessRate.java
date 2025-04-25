package dev.qf.anomaly;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

public class SuccessRate {
    private final Int2IntMap successRate = new Int2IntOpenHashMap();
    public static final SuccessRate EMPTY = new SuccessRate(0,0,0,0,0);

    public SuccessRate(int successRate0, int successRate1, int successRate2, int successRate3, int successRate4) {
        successRate.put(1, successRate0);
        successRate.put(2, successRate1);
        successRate.put(3, successRate2);
        successRate.put(4, successRate3);
        successRate.put(5, successRate4);
    }

    public int getSuccessRate(int level) {
        return Math.clamp(successRate.getOrDefault(level, 0), 0, 1000);
    }
}
