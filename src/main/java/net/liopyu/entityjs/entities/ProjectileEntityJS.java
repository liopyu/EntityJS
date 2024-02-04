package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.util.ContextUtils;
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
        if (builder != null && builder.tick != null) {
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


    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (builder != null && builder.onHitEntity != null) {
            final ContextUtils.ProjectileEntityHitContext context = new ContextUtils.ProjectileEntityHitContext(result, this);
            builder.onHitEntity.accept(context);
        } else {
            super.onHitEntity(result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (builder != null && builder.onHitBlock != null) {
            final ContextUtils.ProjectileBlockHitContext context = new ContextUtils.ProjectileBlockHitContext(result, this);
            builder.onHitBlock.accept(context);
        } else {
            super.onHitBlock(result);
        }
    }


    @Override
    protected boolean canHitEntity(Entity entity) {
        if (builder != null && builder.canHitEntity != null) {
            return builder.canHitEntity.test(entity);
        } else {
            return super.canHitEntity(entity);
        }
    }


    @Override
    public void playerTouch(Player player) {
        if (builder != null && builder.playerTouch != null) {
            final ContextUtils.ProjectilePlayerContext context = new ContextUtils.ProjectilePlayerContext(player, this);
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
