package net.liopyu.entityjs.entities.nonliving.entityjs;

import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.overrides.NonLivingEntityOverrides;
import net.liopyu.entityjs.common.util.overrides.OverrideUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;

public class BaseEntityJS extends Entity implements IAnimatableJSNL {

    protected final BaseEntityJSBuilder builder;
    private final AnimatableInstanceCache getAnimatableInstanceCache;

    public BaseEntityJS(BaseEntityJSBuilder builder, EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

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

    /*@Override
    public boolean isPickable() {
        return builder.isPickable;
    }*/

    //Base Entity Overrides
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (builder.onHurt != null) {
            final ContextUtils.EntityHurtContext context = new ContextUtils.EntityHurtContext(this, pSource, pAmount);
            EntityJSHelperClass.consumerCallback(builder.onHurt, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHurt.");

        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        if (builder.lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, this);
            EntityJSHelperClass.consumerCallback(builder.lerpTo, context, "[EntityJS]: Error in " + entityName() + "builder for field: lerpTo.");
        }
    }

    @Override
    public void tick() {
        NonLivingEntityOverrides.tick(this, builder, super::tick);
    }

    @Override
    public void move(MoverType pType, Vec3 pPos) {
        super.move(pType, pPos);
        if (builder.move != null) {
            final ContextUtils.MovementContext context = new ContextUtils.MovementContext(pType, pPos, this);
            EntityJSHelperClass.consumerCallback(builder.move, context, "[EntityJS]: Error in " + entityName() + "builder for field: move.");
        }
    }

    @Override
    public void playerTouch(Player player) {
        NonLivingEntityOverrides.playerTouch(this, builder, player, () -> super.playerTouch(player));
    }

    @Override
    public void onRemovedFromWorld() {
        NonLivingEntityOverrides.onRemovedFromWorld(this, builder, super::onRemovedFromWorld);
    }

    @Override
    public void thunderHit(ServerLevel p_19927_, LightningBolt p_19928_) {
        if (builder.thunderHit != null) {
            super.thunderHit(p_19927_, p_19928_);
            final ContextUtils.EThunderHitContext context = new ContextUtils.EThunderHitContext(p_19927_, p_19928_, this);
            EntityJSHelperClass.consumerCallback(builder.thunderHit, context, "[EntityJS]: Error in " + entityName() + "builder for field: thunderHit.");
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource damageSource) {
        if (builder.onFall != null) {
            final ContextUtils.EEntityFallDamageContext context = new ContextUtils.EEntityFallDamageContext(this, damageMultiplier, distance, damageSource);
            EntityJSHelperClass.consumerCallback(builder.onFall, context, "[EntityJS]: Error in " + entityName() + "builder for field: onLivingFall.");
        }
        return super.causeFallDamage(distance, damageMultiplier, damageSource);
    }

    @Override
    public void onAddedToWorld() {
        NonLivingEntityOverrides.onAddedToWorld(this, builder, super::onAddedToWorld);
    }

    @Override
    public void setSprinting(boolean sprinting) {
        NonLivingEntityOverrides.setSprinting(this, builder, () -> super.setSprinting(sprinting));
    }


    @Override
    public void stopRiding() {
        super.stopRiding();
        if (builder.onStopRiding != null) {
            EntityJSHelperClass.consumerCallback(builder.onStopRiding, this, "[EntityJS]: Error in " + entityName() + "builder for field: onStopRiding.");
        }
    }


    @Override
    public void rideTick() {
        NonLivingEntityOverrides.rideTick(this, builder, super::rideTick);
    }

    @Override
    public void onClientRemoval() {
        NonLivingEntityOverrides.onClientRemoval(this, builder, super::onClientRemoval);
    }


    @Override
    public void lavaHurt() {
        NonLivingEntityOverrides.lavaHurt(this, builder, super::lavaHurt);
    }


    @Override
    protected void onFlap() {
        if (builder.onFlap != null) {
            EntityJSHelperClass.consumerCallback(builder.onFlap, this, "[EntityJS]: Error in " + entityName() + "builder for field: onFlap.");
        }
        super.onFlap();
    }


    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        if (builder.shouldRenderAtSqrDistance != null) {
            final ContextUtils.EntitySqrDistanceContext context = new ContextUtils.EntitySqrDistanceContext(distance, this);
            Object obj = OverrideUtils.with(() -> super.shouldRenderAtSqrDistance(distance), () -> builder.shouldRenderAtSqrDistance.apply(context));
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid shouldRenderAtSqrDistance for builder: " + obj + ". Must be a boolean. Defaulting to super method: " + super.shouldRenderAtSqrDistance(distance));
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


    /*@Info(value = """
            Calls a triggerable animation to be played anywhere.
            """)
    public void triggerAnimation(String controllerName, String animName) {
        triggerAnim(controllerName, animName);
    }*/

    @Override
    public boolean canCollideWith(Entity pEntity) {
        return NonLivingEntityOverrides.canCollideWith(this, builder, pEntity, () -> super.canCollideWith(pEntity));
    }


    @Override
    protected float getBlockJumpFactor() {
        if (builder.setBlockJumpFactor == null) return super.getBlockJumpFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setBlockJumpFactor.apply(this), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + entityName() + ". Value: " + builder.setBlockJumpFactor.apply(this) + ". Must be a float. Defaulting to " + super.getBlockJumpFactor());
        return super.getBlockJumpFactor();
    }

    @Override
    protected void playCombinationStepSounds(BlockState primaryStepSound, BlockState secondaryStepSound, BlockPos primaryPos, BlockPos secondaryPos) {
        if (builder.playCombinationStepSounds != null) {
            final ContextUtils.PlayCombinationStepSoundsContext context = new ContextUtils.PlayCombinationStepSoundsContext(this, primaryStepSound, secondaryStepSound, primaryPos, secondaryPos);
            EntityJSHelperClass.consumerCallback(builder.playCombinationStepSounds, context, "[EntityJS]: Error in " + entityName() + "builder for field: playCombinationStepSounds.");
            return;
        }
        super.playCombinationStepSounds(primaryStepSound, secondaryStepSound, primaryPos, secondaryPos);
    }

    @Override
    protected void playMuffledStepSound(BlockState blockState, BlockPos pos) {
        if (builder.playMuffledStepSound != null) {
            final ContextUtils.PlayMuffledStepSoundContext context = new ContextUtils.PlayMuffledStepSoundContext(this, blockState, pos);
            EntityJSHelperClass.consumerCallback(builder.playMuffledStepSound, context, "[EntityJS]: Error in " + entityName() + "builder for field: playMuffledStepSound.");
            return;
        }
        super.playMuffledStepSound(blockState, pos);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockState) {
        if (builder.playStepSound != null) {
            final ContextUtils.PlayStepSoundContext context = new ContextUtils.PlayStepSoundContext(this, pos, blockState);
            EntityJSHelperClass.consumerCallback(builder.playStepSound, context, "[EntityJS]: Error in " + entityName() + "builder for field: playStepSound.");
            return;
        }
        super.playStepSound(pos, blockState);
    }

    @Override
    public boolean isPushable() {
        return NonLivingEntityOverrides.isPushable(builder);
    }

    @Override
    protected float getBlockSpeedFactor() {
        if (builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.blockSpeedFactor.apply(this), "float");
        if (obj != null) {
            return (float) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityName() + ". Value: " + builder.blockSpeedFactor.apply(this) + ". Must be a float, defaulting to " + super.getBlockSpeedFactor());
            return super.getBlockSpeedFactor();
        }
    }

    @Override
    protected void positionRider(Entity pPassenger, MoveFunction pCallback) {
        if (builder.positionRider != null) {
            final ContextUtils.PositionRiderContext context = new ContextUtils.PositionRiderContext(this, pPassenger, pCallback);
            EntityJSHelperClass.consumerCallback(builder.positionRider, context, "[EntityJS]: Error in " + entityName() + "builder for field: positionRider.");
            return;
        }
        super.positionRider(pPassenger, pCallback);
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity entity) {
        if (builder.canAddPassenger == null) {
            return super.canAddPassenger(entity);
        }
        final ContextUtils.EPassengerEntityContext context = new ContextUtils.EPassengerEntityContext(entity, this);
        Object obj = OverrideUtils.with(() -> super.canAddPassenger(entity), () -> builder.canAddPassenger.apply(context));
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.canAddPassenger(entity));
        return super.canAddPassenger(entity);
    }


    @Override
    protected boolean isFlapping() {
        if (builder.isFlapping != null) {
            Object obj = OverrideUtils.with(super::isFlapping, () -> builder.isFlapping.apply(this));
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
        if (builder.nextStep != null) {
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
        if (builder.setSwimSplashSound == null) return super.getSwimSplashSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSplashSound));
    }


    @Override
    protected SoundEvent getSwimSound() {
        if (builder.setSwimSound == null) return super.getSwimSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSound));

    }


    @Override
    public boolean canFreeze() {
        return NonLivingEntityOverrides.canFreeze(this, builder, super::canFreeze);
    }


    @Override
    public boolean isFreezing() {
        return NonLivingEntityOverrides.isFreezing(this, builder, super::isFreezing);
    }


    @Override
    public boolean isCurrentlyGlowing() {
        return NonLivingEntityOverrides.isCurrentlyGlowing(this, builder, super::isCurrentlyGlowing);
    }


    @Override
    public boolean dampensVibrations() {
        return NonLivingEntityOverrides.dampensVibrations(this, builder, super::dampensVibrations);
    }

    @Override
    public boolean showVehicleHealth() {
        return NonLivingEntityOverrides.showVehicleHealth(this, builder, super::showVehicleHealth);
    }


    @Override
    public boolean isInvulnerableTo(DamageSource p_20122_) {
        if (builder.isInvulnerableTo != null) {
            final ContextUtils.EDamageContext context = new ContextUtils.EDamageContext(this, p_20122_);
            Object obj = OverrideUtils.with(() -> super.isInvulnerableTo(p_20122_), () -> builder.isInvulnerableTo.apply(context));
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isInvulnerableTo from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isInvulnerableTo(p_20122_));
        }
        return super.isInvulnerableTo(p_20122_);
    }


    @Override
    public boolean canChangeDimensions() {
        if (builder.canChangeDimensions != null) {
            Object obj = OverrideUtils.with(super::canChangeDimensions, () -> builder.canChangeDimensions.apply(this));
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canChangeDimensions());
        }
        return super.canChangeDimensions();
    }


    @Override
    public boolean mayInteract(@NotNull Level p_146843_, @NotNull BlockPos p_146844_) {
        if (builder.mayInteract != null) {
            final ContextUtils.EMayInteractContext context = new ContextUtils.EMayInteractContext(p_146843_, p_146844_, this);
            Object obj = OverrideUtils.with(() -> super.mayInteract(p_146843_, p_146844_), () -> builder.mayInteract.apply(context));
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for mayInteract from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.mayInteract(p_146843_, p_146844_));
        }

        return super.mayInteract(p_146843_, p_146844_);
    }


    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        if (builder.canTrample != null) {
            final ContextUtils.ECanTrampleContext context = new ContextUtils.ECanTrampleContext(state, pos, fallDistance, this);
            Object obj = OverrideUtils.with(() -> super.canTrample(state, pos, fallDistance), () -> builder.canTrample.apply(context));
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTrample(state, pos, fallDistance));
        }

        return super.canTrample(state, pos, fallDistance);
    }


    @Override
    public int getMaxFallDistance() {
        if (builder.setMaxFallDistance == null) return super.getMaxFallDistance();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setMaxFallDistance.apply(this), "integer");
        if (obj != null)
            return (int) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityName() + ". Value: " + builder.setMaxFallDistance.apply(this) + ". Must be an integer. Defaulting to " + super.getMaxFallDistance());
        return super.getMaxFallDistance();
    }

    @Override
    public boolean canBeCollidedWith() {
        if (builder.canBeCollidedWith == null) {
            return super.canBeCollidedWith();
        }
        Object obj = OverrideUtils.with(super::canBeCollidedWith, () -> builder.canBeCollidedWith.apply(this));
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeCollidedWith from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canBeCollidedWith());
        return super.canBeCollidedWith();
    }

}