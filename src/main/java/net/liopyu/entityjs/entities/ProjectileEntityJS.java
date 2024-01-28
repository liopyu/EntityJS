package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.util.Wrappers;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ProjectileEntityJS extends ThrowableItemProjectile implements IProjectileEntityJS, ItemSupplier {


    public ProjectileEntityJSBuilder builder;

    public ProjectileEntityJS(ProjectileEntityJSBuilder builder, EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    public ProjectileEntityJS(EntityType<? extends ThrowableItemProjectile> pEntityType, LivingEntity pShooter, Level pLevel) {
        super(pEntityType, pShooter, pLevel);

    }

    @Override
    public ProjectileEntityBuilder<?> getProjectileBuilder() {
        return builder;
    }


    @Override
    protected Item getDefaultItem() {
        return null;
    }

    @Override
    public ItemStack getItem() {
        return null;
    }


    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return builder.shouldRenderAtSqrDistance != null ? builder.shouldRenderAtSqrDistance.test(distance) : super.shouldRenderAtSqrDistance(distance);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        if (builder.lerpTo != null) {
            builder.lerpTo.accept(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        }
    }


    @Override
    public void tick() {
        super.tick();
        if (builder.tick != null) {
            builder.tick.accept(this);
        }
    }

    @Override
    public void move(MoverType pType, Vec3 pPos) {
        super.move(pType, pPos);
        if (builder.move != null) {
            builder.move.accept(pType, pPos);
        }
    }


    public static class ProjectileEntityHitContext {
        public final ThrowableItemProjectile entity;
        public final EntityHitResult result;

        public ProjectileEntityHitContext(EntityHitResult result, ThrowableItemProjectile entity) {
            this.entity = entity;
            this.result = result;
        }
    }

    public static class ProjectileBlockHitContext {
        public final ThrowableItemProjectile entity;
        public final BlockHitResult result;

        public ProjectileBlockHitContext(BlockHitResult result, ThrowableItemProjectile entity) {
            this.entity = entity;
            this.result = result;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (builder.onHitEntity != null) {
            final ProjectileEntityHitContext context = new ProjectileEntityHitContext(result, this);
            builder.onHitEntity.accept(context);
        } else {
            super.onHitEntity(result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (builder.onHitBlock != null) {
            final ProjectileBlockHitContext context = new ProjectileBlockHitContext(result, this);
            builder.onHitBlock.accept(context);
        } else {
            super.onHitBlock(result);
        }
    }


    @Override
    protected boolean canHitEntity(Entity entity) {
        return builder.canHitEntity != null ? builder.canHitEntity.test(entity) : super.canHitEntity(entity);
    }

    public static class ProjectilePlayerContext {
        public final ThrowableItemProjectile entity;
        public final Player player;

        public ProjectilePlayerContext(Player player, ThrowableItemProjectile entity) {
            this.entity = entity;
            this.player = player;
        }
    }


    @Override
    public void playerTouch(Player player) {
        if (builder.playerTouch != null) {
            final ProjectilePlayerContext context = new ProjectilePlayerContext(player, this);
            builder.playerTouch.accept(context);
        } else {
            super.playerTouch(player);
        }
    }

    @Override
    public boolean isAttackable() {
        return builder.isAttackable != null ? builder.isAttackable.getAsBoolean() : super.isAttackable();
    }
}
