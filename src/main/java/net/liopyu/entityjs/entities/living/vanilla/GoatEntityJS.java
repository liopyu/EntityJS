package net.liopyu.entityjs.entities.living.vanilla;

import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.living.vanilla.GoatJSBuilder;
import net.liopyu.entityjs.entities.living.entityjs.AnimalEntityJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.PartEntityJS;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.events.BuildBrainEventJS;
import net.liopyu.entityjs.events.BuildBrainProviderEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.ModKeybinds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.liopyu.liolib.core.animatable.instance.AnimatableInstanceCache;
import net.liopyu.liolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GoatEntityJS extends Goat implements IAnimatableJS {
    private final AnimatableInstanceCache getAnimatableInstanceCache;


    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    public String entityName() {
        return this.getType().toString();
    }

    protected final GoatJSBuilder builder;


    public final PartEntityJS<?>[] partEntities;

    public GoatEntityJS(GoatJSBuilder builder, EntityType<? extends Goat> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
        List<PartEntityJS<?>> tempPartEntities = new ArrayList<>();
        for (ContextUtils.PartEntityParams<GoatEntityJS> params : builder.partEntityParamsList) {
            PartEntityJS<?> partEntity = new PartEntityJS<>(this, params.name, params.width, params.height, params.builder);
            tempPartEntities.add(partEntity);
        }
        partEntities = tempPartEntities.toArray(new PartEntityJS<?>[0]);
        this.navigation = this.createNavigation(pLevel);
        this.lookControl = createLookControl();
        this.moveControl = createMoveControl();
        this.jumpControl = createJumpControl();
    }


    // Part Entity Logical Overrides --------------------------------
    @Override
    public void setId(int entityId) {
        super.setId(entityId);
        for (int i = 0; i < partEntities.length; i++) {
            PartEntityJS<?> partEntity = partEntities[i];
            if (partEntity != null) {
                partEntity.setId(entityId + i + 1);
            }
        }
    }

    public void tickPart(String partName, double offsetX, double offsetY, double offsetZ) {
        var x = this.getX();
        var y = this.getY();
        var z = this.getZ();
        for (PartEntityJS<?> partEntity : partEntities) {
            if (partEntity.name.equals(partName)) {
                partEntity.movePart(x + offsetX, y + offsetY, z + offsetZ, partEntity.getYRot(), partEntity.getXRot());
                return;
            }
        }
        EntityJSHelperClass.logWarningMessageOnce("Part with name " + partName + " not found for entity: " + entityName());
    }


    @Override
    public boolean isMultipartEntity() {
        return partEntities != null;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
    }

    @Override
    public PartEntity<?>[] getParts() {
        return Objects.requireNonNullElseGet(partEntities, () -> new PartEntity<?>[0]);
    }

    //Builder and Animatable logic
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
    public Brain.Provider<Goat> brainProvider() {
        if (EventHandlers.buildBrainProvider.hasListeners()) {
            final BuildBrainProviderEventJS<Goat> event = new BuildBrainProviderEventJS<>();
            EventHandlers.buildBrainProvider.post(event, getTypeId());
            return event.provide();
        } else {
            return super.brainProvider();
        }
    }

    @Override
    protected Brain<GoatEntityJS> makeBrain(Dynamic<?> p_21069_) {
        if (EventHandlers.buildBrain.hasListeners()) {
            final Brain<GoatEntityJS> brain = UtilsJS.cast(brainProvider().makeBrain(p_21069_));
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


    //Ageable Mob Overrides
    @Override
    public Goat getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        if (builder.setBreedOffspring != null) {
            final ContextUtils.BreedableEntityContext context = new ContextUtils.BreedableEntityContext(this, ageableMob, serverLevel);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setBreedOffspring.apply(context), "resourcelocation");
            if (obj instanceof ResourceLocation resourceLocation) {
                EntityType<?> breedOffspringType = ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
                if (breedOffspringType != null) {
                    Entity breedOffspringEntity = breedOffspringType.create(serverLevel);
                    if (breedOffspringEntity instanceof Goat g) {
                        return g;
                    }
                }
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid resource location or Entity Type for breedOffspring: " + obj + ". Must return an instance of Goat resource location. Defaulting to super method: " + entityName());
        }
        return builder.get().create(serverLevel);
    }

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
            EntityJSHelperClass.consumerCallback(builder.onSpawnChildFromBreeding, context, "[EntityJS]: Error in " + entityName() + "builder for field: onSpawnChildFromBreeding.");
            super.spawnChildFromBreeding(pLevel, pMate);
        } else {
            super.spawnChildFromBreeding(pLevel, pMate);
        }
    }


    //Mob Interact here because it has special implimentations due to breeding in AgeableMob classes.
    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (this.isFood(itemstack) || this.isFoodPredicate(itemstack)) {
            int i = this.getAge();
            if (!this.level.isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.setInLove(pPlayer);
                return InteractionResult.SUCCESS;
            }
            if (this.isBaby()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
            if (this.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
        }
        if (builder.onInteract != null) {
            final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(this, pPlayer, pHand);
            EntityJSHelperClass.consumerCallback(builder.onInteract, context, "[EntityJS]: Error in " + entityName() + "builder for field: onInteract.");
        }
        return super.mobInteract(pPlayer, pHand);
    }

    //Mob Overrides
    public void onJump() {
        if (builder.onLivingJump != null) {
            EntityJSHelperClass.consumerCallback(builder.onLivingJump, this, "[EntityJS]: Error in " + entityName() + "builder for field: onLivingJump.");

        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (canJump() && this.isOnGround() && this.getNavigation().isInProgress() && shouldJump()) {
            jump();
        }
        if (builder.aiStep != null) {
            EntityJSHelperClass.consumerCallback(builder.aiStep, this, "[EntityJS]: Error in " + entityName() + "builder for field: aiStep.");

        }
    }

    @Override
    protected void tickDeath() {
        if (builder.tickDeath != null) {
            EntityJSHelperClass.consumerCallback(builder.tickDeath, this, "[EntityJS]: Error in " + entityName() + "builder for field: tickDeath.");

        } else super.tickDeath();
    }


    @Override
    protected void tickLeash() {
        super.tickLeash();
        if (builder.tickLeash != null) {
            Player $$0 = (Player) this.getLeashHolder();
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext($$0, this);
            EntityJSHelperClass.consumerCallback(builder.tickLeash, context, "[EntityJS]: Error in " + entityName() + "builder for field: tickLeash.");

        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (builder.onTargetChanged != null) {
            final ContextUtils.TargetChangeContext context = new ContextUtils.TargetChangeContext(target, this);
            EntityJSHelperClass.consumerCallback(builder.onTargetChanged, context, "[EntityJS]: Error in " + entityName() + "builder for field: onTargetChanged.");

        }
    }

    @Override
    public void ate() {
        super.ate();
        if (builder.ate != null) {
            EntityJSHelperClass.consumerCallback(builder.ate, this, "[EntityJS]: Error in " + entityName() + "builder for field: ate.");

        }
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        if (builder != null && builder.onHurtTarget != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, this);
            EntityJSHelperClass.consumerCallback(builder.onHurtTarget, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHurtTarget.");
        }
        return super.doHurtTarget(pEntity);
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        if (builder == null || builder.createNavigation == null) return new GroundPathNavigation(this, pLevel);
        final ContextUtils.EntityLevelContext context = new ContextUtils.EntityLevelContext(pLevel, this);
        Object obj = builder.createNavigation.apply(context);
        if (obj instanceof PathNavigation p) return p;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for createNavigation from entity: " + entityName() + ". Value: " + obj + ". Must be PathNavigation. Defaulting to super method.");
        return new GroundPathNavigation(this, pLevel);
    }

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
        if (builder != null) {
            return builder.mobType;
        }
        return super.getMobType();
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
        abstractarrow.shoot(d0, d1 + d3 * 0.20000000298023224, d2, 1.6F, (float) (14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(abstractarrow);
    }

    protected AbstractArrow getArrow(ItemStack pArrowStack, float pVelocity) {
        return ProjectileUtil.getMobArrow(this, pArrowStack, pVelocity);
    }

    public boolean canJump() {
        return Objects.requireNonNullElse(builder.canJump, true);
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
        return this.level.loadedAndEntityCanStandOn(forwardPos, this) && this.getStepHeight() < this.level.getBlockState(forwardPos).getShape(this.level, forwardPos).max(Direction.Axis.Y);
    }

    @Override
    public HumanoidArm getMainArm() {
        if (builder.mainArm != null) return (HumanoidArm) builder.mainArm;
        return super.getMainArm();
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
    protected boolean shouldStayCloseToLeashHolder() {
        if (builder.shouldStayCloseToLeashHolder == null) return super.shouldStayCloseToLeashHolder();
        Object value = builder.shouldStayCloseToLeashHolder.apply(this);
        if (value instanceof Boolean b)
            return b;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldStayCloseToLeashHolder from entity: " + entityName() + ". Value: " + value + ". Must be a boolean. Defaulting to " + super.shouldStayCloseToLeashHolder());
        return super.shouldStayCloseToLeashHolder();
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
    @Override
    public boolean isAlliedTo(Entity pEntity) {
        if (builder.isAlliedTo != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, this);
            Object obj = builder.isAlliedTo.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAlliedTo from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAlliedTo(pEntity));
        }
        return super.isAlliedTo(pEntity);
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
            EntityJSHelperClass.consumerCallback(builder.travel, context, "[EntityJS]: Error in " + entityName() + "builder for field: travel.");

        }
    }

    @Override
    public void tick() {
        super.tick();
        if (builder.tick != null) {
            if (!this.level.isClientSide()) {
                EntityJSHelperClass.consumerCallback(builder.tick, this, "[EntityJS]: Error in " + entityName() + "builder for field: tick.");

            }
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (builder.defaultGoals) {
            super.registerGoals();
        }
        if (builder.onAddedToWorld != null && !this.level.isClientSide()) {
            EntityJSHelperClass.consumerCallback(builder.onAddedToWorld, this, "[EntityJS]: Error in " + entityName() + "builder for field: onAddedToWorld.");

        }
    }


    @Override
    protected void doAutoAttackOnTouch(@NotNull LivingEntity target) {
        super.doAutoAttackOnTouch(target);
        if (builder.doAutoAttackOnTouch != null) {
            final ContextUtils.AutoAttackContext context = new ContextUtils.AutoAttackContext(this, target);
            EntityJSHelperClass.consumerCallback(builder.doAutoAttackOnTouch, context, "[EntityJS]: Error in " + entityName() + "builder for field: doAutoAttackOnTouch.");
        }
    }


    @Override
    protected int decreaseAirSupply(int p_21303_) {
        if (builder.onDecreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(builder.onDecreaseAirSupply, this, "[EntityJS]: Error in " + entityName() + "builder for field: onDecreaseAirSupply.");
        }
        return super.decreaseAirSupply(p_21303_);
    }

    @Override
    protected int increaseAirSupply(int p_21307_) {
        if (builder.onIncreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(builder.onIncreaseAirSupply, this, "[EntityJS]: Error in " + entityName() + "builder for field: onIncreaseAirSupply.");

        }
        return super.increaseAirSupply(p_21307_);
    }

    @Override
    protected void blockedByShield(@NotNull LivingEntity p_21246_) {
        super.blockedByShield(p_21246_);
        if (builder.onBlockedByShield != null) {
            var context = new ContextUtils.LivingEntityContext(this, p_21246_);
            EntityJSHelperClass.consumerCallback(builder.onBlockedByShield, context, "[EntityJS]: Error in " + entityName() + "builder for field: onDecreaseAirSupply.");
        }
    }

    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack previous, ItemStack current) {
        super.onEquipItem(slot, previous, current);
        if (builder.onEquipItem != null) {
            final ContextUtils.EntityEquipmentContext context = new ContextUtils.EntityEquipmentContext(slot, previous, current, this);
            EntityJSHelperClass.consumerCallback(builder.onEquipItem, context, "[EntityJS]: Error in " + entityName() + "builder for field: onEquipItem.");

        }
    }

    @Override
    public void onEffectAdded(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (builder.onEffectAdded != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            EntityJSHelperClass.consumerCallback(builder.onEffectAdded, context, "[EntityJS]: Error in " + entityName() + "builder for field: onEffectAdded.");

        } else {
            super.onEffectAdded(effectInstance, entity);
        }
    }


    @Override
    protected void onEffectRemoved(@NotNull MobEffectInstance effectInstance) {

        if (builder.onEffectRemoved != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            EntityJSHelperClass.consumerCallback(builder.onEffectRemoved, context, "[EntityJS]: Error in " + entityName() + "builder for field: onEffectRemoved.");
        } else {
            super.onEffectRemoved(effectInstance);
        }
    }


    @Override
    public void heal(float amount) {
        super.heal(amount);
        if (builder.onLivingHeal != null) {
            final ContextUtils.EntityHealContext context = new ContextUtils.EntityHealContext(this, amount);
            EntityJSHelperClass.consumerCallback(builder.onLivingHeal, context, "[EntityJS]: Error in " + entityName() + "builder for field: onLivingHeal.");

        }
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        super.die(damageSource);
        if (builder.onDeath != null) {
            final ContextUtils.DeathContext context = new ContextUtils.DeathContext(this, damageSource);
            EntityJSHelperClass.consumerCallback(builder.onDeath, context, "[EntityJS]: Error in " + entityName() + "builder for field: onDeath.");

        }
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int lootingMultiplier, boolean allowDrops) {
        if (builder.dropCustomDeathLoot != null) {
            final ContextUtils.EntityLootContext context = new ContextUtils.EntityLootContext(damageSource, lootingMultiplier, allowDrops, this);
            EntityJSHelperClass.consumerCallback(builder.dropCustomDeathLoot, context, "[EntityJS]: Error in " + entityName() + "builder for field: dropCustomDeathLoot.");

        } else {
            super.dropCustomDeathLoot(damageSource, lootingMultiplier, allowDrops);
        }
    }

    @Override
    protected void onFlap() {
        if (builder.onFlap != null) {
            EntityJSHelperClass.consumerCallback(builder.onFlap, this, "[EntityJS]: Error in " + entityName() + "builder for field: onFlap.");

        }
        super.onFlap();
    }

    protected boolean thisJumping = false;

    public boolean ableToJump() {
        return ModKeybinds.mount_jump.isDown() && this.isOnGround();
    }

    public void setThisJumping(boolean value) {
        this.thisJumping = value;
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
    protected SoundEvent getDeathSound() {
        if (builder.setDeathSound == null) return super.getDeathSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setDeathSound));
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
            EntityJSHelperClass.consumerCallback(builder.onLivingFall, context, "[EntityJS]: Error in " + entityName() + "builder for field: onLivingFall.");

        }
        return super.causeFallDamage(distance, damageMultiplier, damageSource);
    }


    @Override
    public void setSprinting(boolean sprinting) {
        if (builder.onSprint != null) {
            EntityJSHelperClass.consumerCallback(builder.onSprint, this, "[EntityJS]: Error in " + entityName() + "builder for field: onSprint.");

        }
        super.setSprinting(sprinting);
    }


    @Override
    public double getJumpBoostPower() {
        if (builder.jumpBoostPower == null) return super.getJumpBoostPower();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.jumpBoostPower.apply(this), "double");
        if (obj != null) return (double) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for jumpBoostPower from entity: " + entityName() + ". Value: " + builder.jumpBoostPower.apply(this) + ". Must be a double. Defaulting to " + super.getJumpBoostPower());
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
    public void onItemPickup(@NotNull ItemEntity p_21054_) {
        super.onItemPickup(p_21054_);
        if (builder.onItemPickup != null) {
            final ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(this, p_21054_);
            EntityJSHelperClass.consumerCallback(builder.onItemPickup, context, "[EntityJS]: Error in " + entityName() + "builder for field: onItemPickup.");

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
            EntityJSHelperClass.consumerCallback(builder.onEnterCombat, this, "[EntityJS]: Error in " + entityName() + "builder for field: onEnterCombat.");

        } else {
            super.onEnterCombat();
        }
    }


    @Override
    public void onLeaveCombat() {
        if (builder.onLeaveCombat != null) {
            EntityJSHelperClass.consumerCallback(builder.onLeaveCombat, this, "[EntityJS]: Error in " + entityName() + "builder for field: onLeaveCombat.");

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
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, this.level);
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
            EntityJSHelperClass.consumerCallback(builder.onStartSleeping, context, "[EntityJS]: Error in " + entityName() + "builder for field: onStartSleeping.");

        }
        super.startSleeping(blockPos);
    }


    @Override
    public void stopSleeping() {
        if (builder.onStopSleeping != null) {
            EntityJSHelperClass.consumerCallback(builder.onStopSleeping, this, "[EntityJS]: Error in " + entityName() + "builder for field: onStopSleeping.");
        }
        super.stopSleeping();
    }


    @Override
    public @NotNull ItemStack eat(@NotNull Level level, @NotNull ItemStack itemStack) {
        if (builder.eat != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, level);
            EntityJSHelperClass.consumerCallback(builder.eat, context, "[EntityJS]: Error in " + entityName() + "builder for field: eat.");
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
        if (builder.isCurrentlyGlowing != null && !this.level.isClientSide()) {
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
            EntityJSHelperClass.consumerCallback(builder.onClientRemoval, this, "[EntityJS]: Error in " + entityName() + "builder for field: onClientRemoval.");

        }
        super.onClientRemoval();
    }

    @Override
    public void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        if (builder.onHurt != null) {
            final ContextUtils.EntityDamageContext context = new ContextUtils.EntityDamageContext(pDamageSource, pDamageAmount, this);
            EntityJSHelperClass.consumerCallback(builder.onHurt, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHurt.");

        }
        super.actuallyHurt(pDamageSource, pDamageAmount);
    }

    @Override
    public void lavaHurt() {
        if (builder.lavaHurt != null) {
            EntityJSHelperClass.consumerCallback(builder.lavaHurt, this, "[EntityJS]: Error in " + entityName() + "builder for field: lavaHurt.");

        }
        super.lavaHurt();
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
            EntityJSHelperClass.consumerCallback(builder.playerTouch, context, "[EntityJS]: Error in " + entityName() + "builder for field: playerTouch.");
        } else super.playerTouch(p_20081_);
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
            EntityJSHelperClass.consumerCallback(builder.thunderHit, context, "[EntityJS]: Error in " + entityName() + "builder for field: thunderHit.");

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
        if (builder.onRemovedFromWorld != null) {
            EntityJSHelperClass.consumerCallback(builder.onRemovedFromWorld, this, "[EntityJS]: Error in " + entityName() + "builder for field: onRemovedFromWorld.");
        }
        super.onRemovedFromWorld();
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
            EntityJSHelperClass.consumerCallback(builder.lerpTo, context, "[EntityJS]: Error in " + entityName() + "builder for field: lerpTo.");
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