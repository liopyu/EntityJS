package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class ProjectileEntityBuilder<T extends AbstractHurtingProjectile & IProjectileEntityJS> extends BaseProjectileBuilder<T> {
    public transient Function<T, ResourceLocation> getTextureLocation;
    public static final List<ProjectileEntityBuilder<?>> thisList = new ArrayList<>();

    public ProjectileEntityBuilder(ResourceLocation i) {
        super(i);
        getTextureLocation = t -> t.getProjectileBuilder().newID("textures/entity/projectiles/", ".png");
        thisList.add(this);
    }

    @Override
    public EntityType<T> createObject() {
        return new ProjectileEntityTypeBuilder<>(this).get();
    }

    @Info(value = """
            Sets how the texture of the entity is determined, has access to the entity
            to allow changing the texture based on info about the entity
                        
            Defaults to returning <namespace>:textures/entity/projectiles/<path>.png
            """)
    public BaseProjectileBuilder<T> getTextureLocation(Function<T, ResourceLocation> function) {
        getTextureLocation = function;
        return this;
    }
}
