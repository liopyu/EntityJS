package net.liopyu.entityjs.entities.nonliving.vanilla;

import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.TridentJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.overrides.NonLivingEntityOverrides;
import net.liopyu.entityjs.common.util.overrides.OverrideUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;

public class TridentEntityJS extends ThrownTrident implements IAnimatableJSNL {
    public TridentJSBuilder builder;
    private final AnimatableInstanceCache getAnimatableInstanceCache;

    public TridentEntityJS(TridentJSBuilder builder, EntityType<? extends TridentEntityJS> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    }

    public TridentEntityJS(TridentJSBuilder builder, EntityType<? extends TridentEntityJS> pEntityType, LivingEntity pShooter, Level pLevel, ItemStack pItemStack) {
        super(pEntityType, pLevel);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
        this.setPos(pShooter.getX(), pShooter.getEyeY() - 0.10000000149011612, pShooter.getZ());
        this.setOwner(pShooter);
        if (pShooter instanceof Player) {
            this.pickup = AbstractArrow.Pickup.ALLOWED;
        }
        this.tridentItem = pItemStack.copy();
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(pItemStack));
        this.entityData.set(ID_FOIL, pItemStack.hasFoil());
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

    public ItemStack getTridentItem() {
        return tridentItem;
    }

    public void setTridentItem(ItemStack tridentItem) {
        this.tridentItem = tridentItem;
    }

    // Trident Overrides
    @Override
    public boolean isChanneling() {
        if (builder != null && builder.isChanneling != null) {
            Object obj = OverrideUtils.with(() -> EnchantmentHelper.hasChanneling(this.tridentItem), () -> builder.isChanneling.test(this));
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isChanneling from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isChanneling());
        }
        return EnchantmentHelper.hasChanneling(this.tridentItem);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return builder == null ? super.getDefaultHitGroundSoundEvent() : builder.defaultHitGroundSoundEvent;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (builder != null && builder.onHitEntity != null) {
            final ContextUtils.ProjectileEntityHitContext context = new ContextUtils.ProjectileEntityHitContext(result, this);
            EntityJSHelperClass.consumerCallback(builder.onHitEntity, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHitEntity.");
        }
        Entity $$1 = result.getEntity();
        float $$2 = 8.0F;
        if ($$1 instanceof LivingEntity $$3) {
            $$2 += EnchantmentHelper.getDamageBonus(this.tridentItem, $$3.getMobType());
        }

        Entity $$4 = this.getOwner();
        DamageSource $$5 = builder.damageSource != null ? builder.damageSource : this.damageSources().trident(this, (Entity) ($$4 == null ? this : $$4));
        this.dealtDamage = true;
        SoundEvent $$6 = builder == null ? SoundEvents.TRIDENT_HIT : builder.defaultTridentHitSound;
        if ($$1.hurt($$5, $$2)) {
            if ($$1.getType() == EntityType.ENDERMAN) {
                return;
            }

            if ($$1 instanceof LivingEntity) {
                LivingEntity $$7 = (LivingEntity) $$1;
                if ($$4 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects($$7, $$4);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) $$4, $$7);
                }

                this.doPostHurtEffects($$7);
            }
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        float $$8 = 1.0F;
        if (this.level() instanceof ServerLevel && (this.level().isThundering() || builder.alwaysThunder) && this.isChanneling()) {
            BlockPos $$9 = $$1.blockPosition();
            if (this.level().canSeeSky($$9)) {
                LightningBolt $$10 = (LightningBolt) EntityType.LIGHTNING_BOLT.create(this.level());
                $$10.moveTo(Vec3.atBottomCenterOf($$9));
                $$10.setCause($$4 instanceof ServerPlayer ? (ServerPlayer) $$4 : null);
                this.level().addFreshEntity($$10);
                $$6 = builder == null ? SoundEvents.TRIDENT_HIT : builder.thunderHitSound;
                $$8 = builder == null ? 5.0F : builder.thunderHitVolume;
            }
        }
        this.playSound($$6, $$8, 1.0F);
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
        NonLivingEntityOverrides.playerTouch(this, builder, player, () -> super.playerTouch(player));
    }

    @Override
    public void onRemovedFromWorld() {
        NonLivingEntityOverrides.onRemovedFromWorld(this, builder, super::onRemovedFromWorld);
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
        if (builder != null && builder.onAddedToWorld != null) {
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
        if (builder.shouldRenderAtSqrDistance != null) {
            final ContextUtils.EntitySqrDistanceContext context = new ContextUtils.EntitySqrDistanceContext(distance, this);
            Object obj = OverrideUtils.with(() -> super.shouldRenderAtSqrDistance(distance), () -> builder.shouldRenderAtSqrDistance.test(context));
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid shouldRenderAtSqrDistance for builder: " + obj + ". Must be a boolean. Defaulting to super method: " + super.shouldRenderAtSqrDistance(distance));
        }
        return super.shouldRenderAtSqrDistance(distance);
    }

    @Override
    public boolean isAttackable() {
        return builder.isAttackable != null ? builder.isAttackable : super.isAttackable();
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
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (builder != null && builder.onHitBlock != null) {
            final ContextUtils.ProjectileBlockHitContext context = new ContextUtils.ProjectileBlockHitContext(result, this);
            EntityJSHelperClass.consumerCallback(builder.onHitBlock, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHitBlock.");
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (builder != null && builder.canHitEntity != null) {
            Object obj = OverrideUtils.with(() -> super.canHitEntity(entity), () -> builder.canHitEntity.test(entity));
            if (obj instanceof Boolean b) return super.canHitEntity(entity) && b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid canHitEntity for arrow builder: " + obj + ". Must be a boolean. Defaulting to super method: " + super.canHitEntity(entity));
        }
        return super.canHitEntity(entity);
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
    protected float getBlockSpeedFactor() {
        if (builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.blockSpeedFactor.apply(this), "float");
        if (builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
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
        Object obj = OverrideUtils.with(() -> super.canAddPassenger(entity), () -> builder.canAddPassenger.test(context));
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.canAddPassenger(entity));
        return super.canAddPassenger(entity);
    }


    @Override
    protected boolean isFlapping() {
        if (builder.isFlapping != null) {
            Object obj = OverrideUtils.with(super::isFlapping, () -> builder.isFlapping.test(this));
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
            Object obj = OverrideUtils.with(() -> super.isInvulnerableTo(p_20122_), () -> builder.isInvulnerableTo.test(context));
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
            Object obj = OverrideUtils.with(super::canChangeDimensions, () -> builder.canChangeDimensions.test(this));
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
            Object obj = OverrideUtils.with(() -> super.mayInteract(p_146843_, p_146844_), () -> builder.mayInteract.test(context));
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
            Object obj = OverrideUtils.with(() -> super.canTrample(state, pos, fallDistance), () -> builder.canTrample.test(context));
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTrample(state, pos, fallDistance));
        }

        return super.canTrample(state, pos, fallDistance);
    }

    @Override
    public boolean canBeCollidedWith() {
        if (builder.canBeCollidedWith == null) {
            return super.canBeCollidedWith();
        }
        Object obj = OverrideUtils.with(super::canBeCollidedWith, () -> builder.canBeCollidedWith.test(this));
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeCollidedWith from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canBeCollidedWith());
        return super.canBeCollidedWith();
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


}