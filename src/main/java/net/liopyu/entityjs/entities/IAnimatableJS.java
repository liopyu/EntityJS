package net.liopyu.entityjs.entities;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public interface IAnimatableJS extends IAnimatable {

    BaseEntityBuilder<?> getBuilder();

    default void registerControllers(AnimationData data) {

    }

    @Override
    default AnimationFactory getFactory() {
        return GeckoLibUtil.createFactory(this);
    }

    default ResourceLocation getTextureLocation() {
        return getBuilder().textureResource.apply(UtilsJS.cast(this));
    }
}
