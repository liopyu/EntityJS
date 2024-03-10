package net.liopyu.entityjs.builders.partbuilders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.partentities.AnimalPartEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.implementation.EventBasedSpawnModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AnimalEntityPartBuilder {
    public transient boolean isPickable;
    public transient Function<Entity, Boolean> canBeHurt;
    public transient Consumer<ContextUtils.LerpToContext> lerpTo;
    public transient Consumer<ContextUtils.EntityPlayerContext> playerTouch;
    public transient Function<ContextUtils.EntitySqrDistanceContext, Object> shouldRenderAtSqrDistance;
    public transient Consumer<Entity> tick;
    public transient Consumer<ContextUtils.MovementContext> move;
    public transient Boolean isAttackable;

    //new overrides
    public transient float width;
    public transient float height;
    public transient boolean summonable;
    public transient boolean save;
    public transient boolean fireImmune;
    public transient boolean canSpawnFarFromPlayer;
    public transient ResourceLocation[] immuneTo;
    public transient boolean spawnFarFromPlayer;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;
    public transient boolean isPushable;
    public transient Function<LivingEntity, Object> shouldDropLoot;
    public transient Function<ContextUtils.PassengerEntityContext, Object> canAddPassenger;
    public transient Function<LivingEntity, Object> isAffectedByFluids;
    public transient boolean isAlwaysExperienceDropper;
    public transient Function<LivingEntity, Object> isImmobile;
    public transient Function<LivingEntity, Object> setBlockJumpFactor;
    public transient Function<LivingEntity, Object> blockSpeedFactor;
    public transient Float setSoundVolume;
    public transient Float setWaterSlowDown;
    public transient Object setSwimSound;
    public transient Function<LivingEntity, Object> isFlapping;
    public transient Object setDeathSound;
    public transient BaseLivingEntityBuilder.RenderType renderType;
    public transient EntityType<?> getType;
    public transient Object mainArm;

    public transient Consumer<ContextUtils.AutoAttackContext> doAutoAttackOnTouch;

    public transient Function<ContextUtils.EntityPoseDimensionsContext, Object> setStandingEyeHeight;

    public transient Consumer<LivingEntity> onDecreaseAirSupply;
    public transient Consumer<LivingEntity> onBlockedByShield;

    public transient Boolean repositionEntityAfterLoad;

    public transient Function<Entity, Object> nextStep;

    public transient Consumer<LivingEntity> onIncreaseAirSupply;

    public transient Function<ContextUtils.HurtContext, Object> setHurtSound;

    public transient Object setSwimSplashSound;


    public transient Function<ContextUtils.EntityTypeEntityContext, Object> canAttackType;

    public transient Function<LivingEntity, Object> scale;

    public transient Function<LivingEntity, Object> shouldDropExperience;

    public transient Function<LivingEntity, Object> experienceReward;


    public transient Consumer<ContextUtils.EntityEquipmentContext> onEquipItem;


    public transient Function<ContextUtils.VisualContext, Object> visibilityPercent;

    public transient Function<ContextUtils.LivingEntityContext, Object> canAttack;

    public transient Function<ContextUtils.OnEffectContext, Object> canBeAffected;

    public transient Function<LivingEntity, Object> invertedHealAndHarm;

    public transient Consumer<ContextUtils.OnEffectContext> onEffectAdded;


    public transient Consumer<ContextUtils.OnEffectContext> onEffectRemoved;

    public transient Consumer<ContextUtils.EntityHealContext> onLivingHeal;


    public transient Consumer<ContextUtils.EntityDamageContext> onHurt;


    public transient Consumer<ContextUtils.DeathContext> onDeath;


    public transient Consumer<ContextUtils.EntityLootContext> dropCustomDeathLoot;


    public transient LivingEntity.Fallsounds fallSounds;
    public transient Object smallFallSound;
    public transient Object largeFallSound;

    public transient Object eatingSound;

    public transient Function<LivingEntity, Object> onClimbable;
    public transient Boolean canBreatheUnderwater;

    public transient Consumer<ContextUtils.EntityFallDamageContext> onLivingFall;

    public transient Consumer<LivingEntity> onSprint;

    public transient Function<LivingEntity, Object> jumpBoostPower;
    public transient Function<ContextUtils.EntityFluidStateContext, Object> canStandOnFluid;


    public transient Function<LivingEntity, Object> isSensitiveToWater;

    public transient Consumer<LivingEntity> onStopRiding;
    public transient Consumer<LivingEntity> rideTick;


    public transient Consumer<ContextUtils.EntityItemEntityContext> onItemPickup;
    public transient Function<ContextUtils.LineOfSightContext, Object> hasLineOfSight;

    public transient Consumer<LivingEntity> onEnterCombat;
    public transient Consumer<LivingEntity> onLeaveCombat;

    public transient Function<LivingEntity, Object> isAffectedByPotions;


    public transient Function<ContextUtils.EntityItemLevelContext, Object> canTakeItem;

    public transient Function<LivingEntity, Object> isSleeping;
    public transient Consumer<ContextUtils.EntityBlockPosContext> onStartSleeping;
    public transient Consumer<LivingEntity> onStopSleeping;

    public transient Consumer<ContextUtils.EntityItemLevelContext> eat;

    public transient Function<ContextUtils.PlayerEntityContext, Object> shouldRiderFaceForward;

    public transient Function<LivingEntity, Object> canFreeze;
    public transient Function<LivingEntity, Object> isCurrentlyGlowing;
    public transient Function<LivingEntity, Object> canDisableShield;
    public transient Function<LivingEntity, Object> setMaxFallDistance;
    public transient Consumer<ContextUtils.MobInteractContext> onInteract;

    public transient Consumer<LivingEntity> onClientRemoval;
    public transient Consumer<LivingEntity> onAddedToWorld;
    public transient Consumer<LivingEntity> lavaHurt;
    public transient Consumer<LivingEntity> onFlap;
    public transient Function<LivingEntity, Object> dampensVibrations;

    public transient Function<LivingEntity, Object> showVehicleHealth;

    public transient Consumer<ContextUtils.ThunderHitContext> thunderHit;
    public transient Function<ContextUtils.DamageContext, Object> isInvulnerableTo;
    public transient Function<LivingEntity, Object> canChangeDimensions;
    public transient Function<ContextUtils.CalculateFallDamageContext, Object> calculateFallDamage;
    public transient Function<ContextUtils.MayInteractContext, Object> mayInteract;
    public transient Function<ContextUtils.CanTrampleContext, Object> canTrample;
    public transient Consumer<LivingEntity> onRemovedFromWorld;
    public transient Consumer<LivingEntity> onLivingJump;
    public transient Consumer<LivingEntity> aiStep;

    public transient Consumer<AttributeSupplier.Builder> attributes;
    public SpawnPlacements.Type placementType;
    public Heightmap.Types heightMap;
    public SpawnPlacements.SpawnPredicate<? extends Entity> spawnPredicate;
    public static final List<BaseLivingEntityBuilder<?>> spawnList = new ArrayList<>();
    public static final List<EventBasedSpawnModifier.BiomeSpawn> biomeSpawnList = new ArrayList<>();

    public transient Consumer<ContextUtils.RenderContext> render;
    public transient MobType mobType;
    public transient Function<LivingEntity, Object> isFreezing;

    public transient Function<ContextUtils.CollidingEntityContext, Object> canCollideWith;
    public transient Boolean defaultDeathPose;
    public transient Function<ContextUtils.Vec3Context, Object> travelVector;
    public transient Consumer<ContextUtils.Vec3Context> travel;
    public transient Boolean canSteer;
    public transient boolean mountJumpingEnabled;
    public transient Consumer<LivingEntity> tickDeath;

    public AnimalEntityPartBuilder() {
        isPickable = true;
    }

    public AnimalEntityPartBuilder canBeHurt(Function<Entity, Boolean> hurtFunction) {
        this.hurtFunction = hurtFunction;
        return this;
    }

    public AnimalEntityPartBuilder isPickable(boolean isPickable) {
        this.isPickable = isPickable;
        return this;
    }

    @Info(value = """
            Sets a consumer to handle lerping (linear interpolation) of the entity's position.
                        
            @param consumer Consumer accepting a {@link ContextUtils.LerpToContext} parameter,
                            providing information and control over the lerping process.
                        
            Example usage:
            ```javascript
            entityBuilder.lerpTo(context => {
                // Custom logic for lerping the entity's position
                // Access information about the lerping process using the provided context.
            });
            ```
            """)
    public AnimalEntityPartBuilder lerpTo(Consumer<ContextUtils.LerpToContext> consumer) {
        lerpTo = consumer;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity should render at a squared distance.
                        
            @param function Function accepting a {@link ContextUtils.EntitySqrDistanceContext} parameter,
                             defining the conditions under which the entity should render.
                        
            Example usage:
            ```javascript
            entityBuilder.shouldRenderAtSqrDistance(context => {
                // Custom logic to determine whether the entity should render
                // Access information about the distance using the provided context.
                return true;
            });
            ```
            """)
    public AnimalEntityPartBuilder shouldRenderAtSqrDistance(Function<ContextUtils.EntitySqrDistanceContext, Object> func) {
        shouldRenderAtSqrDistance = func;
        return this;
    }


    @Info(value = """
            Sets whether the entity is attackable or not.
                        
            @param b Boolean value indicating whether the entity is attackable.
                        
            Example usage:
            ```javascript
            entityBuilder.isAttackable(true);
            ```
            """)
    public AnimalEntityPartBuilder isAttackable(boolean b) {
        isAttackable = b;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when a player touches the entity.
            The provided Consumer accepts a {@link ContextUtils.EntityPlayerContext} parameter,
            representing the context of the player's interaction with the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.playerTouch(context => {
                // Custom logic to handle the player's touch interaction with the entity
                // Access information about the interaction using the provided context.
            });
            ```
            """)
    public AnimalEntityPartBuilder playerTouch(Consumer<ContextUtils.EntityPlayerContext> consumer) {
        playerTouch = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs a movement action.
            The provided Consumer accepts a {@link ContextUtils.MovementContext} parameter,
            representing the context of the entity's movement.
                        
            Example usage:
            ```javascript
            entityBuilder.move(context => {
                // Custom logic to handle the entity's movement action
                // Access information about the movement using the provided context.
            });
            ```
            """)
    public AnimalEntityPartBuilder move(Consumer<ContextUtils.MovementContext> consumer) {
        move = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed on each tick for the entity.
                        
            @param consumer A Consumer accepting a {@link Entity} parameter, defining the behavior to be executed on each tick.
                        
            Example usage:
            ```javascript
            entityBuilder.tick(entity => {
                // Custom logic to be executed on each tick of the entity.
                // Access information about the entity using the provided parameter.
            });
            ```
            """)
    public AnimalEntityPartBuilder tick(Consumer<Entity> consumer) {
        tick = consumer;
        return this;
    }

}
