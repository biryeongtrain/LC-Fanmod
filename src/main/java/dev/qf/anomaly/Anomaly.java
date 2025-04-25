package dev.qf.anomaly;

import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

public interface Anomaly {
    /**
     * Returns Image that using in canvas
     * @return Canvas Image of the anomaly
     */
    CanvasImage getImage();

    /**
     * returns Risk of Anomaly.
     * @return Risk of Anomaly
     */
    RiskLevel getRisk();

    /**
     * returns Text that player join the Containment Unit
     * @param player the player that join the unit
     * @return display texts in canvas
     */
    List<String> getJoinScript(ServerPlayerEntity player);

    /**
     * returns default success rate of work category and level.
     * @param category what to do
     * @param level level
     * @return success rate. 1000 means 100%
     */
    int getWorkSuccessRate(ServerPlayerEntity employee, WorkCategory category, int level);

    /**
     * returns texts when player select choice.
     * @param category work category that team leader chosen
     * @param choiceId id that player chosen
     * @return text that render in canvas
     */
    List<String> getWorkScript(WorkCategory category, int choiceId);

    /**
     * damage or do extra logic when player failed work.
     * this can be occurred multiple time in one work.
     * this executed when player failed to make pe-box
     * @param player employee player
     */
    void damageWorkFailure(ServerPlayerEntity player);

    /**
     * returns damage type and damage when ne-box created.
     * @param player worker
     * @return damage type and damage amount
     */
    IntObjectPair<DamageType> getFailureDamage(ServerPlayerEntity player);

    /**
     * returns percent of anomaly gives ego gift to player.
     * <br> this is base value.
     * @return ego gift drop rate
     */
    int getEGOGiftRate();

    /**
     * returns if this anomaly can escape from container unit
     * @return value that can anomaly escape.
     */
    boolean isEscapable();

    /**
     * returns qliphoth counter. if this returns -1, it means this anomaly does not have counter.
     * @return
     */
    int getQliphothCounter();

    /**
     * call when employee finished work.
     * @param canvas canvas that rendering anomaly
     * @param player employee
     * @param category work category.
     * @param result result
     */
    void onFinishedWork(PlayerCanvas canvas, ServerPlayerEntity player, WorkCategory category, WorkResult result);
    WorkResult getWorkResult(int peBox);

    class Builder {
        private CanvasImage image;
        private RiskLevel risk;
        private JoinScriptCallback joinScript;
        private IntObjectPair<DamageType> damage;
        private int egoGiftRate;
        private boolean isEscapable;
        private int qliphothCounter;
        private FinishWorkedCallback callback;
        private final EnumMap<WorkCategory, SuccessRate> successRates = new EnumMap<>(WorkCategory.class);

        public Builder image(CanvasImage image) {
            this.image = image;
            return this;
        }

        public Builder risk(RiskLevel risk) {
            this.risk = risk;
            return this;
        }

        public Builder damage(IntObjectPair<DamageType> damage) {
            this.damage = damage;
            return this;
        }

        public Builder egoGiftRate(int egoGiftRate) {
            this.egoGiftRate = egoGiftRate;
            return this;
        }

        public Builder isEscapable(boolean isEscapable) {
            this.isEscapable = isEscapable;
            return this;
        }

        public Builder qliphothCounter(int qliphothCounter) {
            this.qliphothCounter = qliphothCounter;
            return this;
        }

        public Builder callback(FinishWorkedCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder successRate(WorkCategory category, SuccessRate successRate) {
            this.successRates.put(category, successRate);
            return this;
        }
        public Builder successRate(WorkCategory category, int level1, int level2, int level3, int level4, int level5) {
            SuccessRate successRate = new SuccessRate(level1, level2, level3, level4, level5);
            return this.successRate(category, successRate);
        }

        public Builder joinScript(JoinScriptCallback callback) {
            this.joinScript = callback;
            return this;
        }

        public Anomaly build() {
//            return new SimpleAnomaly(image, risk, joinScript, successRates, )
            return null;
        }
    }

    @FunctionalInterface
    interface JoinScriptCallback {
        List<String> onJoinContainerUnit(ServerPlayerEntity player);
    }

    @FunctionalInterface
    interface WorkingScriptCallback {
        List<String> onWorkScript(ServerPlayerEntity player, WorkCategory category, int choiceId);
    }

    @FunctionalInterface
    interface FinishWorkedCallback {
        void onFinishedWork(PlayerCanvas canvas, ServerPlayerEntity player, WorkCategory category, WorkResult result);
    }
}
