package dev.qf;

import dev.qf.canvas.CanvasAssets;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.*;

public class CanvasRenderer {
    public final static int waitTime = 1000 / 60;
    private final static int endFrame = 24;
    private static final ExecutorService threadPoolExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public static CompletableFuture<Boolean> animateContainerUnitJoined(ServerPlayerEntity employee, VirtualDisplay display, CanvasImage anomalySprite) throws ExecutionException, InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerCanvas canvas = display.getCanvas();
                if (employee != null) {
                    employee.playSoundToPlayer(LobotomyCorpSounds.UI_DOWN_SOUND, SoundCategory.PLAYERS, 0.7f, 1.0f);
                }
                for (int i = 1; i <= endFrame; i++) {
                    long startTime = System.currentTimeMillis();
                    int bezier = (int) bezier((float) i / endFrame, -400, -100, 100, 0);
                    CanvasUtils.clear(canvas, CanvasColor.BLACK_LOW);
                    // 28 ~ 476 mid : 224 + 28 = 252
                    CanvasUtils.draw(canvas, canvas.getWidth() / 32, canvas.getHeight() / 12 + bezier, 448, 260, CanvasAssets.CHOICE_ANOMALY_FRAME);
                    CanvasUtils.draw(canvas, 228, -40 + bezier, 48, 80, CanvasAssets.CHOICE_ANOMALY_FRAME_TOP);
                    CanvasUtils.draw(canvas, canvas.getWidth() / 32 + 12, canvas.getHeight() / 12 + 12 + bezier, 424, 240, anomalySprite);
                    CanvasUtils.draw(canvas,  + canvas.getWidth() / 32 + 16, canvas.getWidth() / 12 + 60 + 48 + bezier, 416, 96, CanvasAssets.CHOICE_ANOMALY_TEXT_FRAME);
                    canvas.sendUpdates();
                    long executionTime = System.currentTimeMillis() - startTime;
//                    LCInitializer.LOGGER.info("bezier : {}", bezier);
//                    LCInitializer.LOGGER.info("Execution Time : {}", executionTime);
                    Thread.sleep(Math.max(waitTime - (executionTime), 0));
                }
            } catch (Exception e) {
                LCInitializer.LOGGER.error("Error while animating canvas", e);
                return false;
            }

            return true;
        }, threadPoolExecutor);
    }

    private static float bezier(float delta, float offset0, float offset1, float offset2, float offset3) {
        float delta1 = MathHelper.lerp(delta, offset0, offset1);
        float delta2 = MathHelper.lerp(delta, offset1, offset2);
        float delta3 = MathHelper.lerp(delta, offset2, offset3);

        float b0 = MathHelper.lerp(delta, delta1, delta2);
        float b1 = MathHelper.lerp(delta, delta2, delta3);

        return MathHelper.lerp(delta, b0, b1);
    }
}
