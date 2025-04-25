package dev.qf.canvas;

import dev.qf.LCInitializer;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ChoiceCanvas {
    private VirtualDisplay display;
    private final PlayerCanvas canvas;

    public ChoiceCanvas(BlockPos renderPos, Direction direction) {
        this.canvas = getCanvas();
        this.display = VirtualDisplay
                .builder(canvas, renderPos, direction)
                .callback(this::onClick)
                .invisible()
                .raycast()
                .glowing()
                .build();
    }

    private PlayerCanvas getCanvas() {
        var canvas = DrawableCanvas.create(8, 6);

        return canvas;
    }


    private void onClick(ServerPlayerEntity player, ClickType type, int x, int y) {
        LCInitializer.LOGGER.info("HI");
    }

    public void render() {

    }
}
