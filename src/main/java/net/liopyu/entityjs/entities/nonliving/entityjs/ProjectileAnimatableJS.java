package net.liopyu.entityjs.entities.nonliving.entityjs;

import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileAnimatableJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.liolib.core.animatable.instance.AnimatableInstanceCache;
import net.liopyu.liolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ProjectileAnimatableJS extends ThrowableItemProjectile implements IAnimatableJSNL, ItemSupplier {


    public ProjectileAnimatableJSBuilder builder;
    private final AnimatableInstanceCache getAnimatableInstanceCache;

    public ProjectileAnimatableJS(ProjectileAnimatableJSBuilder builder, EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    }

    public ProjectileAnimatableJS(ProjectileAnimatableJSBuilder builder, EntityType<? extends ThrowableItemProjectile> pEntityType, LivingEntity pShooter, Level pLevel) {
        super(pEntityType, pShooter, pLevel);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    }

    @Override
    public BaseEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return getAnimatableInstanceCache;
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }

    @Override
    public ItemStack getItem() {
        return null;
    }

    public String entityName() {
        return this.getType().toString();
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
        if (builder.onRemovedFromWorld != null) {
            EntityJSHelperClass.consumerCallback(builder.onRemovedFromWorld, this, "[EntityJS]: Error in " + entityName() + "builder for field: onRemovedFromWorld.");
        }
        super.onRemovedFromWorld();
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
    public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource damageSource) {
        if (builder != null && builder.onFall != null) {
            final ContextUtils.EEntityFallDamageContext context = new ContextUtils.EEntityFallDamageContext(this, damageMultiplier, distance, damageSource);
            EntityJSHelperClass.consumerCallback(builder.onFall, context, "[EntityJS]: Error in " + entityName() + "builder for field: onLivingFall.");
        }
        return super.causeFallDamage(distance, damageMultiplier, damageSource);
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
        return builder.isAttackable;
    }


    @Override
    public LivingEntity getControllingPassenger() {
        Entity var2 = this.getFirstPassenger();
        LivingEntity var10000;
        if (var2 instanceof LivingEntity entity) {
            var10000 = entity;
        } else {
            var10000 = null;
        }

        return var10000;
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        if (builder != null && builder.canCollideWith != null) {
            final ContextUtils.ECollidingEntityContext context = new ContextUtils.ECollidingEntityContext(this, pEntity);
            Object obj = builder.canCollideWith.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCollideWith from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canCollideWith(pEntity));
        }
        return super.canCollideWith(pEntity);
    }


    @Override
    protected float getBlockJumpFactor() {
        if (builder != null && builder.setBlockJumpFactor == null) return super.getBlockJumpFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setBlockJumpFactor.apply(this), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + entityName() + ". Value: " + builder.setBlockJumpFactor.apply(this) + ". Must be a float. Defaulting to " + super.getBlockJumpFactor());
        return super.getBlockJumpFactor();
    }


    @Override
    protected float getBlockSpeedFactor() {
        if (builder != null && builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.blockSpeedFactor.apply(this), "float");
        if (builder != null && builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        if (obj != null) {
            return (float) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityName() + ". Value: " + builder.blockSpeedFactor.apply(this) + ". Must be a float, defaulting to " + super.getBlockSpeedFactor());
            return super.getBlockSpeedFactor();
        }
    }

    @Override
    public void positionRider(Entity pPassenger) {
        if (builder.positionRider != null) {
            final ContextUtils.PositionRiderContext context = new ContextUtils.PositionRiderContext(this, pPassenger);
            EntityJSHelperClass.consumerCallback(builder.positionRider, context, "[EntityJS]: Error in " + entityName() + "builder for field: positionRider.");
            return;
        }
        super.positionRider(pPassenger);
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity entity) {
        if (builder != null && builder.canAddPassenger == null) {
            return super.canAddPassenger(entity);
        }
        final ContextUtils.EPassengerEntityContext context = new ContextUtils.EPassengerEntityContext(entity, this);
        Object obj = builder.canAddPassenger.apply(context);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.canAddPassenger(entity));
        return super.canAddPassenger(entity);
    }


    @Override
    protected boolean isFlapping() {
        if (builder != null && builder.isFlapping != null) {
            Object obj = builder.isFlapping.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFlapping from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isFlapping());
        }
        return super.isFlapping();
    }


    @Override
    protected boolean repositionEntityAfterLoad() {
        return Objects.requireNonNullElseGet(builder.repositionEntityAfterLoad, super::repositionEntityAfterLoad);
    }

    @Override
    protected float nextStep() {
        if (builder != null && builder.nextStep != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.nextStep.apply(this), "float");
            if (obj != null) {
                return (float) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for nextStep from entity: " + entityName() + ". Value: " + builder.nextStep.apply(this) + ". Must be a float, defaulting to " + super.nextStep());
            }
        }
        return super.nextStep();
    }


    @Override
    protected SoundEvent getSwimSplashSound() {
        if (builder != null && builder.setSwimSplashSound == null) return super.getSwimSplashSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSplashSound));
    }


    @Override
    protected SoundEvent getSwimSound() {
        if (builder != null && builder.setSwimSound == null) return super.getSwimSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSound));

    }


    @Override
    public boolean canFreeze() {
        if (builder != null && builder.canFreeze != null) {
            Object obj = builder.canFreeze.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canFreeze());
        }
        return super.canFreeze();
    }


    @Override
    public boolean isFreezing() {
        if (builder != null && builder.isFreezing != null) {
            Object obj = builder.isFreezing.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFreezing from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isFreezing());
        }
        return super.isFreezing();
    }


    @Override
    public boolean isCurrentlyGlowing() {
        if (builder != null && builder.isCurrentlyGlowing != null && !this.level.isClientSide()) {
            Object obj = builder.isCurrentlyGlowing.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isCurrentlyGlowing());
        }
        return super.isCurrentlyGlowing();
    }


    @Override
    public boolean dampensVibrations() {
        if (builder != null && builder.dampensVibrations != null) {
            Object obj = builder.dampensVibrations.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for dampensVibrations from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.dampensVibrations());
        }
        return super.dampensVibrations();
    }

    @Override
    public boolean showVehicleHealth() {
        if (builder != null && builder.showVehicleHealth != null) {
            Object obj = builder.showVehicleHealth.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for showVehicleHealth from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.showVehicleHealth());
        }
        return super.showVehicleHealth();
    }


    @Override
    public boolean isInvulnerableTo(DamageSource p_20122_) {
        if (builder != null && builder.isInvulnerableTo != null) {
            final ContextUtils.EDamageContext context = new ContextUtils.EDamageContext(this, p_20122_);
            Object obj = builder.isInvulnerableTo.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isInvulnerableTo from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isInvulnerableTo(p_20122_));
        }
        return super.isInvulnerableTo(p_20122_);
    }


    @Override
    public boolean canChangeDimensions() {
        if (builder != null && builder.canChangeDimensions != null) {
            Object obj = builder.canChangeDimensions.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canChangeDimensions());
        }
        return super.canChangeDimensions();
    }


    @Override
    public boolean mayInteract(@NotNull Level p_146843_, @NotNull BlockPos p_146844_) {
        if (builder != null && builder.mayInteract != null) {
            final ContextUtils.EMayInteractContext context = new ContextUtils.EMayInteractContext(p_146843_, p_146844_, this);
            Object obj = builder.mayInteract.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for mayInteract from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.mayInteract(p_146843_, p_146844_));
        }

        return super.mayInteract(p_146843_, p_146844_);
    }


    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        if (builder != null && builder.canTrample != null) {
            final ContextUtils.ECanTrampleContext context = new ContextUtils.ECanTrampleContext(state, pos, fallDistance, this);
            Object obj = builder.canTrample.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTrample(state, pos, fallDistance));
        }

        return super.canTrample(state, pos, fallDistance);
    }


    @Override
    public int getMaxFallDistance() {
        if (builder != null && builder.setMaxFallDistance == null) return super.getMaxFallDistance();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setMaxFallDistance.apply(this), "integer");
        if (obj != null)
            return (int) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityName() + ". Value: " + builder.setMaxFallDistance.apply(this) + ". Must be an integer. Defaulting to " + super.getMaxFallDistance());
        return super.getMaxFallDistance();
    }

    //Projectile overrides

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void push(Entity pEntity) {
        if (builder.onEntityCollision != null) {
            final ContextUtils.CollidingProjectileEntityContext context = new ContextUtils.CollidingProjectileEntityContext(this, pEntity);
            EntityJSHelperClass.consumerCallback(builder.onEntityCollision, context, "[EntityJS]: Error in " + entityName() + "builder for field: onEntityCollision.");
        }
        if (builder.isPushable) {
            super.push(pEntity);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (builder != null && builder.onHitEntity != null) {
            final ContextUtils.ProjectileEntityHitContext context = new ContextUtils.ProjectileEntityHitContext(result, this);
            EntityJSHelperClass.consumerCallback(builder.onHitEntity, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHitEntity.");
            super.onHitEntity(result);
        } else {
            super.onHitEntity(result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {

        if (builder != null && builder.onHitBlock != null) {
            final ContextUtils.ProjectileBlockHitContext context = new ContextUtils.ProjectileBlockHitContext(result, this);
            EntityJSHelperClass.consumerCallback(builder.onHitBlock, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHitBlock.");
            super.onHitBlock(result);
        } else super.onHitBlock(result);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (builder != null && builder.canHitEntity != null) {
            Object obj = builder.canHitEntity.apply(entity);
            if (obj instanceof Boolean b) return super.canHitEntity(entity) && b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid canHitEntity for arrow builder: " + obj + ". Must be a boolean. Defaulting to super method: " + super.canHitEntity(entity));
        }
        return super.canHitEntity(entity);
    }
}