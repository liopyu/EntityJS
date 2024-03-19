package net.liopyu.entityjs.builders.nonliving.entityjs;


import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.BaseEntityJS;
import net.liopyu.entityjs.entities.living.entityjs.BaseLivingEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class BaseEntityJSBuilder extends BaseEntityBuilder<BaseEntityJS> {

    public BaseEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityType.EntityFactory<BaseEntityJS> factory() {
        return (type, level) -> new BaseEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        //final AttributeSupplier.Builder builder = BaseEntityJS.createLivingAttributes();
        return null; //BaseEntityJS.createLivingAttributes();
    }
}