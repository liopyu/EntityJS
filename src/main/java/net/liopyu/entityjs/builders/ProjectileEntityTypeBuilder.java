package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;


public class ProjectileEntityTypeBuilder<B extends Projectile> {

    private final BaseProjectileBuilder<?> builder;

    public <T extends BaseProjectileBuilder<B>> ProjectileEntityTypeBuilder(T builder) {
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

