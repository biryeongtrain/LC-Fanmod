package com.biryeongtrain.lc.game.containment;

import com.biryeongtrain.lc.entity.ContainmentAbnormalityEntity;
import com.biryeongtrain.lc.game.data.DepartmentType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ContainmentChamberManager {
    private static final Map<UUID, ContainmentChamber> CHAMBERS = new HashMap<>();

    private ContainmentChamberManager() {
    }

    public static ContainmentChamber register(ContainmentChamber chamber) {
        CHAMBERS.put(chamber.id(), chamber);
        return chamber;
    }

    public static ContainmentChamber create(ServerWorld world, BlockPos center, int horizontalRadius, int verticalHalfHeight) {
        return register(new ContainmentChamber(UUID.randomUUID(), world.getRegistryKey(), center, horizontalRadius, verticalHalfHeight));
    }

    public static ContainmentChamber create(
            ServerWorld world,
            BlockPos center,
            int horizontalRadius,
            int verticalHalfHeight,
            @Nullable DepartmentType department
    ) {
        return register(new ContainmentChamber(
                UUID.randomUUID(),
                world.getRegistryKey(),
                center,
                horizontalRadius,
                verticalHalfHeight,
                department
        ));
    }

    public static Optional<ContainmentChamber> get(UUID chamberId) {
        return Optional.ofNullable(CHAMBERS.get(chamberId));
    }

    public static void remove(UUID chamberId) {
        CHAMBERS.remove(chamberId);
    }

    public static Collection<ContainmentChamber> all() {
        return CHAMBERS.values();
    }

    @Nullable
    public static ContainmentChamber findNearest(ServerWorld world, BlockPos pos) {
        return CHAMBERS.values().stream()
                .filter(chamber -> chamber.worldKey().equals(world.getRegistryKey()))
                .min(Comparator.comparingDouble(chamber -> chamber.center().getSquaredDistance(pos)))
                .orElse(null);
    }

    @Nullable
    public static ContainmentChamber findContaining(ServerWorld world, BlockPos pos) {
        return CHAMBERS.values().stream()
                .filter(chamber -> chamber.worldKey().equals(world.getRegistryKey()))
                .filter(chamber -> chamber.contains(pos))
                .findFirst()
                .orElse(null);
    }

    public static void attachOccupant(UUID chamberId, UUID entityId) {
        ContainmentChamber chamber = CHAMBERS.get(chamberId);
        if (chamber != null) {
            chamber.addOccupant(entityId);
        }
    }

    public static void detachOccupant(UUID chamberId, UUID entityId) {
        ContainmentChamber chamber = CHAMBERS.get(chamberId);
        if (chamber != null) {
            chamber.removeOccupant(entityId);
        }
    }

    public static void assignDepartment(UUID chamberId, @Nullable DepartmentType department) {
        ContainmentChamber chamber = CHAMBERS.get(chamberId);
        if (chamber != null) {
            chamber.setDepartment(department);
        }
    }

    public static void bindAbnormality(UUID chamberId, ContainmentAbnormalityEntity abnormalityEntity) {
        ContainmentChamber chamber = CHAMBERS.get(chamberId);
        if (chamber != null) {
            chamber.setAbnormalityEntity(abnormalityEntity);
            chamber.setAbnormalityEscaping(false);
        }
    }

    public static void unbindAbnormality(UUID chamberId, UUID abnormalityEntityId) {
        ContainmentChamber chamber = CHAMBERS.get(chamberId);
        if (chamber != null && abnormalityEntityId.equals(chamber.abnormalityEntityUuid())) {
            chamber.clearAbnormalityEntity();
        }
    }

    public static void setAbnormalityEscaping(UUID chamberId, boolean escaping) {
        ContainmentChamber chamber = CHAMBERS.get(chamberId);
        if (chamber != null) {
            chamber.setAbnormalityEscaping(escaping);
        }
    }

    public static void clear() {
        CHAMBERS.clear();
    }
}
