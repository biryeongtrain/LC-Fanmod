package kim.biryeong.lc.game.data;

/**
 * Metadata for linking an abnormality with its Blockbench (.bbmodel) asset.
 */
public record BbModelData(
        String key,
        String resourcePath
) {
}

