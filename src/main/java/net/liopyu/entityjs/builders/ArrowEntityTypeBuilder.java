package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;


public class ArrowEntityTypeBuilder<B extends Arrow & IArrowEntityJS> {

    private final ArrowEntityBuilder<?> builder;

    public <T extends ArrowEntityBuilder<B>> ArrowEntityTypeBuilder(T builder) {
        this.builder = builder;
    }


    public EntityType<B> get() {
        var js = this.builder;
        var builder = EntityType.Builder.of(js.factory(), js.mobCategory);
        builder
                .sized(js.width, js.height)
                .clientTrackingRange(js.clientTrackingRange)
                .updateInterval(js.updateInterval);

        return UtilsJS.cast(builder.build(js.id.toString())); // If this fails, uh... do better?
    }
}

