package net.liopyu.entityjs.builders.nonliving.vanilla;

import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.BaseLivingEntityJS;
import net.liopyu.entityjs.entities.nonliving.vanilla.BoatEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BoatJSBuilder extends BaseEntityBuilder<BoatEntityJS> {
    public BoatJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityType.EntityFactory<BoatEntityJS> factory() {
        return (type, level) -> new BoatEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        final AttributeSupplier.Builder builder = BaseLivingEntityJS.createLivingAttributes();
        builder.add(Attributes.ATTACK_DAMAGE);
        builder.add(Attributes.ATTACK_SPEED);
        builder.add(Attributes.ATTACK_KNOCKBACK);
        return BaseLivingEntityJS.createLivingAttributes();
    }
}
