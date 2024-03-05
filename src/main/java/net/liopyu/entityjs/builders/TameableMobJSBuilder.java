package net.liopyu.entityjs.builders;


import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.MobEntityJS;
import net.liopyu.entityjs.entities.TameableMobJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TameableMobJSBuilder extends TameableMobBuilder<TameableMobJS> {

    public TameableMobJSBuilder(ResourceLocation i) {
        super(i);

    }

    @Override
    public EntityType.EntityFactory<TameableMobJS> factory() {
        return (type, level) -> new TameableMobJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return MobEntityJS.createMobAttributes()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.LUCK)
                .add(Attributes.MOVEMENT_SPEED);
    }
}