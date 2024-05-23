package net.liopyu.entityjs.entities.nonliving.entityjs;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ArrowEntityJS extends AbstractArrow implements IArrowEntityJS {

    public final ArrowEntityJSBuilder builder;
    @NotNull
    protected ItemStack pickUpStack;
    private double baseDamage;
    private int knockback;
    @Nullable
    private IntOpenHashSet piercingIgnoreEntityIds;
    @Nullable
    private List<Entity> piercedAndKilledEntities;
    private EntityJSHelperClass.EntityMovementTracker movementTracker;

    private boolean isMoving;

    public ArrowEntityJS(ArrowEntityJSBuilder builder, EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
        this.baseDamage = builder.setBaseDamage;
        this.movementTracker = new EntityJSHelperClass.EntityMovementTracker();
    }

    public ArrowEntityJS(Level level, LivingEntity shooter, ArrowEntityJSBuilder builder) {
        super(builder.get(), shooter, level);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
        this.baseDamage = builder.setBaseDamage;
        this.movementTracker = new EntityJSHelperClass.EntityMovementTracker();
    }

    @Override
    public ArrowEntityBuilder<?> getArrowBuilder() {
        return builder;
    }

    @Override
    public void setPickUpItem(ItemStack stack) {
        pickUpStack = stack;
    }

    @Override
    protected ItemStack getPickupItem() {
        return pickUpStack;
    }

    public String entityName() {
        return this.getType().toString();
    }

    //Arrow Overrides
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
    protected void tickDespawn() {
        if (builder.tickDespawn != null) {
            EntityJSHelperClass.consumerCallback(builder.tickDespawn, this, "[EntityJS]: Error in " + entityName() + "builder for field: tickDespawn.");

        } else super.tickDespawn();
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        if (builder.doPostHurtEffects != null) {
            final ContextUtils.ArrowLivingEntityContext context = new ContextUtils.ArrowLivingEntityContext(this, target);
            EntityJSHelperClass.consumerCallback(builder.doPostHurtEffects, context, "[EntityJS]: Error in " + entityName() + "builder for field: doPostHurtEffects.");

        } else {
            super.doPostHurtEffects(target);
        }
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        if (builder != null && builder.defaultHitGroundSoundEvent != null) {
            return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.defaultHitGroundSoundEvent));
        }
        return super.getDefaultHitGroundSoundEvent();
    }


    @Override
    protected float getWaterInertia() {
        return builder.setWaterInertia != null ? builder.setWaterInertia : super.getWaterInertia();
    }


    @Override
    protected boolean tryPickup(Player player) {
        if (builder.tryPickup == null) return super.tryPickup(player);
        final ContextUtils.ArrowPlayerContext context = new ContextUtils.ArrowPlayerContext(player, this);
        Object obj = builder.tryPickup.apply(context);
        if (obj instanceof Boolean b) return b;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for tryPickup from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.tryPickup(player));
        return super.tryPickup(player);
    }

    private void resetPiercedEntities() {
        if (this.piercedAndKilledEntities != null) {
            this.piercedAndKilledEntities.clear();
        }

        if (this.piercingIgnoreEntityIds != null) {
            this.piercingIgnoreEntityIds.clear();
        }

    }

    public double setDamageFunction() {
        if (builder.setDamageFunction != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setDamageFunction.apply(this), "double");
            if (obj != null) return (double) obj;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid setDamageFunction for arrow builder: " + builder.setDamageFunction.apply(this) + ". Must be a double. Defaulting to super method: " + 0);
            return 0;
        }
        return 0;
    }

    public void setBaseDamage(double pBaseDamage) {
        this.baseDamage = pBaseDamage + builder.setBaseDamage + setDamageFunction();
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }


    public void setKnockback(int pKnockback) {
        if (builder.setKnockback != null) this.knockback = builder.setKnockback + pKnockback;
        else this.knockback = pKnockback;
    }

    public int getKnockback() {
        return this.knockback;
    }

    @Override
    public void setEnchantmentEffectsFromEntity(LivingEntity pShooter, float pVelocity) {
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, pShooter);
        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, pShooter);
        this.setBaseDamage((double) (pVelocity * 2.0F) + this.random.triangle((double) this.level().getDifficulty().getId() * 0.11, 0.57425));
        if (i > 0) {
            this.setBaseDamage(this.getBaseDamage() + (double) i * 0.5 + 0.5);
        }

        if (j > 0) {
            this.setKnockback(j);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, pShooter) > 0) {
            this.setSecondsOnFire(100);
        }

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
        super.tick();
        isMoving = movementTracker.isMoving(this);
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
        if (builder.onRemovedFromWorld != null) {
            EntityJSHelperClass.consumerCallback(builder.onRemovedFromWorld, this, "[EntityJS]: Error in " + entityName() + "builder for field: onRemovedFromWorld.");
        }
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
        super.onAddedToWorld();
        if (builder.onAddedToWorld != null && !this.level().isClientSide()) {
            EntityJSHelperClass.consumerCallback(builder.onAddedToWorld, this, "[EntityJS]: Error in " + entityName() + "builder for field: onAddedToWorld.");
        }
    }

    @Override
    public void setSprinting(boolean sprinting) {
        if (builder.onSprint != null) {
            EntityJSHelperClass.consumerCallback(builder.onSprint, this, "[EntityJS]: Error in " + entityName() + "builder for field: onSprint.");
        }
        super.setSprinting(sprinting);
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
        super.rideTick();
        if (builder.rideTick != null) {
            EntityJSHelperClass.consumerCallback(builder.rideTick, this, "[EntityJS]: Error in " + entityName() + "builder for field: rideTick.");
        }
    }

    @Override
    public void onClientRemoval() {
        if (builder.onClientRemoval != null) {
            EntityJSHelperClass.consumerCallback(builder.onClientRemoval, this, "[EntityJS]: Error in " + entityName() + "builder for field: onClientRemoval.");
        }
        super.onClientRemoval();
    }


    @Override
    public void lavaHurt() {
        if (builder.lavaHurt != null) {
            EntityJSHelperClass.consumerCallback(builder.lavaHurt, this, "[EntityJS]: Error in " + entityName() + "builder for field: lavaHurt.");
        }
        super.lavaHurt();
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
            Object obj = builder.shouldRenderAtSqrDistance.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid shouldRenderAtSqrDistance for arrow builder: " + obj + ". Must be a boolean. Defaulting to super method: " + super.shouldRenderAtSqrDistance(distance));
        }
        return super.shouldRenderAtSqrDistance(distance);
    }


    public boolean isMoving() {
        return isMoving;
    }

    @Override
    public boolean isAttackable() {
        return builder.isAttackable != null ? builder.isAttackable : super.isAttackable();
    }

    //Projectile Overrides

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        float f = (float) this.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double) f * this.baseDamage, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long j = (long) this.random.nextInt(i / 2 + 2);
            i = (int) Math.min(j + (long) i, 2147483647L);
        }

        Entity entity1 = this.getOwner();
        DamageSource damagesource;
        if (entity1 == null) {
            damagesource = this.damageSources().arrow(this, this);
        } else {
            damagesource = this.damageSources().arrow(this, entity1);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity) entity1).setLastHurtMob(entity);
            }
        }

        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && !flag) {
            entity.setSecondsOnFire(5);
        }

        if (entity.hurt(damagesource, (float) i)) {
            if (flag) {
                return;
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity) entity;
                if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    double d0 = Math.max(0.0, 1.0 - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double) this.knockback * 0.6 * d0);
                    if (vec3.lengthSqr() > 0.0) {
                        livingentity.push(vec3.x, 0.1, vec3.z);
                    }
                }

                if (!this.level().isClientSide && entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) entity1, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (entity1 != null && livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer) entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level().isClientSide && entity1 instanceof ServerPlayer) {
                    ServerPlayer serverplayer = (ServerPlayer) entity1;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, Arrays.asList(entity));
                    }
                }
            }

            this.playSound(this.getDefaultHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
            if (!this.level().isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            }
        }
        if (builder != null && builder.onHitEntity != null) {
            final ContextUtils.ArrowEntityHitContext context = new ContextUtils.ArrowEntityHitContext(pResult, this);
            EntityJSHelperClass.consumerCallback(builder.onHitEntity, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHitEntity.");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putDouble("damage", this.baseDamage);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("damage", 99)) {
            this.baseDamage = pCompound.getDouble("damage");
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.setSoundEvent(this.getDefaultHitGroundSoundEvent());
        this.resetPiercedEntities();
        if (builder != null && builder.onHitBlock != null) {
            final ContextUtils.ArrowBlockHitContext context = new ContextUtils.ArrowBlockHitContext(result, this);
            EntityJSHelperClass.consumerCallback(builder.onHitBlock, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHitBlock.");
        }
        super.onHitBlock(result);
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
        if (builder.canCollideWith != null) {
            final ContextUtils.ECollidingEntityContext context = new ContextUtils.ECollidingEntityContext(this, pEntity);
            Object obj = builder.canCollideWith.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCollideWith from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canCollideWith(pEntity));
        }
        return super.canCollideWith(pEntity);
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
        Object obj = builder.canAddPassenger.apply(context);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.canAddPassenger(entity));
        return super.canAddPassenger(entity);
    }


    @Override
    protected boolean isFlapping() {
        if (builder.isFlapping != null) {
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
        if (builder.canFreeze != null) {
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
        if (builder.isFreezing != null) {
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
        if (builder.isCurrentlyGlowing != null && !this.level().isClientSide()) {
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
        if (builder.dampensVibrations != null) {
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
        if (builder.showVehicleHealth != null) {
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
        if (builder.isInvulnerableTo != null) {
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
        if (builder.canChangeDimensions != null) {
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
        if (builder.mayInteract != null) {
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
        if (builder.canTrample != null) {
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
        if (builder.setMaxFallDistance == null) return super.getMaxFallDistance();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setMaxFallDistance.apply(this), "integer");
        if (obj != null)
            return (int) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityName() + ". Value: " + builder.setMaxFallDistance.apply(this) + ". Must be an integer. Defaulting to " + super.getMaxFallDistance());
        return super.getMaxFallDistance();
    }


}
