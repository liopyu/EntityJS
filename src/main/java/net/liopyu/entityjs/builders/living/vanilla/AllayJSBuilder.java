package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.AllayEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.CreeperEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AllayJSBuilder extends PathfinderMobBuilder<AllayEntityJS> {

    public AllayJSBuilder(ResourceLocation i) {
        super(i);
    }


    @Override
    public EntityType.EntityFactory<AllayEntityJS> factory() {
        return (type, level) -> new AllayEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return AllayEntityJS.createAttributes();
    }
}
