package dev.qf.canvas;

import dev.qf.LCInitializer;
import eu.pb4.mapcanvas.api.font.CanvasFont;
import eu.pb4.mapcanvas.api.font.FontUtils;
import net.fabricmc.loader.api.FabricLoader;

import java.awt.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class CanvasFonts {
    // USED IN CHOICE TITLE
    public static final CanvasFont KOTLA_BOLD;
    // USED IN CHOICE BODY FONT
    public static final CanvasFont PRETENDARD_REGULAR;
    public static final CanvasFont PRETENDARD_BOLD;

    static {
        var path = FabricLoader.getInstance().getModContainer(LCInitializer.MOD_ID).get().getPath("fonts");
        KOTLA_BOLD = loadFont(
                path.resolve("KOTRA_BOLD.ttf"),
                new CanvasFont.Metadata("Kotra_bold", List.of("KOTRA"), Optional.of("KOTLA 무역투자 폰트"))
        );
        PRETENDARD_REGULAR = loadFont(
                path.resolve("Pretendard-Regular.ttf"),
                new CanvasFont.Metadata("Pretendard-Regular", List.of("Kil Hyung-jin"), Optional.of("Pretendard"))
        );

        PRETENDARD_BOLD = loadFont(
                path.resolve("Pretendard-Bold.ttf"),
                new CanvasFont.Metadata("Pretendard-Bold", List.of("Kil Hyung-jin"), Optional.of("Pretendard"))
        );
    }

    private static CanvasFont loadFont(Path path, CanvasFont.Metadata metadata) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, path.toFile());
            return FontUtils.fromAwtFont(font, metadata);
        } catch (Exception e) {
            LCInitializer.LOGGER.error("Failed to load font from {}", path, e);
        }

        return null;
    }

    public static void load() {
        // NO-OP
    }
}
