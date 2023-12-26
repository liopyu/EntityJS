package net.liopyu.entityjs.builders;


import net.liopyu.entityjs.entities.BaseEntityJS;
import net.minecraft.resources.ResourceLocation;

public class BaseEntityJSBuilder extends BaseEntityBuilder<BaseEntityJS> {

    public BaseEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityTypeBuilderJS.Factory<BaseEntityJS> factory() {
        return ((builder, type, level) -> new BaseEntityJS((BaseEntityJSBuilder) builder, type, level));
    }
}
