package dev.qf.mixin;

import dev.qf.player.EmployeeType;
import dev.qf.player.Employees;
import dev.qf.player.StatType;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.EnumMap;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerMixin implements Employees {
    @Unique
    private final EnumMap<StatType, Integer> statMap = new EnumMap<>(StatType.class);

    @Unique
    private EmployeeType employeeType = EmployeeType.NONE;
    @Unique
    private int currentSP = StatType.FORTITUDE.baseValue;
    @Override
    public EmployeeType lc$getType() {
        return employeeType;
    }

    @Override
    public float lc$getCurrentSP() {
        return this.currentSP;

    }

    @Override
    public int getFortitude() {
        return this.statMap.computeIfAbsent(StatType.FORTITUDE, StatType::getDefaultValue);
    }

    @Override
    public int getPrudence() {
        return this.statMap.computeIfAbsent(StatType.PRUDENCE, StatType::getDefaultValue);
    }

    @Override
    public int getTemperance() {
        return this.statMap.computeIfAbsent(StatType.TEMPERANCE, StatType::getDefaultValue);
    }

    @Override
    public int getJustice() {
        return this.statMap.computeIfAbsent(StatType.JUSTICE, StatType::getDefaultValue);
    }

    @Override
    public void lc$applyAttribute() {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        AttributeContainer container = player.getAttributes();
        for (StatType type : StatType.values()) {
            var value = this.statMap.computeIfAbsent(type, StatType::getDefaultValue);
            type.applyAttribute(container, value);
        }
    }

    @Override
    public int lc$getStat(StatType stat) {
        return this.statMap.computeIfAbsent(stat, StatType::getDefaultValue);
    }
}
