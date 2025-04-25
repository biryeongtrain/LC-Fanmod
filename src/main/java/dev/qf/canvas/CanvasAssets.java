package dev.qf.canvas;

import dev.qf.LCInitializer;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import net.fabricmc.loader.api.FabricLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * 해당 클래스는 프로젝트 문의 리소스를 열거해둔 클래스입니다. 깃헙에 업로드된 소스는 프로젝트 문의 리소스가 업로드 되지 않기 때문에, 해당 클래스를
 * 리소스 없이 로드할 경우 예외가 발생하게 됩니다. <br>
 *
 */
public class CanvasAssets {
    private static final BufferedImage CHOICE_5_IMAGE;
    private static final BufferedImage CHOICE_6_IMAGE;
    private static final BufferedImage CHOICE_7_IMAGE;
    private static final BufferedImage CHOICE_8_IMAGE;

    public static final CanvasImage CHOICE_ANOMALY_FRAME_TOP;
    public static final CanvasImage CHOICE_ANOMALY_FRAME;
    public static final CanvasImage CHOICE_ANOMALY_TEXT_FRAME;
    public static final CanvasImage CHOICE_TITLE_BG;
    public static final CanvasImage CHOICE_FRAME_LEFT_EDGE;
    public static final CanvasImage CHOICE_FRAME_CENTER;
    public static final CanvasImage CHOICE_FRAME_RIGHT_EDGE;

    // Anomaly Sprites
    public static final CanvasImage OP_SKULL;
    public static final CanvasImage DEAD_BUTTERFLIES;

    static {
        var path = FabricLoader.getInstance().getModContainer(LCInitializer.MOD_ID).get().findPath("project_moon_assets").get();
        if (!Files.exists(path)) {
            throw new IllegalStateException("Can't find project moon_assets. " +
                    "did you build from github? these resource will not upload in github because of the copyright.");
        }
        CHOICE_5_IMAGE = loadImage(path.resolve("BattleUI_Choice_5.png"));
        CHOICE_6_IMAGE = loadImage(path.resolve("BattleUI_Choice_6.png"));
        CHOICE_7_IMAGE = loadImage(path.resolve("BattleUI_Choice_7.png"));
        CHOICE_8_IMAGE = loadImage(path.resolve("BattleUI_Choice_8.png"));

        CHOICE_ANOMALY_FRAME_TOP = CanvasImage.from(CHOICE_5_IMAGE.getSubimage(776, 260, 204, 260));
        CHOICE_ANOMALY_FRAME = CanvasImage.from(CHOICE_7_IMAGE.getSubimage(0, 1212, 1024, 700));
        CHOICE_ANOMALY_TEXT_FRAME = CanvasImage.from(CHOICE_7_IMAGE.getSubimage(0, 876, 1024, 324));
        CHOICE_TITLE_BG = CanvasImage.from(CHOICE_7_IMAGE.getSubimage(1048 , 300, 512, 124));
        OP_SKULL = CanvasImage.from(loadImage(path.resolve("ChoiceEvent_971034.png")));
        DEAD_BUTTERFLIES = CanvasImage.from(loadImage(path.resolve("ChoiceEvent_971035.png")));
        CHOICE_FRAME_LEFT_EDGE = CanvasImage.from(CanvasAssets.CHOICE_7_IMAGE.getSubimage(764, 0, 56, 280));
        CHOICE_FRAME_CENTER = CanvasImage.from(CanvasAssets.CHOICE_7_IMAGE.getSubimage(820, 0, 176, 280));
        CHOICE_FRAME_RIGHT_EDGE = CanvasImage.from(CanvasAssets.CHOICE_7_IMAGE.getSubimage(1000, 0, 48, 280));
    }

    private static BufferedImage loadImage(Path path)  {
        try {
            return ImageIO.read(Files.newInputStream(path));
        } catch (IOException e) {
            LCInitializer.LOGGER.error("Failed to load image : {}", path);
            return null;
        }
    }

    public static void load() {
        // NO-OP
    }
}
