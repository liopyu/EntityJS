package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;


public class ArrowEntityTypeBuilder<B extends Projectile & IArrowEntityJS> {

    private final ProjectileEntityBuilder<?> builder;

    public <T extends ProjectileEntityBuilder<B>> ArrowEntityTypeBuilder(T builder) {
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

