package net.liopyu.entityjs.builders;


import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class AnimalEntityJSBuilder extends AnimalEntityBuilder<AnimalEntityJS> {

    public AnimalEntityJSBuilder(ResourceLocation i) {
        super(i);

    }

    @Override
    public EntityType.EntityFactory<AnimalEntityJS> factory() {
        return (type, level) -> new AnimalEntityJS(this, type, level);
    }


    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        final AttributeSupplier.Builder builder = AnimalEntityJS.createLivingAttributes();
        attributes.accept(builder);
        return builder;
    }

}
