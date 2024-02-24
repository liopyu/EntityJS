package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.BaseLivingEntityJS;
import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BaseLivingEntityJSBuilder extends BaseLivingEntityBuilder<BaseLivingEntityJS> {

    public BaseLivingEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityType.EntityFactory<BaseLivingEntityJS> factory() {
        return (type, level) -> new BaseLivingEntityJS(this, type, level);
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
