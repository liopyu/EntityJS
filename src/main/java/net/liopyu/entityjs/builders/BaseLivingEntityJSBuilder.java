package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.BaseLivingEntityJS;
import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

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
        attributes.accept(builder);
        return builder;
    }


}
