import dev.qf.canvas.CanvasAssets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BufferedImageTest {
    @Test
    public void imageTest() {
        var image = CanvasAssets.CHOICE_ANOMALY_FRAME;
        var image2 = CanvasAssets.CHOICE_TITLE_BG;

        Assertions.assertNotNull(image);
    }
}
