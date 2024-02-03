package net.liopyu.entityjs.builders;


import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

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
        final AttributeSupplier.Builder builder = MobEntityJS.createMobAttributes();
        builder.add(Attributes.FOLLOW_RANGE, 16.0F);
        attributes.accept(builder);
        return builder;
    }
}
