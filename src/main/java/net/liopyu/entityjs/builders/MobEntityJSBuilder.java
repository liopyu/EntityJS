package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class MobEntityJSBuilder extends MobBuilder<MobEntityJS> {

    public MobEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityType.EntityFactory<MobEntityJS> factory() {
        return (type, level) -> new MobEntityJS(this, type ,level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        final AttributeSupplier.Builder builder = MobEntityJS.createMobAttributes();
        attributes.accept(builder);
        return builder;
    }
}
