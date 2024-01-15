package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.ArrowEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class ArrowEntityJSBuilder extends ArrowEntityBuilder<ArrowEntityJS> {
    public ArrowEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityType.EntityFactory<ArrowEntityJS> factory() {
        return (type, level) -> new ArrowEntityJS(this, type, level);
    }
}
