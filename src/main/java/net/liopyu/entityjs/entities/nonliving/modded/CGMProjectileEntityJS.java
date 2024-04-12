package net.liopyu.entityjs.entities.nonliving.modded;

import com.mrcrayfish.guns.common.Gun;
import com.mrcrayfish.guns.entity.MissileEntity;
import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.item.GunItem;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.modded.CGMProjectileEntityJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.entities.nonliving.entityjs.IProjectileEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.liolib.core.animatable.instance.AnimatableInstanceCache;
import net.liopyu.liolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CGMProjectileEntityJS extends MissileEntity implements IAnimatableJSNL {
    private final AnimatableInstanceCache getAnimatableInstanceCache;

    public CGMProjectileEntityJSBuilder builder;

    public CGMProjectileEntityJS(CGMProjectileEntityJSBuilder builder, EntityType<? extends ProjectileEntity> entityType, Level worldIn) {
        super(entityType, worldIn);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    }

    public CGMProjectileEntityJS(CGMProjectileEntityJSBuilder builder, EntityType<? extends ProjectileEntity> entityType, Level worldIn, LivingEntity shooter, ItemStack weapon, GunItem item, Gun modifiedGun, AnimatableInstanceCache getAnimatableInstanceCache) {
        super(entityType, worldIn, shooter, weapon, item, modifiedGun);
        this.getAnimatableInstanceCache = getAnimatableInstanceCache;
    }


    @Override
    public BaseEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return getAnimatableInstanceCache;
    }

    public String entityName() {
        return this.getType().toString();
    }

    //CGM Overrides
    @Override
    protected void onProjectileTick() {
        if (builder.onProjectileTick != null) {
            EntityJSHelperClass.consumerCallback(builder.onProjectileTick, this, "[EntityJS]: Error in " + entityName() + "builder for field: onProjectileTick.");
        } else super.onProjectileTick();

    }

    public static class ShotContext {
        @Info("The projectile entity")
        public final Entity entity;
        @Info("The target")
        public final Entity target;

        @Info("The hit vector")
        public final Vec3 hitVec;

        @Info("The start vector")
        public final Vec3 startVec;

        @Info("The end vector")
        public final Vec3 endVec;

        @Info("Whether it's a headshot")
        public final boolean headshot;

        public ShotContext(Entity entity, Entity target, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
            this.entity = entity;
            this.target = target;
            this.hitVec = hitVec;
            this.startVec = startVec;
            this.endVec = endVec;
            this.headshot = headshot;
        }
    }


    public static class HitBlockContext {
        @Info("The projectile entity")
        public final Entity entity;
        @Info("The block state")
        public final BlockState state;

        @Info("The block position")
        public final BlockPos pos;

        @Info("The direction of the hit")
        public final Direction face;

        @Info("The x coordinate of the hit")
        public final double x;

        @Info("The y coordinate of the hit")
        public final double y;

        @Info("The z coordinate of the hit")
        public final double z;

        public HitBlockContext(Entity entity, BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
            this.entity = entity;
            this.state = state;
            this.pos = pos;
            this.face = face;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }


    @Override
    public ItemStack getItem() {
        if (builder.setItem != null) {
            return builder.setItem;
        }

        return super.getItem();
    }

    @Override
    public void onExpired() {
        if (builder.explosionEnabled) {
            super.onExpired();
        }
    }

    @Override
    protected void onHitEntity(Entity entity, Vec3 hitVec, Vec3 startVec, Vec3 endVec, boolean headshot) {
        if (builder.explosionEnabled) {
            super.onHitEntity(entity, hitVec, startVec, endVec, headshot);
        }
        if (builder.onHitEntity != null) {
            final ShotContext context = new ShotContext(this, entity, hitVec, startVec, endVec, headshot);
            EntityJSHelperClass.consumerCallback(builder.onHitEntity, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHitBlock.");
        }
    }

    @Override
    protected void onHitBlock(BlockState state, BlockPos pos, Direction face, double x, double y, double z) {
        if (builder.explosionEnabled) {
            super.onHitBlock(state, pos, face, x, y, z);
        }
        if (builder.onHitBlock != null) {
            final HitBlockContext context = new HitBlockContext(this, state, pos, face, x, y, z);
            EntityJSHelperClass.consumerCallback(builder.onHitBlock, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHitBlock.");

        }
    }

    @Override
    public ItemStack getWeapon() {
        if (builder.setWeapon != null) {
            return builder.setWeapon;
        }
        return super.getWeapon();
    }

    //Base Entity Overrides
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (builder != null && builder.onHurt != null) {
            final ContextUtils.EntityHurtContext context = new ContextUtils.EntityHurtContext(this, pSource, pAmount);
            EntityJSHelperClass.consumerCallback(builder.onHurt, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHurt.");

        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        if (builder != null && builder.lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, this);
            EntityJSHelperClass.consumerCallback(builder.lerpTo, context, "[EntityJS]: Error in " + entityName() + "builder for field: lerpTo.");
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (builder != null && builder.tick != null) {
            EntityJSHelperClass.consumerCallback(builder.tick, this, "[EntityJS]: Error in " + entityName() + "builder for field: tick.");
        }
    }

    @Override
    public void move(MoverType pType, Vec3 pPos) {
        super.move(pType, pPos);
        if (builder != null && builder.move != null) {
            final ContextUtils.MovementContext context = new ContextUtils.MovementContext(pType, pPos, this);
            EntityJSHelperClass.consumerCallback(builder.move, context, "[EntityJS]: Error in " + entityName() + "builder for field: move.");
        }
    }

    @Override
    public void playerTouch(Player player) {
        if (builder != null && builder.playerTouch != null) {
            final ContextUtils.EntityPlayerContext context = new ContextUtils.EntityPlayerContext(player, this);
            EntityJSHelperClass.consumerCallback(builder.playerTouch, context, "[EntityJS]: Error in " + entityName() + "builder for field: playerTouch.");
        } else {
            super.playerTouch(player);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (builder != null && builder.onRemovedFromWorld != null) {
            EntityJSHelperClass.consumerCallback(builder.onRemovedFromWorld, this, "[EntityJS]: Error in " + entityName() + "builder for field: onRemovedFromWorld.");
        }
    }

    @Override
    public void thunderHit(ServerLevel p_19927_, LightningBolt p_19928_) {
        if (builder != null && builder.thunderHit != null) {
            super.thunderHit(p_19927_, p_19928_);
            final ContextUtils.EThunderHitContext context = new ContextUtils.EThunderHitContext(p_19927_, p_19928_, this);
            EntityJSHelperClass.consumerCallback(builder.thunderHit, context, "[EntityJS]: Error in " + entityName() + "builder for field: thunderHit.");
        }
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (builder != null && builder.onFall != null) {
            final ContextUtils.EEntityFallDamageContext context = new ContextUtils.EEntityFallDamageContext(this, pMultiplier, pFallDistance, pSource);
            EntityJSHelperClass.consumerCallback(builder.onFall, context, "[EntityJS]: Error in " + entityName() + "builder for field: onLivingFall.");
        }
        return super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }


    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (builder != null && builder.onAddedToWorld != null && !this.level.isClientSide()) {
            EntityJSHelperClass.consumerCallback(builder.onAddedToWorld, this, "[EntityJS]: Error in " + entityName() + "builder for field: onAddedToWorld.");
        }
    }

    @Override
    public void setSprinting(boolean sprinting) {
        if (builder != null && builder.onSprint != null) {
            EntityJSHelperClass.consumerCallback(builder.onSprint, this, "[EntityJS]: Error in " + entityName() + "builder for field: onSprint.");
        }
        super.setSprinting(sprinting);
    }


    @Override
    public void stopRiding() {
        super.stopRiding();
        if (builder != null && builder.onStopRiding != null) {
            EntityJSHelperClass.consumerCallback(builder.onStopRiding, this, "[EntityJS]: Error in " + entityName() + "builder for field: onStopRiding.");
        }
    }


    @Override
    public void rideTick() {
        super.rideTick();
        if (builder != null && builder.rideTick != null) {
            EntityJSHelperClass.consumerCallback(builder.rideTick, this, "[EntityJS]: Error in " + entityName() + "builder for field: rideTick.");
        }
    }

    @Override
    public void onClientRemoval() {
        if (builder != null && builder.onClientRemoval != null) {
            EntityJSHelperClass.consumerCallback(builder.onClientRemoval, this, "[EntityJS]: Error in " + entityName() + "builder for field: onClientRemoval.");
        }
        super.onClientRemoval();
    }


    @Override
    public void lavaHurt() {
        if (builder != null && builder.lavaHurt != null) {
            EntityJSHelperClass.consumerCallback(builder.lavaHurt, this, "[EntityJS]: Error in " + entityName() + "builder for field: lavaHurt.");
        }
        super.lavaHurt();
    }


    @Override
    protected void onFlap() {
        if (builder != null && builder.onFlap != null) {
            EntityJSHelperClass.consumerCallback(builder.onFlap, this, "[EntityJS]: Error in " + entityName() + "builder for field: onFlap.");
        }
        super.onFlap();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        if (builder != null && builder.shouldRenderAtSqrDistance != null) {
            final ContextUtils.EntitySqrDistanceContext context = new ContextUtils.EntitySqrDistanceContext(distance, this);
            Object obj = builder.shouldRenderAtSqrDistance.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid shouldRenderAtSqrDistance for arrow builder: " + obj + ". Must be a boolean. Defaulting to super method: " + super.shouldRenderAtSqrDistance(distance));
        }
        return super.shouldRenderAtSqrDistance(distance);
    }

    @Override
    public boolean isAttackable() {
        return builder.isAttackable != null ? builder.isAttackable : super.isAttackable();
    }

    @Override
    public boolean isPushable() {
        return builder.isPushable;
    }
}
