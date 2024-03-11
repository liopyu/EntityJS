package net.liopyu.entityjs.builders.nonliving;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;


public class EntityTypeBuilder<B extends Entity> {

    private final BaseEntityBuilder<?> builder;

    public <T extends BaseEntityBuilder<B>> EntityTypeBuilder(T builder) {
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

