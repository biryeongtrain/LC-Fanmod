package dev.qf.player;

import dev.qf.LCInitializer;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;

public enum StatType {
    FORTITUDE(20) {
        @Override
        public void applyAttribute(AttributeContainer attributeContainer, int value) {
            EntityAttributeInstance instance = attributeContainer.getCustomInstance(EntityAttributes.MAX_HEALTH);
            instance.setBaseValue(value);
        }
    },
    PRUDENCE(20),
    TEMPERANCE(10),
    JUSTICE(0) {
        @Override
        public void applyAttribute(AttributeContainer attributeContainer, int value) {
            EntityAttributeInstance attackSpeedInstance = attributeContainer.getCustomInstance(EntityAttributes.ATTACK_SPEED);
            EntityAttributeInstance movementSpeedInstance = attributeContainer.getCustomInstance(EntityAttributes.MOVEMENT_SPEED);

            attackSpeedInstance.addPersistentModifier(new EntityAttributeModifier(LCInitializer.id("justice_modifier"), 0.87 * value, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            movementSpeedInstance.addPersistentModifier(new EntityAttributeModifier(LCInitializer.id("justice_modifier"), 0.01 * value, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
    ;

    StatType(int baseValue) {
        this.baseValue = baseValue;
    }

    public final int baseValue;

    public void applyAttribute(AttributeContainer attributeContainer, int value) {}
    public static int getDefaultValue(StatType statType) {
        return statType.baseValue;
    }

    public static StatType getByName(String name) {
        for (StatType value : values()) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
