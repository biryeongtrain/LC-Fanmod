package dev.qf.anomaly;

import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.EnumMap;
import java.util.List;

class SimpleAnomaly implements Anomaly {
    private final CanvasImage image;
    private final RiskLevel risk;
    private final JoinScriptCallback joinScript;
    private final EnumMap<WorkCategory, SuccessRate> workSuccessRate;
    private final List<String> workScript;
    private final int egoGiftRate;
    private final boolean isEscapable;
    private final int qliphothCounter;
    private final FinishWorkedCallback callback;
    private final EnumMap<WorkResult, Integer> results;

    public SimpleAnomaly(CanvasImage image, RiskLevel risk, JoinScriptCallback joinScript, EnumMap<WorkCategory, SuccessRate> workSuccessRate, List<String> workScript, int egoGiftRate, boolean isEscapable, int qliphothCounter, FinishWorkedCallback callback, EnumMap<WorkResult, Integer> results) {
        this.image = image;
        this.risk = risk;
        this.joinScript = joinScript;
        this.workSuccessRate = workSuccessRate;
        this.workScript = workScript;
        this.egoGiftRate = egoGiftRate;
        this.isEscapable = isEscapable;
        this.qliphothCounter = qliphothCounter;
        this.callback = callback;
        this.results = results;
    }

    @Override
    public CanvasImage getImage() {
        return this.image;
    }

    @Override
    public RiskLevel getRisk() {
        return this.risk;
    }

    @Override
    public List<String> getJoinScript(ServerPlayerEntity player) {
        return this.joinScript.onJoinContainerUnit(player);
    }

    @Override
    public int getWorkSuccessRate(ServerPlayerEntity employee, WorkCategory category, int level) {
        return this.workSuccessRate.getOrDefault(category, SuccessRate.EMPTY).getSuccessRate(level);
    }

    @Override
    public List<String> getWorkScript(WorkCategory category, int choiceId) {
        return this.workScript;
    }

    @Override
    public void damageWorkFailure(ServerPlayerEntity player) {
        this.getFailureDamage(player);
        // TODO : DAMAGE;
    }

    @Override
    public IntObjectPair<DamageType> getFailureDamage(ServerPlayerEntity player) {
        return null;
    }

    @Override
    public int getEGOGiftRate() {
        return this.egoGiftRate;
    }

    @Override
    public boolean isEscapable() {
        return this.isEscapable;
    }

    @Override
    public int getQliphothCounter() {
        return this.qliphothCounter;
    }

    @Override
    public void onFinishedWork(PlayerCanvas canvas, ServerPlayerEntity player, WorkCategory category, WorkResult result) {
        callback.onFinishedWork(canvas, player, category, result);
    }

    @Override
    public WorkResult getWorkResult(int peBox) {
        return WorkResult.GOOD;
    }
}
