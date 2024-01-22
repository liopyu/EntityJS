package net.liopyu.entityjs.builders;


import net.liopyu.entityjs.entities.BaseEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class BaseEntityJSBuilder extends BaseLivingEntityBuilder<BaseEntityJS> {

    public BaseEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityType.EntityFactory<BaseEntityJS> factory() {
        return (type, level) -> new BaseEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        final AttributeSupplier.Builder builder = BaseEntityJS.createLivingAttributes();
        attributes.accept(builder);
        return builder;
    }
}
