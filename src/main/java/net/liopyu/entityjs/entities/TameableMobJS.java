package net.liopyu.entityjs.entities;

import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.TameableMobJSBuilder;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.events.BuildBrainEventJS;
import net.liopyu.entityjs.events.BuildBrainProviderEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.ModKeybinds;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TameableMobJS extends TamableAnimal implements IAnimatableJS, RangedAttackMob, OwnableEntity, NeutralMob {

    private final AnimatableInstanceCache getAnimatableInstanceCache;

    protected final TameableMobJSBuilder builder;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    public String entityName() {
        return this.getType().toString();
    }

    private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID;
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME;
    private static final UniformInt PERSISTENT_ANGER_TIME;
    @javax.annotation.Nullable
    private UUID persistentAngerTarget;

    public TameableMobJS(TameableMobJSBuilder builder, EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        this.setTame(false);
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);

    }

    static {
        DATA_INTERESTED_ID = SynchedEntityData.defineId(TameableMobJS.class, EntityDataSerializers.BOOLEAN);
        DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(TameableMobJS.class, EntityDataSerializers.INT);
        PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    }

    @Override
    public BaseLivingEntityBuilder<?> getBuilder() {
        return builder;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return getAnimatableInstanceCache;
    }


    //Some logic overrides up here because there are different implementations in the other builders.


    @Override
    protected Brain.Provider<?> brainProvider() {
        if (EventHandlers.buildBrainProvider.hasListeners()) {
            final BuildBrainProviderEventJS event = new BuildBrainProviderEventJS();
            EventHandlers.buildBrainProvider.post(event, getTypeId());
            return event.provide();
        } else {
            return super.brainProvider();
        }
    }

    @Override
    protected Brain<AnimalEntityJS> makeBrain(Dynamic<?> p_21069_) {
        if (EventHandlers.buildBrain.hasListeners()) {
            final Brain<AnimalEntityJS> brain = UtilsJS.cast(brainProvider().makeBrain(p_21069_));
            EventHandlers.buildBrain.post(new BuildBrainEventJS<>(brain), getTypeId());
            return brain;
        } else {
            return UtilsJS.cast(super.makeBrain(p_21069_));
        }
    }

    @Override
    protected void registerGoals() {
        if (EventHandlers.addGoalTargets.hasListeners()) {
            EventHandlers.addGoalTargets.post(new AddGoalTargetsEventJS<>(this, targetSelector), getTypeId());
        }
        if (EventHandlers.addGoalSelectors.hasListeners()) {
            EventHandlers.addGoalSelectors.post(new AddGoalSelectorsEventJS<>(this, goalSelector), getTypeId());
        }
    }

    //Tameable Mob Overrides
    public boolean tamableFood(ItemStack pStack) {
        if (builder.tamableFood != null) {
            return builder.tamableFood.test(pStack);
        }
        return false;
    }

    public boolean tamableFoodPredicate(ItemStack pStack) {
        if (builder.tamableFoodPredicate == null) return false;
        final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(pStack, this);
        Object obj = builder.tamableFoodPredicate.apply(context);
        if (obj instanceof Boolean b) {
            return b;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for tamableFoodPredicate from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to false.");
        return false;
    }

    @Override
    public void tame(Player pPlayer) {
        if (builder.tameOverride != null) {
            this.setTame(true);
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(pPlayer, this);
            builder.tameOverride.accept(context);
            if (pPlayer instanceof ServerPlayer) {
                CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer) pPlayer, this);
            }
        } else super.tame(pPlayer);
        if (builder.onTamed != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(pPlayer, this);
            builder.onTamed.accept(context);
        }
    }

    // Basic Tameable Overrides
    @Override
    public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
        if (!(pTarget instanceof Creeper) && !(pTarget instanceof Ghast)) {
            if (pTarget instanceof TameableMobJS mobjs) {
                return !mobjs.isTame() || mobjs.getOwner() != pOwner;
            } else if (pTarget instanceof Player && pOwner instanceof Player && !((Player) pOwner).canHarmPlayer((Player) pTarget)) {
                return false;
            } else if (pTarget instanceof AbstractHorse && ((AbstractHorse) pTarget).isTamed()) {
                return false;
            } else {
                return !(pTarget instanceof TamableAnimal) || !((TamableAnimal) pTarget).isTame();
            }
        } else {
            return false;
        }
    }

    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        if (builder.setBreedOffspring != null) {
            final ContextUtils.BreedableEntityContext context = new ContextUtils.BreedableEntityContext(this, ageableMob, serverLevel);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setBreedOffspring.apply(context), "resourcelocation");
            if (obj instanceof ResourceLocation resourceLocation) {
                EntityType<?> breedOffspringType = ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
                if (breedOffspringType != null) {
                    Object breedOffspringEntity = breedOffspringType.create(serverLevel);
                    if (breedOffspringEntity instanceof TamableAnimal animal) {
                        UUID uuid = this.getOwnerUUID();
                        if (uuid != null) {
                            animal.setOwnerUUID(uuid);
                            animal.setTame(true);
                        }
                        return (AgeableMob) breedOffspringEntity;
                    } else if (breedOffspringEntity instanceof AgeableMob) {
                        return (AgeableMob) breedOffspringEntity;
                    }
                }
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid resource location or Entity Type for breedOffspring: " + builder.setBreedOffspring.apply(context) + ". Must return a TamableAnimal/AgableMob ResourceLocation. Defaulting to super method: " + builder.get());
            }
        } else return builder.get().create(serverLevel);
        return null;
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        boolean flag = pEntity.hurt(this.damageSources().mobAttack(this), (float) ((int) this.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        if (flag) {
            this.doEnchantDamageEffects(this, pEntity);
        }
        return flag;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false);
            }
            return super.hurt(pSource, pAmount);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.addPersistentAngerSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.readPersistentAngerSaveData(this.level(), pCompound);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_INTERESTED_ID, false);
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    protected void spawnTamingParticles(boolean pTamed) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!pTamed) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleoptions, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d0, d1, d2);
        }
    }


    //NeutralMob Overrides
    public int getRemainingPersistentAngerTime() {
        return (Integer) this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    public void setRemainingPersistentAngerTime(int pTime) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, pTime);
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void setPersistentAngerTarget(@javax.annotation.Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }
    //Ageable Mob Overrides

    @Override
    public boolean isFood(ItemStack pStack) {
        if (builder.isFood != null) {
            return builder.isFood.test(pStack);
        }
        return super.isFood(pStack);
    }

    public boolean isFoodPredicate(ItemStack pStack) {
        if (builder.isFoodPredicate == null) {
            return super.isFood(pStack);
        }
        final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(pStack, this);
        Object obj = builder.isFoodPredicate.apply(context);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFoodPredicate from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to false.");
        return false;
    }


    @Override
    public boolean canBreed() {
        if (builder.canBreed == null) {
            return super.canBreed();
        }
        Object obj = builder.canBreed.apply(this);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBreed from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to super method: " + super.canBreed());
        return super.canBreed();
    }

    @Override
    public boolean canMate(Animal pOtherAnimal) {
        if (builder.canMate == null) {
            return super.canMate(pOtherAnimal);
        }
        final ContextUtils.EntityAnimalContext context = new ContextUtils.EntityAnimalContext(this, pOtherAnimal);
        Object obj = builder.canMate.apply(context);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canMate from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canMate(pOtherAnimal));
        return super.canMate(pOtherAnimal);
    }


    @Override
    public void spawnChildFromBreeding(ServerLevel pLevel, Animal pMate) {
        if (builder.onSpawnChildFromBreeding != null) {
            final ContextUtils.LevelAnimalContext context = new ContextUtils.LevelAnimalContext(pMate, this, pLevel);
            builder.onSpawnChildFromBreeding.accept(context);
            super.spawnChildFromBreeding(pLevel, pMate);
        } else {
            super.spawnChildFromBreeding(pLevel, pMate);
        }
    }


    //Mob Interact here because it has special implimentations due to breeding in AgeableMob classes.

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        if (this.level().isClientSide) {
            boolean flag = this.isOwnedBy(pPlayer) || this.isTame() || (this.tamableFood(itemstack) || this.tamableFoodPredicate(itemstack)) && !this.isTame() && !this.isAngry();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (this.isTame()) {
                if (builder.onInteract != null) {
                    final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(this, pPlayer, pHand);
                    builder.onInteract.accept(context);
                }
                if ((this.isFood(itemstack) || this.isFoodPredicate(itemstack)) && this.getHealth() < this.getMaxHealth()) {
                    if (itemstack.isEdible()) {
                        this.heal((float) Objects.requireNonNull(itemstack.getFoodProperties(this)).getNutrition());

                        if (!pPlayer.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }

                        this.gameEvent(GameEvent.EAT, this);
                        return InteractionResult.SUCCESS;
                    }
                }

                InteractionResult interactionresult = super.mobInteract(pPlayer, pHand);
                if ((!interactionresult.consumesAction() || this.isBaby()) && this.isOwnedBy(pPlayer)) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget((LivingEntity) null);
                    return InteractionResult.SUCCESS;
                }

                return interactionresult;
            } else if ((this.tamableFood(itemstack) || this.tamableFoodPredicate(itemstack)) && !this.isAngry()) {
                if (!pPlayer.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                if (this.random.nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(this, pPlayer)) {
                    this.tame(pPlayer);
                    this.navigation.stop();
                    this.setTarget((LivingEntity) null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }

                return InteractionResult.SUCCESS;
            }
            if (builder.onInteract != null) {
                final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(this, pPlayer, pHand);
                builder.onInteract.accept(context);
            }
            return super.mobInteract(pPlayer, pHand);
        }
    }

    //Mob Overrides
    @Override
    public boolean canBeLeashed(Player pPlayer) {
        if (builder.canBeLeashed != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(pPlayer, this);
            Object obj = builder.canBeLeashed.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeLeashed from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canBeLeashed(pPlayer));
        }
        return super.canBeLeashed(pPlayer);
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        if (builder.removeWhenFarAway == null) {
            return super.removeWhenFarAway(pDistanceToClosestPlayer);
        }
        final ContextUtils.EntityDistanceToPlayerContext context = new ContextUtils.EntityDistanceToPlayerContext(pDistanceToClosestPlayer, this);
        Object obj = builder.removeWhenFarAway.apply(context);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for removeWhenFarAway from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.removeWhenFarAway(pDistanceToClosestPlayer));
        return super.removeWhenFarAway(pDistanceToClosestPlayer);
    }

    @Override
    protected double followLeashSpeed() {
        return Objects.requireNonNullElseGet(builder.followLeashSpeed, super::followLeashSpeed);
    }

    @Override
    public int getAmbientSoundInterval() {
        if (builder.ambientSoundInterval != null) return (int) builder.ambientSoundInterval;
        return super.getAmbientSoundInterval();
    }

    @Override
    public double getMyRidingOffset() {
        if (builder.myRidingOffset == null) return super.getMyRidingOffset();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.myRidingOffset.apply(this), "double");
        if (obj != null) return (double) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for myRidingOffset from entity: " + entityName() + ". Value: " + builder.myRidingOffset.apply(this) + ". Must be a double. Defaulting to " + super.getMyRidingOffset());
        return super.getMyRidingOffset();
    }

    @Override
    public MobType getMobType() {
        return builder.mobType;
    }

    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        ItemStack itemstack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, (item) -> {
            return item instanceof BowItem;
        })));
        AbstractArrow abstractarrow = this.getArrow(itemstack, pDistanceFactor);
        if (this.getMainHandItem().getItem() instanceof BowItem) {
            abstractarrow = ((BowItem) this.getMainHandItem().getItem()).customArrow(abstractarrow);
        }

        double d0 = pTarget.getX() - this.getX();
        double d1 = pTarget.getY(0.3333333333333333) - abstractarrow.getY();
        double d2 = pTarget.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        abstractarrow.shoot(d0, d1 + d3 * 0.20000000298023224, d2, 1.6F, (float) (14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(abstractarrow);
    }

    protected AbstractArrow getArrow(ItemStack pArrowStack, float pVelocity) {
        return ProjectileUtil.getMobArrow(this, pArrowStack, pVelocity);
    }

    public boolean canJump() {
        return Objects.requireNonNullElse(builder.canJump, true);
    }

    public void onJump() {
        if (builder.onLivingJump != null) {
            builder.onLivingJump.accept(this);
        }
    }

    public void jump() {
        double jumpPower = this.getJumpPower() + this.getJumpBoostPower();
        Vec3 currentVelocity = this.getDeltaMovement();

        // Adjust the Y component of the velocity to the calculated jump power
        this.setDeltaMovement(currentVelocity.x, jumpPower, currentVelocity.z);
        this.hasImpulse = true;
        if (this.isSprinting()) {
            // If sprinting, add a horizontal impulse for forward boost
            float yawRadians = this.getYRot() * 0.017453292F;
            this.setDeltaMovement(
                    this.getDeltaMovement().add(
                            -Math.sin(yawRadians) * 0.2,
                            0.0,
                            Math.cos(yawRadians) * 0.2
                    )
            );
        }

        this.hasImpulse = true;
        onJump();
        ForgeHooks.onLivingJump(this);
    }

    public boolean shouldJump() {
        BlockPos forwardPos = this.blockPosition().relative(this.getDirection());
        return this.level().loadedAndEntityCanStandOn(forwardPos, this) && this.getStepHeight() < this.level().getBlockState(forwardPos).getShape(this.level(), forwardPos).max(Direction.Axis.Y);
    }

    @Override
    public HumanoidArm getMainArm() {
        if (builder.mainArm != null) return (HumanoidArm) builder.mainArm;
        return super.getMainArm();
    }


    @Override
    public void aiStep() {
        super.aiStep();
        if (builder.aiStep != null) {
            builder.aiStep.accept(this);
        }
        if (this.onGround() && this.getNavigation().isInProgress() && shouldJump()) {
            jump();
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader levelReader) {
        if (builder.walkTargetValue == null) return super.getWalkTargetValue(pos, levelReader);
        final ContextUtils.EntityBlockPosLevelContext context = new ContextUtils.EntityBlockPosLevelContext(pos, levelReader, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.walkTargetValue.apply(context), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for walkTargetValue from entity: " + entityName() + ". Value: " + builder.walkTargetValue.apply(context) + ". Must be a float. Defaulting to " + super.getWalkTargetValue(pos, levelReader));
        return super.getWalkTargetValue(pos, levelReader);
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
    }

    @Override
    protected void tickLeash() {
        super.tickLeash();
        if (builder.tickLeash != null) {
            Player $$0 = (Player) this.getLeashHolder();
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext($$0, this);
            builder.tickLeash.accept(context);
        }
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        if (builder.shouldStayCloseToLeashHolder == null) return super.shouldStayCloseToLeashHolder();
        Object value = builder.shouldStayCloseToLeashHolder.apply(this);
        if (value instanceof Boolean b)
            return b;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldStayCloseToLeashHolder from entity: " + entityName() + ". Value: " + value + ". Must be a boolean. Defaulting to " + super.shouldStayCloseToLeashHolder());
        return super.shouldStayCloseToLeashHolder();
    }


    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (builder.onTargetChanged != null) {
            final ContextUtils.TargetChangeContext context = new ContextUtils.TargetChangeContext(target, this);
            builder.onTargetChanged.accept(context);
        }
    }

    public boolean canFireProjectileWeaponPredicate(ProjectileWeaponItem projectileWeapon) {
        if (builder.canFireProjectileWeaponPredicate != null) {
            final ContextUtils.EntityProjectileWeaponContext context = new ContextUtils.EntityProjectileWeaponContext(projectileWeapon, this);
            Object obj = builder.canFireProjectileWeaponPredicate.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFireProjectileWeaponPredicate from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to false.");
        }
        return false;
    }


    public boolean canFireProjectileWeapons(ProjectileWeaponItem projectileWeapon) {
        if (builder.canFireProjectileWeapon != null) {
            return builder.canFireProjectileWeapon.test(projectileWeapon.getDefaultInstance()) && projectileWeapon instanceof ProjectileWeaponItem;
        }
        return super.canFireProjectileWeapon(projectileWeapon);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
        if (canFireProjectileWeapons(projectileWeapon) || canFireProjectileWeaponPredicate(projectileWeapon)) {
            return canFireProjectileWeapons(projectileWeapon) && canFireProjectileWeaponPredicate(projectileWeapon);
        }
        return super.canFireProjectileWeapon(projectileWeapon);
    }

    @Override
    public void ate() {
        super.ate();
        if (builder.ate != null) {
            builder.ate.accept(this);
        }
    }


    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (builder.setAmbientSound != null) {
            return ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setAmbientSound);
        } else {
            return super.getAmbientSound();
        }
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        if (builder.canHoldItem != null) {
            final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(stack, this);
            Object obj = builder.canHoldItem.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canHoldItem from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canHoldItem(stack));
        }
        return super.canHoldItem(stack);
    }


    @Override
    protected boolean shouldDespawnInPeaceful() {
        return Objects.requireNonNullElseGet(builder.shouldDespawnInPeaceful, super::shouldDespawnInPeaceful);
    }

    @Override
    public boolean isPersistenceRequired() {
        return Objects.requireNonNullElseGet(builder.isPersistenceRequired, super::isPersistenceRequired);
    }


    @Override
    public double getMeleeAttackRangeSqr(LivingEntity entity) {
        if (builder.meleeAttackRangeSqr != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.meleeAttackRangeSqr.apply(this), "double");
            if (obj != null) {
                return (double) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for meleeAttackRangeSqr from entity: " + entityName() + ". Value: " + builder.meleeAttackRangeSqr.apply(this) + ". Must be a double. Defaulting to " + super.getMeleeAttackRangeSqr(entity));
        }
        return super.getMeleeAttackRangeSqr(entity);
    }


    //(Base LivingEntity/Entity Overrides)
    protected boolean thisJumping = false;

    public boolean ableToJump() {
        return ModKeybinds.mount_jump.isDown() && this.onGround();
    }

    public void setThisJumping(boolean value) {
        this.thisJumping = value;
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        LivingEntity livingentity = this.getControllingPassenger();
        if (this.isAlive() && this.isVehicle() && builder.canSteer && livingentity != null) {
            if (this.getControllingPassenger() instanceof Player && builder.mountJumpingEnabled) {
                if (this.ableToJump()) {
                    this.setThisJumping(true);
                }
                if (this.thisJumping) {
                    this.setThisJumping(false);

                    double jumpPower = this.getJumpPower() + this.getJumpBoostPower();
                    Vec3 currentVelocity = this.getDeltaMovement();

                    // Add the jump velocity to the current velocity
                    double newVelocityX = currentVelocity.x;
                    double newVelocityY = currentVelocity.y + jumpPower; // Add jump velocity
                    double newVelocityZ = currentVelocity.z;

                    this.setDeltaMovement(newVelocityX, newVelocityY, newVelocityZ);
                    onJump();
                    ForgeHooks.onLivingJump(this);
                }
            }

            LivingEntity passenger = this.getControllingPassenger();
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
            this.setYRot(passenger.getYRot());
            this.setXRot(passenger.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;
            float x = passenger.xxa * 0.5F;
            float z = passenger.zza;
            if (z <= 0.0F) {
                z *= 0.25F;
            }
            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));


            super.travel(new Vec3((double) x, pTravelVector.y, (double) z));

        } else super.travel(pTravelVector);

        if (builder.travel != null) {
            final ContextUtils.Vec3Context context = new ContextUtils.Vec3Context(pTravelVector, this);
            builder.travel.accept(context);
        }
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


    @Info(value = """
            Calls a triggerable animation to be played anywhere.
            """)
    public void triggerAnimation(String controllerName, String animName) {
        triggerAnim(controllerName, animName);
    }

    @Override
    public boolean canCollideWith(Entity pEntity) {
        if (builder.canCollideWith != null) {
            final ContextUtils.CollidingEntityContext context = new ContextUtils.CollidingEntityContext(this, pEntity);
            Object obj = builder.canCollideWith.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCollideWith from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canCollideWith(pEntity));
        }
        return super.canCollideWith(pEntity);
    }

    @Override
    protected float getSoundVolume() {
        return Objects.requireNonNullElseGet(builder.setSoundVolume, super::getSoundVolume);
    }

    @Override
    protected float getWaterSlowDown() {
        return Objects.requireNonNullElseGet(builder.setWaterSlowDown, super::getWaterSlowDown);
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
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        if (builder == null || builder.setStandingEyeHeight == null)
            return super.getStandingEyeHeight(pPose, pDimensions);
        final ContextUtils.EntityPoseDimensionsContext context = new ContextUtils.EntityPoseDimensionsContext(pPose, pDimensions, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setStandingEyeHeight.apply(context), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setStandingEyeHeight from entity: " + entityName() + ". Value: " + builder.setStandingEyeHeight.apply(context) + ". Must be a float. Defaulting to " + super.getStandingEyeHeight(pPose, pDimensions));
        return super.getStandingEyeHeight(pPose, pDimensions);
    }


    @Override
    public boolean isPushable() {
        return builder.isPushable;
    }

    @Override
    protected float getBlockSpeedFactor() {
        if (builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.blockSpeedFactor.apply(this), "float");
        if (builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        if (obj != null) {
            return (float) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + builder.get() + ". Value: " + builder.blockSpeedFactor.apply(this) + ". Must be a float, defaulting to " + super.getBlockSpeedFactor());
            return super.getBlockSpeedFactor();
        }
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity entity) {
        if (builder.canAddPassenger == null) {
            return super.canAddPassenger(entity);
        }
        final ContextUtils.PassengerEntityContext context = new ContextUtils.PassengerEntityContext(entity, this);
        Object obj = builder.canAddPassenger.apply(context);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.canAddPassenger(entity));
        return super.canAddPassenger(entity);
    }


    @Override
    protected boolean shouldDropLoot() {
        if (builder.shouldDropLoot != null) {
            Object obj = builder.shouldDropLoot.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropLoot from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.shouldDropLoot());
        }
        return super.shouldDropLoot();
    }


    @Override
    protected boolean isAffectedByFluids() {
        if (builder.isAffectedByFluids != null) {
            Object obj = builder.isAffectedByFluids.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByFluids from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAffectedByFluids());
        }
        return super.isAffectedByFluids();
    }


    @Override
    protected boolean isAlwaysExperienceDropper() {
        return builder.isAlwaysExperienceDropper;
    }


    @Override
    protected boolean isImmobile() {
        if (builder.isImmobile != null) {
            Object obj = builder.isImmobile.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isImmobile from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isImmobile());
        }
        return super.isImmobile();
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
    public int calculateFallDamage(float fallDistance, float pDamageMultiplier) {
        if (builder.calculateFallDamage == null) return super.calculateFallDamage(fallDistance, pDamageMultiplier);
        final ContextUtils.CalculateFallDamageContext context = new ContextUtils.CalculateFallDamageContext(fallDistance, pDamageMultiplier, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.calculateFallDamage.apply(context), "integer");
        if (obj != null) {
            return (int) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for calculateFallDamage from entity: " + entityName() + ". Value: " + builder.calculateFallDamage.apply(context) + ". Must be an int, defaulting to " + super.calculateFallDamage(fallDistance, pDamageMultiplier));
        return super.calculateFallDamage(fallDistance, pDamageMultiplier);
    }

    @Override
    public void tick() {

        super.tick();
        if (builder.tick != null) {
            if (!this.level().isClientSide()) {
                builder.tick.accept(this);
            }
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (builder.onAddedToWorld != null && !this.level().isClientSide()) {
            builder.onAddedToWorld.accept(this);
        }
    }


    @Override
    protected void doAutoAttackOnTouch(@NotNull LivingEntity target) {
        super.doAutoAttackOnTouch(target);
        if (builder.doAutoAttackOnTouch != null) {
            final ContextUtils.AutoAttackContext context = new ContextUtils.AutoAttackContext(this, target);
            builder.doAutoAttackOnTouch.accept(context);
        }
    }


    @Override
    protected int decreaseAirSupply(int p_21303_) {
        if (builder.onDecreaseAirSupply != null) {
            builder.onDecreaseAirSupply.accept(this);
        }
        return super.decreaseAirSupply(p_21303_);
    }

    @Override
    protected int increaseAirSupply(int p_21307_) {
        if (builder.onIncreaseAirSupply != null) {
            builder.onIncreaseAirSupply.accept(this);
        }
        return super.increaseAirSupply(p_21307_);
    }

    @Override
    protected void blockedByShield(@NotNull LivingEntity p_21246_) {
        super.blockedByShield(p_21246_);
        if (builder.onBlockedByShield != null) {
            builder.onBlockedByShield.accept(p_21246_);
        }
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


    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource p_21239_) {
        if (builder.setHurtSound == null) return super.getHurtSound(p_21239_);
        final ContextUtils.HurtContext context = new ContextUtils.HurtContext(this, p_21239_);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setHurtSound.apply(context), "resourcelocation");
        if (obj != null) return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) obj));
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setHurtSound from entity: " + entityName() + ". Value: " + builder.setHurtSound.apply(context) + ". Must be a ResourceLocation or String. Defaulting to \"minecraft:entity.generic.hurt\"");
        return super.getHurtSound(p_21239_);
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
    public boolean canAttackType(@NotNull EntityType<?> entityType) {
        if (builder.canAttackType != null) {
            final ContextUtils.EntityTypeEntityContext context = new ContextUtils.EntityTypeEntityContext(this, entityType);
            Object obj = builder.canAttackType.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttackType from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canAttackType(entityType));
        }
        return super.canAttackType(entityType);
    }


    @Override
    public float getScale() {
        if (builder.scale == null) return super.getScale();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.scale.apply(this), "float");
        if (obj != null) {
            return (float) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for scale from entity: " + entityName() + ". Value: " + builder.scale.apply(this) + ". Must be a float. Defaulting to " + super.getScale());
            return super.getScale();
        }
    }


    /*@Override
    public boolean rideableUnderWater() {
        return Objects.requireNonNullElseGet(builder.rideableUnderWater, super::rideableUnderWater);
    }*/


    @Override
    public boolean shouldDropExperience() {
        if (builder.shouldDropExperience != null) {
            Object obj = builder.shouldDropExperience.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropExperience from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.shouldDropExperience());
        }
        return super.shouldDropExperience();
    }


    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack previous, ItemStack current) {
        super.onEquipItem(slot, previous, current);
        if (builder.onEquipItem != null) {
            final ContextUtils.EntityEquipmentContext context = new ContextUtils.EntityEquipmentContext(slot, previous, current, this);
            builder.onEquipItem.accept(context);
        }
    }


    @Override
    public double getVisibilityPercent(@Nullable Entity p_20969_) {
        if (builder.visibilityPercent != null) {
            final ContextUtils.VisualContext context = new ContextUtils.VisualContext(p_20969_, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.visibilityPercent.apply(context), "double");
            if (obj != null) {
                return (double) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for visibilityPercent from entity: " + entityName() + ". Value: " + builder.visibilityPercent.apply(context) + ". Must be a double. Defaulting to " + super.getVisibilityPercent(p_20969_));
                return super.getVisibilityPercent(p_20969_);
            }
        } else {
            return super.getVisibilityPercent(p_20969_);
        }
    }


    @Override
    public boolean canAttack(@NotNull LivingEntity entity) {
        if (builder.canAttack != null) {
            final ContextUtils.LivingEntityContext context = new ContextUtils.LivingEntityContext(this, entity);
            Object obj = builder.canAttack.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj && super.canAttack(entity);
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttack from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canAttack(entity));
        }
        return super.canAttack(entity);
    }


    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {
        if (builder.canBeAffected == null) {
            return super.canBeAffected(effectInstance);
        }
        final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
        Object result = builder.canBeAffected.apply(context);
        if (result instanceof Boolean) {
            return (boolean) result;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeAffected from entity: " + entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + super.canBeAffected(effectInstance));
        return super.canBeAffected(effectInstance);
    }


    @Override
    public boolean isInvertedHealAndHarm() {
        if (builder.invertedHealAndHarm == null) {
            return super.isInvertedHealAndHarm();
        }
        Object obj = builder.invertedHealAndHarm.apply(this);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for invertedHealAndHarm from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isInvertedHealAndHarm());
        return super.isInvertedHealAndHarm();
    }


    @Override
    public void onEffectAdded(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (builder.onEffectAdded != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            builder.onEffectAdded.accept(context);
        } else {
            super.onEffectAdded(effectInstance, entity);
        }
    }


    @Override
    protected void onEffectRemoved(@NotNull MobEffectInstance effectInstance) {

        if (builder.onEffectRemoved != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            builder.onEffectRemoved.accept(context);
        } else {
            super.onEffectRemoved(effectInstance);
        }
    }


    @Override
    public void heal(float amount) {
        super.heal(amount);
        if (builder.onLivingHeal != null) {
            final ContextUtils.EntityHealContext context = new ContextUtils.EntityHealContext(this, amount);
            builder.onLivingHeal.accept(context);
        }
    }


    @Override
    public void die(@NotNull DamageSource damageSource) {
        super.die(damageSource);
        if (builder.onDeath != null) {
            final ContextUtils.DeathContext context = new ContextUtils.DeathContext(this, damageSource);
            builder.onDeath.accept(context);
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (builder.setDeathSound == null) return super.getDeathSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setDeathSound));
    }


    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int lootingMultiplier, boolean allowDrops) {
        if (builder.dropCustomDeathLoot != null) {
            final ContextUtils.EntityLootContext context = new ContextUtils.EntityLootContext(damageSource, lootingMultiplier, allowDrops, this);
            builder.dropCustomDeathLoot.accept(context);
        } else {
            super.dropCustomDeathLoot(damageSource, lootingMultiplier, allowDrops);
        }
    }


    @Override
    public @NotNull Fallsounds getFallSounds() {
        if (builder.fallSounds != null)
            return new Fallsounds(
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.smallFallSound)),
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.largeFallSound))
            );
        return super.getFallSounds();
    }

    @Override
    public @NotNull SoundEvent getEatingSound(@NotNull ItemStack itemStack) {
        if (builder.eatingSound != null)
            return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.eatingSound));
        return super.getEatingSound(itemStack);
    }

    @Override
    public boolean onClimbable() {
        if (builder.onClimbable == null) {
            return super.onClimbable();
        }
        Object obj = builder.onClimbable.apply(this);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for onClimbable from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to super.onClimbable(): " + super.onClimbable());
        return super.onClimbable();
    }


    //Deprecated but still works for 1.20.4 :shrug:
    @Override
    public boolean canBreatheUnderwater() {
        return Objects.requireNonNullElseGet(builder.canBreatheUnderwater, super::canBreatheUnderwater);
    }


    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource damageSource) {
        if (builder.onLivingFall != null) {
            final ContextUtils.EntityFallDamageContext context = new ContextUtils.EntityFallDamageContext(this, damageMultiplier, distance, damageSource);
            builder.onLivingFall.accept(context);
        }
        return super.causeFallDamage(distance, damageMultiplier, damageSource);
    }


    @Override
    public void setSprinting(boolean sprinting) {
        if (builder.onSprint != null) {
            builder.onSprint.accept(this);
        }
        super.setSprinting(sprinting);
    }


    @Override
    public float getJumpBoostPower() {
        if (builder.jumpBoostPower == null) return super.getJumpBoostPower();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.jumpBoostPower.apply(this), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for jumpBoostPower from entity: " + entityName() + ". Value: " + builder.jumpBoostPower.apply(this) + ". Must be a float. Defaulting to " + super.getJumpBoostPower());
        return super.getJumpBoostPower();
    }


    @Override
    public boolean canStandOnFluid(@NotNull FluidState fluidState) {
        if (builder.canStandOnFluid != null) {
            final ContextUtils.EntityFluidStateContext context = new ContextUtils.EntityFluidStateContext(this, fluidState);
            Object obj = builder.canStandOnFluid.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canStandOnFluid from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canStandOnFluid(fluidState));
        }
        return super.canStandOnFluid(fluidState);
    }


    @Override
    public boolean isSensitiveToWater() {
        if (builder.isSensitiveToWater != null) {
            Object obj = builder.isSensitiveToWater.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSensitiveToWater from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isSensitiveToWater());
        }
        return super.isSensitiveToWater();
    }


    @Override
    public void stopRiding() {
        super.stopRiding();
        if (builder.onStopRiding != null) {
            builder.onStopRiding.accept(this);
        }
    }


    @Override
    public void rideTick() {
        super.rideTick();
        if (builder.rideTick != null) {
            builder.rideTick.accept(this);
        }
    }


    @Override
    public void onItemPickup(@NotNull ItemEntity p_21054_) {
        super.onItemPickup(p_21054_);
        if (builder.onItemPickup != null) {
            final ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(this, p_21054_);
            builder.onItemPickup.accept(context);
        }
    }


    @Override
    public boolean hasLineOfSight(@NotNull Entity entity) {
        if (builder.hasLineOfSight != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(entity, this);
            Object obj = builder.hasLineOfSight.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for hasLineOfSight from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.hasLineOfSight(entity));
        }
        return super.hasLineOfSight(entity);
    }


    @Override
    public void onEnterCombat() {
        if (builder.onEnterCombat != null) {
            builder.onEnterCombat.accept(this);
        } else {
            super.onEnterCombat();
        }
    }


    @Override
    public void onLeaveCombat() {
        if (builder.onLeaveCombat != null) {
            builder.onLeaveCombat.accept(this);
        }
        super.onLeaveCombat();
    }

    @Override
    public boolean isAffectedByPotions() {
        if (builder.isAffectedByPotions != null) {
            Object obj = builder.isAffectedByPotions.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByPotions from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAffectedByPotions());
        }
        return super.isAffectedByPotions();
    }


    @Override
    public boolean attackable() {
        if (builder.isAttackable != null) {
            Object obj = builder.isAttackable.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAttackable from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.attackable());
        }
        return super.attackable();
    }


    @Override
    public boolean canTakeItem(@NotNull ItemStack itemStack) {
        if (builder.canTakeItem != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, this.level());
            Object obj = builder.canTakeItem.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTakeItem(itemStack));
        }
        return super.canTakeItem(itemStack);
    }


    @Override
    public boolean isSleeping() {
        if (builder.isSleeping != null) {
            Object obj = builder.isSleeping.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSleeping from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isSleeping());
        }
        return super.isSleeping();
    }


    @Override
    public void startSleeping(@NotNull BlockPos blockPos) {

        if (builder.onStartSleeping != null) {
            final ContextUtils.EntityBlockPosContext context = new ContextUtils.EntityBlockPosContext(this, blockPos);
            builder.onStartSleeping.accept(context);
        }
        super.startSleeping(blockPos);
    }


    @Override
    public void stopSleeping() {
        if (builder.onStopSleeping != null) {
            builder.onStopSleeping.accept(this);
        }
        super.stopSleeping();
    }


    @Override
    public @NotNull ItemStack eat(@NotNull Level level, @NotNull ItemStack itemStack) {
        if (builder.eat != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, level);
            builder.eat.accept(context);
            return itemStack;
        }
        return super.eat(level, itemStack);
    }


    @Override
    public boolean shouldRiderFaceForward(@NotNull Player player) {
        if (builder.shouldRiderFaceForward != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(player, this);
            Object obj = builder.shouldRiderFaceForward.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldRiderFaceForward from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.shouldRiderFaceForward(player));
        }
        return super.shouldRiderFaceForward(player);
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
    public boolean canDisableShield() {
        if (builder.canDisableShield != null) {
            Object obj = builder.canDisableShield.apply(this);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canDisableShield from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canDisableShield());
        }
        return super.canDisableShield();
    }


    @Override
    public void onClientRemoval() {
        if (builder.onClientRemoval != null) {
            builder.onClientRemoval.accept(this);
        }
        super.onClientRemoval();
    }

    @Override
    protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        if (builder.onHurt != null) {
            final ContextUtils.EntityDamageContext context = new ContextUtils.EntityDamageContext(pDamageSource, pDamageAmount, this);
            builder.onHurt.accept(context);
        }
        super.actuallyHurt(pDamageSource, pDamageAmount);
    }

    @Override
    public void lavaHurt() {
        if (builder.lavaHurt != null) {
            builder.lavaHurt.accept(this);
        }
        super.lavaHurt();
    }


    @Override
    protected void onFlap() {
        if (builder.onFlap != null) {
            builder.onFlap.accept(this);
        }
        super.onFlap();
    }

    @Override
    public int getExperienceReward() {
        if (builder.experienceReward != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.experienceReward.apply(this), "integer");
            if (obj != null) {
                return (int) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for experienceReward from entity: " + entityName() + ". Value: " + builder.experienceReward.apply(this) + ". Must be an integer. Defaulting to " + super.getExperienceReward());
        }
        return super.getExperienceReward();
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
    public void playerTouch(Player p_20081_) {
        if (builder.playerTouch != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(p_20081_, this);
            builder.playerTouch.accept(context);
        }
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
    public void thunderHit(ServerLevel p_19927_, LightningBolt p_19928_) {
        if (builder.thunderHit != null) {
            super.thunderHit(p_19927_, p_19928_);
            final ContextUtils.ThunderHitContext context = new ContextUtils.ThunderHitContext(p_19927_, p_19928_, this);
            builder.thunderHit.accept(context);
        }
    }


    @Override
    public boolean isInvulnerableTo(DamageSource p_20122_) {
        if (builder.isInvulnerableTo != null) {
            final ContextUtils.DamageContext context = new ContextUtils.DamageContext(this, p_20122_);
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
            final ContextUtils.MayInteractContext context = new ContextUtils.MayInteractContext(p_146843_, p_146844_, this);
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
            final ContextUtils.CanTrampleContext context = new ContextUtils.CanTrampleContext(state, pos, fallDistance, this);
            Object obj = builder.canTrample.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTrample(state, pos, fallDistance));
        }

        return super.canTrample(state, pos, fallDistance);
    }


    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (builder.onRemovedFromWorld != null) {
            builder.onRemovedFromWorld.accept(this);
        }
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
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        if (builder.lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, this);
            builder.lerpTo.accept(context);
        }
    }


    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return handItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> handItems.get(slot.getIndex());
            case ARMOR -> armorItems.get(slot.getIndex());
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        verifyEquippedItem(stack);
        switch (slot.getType()) {
            case HAND -> onEquipItem(slot, handItems.set(slot.getIndex(), stack), stack);
            case ARMOR -> onEquipItem(slot, armorItems.set(slot.getIndex(), stack), stack);
        }
    }
}