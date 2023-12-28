package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.BaseEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;

public class BaseEntityJSBuilder extends BaseEntityBuilder<BaseEntityJS> {
    private double maxHealth = 20.0D;
    private double attackDamage = 3.0D;
    private double attackSpeed = 1.0D;
    private double movementSpeed = 0.4D;
    private final AttributeSupplier.Builder attributeBuilder;
    private final Map<String, Attribute> attributeMap;

    public BaseEntityJSBuilder(ResourceLocation i) {
        super(i);
        this.attributeBuilder = AttributeSupplier.builder();
        this.attributeMap = new HashMap<>();
        initializeAttributeMap();
    }

    private void initializeAttributeMap() {
        attributeMap.put("max_health", Attributes.MAX_HEALTH);
        attributeMap.put("follow_range", Attributes.FOLLOW_RANGE);
        attributeMap.put("knockback_resistance", Attributes.KNOCKBACK_RESISTANCE);
        attributeMap.put("movement_speed", Attributes.MOVEMENT_SPEED);
        attributeMap.put("flying_speed", Attributes.FLYING_SPEED);
        attributeMap.put("attack_damage", Attributes.ATTACK_DAMAGE);
        attributeMap.put("attack_knockback", Attributes.ATTACK_KNOCKBACK);
        attributeMap.put("attack_speed", Attributes.ATTACK_SPEED);
        attributeMap.put("armor", Attributes.ARMOR);
        attributeMap.put("armor_toughness", Attributes.ARMOR_TOUGHNESS);
        attributeMap.put("luck", Attributes.LUCK);
        attributeMap.put("zombie.spawn_reinforcements", Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        attributeMap.put("horse.jump_strength", Attributes.JUMP_STRENGTH);
    }

    @Override
    public EntityTypeBuilderJS.Factory<BaseEntityJS> factory() {
        return ((builder, type, level) -> new BaseEntityJS((BaseEntityJSBuilder) builder, type, level));
    }

    public BaseEntityJSBuilder attribute(String attribute, double value) {
        Attribute attr = attributeMap.get(attribute);
        if (attr != null) {
            attributeBuilder.add(attr, value);
        }
        return this;
    }

    public AttributeSupplier setAttributes() {
        return attributeBuilder.build();
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(double attackDamage) {
        this.attackDamage = attackDamage;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public void setAttackSpeed(double attackSpeed) {
        this.attackSpeed = attackSpeed;
    }

    public double getMovementSpeed() {
        return movementSpeed;
    }

    public void setMovementSpeed(double movementSpeed) {
        this.movementSpeed = movementSpeed;
    }
}