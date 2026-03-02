package com.biryeongtrain.lc.game.data;

public record AbnormalityDefinition(
        String id,
        String name,
        AbnormalityRiskLevel riskLevel,
        BbModelData bbModelData
) {
}
