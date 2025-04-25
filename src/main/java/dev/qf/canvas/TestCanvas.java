package dev.qf.canvas;

import dev.qf.LCInitializer;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.core.PlayerCanvas;

public class TestCanvas {
    public static PlayerCanvas getCanvas() {
        var canvas = DrawableCanvas.create(7, 3);
        canvas.fill(CanvasColor.WHITE_HIGH);
        for (int i = 0; i < 10; i++) {
            CanvasFonts.PRETENDARD_REGULAR.drawText(canvas, String.valueOf(i), canvas.getWidth() / 5, canvas.getHeight() * i / 10, 20,CanvasColor.BLACK_HIGH);
//            DefaultFonts.UNIFONT.drawText(canvas, );
        }
        LCInitializer.LOGGER.info("width : {}, height : {}", canvas.getWidth(), canvas.getHeight());

        return canvas;
//
//        CanvasUtils.draw(canvas,0, 0,  CanvasImage.from(CanvasAssets.CHOICE_ANOMALY_FRAME));
//        CanvasUtils.draw(canvas, 0, )
    }
}
