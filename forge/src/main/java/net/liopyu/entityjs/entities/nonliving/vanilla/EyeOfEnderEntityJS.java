package net.liopyu.entityjs.entities.nonliving.vanilla;

import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IProjectileEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.overrides.NonLivingEntityOverrides;
import net.liopyu.entityjs.common.util.overrides.OverrideUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EyeOfEnderEntityJS extends EyeOfEnder implements IProjectileEntityJS {
    protected final EyeOfEnderJSBuilder builder;

    public EyeOfEnderEntityJS(EyeOfEnderJSBuilder builder, EntityType<? extends EyeOfEnder> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    public EyeOfEnderEntityJS(EyeOfEnderJSBuilder builder, Level pLevel, EntityType<? extends EyeOfEnder> pEntityType, double pX, double pY, double pZ) {
        super(pEntityType, pLevel);
        this.builder = builder;
        this.setPos(pX, pY, pZ);
    }

    public void signalTo(BlockPos pPos) {
        double $$1 = (double) pPos.getX();
        int $$2 = pPos.getY();
        double $$3 = (double) pPos.getZ();
        double $$4 = $$1 - this.getX();
        double $$5 = $$3 - this.getZ();
        double $$6 = Math.sqrt($$4 * $$4 + $$5 * $$5);
        if ($$6 > 12.0) {
            this.tx = this.getX() + $$4 / $$6 * 12.0;
            this.tz = this.getZ() + $$5 / $$6 * 12.0;
            this.ty = this.getY() + 8.0;
        } else {
            this.tx = $$1;
            this.ty = (double) $$2;
            this.tz = $$3;
        }
        this.life = 0;
        this.surviveAfterDeath = builder.survivalChance != null ?
                this.random.nextFloat() < builder.survivalChance : this.random.nextInt(5) > 0;
    }

    @Override
    public ItemStack getItem() {
        if (builder.getItem != null) {
            Object obj = OverrideUtils.with(super::getItem, () -> builder.getItem.apply(this));
            if (obj instanceof ItemStack i) return i;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for getItem in builder: " + obj + ". Must be an ItemStack. Defaulting to super method: " + super.getItem());
        }
        return super.getItem();
    }

    public String entityName() {
        return this.getType().toString();
    }


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
        super.baseTick();
        Vec3 $$0 = this.getDeltaMovement();
        double $$1 = this.getX() + $$0.x;
        double $$2 = this.getY() + $$0.y;
        double $$3 = this.getZ() + $$0.z;
        double $$4 = $$0.horizontalDistance();
        this.setXRot(Projectile.lerpRotation(this.xRotO, (float) (Mth.atan2($$0.y, $$4) * 57.2957763671875)));
        this.setYRot(Projectile.lerpRotation(this.yRotO, (float) (Mth.atan2($$0.x, $$0.z) * 57.2957763671875)));
        if (!this.level().isClientSide) {
            double $$5 = this.tx - $$1;
            double $$6 = this.tz - $$3;
            float $$7 = (float) Math.sqrt($$5 * $$5 + $$6 * $$6);
            float $$8 = (float) Mth.atan2($$6, $$5);
            double $$9 = Mth.lerp(0.0025, $$4, (double) $$7);
            double $$10 = $$0.y;
            if ($$7 < 1.0F) {
                $$9 *= 0.8;
                $$10 *= 0.8;
            }

            int $$11 = this.getY() < this.ty ? 1 : -1;
            $$0 = new Vec3(Math.cos((double) $$8) * $$9, $$10 + ((double) $$11 - $$10) * 0.014999999664723873, Math.sin((double) $$8) * $$9);
            this.setDeltaMovement($$0);
        }

        float $$12 = 0.25F;
        if (!builder.disableTrailParticles) {
            if (this.isInWater()) {
                for (int $$13 = 0; $$13 < 4; ++$$13) {
                    this.level().addParticle(ParticleTypes.BUBBLE, $$1 - $$0.x * 0.25, $$2 - $$0.y * 0.25, $$3 - $$0.z * 0.25, $$0.x, $$0.y, $$0.z);
                }
            } else {
                this.level().addParticle(ParticleTypes.PORTAL, $$1 - $$0.x * 0.25 + this.random.nextDouble() * 0.6 - 0.3, $$2 - $$0.y * 0.25 - 0.5, $$3 - $$0.z * 0.25 + this.random.nextDouble() * 0.6 - 0.3, $$0.x, $$0.y, $$0.z);
            }
        }
        if (!this.level().isClientSide) {
            this.setPos($$1, $$2, $$3);
            ++this.life;
            if (this.life > 80 && !this.level().isClientSide) {
                if (!builder.disableDefaultDeathLogic) {
                    this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
                    this.discard();
                    if (this.surviveAfterDeath) {
                        this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
                    } else {
                        this.level().levelEvent(2003, this.blockPosition(), 0);
                    }
                } else {
                    if (this.surviveAfterDeath) {
                        this.level().addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.getItem()));
                    }
                    this.discard();
                }
            }
        } else {
            this.setPosRaw($$1, $$2, $$3);
        }
        if (builder.tick != null) {
            EntityJSHelperClass.consumerCallback(builder.tick, this, "[EntityJS]: Error in " + entityName() + "builder for field: tick.");
        }
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

    /*public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        if (pPlayer.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else {
            if (!this.level().isClientSide) {
                return pPlayer.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
            } else {
                return InteractionResult.SUCCESS;
            }
        }
    }*/

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
    public BaseNonAnimatableEntityBuilder<?> getProjectileBuilder() {
        return builder;
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
