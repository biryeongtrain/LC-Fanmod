package kim.biryeong.lc.game.containment;

import kim.biryeong.lc.entity.ContainmentAbnormalityEntity;
import kim.biryeong.lc.game.data.DepartmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ContainmentChamber {
    private final UUID id;
    private final RegistryKey<World> worldKey;
    private final BlockPos center;
    private final int horizontalRadius;
    private final int verticalHalfHeight;
    private final Set<UUID> occupantEntityIds = new HashSet<>();
    @Nullable
    private DepartmentType department;
    @Nullable
    private ContainmentAbnormalityEntity abnormalityEntity;
    @Nullable
    private UUID abnormalityEntityUuid;
    private boolean abnormalityEscaping;

    public ContainmentChamber(UUID id, RegistryKey<World> worldKey, BlockPos center, int horizontalRadius, int verticalHalfHeight) {
        this(id, worldKey, center, horizontalRadius, verticalHalfHeight, null);
    }

    public ContainmentChamber(
            UUID id,
            RegistryKey<World> worldKey,
            BlockPos center,
            int horizontalRadius,
            int verticalHalfHeight,
            @Nullable DepartmentType department
    ) {
        this.id = id;
        this.worldKey = worldKey;
        this.center = center.toImmutable();
        this.horizontalRadius = Math.max(1, horizontalRadius);
        this.verticalHalfHeight = Math.max(1, verticalHalfHeight);
        this.department = department;
        this.abnormalityEscaping = false;
    }

    public UUID id() {
        return this.id;
    }

    public BlockPos center() {
        return this.center;
    }

    public RegistryKey<World> worldKey() {
        return this.worldKey;
    }

    public Vec3d centerVec() {
        return Vec3d.ofCenter(this.center);
    }

    public int horizontalRadius() {
        return this.horizontalRadius;
    }

    public int verticalHalfHeight() {
        return this.verticalHalfHeight;
    }

    public Box bounds() {
        return new Box(
                this.center.getX() - this.horizontalRadius,
                this.center.getY() - this.verticalHalfHeight,
                this.center.getZ() - this.horizontalRadius,
                this.center.getX() + this.horizontalRadius + 1,
                this.center.getY() + this.verticalHalfHeight + 1,
                this.center.getZ() + this.horizontalRadius + 1
        );
    }

    public boolean contains(BlockPos pos) {
        return this.bounds().contains(Vec3d.ofCenter(pos));
    }

    public boolean contains(Vec3d pos) {
        return this.bounds().contains(pos);
    }

    public void addOccupant(UUID entityUuid) {
        this.occupantEntityIds.add(entityUuid);
    }

    public void removeOccupant(UUID entityUuid) {
        this.occupantEntityIds.remove(entityUuid);
        if (entityUuid.equals(this.abnormalityEntityUuid)) {
            this.clearAbnormalityEntity();
        }
    }

    public Set<UUID> occupantEntityIds() {
        return Set.copyOf(this.occupantEntityIds);
    }

    @Nullable
    public DepartmentType department() {
        return this.department;
    }

    public void setDepartment(@Nullable DepartmentType department) {
        this.department = department;
    }

    @Nullable
    public ContainmentAbnormalityEntity abnormalityEntity() {
        if (this.abnormalityEntity != null && this.abnormalityEntity.isRemoved()) {
            this.clearAbnormalityEntity();
        }
        return this.abnormalityEntity;
    }

    @Nullable
    public UUID abnormalityEntityUuid() {
        return this.abnormalityEntityUuid;
    }

    public void setAbnormalityEntity(@Nullable ContainmentAbnormalityEntity abnormalityEntity) {
        this.abnormalityEntity = abnormalityEntity;
        if (abnormalityEntity != null) {
            UUID uuid = abnormalityEntity.getUuid();
            this.abnormalityEntityUuid = uuid;
            this.occupantEntityIds.add(uuid);
        } else {
            this.abnormalityEntityUuid = null;
            this.abnormalityEscaping = false;
        }
    }

    public void clearAbnormalityEntity() {
        if (this.abnormalityEntityUuid != null) {
            this.occupantEntityIds.remove(this.abnormalityEntityUuid);
        }
        this.abnormalityEntity = null;
        this.abnormalityEntityUuid = null;
        this.abnormalityEscaping = false;
    }

    public boolean isAbnormalityEscaping() {
        return this.abnormalityEscaping;
    }

    public void setAbnormalityEscaping(boolean abnormalityEscaping) {
        this.abnormalityEscaping = abnormalityEscaping && this.abnormalityEntityUuid != null;
    }

    public boolean isSameDimension(World world) {
        return this.worldKey.equals(world.getRegistryKey());
    }
}

