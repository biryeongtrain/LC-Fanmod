package kim.biryeong.lc.entity;

import kim.biryeong.lc.LobotomyCorporationInitializer;
import kim.biryeong.lc.game.containment.ContainmentChamber;
import kim.biryeong.lc.game.containment.ContainmentChamberManager;
import kim.biryeong.lc.game.data.AbnormalityCatalog;
import kim.biryeong.lc.game.data.AbnormalityDefinition;
import kim.biryeong.lc.game.data.BbModelData;
import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.wrapper.DisplayWrapper;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.core.model.Pose;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

public class ContainmentAbnormalityEntity extends Entity implements AnimatedEntity {
    private static final String NBT_ABNORMALITY_ID = "lc$abnormality_id";
    private static final String NBT_CHAMBER_ID = "lc$containment_chamber_id";
    private static final String MODEL_DIR_PREFIX = "model/";
    private static final String MODEL_FILE_SUFFIX = ".bbmodel";
    private static final Map<Identifier, Model> MODEL_CACHE = new ConcurrentHashMap<>();
    private static final Set<Identifier> MODEL_LOAD_FAILURES = ConcurrentHashMap.newKeySet();

    private String abnormalityId = AbnormalityCatalog.fallback().id();
    @Nullable
    private UUID chamberId;
    @Nullable
    private ContainmentChamber chamber;
    @Nullable
    private EntityHolder<ContainmentAbnormalityEntity> holder;
    @Nullable
    private Identifier holderModelIdentifier;
    @Nullable
    private String unresolvedModelToken;

    public ContainmentAbnormalityEntity(EntityType<? extends ContainmentAbnormalityEntity> entityType, World world) {
        super(entityType, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setAbnormalityId(view.getString(NBT_ABNORMALITY_ID, AbnormalityCatalog.fallback().id()));

        this.unbindChamber();
        view.getOptionalString(NBT_CHAMBER_ID).ifPresent(chamberIdString -> {
            try {
                this.chamberId = UUID.fromString(chamberIdString);
                this.chamber = null;
                this.resolveChamberReference();
            } catch (IllegalArgumentException ignored) {
                this.unbindChamber();
            }
        });
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putString(NBT_ABNORMALITY_ID, this.abnormalityId);
        if (this.chamberId != null) {
            view.putString(NBT_CHAMBER_ID, this.chamberId.toString());
        }
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket(this, entityTrackerEntry);
    }

    @Override
    @Nullable
    public AnimatedEntityHolder getHolder() {
        this.refreshHolderModel();
        return this.holder;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient()) {
            return;
        }

        this.resolveChamberReference();
        this.enforceContainmentBounds();
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void remove(RemovalReason reason) {
        this.destroyHolder();
        this.unbindChamber();
        super.remove(reason);
    }

    public void setAbnormalityId(String abnormalityId) {
        AbnormalityDefinition definition = AbnormalityCatalog.byId(abnormalityId);
        this.abnormalityId = definition != null ? definition.id() : AbnormalityCatalog.fallback().id();
        this.refreshHolderModel();
    }

    public String getAbnormalityId() {
        return this.abnormalityId;
    }

    public AbnormalityDefinition getAbnormality() {
        AbnormalityDefinition definition = AbnormalityCatalog.byId(this.abnormalityId);
        if (definition != null) {
            return definition;
        }
        return AbnormalityCatalog.fallback();
    }

    public BbModelData getBbModelData() {
        return this.getAbnormality().bbModelData();
    }

    public Optional<ContainmentChamber> getChamber() {
        this.resolveChamberReference();
        return Optional.ofNullable(this.chamber);
    }

    public boolean isEscapingFromChamber() {
        return this.getChamber()
                .map(ContainmentChamber::isAbnormalityEscaping)
                .orElse(false);
    }

    public void setEscapingFromChamber(boolean escaping) {
        this.resolveChamberReference();
        if (this.chamberId != null) {
            ContainmentChamberManager.setAbnormalityEscaping(this.chamberId, escaping);
        }
    }

    @Nullable
    public UUID getChamberId() {
        return this.chamberId;
    }

    public void bindToChamber(ContainmentChamber chamber) {
        Objects.requireNonNull(chamber, "chamber");
        this.unbindChamber();

        ContainmentChamberManager.register(chamber);
        this.chamber = chamber;
        this.chamberId = chamber.id();
        ContainmentChamberManager.bindAbnormality(chamber.id(), this);

        Vec3d center = chamber.centerVec();
        this.refreshPositionAndAngles(center.x, center.y, center.z, this.getYaw(), this.getPitch());
    }

    public static ContainmentAbnormalityEntity spawn(
            ServerWorld world,
            ContainmentChamber chamber,
            AbnormalityDefinition abnormality
    ) {
        ContainmentAbnormalityEntity entity = LCEntities.create(world);
        entity.setAbnormalityId(abnormality.id());
        entity.bindToChamber(chamber);
        world.spawnEntity(entity);
        return entity;
    }

    public static void preloadCatalogModels() {
        for (AbnormalityDefinition abnormality : AbnormalityCatalog.all()) {
            Identifier modelIdentifier = resolveModelIdentifier(abnormality.bbModelData(), abnormality.id());
            if (modelIdentifier == null) {
                continue;
            }
            loadModel(modelIdentifier, abnormality.id(), abnormality.bbModelData().resourcePath());
        }
    }

    private void resolveChamberReference() {
        if (this.chamber != null) {
            return;
        }
        if (this.chamberId == null) {
            return;
        }

        ContainmentChamberManager.get(this.chamberId).ifPresentOrElse(chamberRef -> {
            this.chamber = chamberRef;
            ContainmentChamberManager.attachOccupant(chamberRef.id(), this.getUuid());
            UUID abnormalityEntityId = chamberRef.abnormalityEntityUuid();
            if (abnormalityEntityId == null || abnormalityEntityId.equals(this.getUuid())) {
                ContainmentChamberManager.bindAbnormality(chamberRef.id(), this);
            }
        }, this::unbindChamber);
    }

    private void enforceContainmentBounds() {
        if (this.chamber == null) {
            return;
        }

        if (this.chamber.isAbnormalityEscaping()) {
            return;
        }

        if (!this.chamber.isSameDimension(this.getWorld())) {
            return;
        }

        BlockPos currentPos = this.getBlockPos();
        if (!this.chamber.contains(currentPos)) {
            Vec3d center = this.chamber.centerVec();
            this.setPosition(center.x, center.y, center.z);
            this.setVelocity(Vec3d.ZERO);
            this.velocityModified = true;
        }
    }

    private void unbindChamber() {
        if (this.chamberId != null) {
            ContainmentChamberManager.detachOccupant(this.chamberId, this.getUuid());
            ContainmentChamberManager.unbindAbnormality(this.chamberId, this.getUuid());
        }
        this.chamber = null;
        this.chamberId = null;
    }

    private void refreshHolderModel() {
        if (this.getWorld().isClient()) {
            return;
        }

        AbnormalityDefinition abnormality = this.getAbnormality();
        String modelToken = abnormality.id() + "|" + abnormality.bbModelData().resourcePath();
        if (this.holder == null && modelToken.equals(this.unresolvedModelToken)) {
            return;
        }
        if (this.holder != null && modelToken.equals(this.unresolvedModelToken)) {
            this.unresolvedModelToken = null;
        }

        ModelSelection selectedModel = this.resolveModel(abnormality);
        if (selectedModel == null) {
            if (this.holder == null) {
                this.unresolvedModelToken = modelToken;
            }
            return;
        }

        if (this.holder != null && selectedModel.identifier().equals(this.holderModelIdentifier)) {
            return;
        }

        this.unresolvedModelToken = null;
        this.destroyHolder();

        this.holder = new EntityHolder<>(this, selectedModel.model()) {
            @Override
            public void updateElement(ServerPlayerEntity serverPlayerEntity, DisplayWrapper<?> display, @Nullable Pose pose) {
                display.element().setYaw(this.parent.getYaw());
                display.element().setPitch(this.parent.getPitch());
                super.updateElement(serverPlayerEntity, display, pose);
            }
        };
        this.holderModelIdentifier = selectedModel.identifier();
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Nullable
    private ModelSelection resolveModel(AbnormalityDefinition abnormality) {
        Identifier requestedIdentifier = this.resolveModelIdentifier(abnormality.bbModelData(), abnormality.id());
        if (requestedIdentifier != null) {
            Model requestedModel = this.loadModel(requestedIdentifier, abnormality.id(), abnormality.bbModelData().resourcePath());
            if (requestedModel != null) {
                return new ModelSelection(requestedIdentifier, requestedModel);
            }
        }

        AbnormalityDefinition fallback = AbnormalityCatalog.fallback();
        if (!fallback.id().equals(abnormality.id())) {
            Identifier fallbackIdentifier = this.resolveModelIdentifier(fallback.bbModelData(), fallback.id());
            if (fallbackIdentifier != null) {
                Model fallbackModel = this.loadModel(fallbackIdentifier, fallback.id(), fallback.bbModelData().resourcePath());
                if (fallbackModel != null) {
                    LobotomyCorporationInitializer.LOGGER.warn(
                            "Failed to load bbmodel for abnormality {} (path={}), using fallback {}",
                            abnormality.id(),
                            abnormality.bbModelData().resourcePath(),
                            fallbackIdentifier
                    );
                    return new ModelSelection(fallbackIdentifier, fallbackModel);
                }
            }
        }

        LobotomyCorporationInitializer.LOGGER.error(
                "Unable to resolve any bbmodel for abnormality {} (path={})",
                abnormality.id(),
                abnormality.bbModelData().resourcePath()
        );
        return null;
    }

    @Nullable
    private static Identifier resolveModelIdentifier(BbModelData modelData, String abnormalityId) {
        String configuredPath = modelData.resourcePath();
        if (configuredPath == null || configuredPath.isBlank()) {
            LobotomyCorporationInitializer.LOGGER.error(
                    "Missing bbmodel path in abnormality definition {}",
                    abnormalityId
            );
            return null;
        }

        String namespace = LobotomyCorporationInitializer.MOD_ID;
        String path = configuredPath.trim();

        int separatorIndex = path.indexOf(':');
        if (separatorIndex >= 0) {
            namespace = path.substring(0, separatorIndex);
            path = path.substring(separatorIndex + 1);
        }

        String normalizedPath = normalizeModelPath(path);
        if (normalizedPath.isBlank()) {
            LobotomyCorporationInitializer.LOGGER.error(
                    "Invalid bbmodel path '{}' for abnormality {}",
                    configuredPath,
                    abnormalityId
            );
            return null;
        }

        try {
            return Identifier.of(namespace, normalizedPath);
        } catch (IllegalArgumentException exception) {
            LobotomyCorporationInitializer.LOGGER.error(
                    "Failed to parse bbmodel identifier: abnormality={} path='{}' namespace='{}' normalized='{}'",
                    abnormalityId,
                    configuredPath,
                    namespace,
                    normalizedPath,
                    exception
            );
            return null;
        }
    }

    private static String normalizeModelPath(String path) {
        String normalized = path.replace('\\', '/').trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith(MODEL_DIR_PREFIX)) {
            normalized = normalized.substring(MODEL_DIR_PREFIX.length());
        }
        if (normalized.endsWith(MODEL_FILE_SUFFIX)) {
            normalized = normalized.substring(0, normalized.length() - MODEL_FILE_SUFFIX.length());
        }
        return normalized;
    }

    @Nullable
    private static Model loadModel(Identifier modelIdentifier, String abnormalityId, String configuredPath) {
        Model cached = MODEL_CACHE.get(modelIdentifier);
        if (cached != null) {
            return cached;
        }

        if (MODEL_LOAD_FAILURES.contains(modelIdentifier)) {
            return null;
        }

        try {
            Model model = BbModelLoader.load(modelIdentifier);
            MODEL_CACHE.put(modelIdentifier, model);
            return model;
        } catch (RuntimeException exception) {
            MODEL_LOAD_FAILURES.add(modelIdentifier);
            LobotomyCorporationInitializer.LOGGER.error(
                    "Failed to load bbmodel: abnormality={} configuredPath={} resolvedIdentifier={}",
                    abnormalityId,
                    configuredPath,
                    modelIdentifier,
                    exception
            );
            return null;
        }
    }

    private void destroyHolder() {
        if (this.holder != null) {
            this.holder.destroy();
            this.holder = null;
            this.holderModelIdentifier = null;
        }
    }

    private record ModelSelection(Identifier identifier, Model model) {
    }
}

