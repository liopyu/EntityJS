package net.liopyu.entityjs.builders.living.vanilla;

import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.AllayEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.BatEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BatJSBuilder extends MobBuilder<BatEntityJS> {
    public BatJSBuilder(ResourceLocation i) {
        super(i);
    }


    @Override
    public EntityType.EntityFactory<BatEntityJS> factory() {
        return (type, level) -> new BatEntityJS(this, type, level);
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
                .add(Attributes.FLYING_SPEED)
                .add(Attributes.MOVEMENT_SPEED);
    }
}
