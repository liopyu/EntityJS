package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.ArrowEntityJS;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.liopyu.entityjs.entities.ProjectileEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.BiConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public abstract class ProjectileEntityBuilder<T extends ThrowableItemProjectile & IProjectileEntityJS> extends BaseEntityBuilder<T> {
    public transient Function<T, ResourceLocation> getTextureLocation;
    public static final List<ProjectileEntityBuilder<?>> thisList = new ArrayList<>();
    public transient Consumer<Object> setSoundEvent;

    public transient Predicate<Double> shouldRenderAtSqrDistance;
    public transient BaseLivingEntityBuilder.HeptConsumer lerpTo;
    public transient Consumer<Projectile> tick;
    public transient BiConsumer<MoverType, Vec3> move;

    public transient Consumer<ContextUtils.ProjectileEntityHitContext> onHitEntity;
    public transient Consumer<ContextUtils.ProjectileBlockHitContext> onHitBlock;

    public transient Predicate<Entity> canHitEntity;
    public transient Consumer<ContextUtils.ProjectilePlayerContext> playerTouch;

    public transient BooleanSupplier isAttackable;

    public ProjectileEntityBuilder(ResourceLocation i) {
        super(i);
        getTextureLocation = t -> t.getProjectileBuilder().newID("textures/entity/projectiles/", ".png");
        thisList.add(this);
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }

    @Info(value = """
            Sets how the texture of the entity is determined, has access to the entity
            to allow changing the texture based on info about the entity
                        
            Defaults to returning <namespace>:textures/entity/projectiles/<path>.png
            """)
    public BaseEntityBuilder<T> getTextureLocation(Function<T, ResourceLocation> function) {
        getTextureLocation = function;
        return this;
    }

    public ProjectileEntityBuilder<T> setSoundEvent(Consumer<Object> consumer) {
        setSoundEvent = consumer;
        return this;
    }

    @Info(value = "Sets whether the entity should render at a squared distance.")
    public ProjectileEntityBuilder<T> shouldRenderAtSqrDistance(Predicate<Double> predicate) {
        shouldRenderAtSqrDistance = predicate;
        return this;
    }

    @Info(value = "Sets the lerpTo behavior with parameters (x, y, z, yaw, pitch, posRotationIncrements, teleport).")
    public ProjectileEntityBuilder<T> lerpTo(BaseLivingEntityBuilder.HeptConsumer consumer) {
        lerpTo = consumer;
        return this;
    }

    @Info(value = "Sets the custom tick behavior.")
    public ProjectileEntityBuilder<T> tick(Consumer<Projectile> consumer) {
        tick = consumer;
        return this;
    }

    @Info(value = "Sets the custom move behavior with parameters (pType, pPos).")
    public ProjectileEntityBuilder<T> move(BiConsumer<MoverType, Vec3> consumer) {
        move = consumer;
        return this;
    }


    @Info(value = "Sets the behavior when the arrow hits an entity.")
    public ProjectileEntityBuilder<T> onHitEntity(Consumer<ContextUtils.ProjectileEntityHitContext> consumer) {
        onHitEntity = consumer;
        return this;
    }

    @Info(value = "Sets the behavior when the projectile hits a block.")
    public ProjectileEntityBuilder<T> onHitBlock(Consumer<ContextUtils.ProjectileBlockHitContext> consumer) {
        onHitBlock = consumer;
        return this;
    }


    @Info(value = "Sets the canHitEntity behavior with parameters (entity).")
    public ProjectileEntityBuilder<T> canHitEntity(Predicate<Entity> predicate) {
        canHitEntity = predicate;
        return this;
    }

    @Info(value = "Sets the playerTouch behavior with parameters (player).")
    public ProjectileEntityBuilder<T> playerTouch(Consumer<ContextUtils.ProjectilePlayerContext> consumer) {
        playerTouch = consumer;
        return this;
    }

    @Info(value = "Sets whether the arrow entity can be attacked.")
    public ProjectileEntityBuilder<T> isAttackable(BooleanSupplier supplier) {
        isAttackable = supplier;
        return this;
    }


}
