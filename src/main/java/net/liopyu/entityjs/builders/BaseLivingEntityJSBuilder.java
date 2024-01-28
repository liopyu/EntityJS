package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.BaseLivingEntityJS;
import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Consumer;

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
        builder.add(Attributes.FOLLOW_RANGE, 16.0F);
        return builder;
    }

}
