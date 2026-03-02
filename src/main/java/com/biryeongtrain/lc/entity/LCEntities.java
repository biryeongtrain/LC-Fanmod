package com.biryeongtrain.lc.entity;

import com.biryeongtrain.lc.LobotomyCorporationInitializer;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class LCEntities {
    public static final EntityType<ContainmentAbnormalityEntity> CONTAINMENT_ABNORMALITY = register(
            "containment_abnormality",
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ContainmentAbnormalityEntity::new)
                    .trackRangeBlocks(8)
                    .trackedUpdateRate(1)
                    .dimensions(EntityDimensions.fixed(0.8f, 1.8f))
    );

    private LCEntities() {
    }

    public static void register() {
        PolymerEntityUtils.registerType(CONTAINMENT_ABNORMALITY);
    }

    public static ContainmentAbnormalityEntity create(ServerWorld world) {
        return new ContainmentAbnormalityEntity(CONTAINMENT_ABNORMALITY, world);
    }

    private static <T extends net.minecraft.entity.Entity> EntityType<T> register(String path, FabricEntityTypeBuilder<T> builder) {
        Identifier id = Identifier.of(LobotomyCorporationInitializer.MOD_ID, path);
        RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
        EntityType<T> type = builder.build(key);
        return Registry.register(Registries.ENTITY_TYPE, id, type);
    }
}
