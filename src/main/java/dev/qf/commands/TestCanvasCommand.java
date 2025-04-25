package dev.qf.commands;

import com.mojang.brigadier.context.CommandContext;
import dev.qf.CanvasRenderer;
import dev.qf.LCInitializer;
import dev.qf.LobotomyCorpSounds;
import dev.qf.canvas.CanvasAssets;
import dev.qf.canvas.CanvasFonts;
import dev.qf.canvas.TestCanvas;
import dev.qf.sidebar.TestEmployeeSidebar;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import eu.pb4.sidebars.api.SidebarInterface;
import eu.pb4.sidebars.impl.SidebarHolder;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.*;
import org.lwjgl.system.MathUtil;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCanvasCommand {
    public static VirtualDisplay display;
    public static PlayerCanvas canvas = TestCanvas.getCanvas();
    public static Position pos;
    private static AtomicInteger globalOffset = new AtomicInteger(-400);
    private static Thread animator;
    private static AtomicBoolean animating = new AtomicBoolean(false);
    private static final int offsetMultiplier = 20;
    public static int generate(CommandContext<ServerCommandSource> ctx) {
        if (ctx.getSource().getPlayer() == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("Only players can use this command!"), false);
            return -1;
        }

        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (display != null) {
            display.destroy();
        }
        pos = player.getPos().add(0, 2, 0);
        display = VirtualDisplay.builder(canvas, BlockPos.ofFloored(pos), player.getFacing())
                .glowing()
                .invisible()
                .raycast()
                .callback(TestCanvasCommand::onClick)
                .build();


        ctx.getSource()
                .getServer()
                .getPlayerManager()
                .getPlayerList()
                .forEach(p -> {
                    canvas.addPlayer(p);
                    display.addPlayer(p);
                });


        return 1;
    }

    public static int destroy(CommandContext<ServerCommandSource> ctx) {
        if (display != null) {
            display.destroy();
            display = null;
        }

        return 1;
    }

    public static int draw(CommandContext<ServerCommandSource> ctx) {
        if (display == null) {
            return 1;
        }
        // 128 * 4 / 128 * 3 = 512 / 384
        // 128 * 7 / 128 * 3 = 896 / 384
        CanvasUtils.clear(canvas, CanvasColor.BLACK_LOW);
        // 28 ~ 476 mid : 224 + 28 = 252
        CanvasUtils.draw(canvas, canvas.getWidth() / 32, canvas.getHeight() / 12, 448, 260 , CanvasAssets.CHOICE_ANOMALY_FRAME);
        CanvasUtils.draw(canvas, 228, -40, 48, 80, CanvasAssets.CHOICE_ANOMALY_FRAME_TOP);
        CanvasUtils.draw(canvas, canvas.getWidth() / 32 + 12, canvas.getHeight() / 12 + 12, 424, 240, CanvasAssets.OP_SKULL);

        // Text and Text Frame
        CanvasUtils.draw(canvas,  + canvas.getWidth() / 32 + 16, canvas.getWidth() / 12 + 60 + 48, 416, 96, CanvasAssets.CHOICE_ANOMALY_TEXT_FRAME);
        CanvasFonts.PRETENDARD_REGULAR.drawText(canvas, "닫혀있는 문틈과 유리를 통해 빛이 새어 나온다.",canvas.getWidth() / 32 + 24, canvas.getWidth() / 12 + 72 + 48, 15, CanvasColor.PALE_YELLOW_HIGH);
        CanvasFonts.PRETENDARD_REGULAR.drawText(canvas, "유리 너머 기괴한 해골이 보인다.", canvas.getWidth() / 32 + 24, canvas.getWidth() / 12 + 72 + 64,15, CanvasColor.PALE_YELLOW_HIGH);
        CanvasFonts.PRETENDARD_REGULAR.drawText(canvas, "해골은 십자가에 가시나무로 묶인 채 둥근 빛을 띠며 부유하고 있다.",canvas.getWidth() / 32 + 24, canvas.getWidth() / 12 + 72 + 96, 15, CanvasColor.PALE_YELLOW_HIGH);
        CanvasFonts.PRETENDARD_REGULAR.drawText(canvas, "공허한 두개골 속의 눈구멍이 당신을 직시한다.", canvas.getWidth() / 32 + 24, canvas.getWidth() / 12 + 72 + 114,15, CanvasColor.PALE_YELLOW_HIGH);

        // CHOICE TITLE
        CanvasUtils.draw(canvas, canvas.getWidth() / 32 + 460, canvas.getHeight() / 12, 224, 62, CanvasAssets.CHOICE_TITLE_BG);
        CanvasFonts.KOTLA_BOLD.drawText(canvas, "선택지", canvas.getWidth() / 32 + 460 + 64, canvas.getHeight() / 12 + 16, 32, CanvasColor.PALE_YELLOW_HIGH);

        // CHOICE FRAME
        CanvasUtils.draw(canvas, canvas.getWidth() / 32 + 460, canvas.getHeight() / 12 + 64, 21, 80, CanvasAssets.CHOICE_FRAME_LEFT_EDGE);
        CanvasUtils.draw(canvas, canvas.getWidth() / 32 + 481, canvas.getHeight() / 12 + 64, 312, 80, CanvasAssets.CHOICE_FRAME_CENTER);
        CanvasUtils.draw(canvas, canvas.getWidth() / 32 + 481 + 312, canvas.getHeight() / 12 + 64, 21, 80, CanvasAssets.CHOICE_FRAME_RIGHT_EDGE);
        // THIS MEANS CANVAS AREA = 28+ 460 ~ 28 + 793 = 488 ~ 842. y = 32 + 64 ~ 32 + 80 + 64

        CanvasFonts.PRETENDARD_BOLD.drawText(canvas, "본능 작업을 실시한다.",canvas.getWidth() / 32 + 500, canvas.getHeight() / 12 + 84, 21, CanvasColor.PALE_YELLOW_HIGH);
        CanvasFonts.PRETENDARD_REGULAR.drawText(canvas, "관리자의 지시를 이행한다.", canvas.getWidth() / 32 + 500, canvas.getHeight() / 12 + 106, 15, CanvasColor.PALE_YELLOW_HIGH);
        canvas.sendUpdates();
        return 1;
    }

    private static void onClick(ServerPlayerEntity player, ClickType type, int x, int y) {
        Box box = new Box(488, 0, 96, 842, 1,176);
        if (box.contains(x, 0, y)) {
            player.playSoundToPlayer(LobotomyCorpSounds.UI_CLICK_SOUND, SoundCategory.MASTER, 0.6f, 1.0f);
        }
    }

    public static int animate(CommandContext<ServerCommandSource> ctx) {
//        if (animator == null) {
//            animator = new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        while (true) {
//                            if (!animating.get() || globalOffset.intValue() > 0) {
//                                Thread.sleep(100);
//                                continue;
//                            }
//                            // 128 * 4 / 128 * 3 = 512 / 384
//                            // 128 * 7 / 128 * 3 = 896 / 384
//                            int offset = globalOffset.getAndAdd(offsetMultiplier);
//                            CanvasUtils.clear(canvas, CanvasColor.BLACK_LOW);
//                            // 28 ~ 476 mid : 224 + 28 = 252
//                            CanvasUtils.draw(canvas, canvas.getWidth() / 32, canvas.getHeight() / 12 + offset, 448, 260, CanvasAssets.CHOICE_ANOMALY_FRAME);
//                            CanvasUtils.draw(canvas, 228, -40 + offset, 48, 80, CanvasAssets.CHOICE_ANOMALY_FRAME_TOP);
//                            CanvasUtils.draw(canvas, canvas.getWidth() / 32 + 12, canvas.getHeight() / 12 + 12 + offset, 424, 240, CanvasAssets.OP_SKULL);
//                            canvas.sendUpdates();
//
//                            Thread.sleep(1000 / 128);
//                        }
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            };
//            animator.setDaemon(true);
//            animator.start();
//        }
//        animating.set(!animating.get());
//        globalOffset.set(-400);
        try {

            CanvasRenderer.animateContainerUnitJoined(ctx.getSource().getPlayer(), display, CanvasAssets.OP_SKULL);
        } catch (Exception e) {
            LCInitializer.LOGGER.error(e.getMessage());
        }
        return 1;
    }

    public static int toggleEmployeeSidebar(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player) {
        SidebarHolder holder = SidebarHolder.of(player.networkHandler);
        Optional<SidebarInterface> optionalSidebar = holder.sidebarApi$getAll()
                .stream()
                .filter(sidebar -> sidebar instanceof TestEmployeeSidebar)
                .findAny();

        optionalSidebar.ifPresentOrElse(holder::sidebarApi$remove, () ->{
            TestEmployeeSidebar sidebar = new TestEmployeeSidebar();
            sidebar.show();
            sidebar.addPlayer(player);
        });

        return 1;
    }
}
