package net.liopyu.entityjs.util.ai;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.warden.ForceUnmount;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Random;
import java.util.function.*;

@SuppressWarnings("unused")
public enum Behaviors {
    INSTANCE;

    @Info(value = "Creates an `AcquirePoi` behavior, only applicable to **pathfinder** entities", params = {
            @Param(name = "poiType", value = "A predicate for pois the entity will attempt to acquire"),
            @Param(name = "memoryKey", value = "The memory type that may not be present for this behavior to be enabled, villagers use `minecraft:job_site` here"),
            @Param(name = "memoryToAcquire", value = "The memory type to use when a poi is acquired, villagers use `minecraft:potential_job_site` here"),
            @Param(name = "onlyIfAdult", value = "If this behavior should only apply when the entity is an adult"),
            @Param(name = "onPoiAcquisitionEvent", value = "The entity event to be sent to the entity when it acquires the poi, may be null to not send a client bound packet. This value is handled by an entity's implementation of the `handleEntityEvent` method")
    })
    public BehaviorControl<PathfinderMob> acquirePoi(Predicate<Holder<PoiType>> poiType, MemoryModuleType<GlobalPos> memoryKey, MemoryModuleType<GlobalPos> memoryToAcquire, boolean onlyIfAdult, @Nullable Byte onPoiAcquisitionEvent) {
        return AcquirePoi.create(poiType, memoryKey, memoryToAcquire, onlyIfAdult, Optional.ofNullable(onPoiAcquisitionEvent));
    }

    @Info(value = "Creates an `AnimalMakeLove` behavior, only applicable to **animal** entities", params = {
            @Param(name = "partnerType", value = "The entity type the animal can breed with, note: both animals must have the same class unless their `canBreed` methods have been overridden"),
            @Param(name = "speedModifier", value = "The modifier to the animal's speed when this behavior is active")
    })
    public AnimalMakeLove animalMakeLove(EntityType<? extends Animal> partnerType, float speedModifier) {
        return new AnimalMakeLove(partnerType, speedModifier);
    }

    @Info(value = "Creates an `AnimalPanic` behavior, only applicable to **pathfinder** entities", params = {
            @Param(name = "speedModifier", value = "The modifier to the animal's speed when this behavior is active")
    })
    public AnimalPanic animalPanic(float speedMultiplier) {
        return new AnimalPanic(speedMultiplier);
    }

    @Info(value = "Creates an `BabyFollowAdult` behavior, only applicable to **ageable** mobs", params = {
            @Param(name = "minFollowRange", value = "The minimum follow distance of the baby"),
            @Param(name = "maxFollowRange", value = "The maximum follow distance of the baby"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public OneShot<AgeableMob> babyFollowAdult(int minFollowRange, int maxFollowRange, Function<LivingEntity, Float> speedModifier) {
        return BabyFollowAdult.create(UniformInt.of(minFollowRange, maxFollowRange), speedModifier);
    }

    @Info(value = "Creates a `CountCooldownTicks` behavior", params = {
            @Param(name = "coolDownTicks", value = "The memory type to use to keep track of the cool down")
    })
    public CountDownCooldownTicks countDownCooldownTicks(MemoryModuleType<Integer> coolDownTicks) {
        return new CountDownCooldownTicks(coolDownTicks);
    }

    @Info(value = "Creates a `DismountOrSkipMounting` behavior", params = {
            @Param(name = "maxWalkDistToRideTarget", value = "The maximum distance the entity is willing to walk to ride an entity"),
            @Param(name = "dontRideIf", value = "The predicate for when the entity should get off its mount")
    })
    public <E extends LivingEntity> BehaviorControl<E> dismountOrSkipMounting(int maxWalkDistToRideTarget, BiPredicate<E, Entity> dontRideIf) {
        return DismountOrSkipMounting.create(maxWalkDistToRideTarget, dontRideIf);
    }

    @Info(value = "Creates a `FlyingRandomStroll` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<PathfinderMob> flyingRandomStroll(float speedModifier) {
        return RandomStroll.fly(speedModifier);
    }

    @Info(value = "Creates a `FollowTemptation` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public FollowTemptation followTemptation(Function<LivingEntity, Float> speedModifier) {
        return new FollowTemptation(speedModifier);
    }

    @Info(value = "Creates a `ForceUnmount` behavior")
    public ForceUnmount forceUnmount() {
        return new ForceUnmount();
    }

    @Info(value = "Creates a `MoveToSkySeeingSpot` behavior", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public OneShot<LivingEntity> moveToSkySeeingSpot(float speedModifier) {
        return MoveToSkySeeingSpot.create(speedModifier);
    }

    @Info(value = "Creates a `GoToTargetLocation` behavior, only applicable to **mob** entities", params = {
            @Param(name = "locationMemory", value = "The memory type to use to store the target location"),
            @Param(name = "closeEnoughDistance", value = "The distance that is close enough to the location for the entity to consider it 'at' the target location"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public <E extends Mob> OneShot<E> gotoTargetLocation(MemoryModuleType<BlockPos> locationMemory, int closeEnoughDistance, float speedModifier) {
        return GoToTargetLocation.create(locationMemory, closeEnoughDistance, speedModifier);
    }

    @Info(value = "Creates a `GoToWantedItem` behavior", params = {
            @Param(name = "predicate", value = "The predicate that is checked to determine if the entity may use this behavior"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "maxDistToWalk", value = "The maximum distance the entity will walk to go to the wanted item"),
            @Param(name = "hasWlkTargetMemoryModuleType", value = "If the entity has the `minecraft:walk_target` memory type")
    })
    public <E extends LivingEntity> BehaviorControl<E> goToWantedItem(Predicate<E> predicate, float speedModifier, int maxDistToWalk, boolean hasWalkTargetMemoryModuleType) {
        return GoToWantedItem.create(predicate, speedModifier, hasWalkTargetMemoryModuleType, maxDistToWalk);
    }

    @Info(value = "Creates a `InsideBrownianWalk` behavior, only applicable to **pathfinder** entities", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<PathfinderMob> insideBrownianWalk(float speedModifier) {
        return InsideBrownianWalk.create(speedModifier);
    }

    @Info(value = "Creates an `InteractWith` behavior", params = {
            @Param(name = "typeToInteractWith", value = "The entity type to interact with"),
            @Param(name = "interactionRange", value = "The range the entity will interact with the other entity"),
            @Param(name = "selfFilter", value = "A self-predicate which determines when this behavior can be used"),
            @Param(name = "targetFilter", value = "A target-predicate which determines when this behavior can be used"),
            @Param(name = "memory", value = "The memory type to use for this behavior"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "maxDistance", value = "The maximum distance they entity may acquire an interaction target from")
    })
    public <E extends LivingEntity, T extends LivingEntity> BehaviorControl<E> interactWith(
            EntityType<? extends T> typeToInteractWith,
            int interactionRange,
            Predicate<E> selfFilter,
            Predicate<T> targetFilter,
            MemoryModuleType<T> memory,
            float speedModifier,
            int maxDistance
    ) {
        return InteractWith.of(
                typeToInteractWith,
                interactionRange,
                selfFilter,
                targetFilter,
                memory,
                speedModifier,
                maxDistance
        );
    }

    @Info(value = "Creates an `InteractWithDoor` behavior")
    public InteractWithDoor interactWithDoor() {
        return new InteractWithDoor();
    }

    @Info(value = "Creates a `JumpOnBed` behavior, only applicable to **mob** entities", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public JumpOnBed jumpOnBed(float speedModifier) {
        return new JumpOnBed(speedModifier);
    }

    @Info(value = "Creates a `LocateHidingPlace` behavior", params = {
            @Param(name = "radius", value = "The maximum radius a hiding place will be searched for"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "closeEnoughDistance", value = "The distance at which the entity considers itself close enough to the hiding place")
    })
    public OneShot<LivingEntity> locateHidingPlace(int radius, float speedModifier, int closeEnoughDistance) {
        return LocateHidingPlace.create(radius, speedModifier, closeEnoughDistance);
    }

    @Info(value = "Creates a `LongJumpMidJump` behavior, only applicable to **mob** entities", params = {
            @Param(name = "minTicksBetweenJumps", value = "The minimum number of ticks that must pass before the entity must jump"),
            @Param(name = "maxTicksBetweenJumps", value = "The maximum number of ticks that must pass before the entity must jump"),
            @Param(name = "landingSound", value = "The sound event that will be broadcast when the entity lands")
    })
    public LongJumpMidJump longJumpMidJump(int minTicksBetweenJumps, int maxTicksBetweenJumps, SoundEvent landingSound) {
        return new LongJumpMidJump(UniformInt.of(minTicksBetweenJumps, maxTicksBetweenJumps), landingSound);
    }

    @Info(value = "Creates a `BecomePassiveIfMemoryPresent` behavior", params = {
            @Param(name = "memoryType", value = "The memory type that will pacify the entity"),
            @Param(name = "pacifyDuration", value = "How long the entity will be pacified for")
    })
    public BehaviorControl<LivingEntity> becomePassiveIfMemoryPresent(MemoryModuleType<?> memoryType, int pacifyDuration) {
        return BecomePassiveIfMemoryPresent.create(memoryType, pacifyDuration);
    }

    @Info(value = "Creates a `DoNothing` behavior", params = {
            @Param(name = "minTime", value = "The minimum amount of time to do nothing for"),
            @Param(name = "maxTime", value = "The maximum amount of time to do nothing for")
    })
    public DoNothing doNothing(int minTime, int maxTime) {
        return new DoNothing(minTime, maxTime);
    }

    @Info(value = "Creates a `EraseMemoryIf` behavior", params = {
            @Param(name = "predicate", value = "When to erase the memory"),
            @Param(name = "memoryType", value = "The memory type to be erased")
    })
    public <E extends LivingEntity> BehaviorControl<E> eraseMemoryIf(Predicate<E> predicate, MemoryModuleType<?> memoryType) {
        return EraseMemoryIf.create(predicate, memoryType);
    }

    @Info(value = "Creates a `BackUpIfTooClose` behavior, only applicable to **mob** entities", params = {
            @Param(name = "tooCloseDistance", value = "The distance at which the mob will begin to backup"),
            @Param(name = "strafeSpeed", value = "The speed at which the entity will back away")
    })
    public OneShot<Mob> backUpIfTooClose(int tooCloseDistance, float strafeSpeed) {
        return BackUpIfTooClose.create(tooCloseDistance, strafeSpeed);
    }

    @Info(value = "Creates a `LongJumpToPreferredBlock` behavior, only applicable to **mob** entities", params = {
            @Param(name = "minTimeBetweenJumps", value = "The minimum number of ticks between jumps"),
            @Param(name = "maxTimeBetweenJumps", value = "The maximum number of ticks between jumps"),
            @Param(name = "maxJumpHeight", value = "The maximum vertical distance the mob will attempt to jump between"),
            @Param(name = "maxJumpWidth", value = "the maximum horizontal distance the mob will attempt to jump"),
            @Param(name = "maxJumpVelocity", value = "The maximum velocity the mob may jump at"),
            @Param(name = "jumpSound", value = "The sound that is played when the mob jumps"),
            @Param(name = "preferredBlockTag", value = "A block tag, the blocks which the mob will attempt to jump to"),
            @Param(name = "preferredBlockChance", value = "The chance that the behavior will use its preferred blocks for jumps instead of any block. Range: [0, 1]"),
            @Param(name = "acceptableLandingSpot", value = "A filter for what blocks are acceptable to land on")
    })
    public <E extends Mob> LongJumpToPreferredBlock<E> longJumpToPreferredBlock(
            int minTimeBetweenJumps,
            int maxTimeBetweenJumps,
            int maxJumpHeight,
            int maxJumpWidth,
            float maxJumpVelocity,
            Function<E, SoundEvent> jumpSound,
            ResourceLocation preferredBlockTag,
            float preferredBlockChance,
            BiPredicate<E, BlockPos> acceptableLandingSpot
    ) {
        return new LongJumpToPreferredBlock<>(
                UniformInt.of(minTimeBetweenJumps, maxTimeBetweenJumps),
                maxJumpHeight,
                maxJumpWidth,
                maxJumpVelocity,
                jumpSound,
                TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), preferredBlockTag),
                preferredBlockChance,
                acceptableLandingSpot
        );
    }

    @Info(value = "Creates a `LongJumpToRandomPos` behavior, only applicable to **mob** entities", params = {
            @Param(name = "minTimeBetweenJumps", value = "The minimum number of ticks between jumps"),
            @Param(name = "maxTimeBetweenJumps", value = "The maximum number of ticks between jumps"),
            @Param(name = "maxJumpHeight", value = "The maximum vertical distance the mob will attempt to jump between"),
            @Param(name = "maxJumpWidth", value = "the maximum horizontal distance the mob will attempt to jump"),
            @Param(name = "maxJumpVelocity", value = "The maximum velocity the mob may jump at"),
            @Param(name = "jumpSound", value = "The sound that is played when the mob jumps"),
            @Param(name = "acceptableLandingSpot", value = "A filter for what blocks are acceptable to land on")
    })
    public <E extends Mob> LongJumpToRandomPos<E> longJumpToRandomPos(
            int minTimeBetweenJumps,
            int maxTimeBetweenJumps,
            int maxJumpHeight,
            int maxJumpWidth,
            float maxJumpVelocity,
            Function<E, SoundEvent> jumpSound,
            BiPredicate<E, BlockPos> acceptableLandingSpot
    ) {
        return new LongJumpToRandomPos<>(
                UniformInt.of(minTimeBetweenJumps, maxTimeBetweenJumps),
                maxJumpHeight,
                maxJumpWidth,
                maxJumpVelocity,
                jumpSound,
                acceptableLandingSpot
        );
    }

    @Info(value = "Creates a `LookAtTargetSink` behavior, only applicable to **mob** entities", params = {
            @Param(name = "minDuration", value = "The minimum duration of the behavior"),
            @Param(name = "maxDuration", value = "The maximum duration of the behavior")
    })
    public LookAtTargetSink lookAtTargetSink(int minDuration, int maxDuration) {
        return new LookAtTargetSink(minDuration, maxDuration);
    }

    @Info(value = "Creates a `MeleeAttack` behavior, only applicable to **mob** entities", params = {
            @Param(name = "attackCooldown", value = "The attack cooldown of the entity when this behavior is active")
    })
    public OneShot<Mob> meleeAttack(int attackCooldown) {
        return MeleeAttack.create(attackCooldown);
    }

    @Info(value = "Creates a `Mount` behavior", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<LivingEntity> mount(float speedModifier) {
        return Mount.create(speedModifier);
    }

    @Info(value = "Creates a `MoveToTargetSink` behavior, only applicable to **mob** entities", params = {
            @Param(name = "minDuration", value = "The minimum duration of the behavior"),
            @Param(name = "maxDuration", value = "The maximum duration of the behavior")
    })
    public MoveToTargetSink moveToTargetSink(int minDuration, int maxDuration) {
        return new MoveToTargetSink(minDuration, maxDuration);
    }

    @Info(value = "Creates a `PlayTagWithOtherKids` behavior, only applicable to **pathfinder** mobs")
    public PlayTagWithOtherKids playTagWithOtherKids() {
        return new PlayTagWithOtherKids();
    }

    @Info(value = "Creates a new `TargetingConditions` for use in `.prepareRamNearestTarget()`", params = {
            @Param(name = "isForCombat", value = "If the conditions will be used for combat"),
            @Param(name = "range", value = "The range at which the entity will target"),
            @Param(name = "ignoreLineOfSight", value = "If the line of sight requirement should be ignored"),
            @Param(name = "ignoreInvisibilityTesting", value = "If the consideration of the target's invisibility status should be ignored"),
            @Param(name = "selector", value = "Sets the predicate for the target, may be null to accept all entities")
    })
    public TargetingConditions targetingConditions(boolean isForCombat, double range, boolean ignoreLineOfSight, boolean ignoreInvisibilityTesting, @Nullable Predicate<LivingEntity> selector) {
        final TargetingConditions conditions = isForCombat ? TargetingConditions.forCombat() : TargetingConditions.forNonCombat();
        conditions.range(range);
        if (ignoreLineOfSight) conditions.ignoreLineOfSight();
        if (ignoreInvisibilityTesting) conditions.ignoreInvisibilityTesting();
        conditions.selector(selector);
        return conditions;
    }

    @Info(value = "Creates a `PrepareRanNearestTarget` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "cooldownOnFall", value = "Sets the `minecraft:ram_cooldown_ticks` memory based on the entity when the behavior ends"),
            @Param(name = "minRamDistance", value = "The minimum distance something will be rammed at"),
            @Param(name = "maxRamDistance", value = "The maximum distance something will be rammed at"),
            @Param(name = "walkSpeed", value = "The speed at which the mob will walk at"),
            @Param(name = "targetingConditions", value = "The targeting conditions used by the entity with this behavior"),
            @Param(name = "ramPrepareTime", value = "The amount of ticks the entity will prepare to ram its target"),
            @Param(name = "prepareRamSound", value = "The sound event that will be played based on the entity")
    })
    public <E extends PathfinderMob> PrepareRamNearestTarget<E> prepareRamNearestTarget(
            ToIntFunction<E> cooldownOnFall,
            int minRamDistance,
            int maxRamDistance,
            float walkSpeed,
            TargetingConditions targetingConditions,
            int ramPrepareTime,
            Function<E, SoundEvent> prepareRamSound
    ) {
        return new PrepareRamNearestTarget<>(
                cooldownOnFall,
                minRamDistance,
                maxRamDistance,
                walkSpeed,
                targetingConditions,
                ramPrepareTime,
                prepareRamSound
        );
    }

    @Info(value = "Creates a `RandomStroll` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "maxHorizontalDistance", value = "The maximum horizontal distance the mob will stroll"),
            @Param(name = "maxVerticalDistance", value = "The maximum vertical distance the mob will stroll")
    })
    public BehaviorControl<PathfinderMob> randomStroll(float speedModifier, int maxHorizontalDistance, int maxVerticalDistance) {
        return RandomStroll.stroll(speedModifier, maxHorizontalDistance, maxVerticalDistance);
    }

    @Info(value = "Creates a `RandomSwim` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<PathfinderMob> randomSwim(float speedModifier) {
        return RandomStroll.swim(speedModifier);
    }

    @Info(value = "Creates a `ReactToBell` behavior")
    public BehaviorControl<LivingEntity> reactToBell() {
        return ReactToBell.create();
    }

    @Info(value = "Creates a `ResetRaidStatus` behavior")
    public BehaviorControl<LivingEntity> resetRaidStatus() {
        return ResetRaidStatus.create();
    }

    @Info(value = "Creates a `RingBell` behavior")
    public BehaviorControl<LivingEntity> ringBell() {
        return RingBell.create();
    }

    // These may cause problems with B E A N S, but it's a simple fix if so
    @Info(value = "Creates a `SetClosestHomeAsWalkTarget` behavior", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<PathfinderMob> setClosestHomeAsWalkTarget(float speedModifier) {
        return SetClosestHomeAsWalkTarget.create(speedModifier);
    }

    @Info(value = "Creates a `setEntityLookTarget` behavior", params = {
            @Param(name = "predicate", value = "A predicate for valid target entities"),
            @Param(name = "maxDist", value = "The maximum distance a target may be")
    })
    public BehaviorControl<LivingEntity> setEntityLookTarget(Predicate<LivingEntity> predicate, float maxDist) {
        return SetEntityLookTarget.create(predicate, maxDist);
    }

    @Info(value = "Creates a `SetHiddenState` behavior", params = {
            @Param(name = "stayHiddenSeconds", value = "How long the entity should be hidden for"),
            @Param(name = "closeEnoughDist", value = "The distance that is considered close enough to a hiding place")
    })
    public BehaviorControl<LivingEntity> setHiddenState(int stayHiddenSeconds, int closeEnoughDist) {
        return SetHiddenState.create(stayHiddenSeconds, closeEnoughDist);
    }

    @Info(value = "Creates a `SetLookAndInteract` behavior", params = {
            @Param(name = "type", value = "The entity type that the entity interacts with"),
            @Param(name = "interactionRange", value = "The range that the entity will interact with the target")
    })
    public BehaviorControl<LivingEntity> setLookAndInteract(EntityType<?> type, int interactionRange) {
        return SetLookAndInteract.create(type, interactionRange);
    }

    @Info(value = "Creates a `SetRaidStatus` behavior")
    public BehaviorControl<LivingEntity> setRaidStatus() {
        return SetRaidStatus.create();
    }

    @Info(value = "Creates a `SetWalkTargetAwayFrom` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "pWalkTargetAwayFromMemory", value = "The memory type to use as the walk away from target"),
            @Param(name = "pSpeedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "pDesiredDistance", value = "The desired distance away from the target the entity will attempt to be"),
            @Param(name = "pHasTarget", value = "If the entity needs the `minecraft:walk_target` memory type")
    })
    public OneShot<PathfinderMob> setWalkTargetAwayFrom(
            MemoryModuleType<? extends Entity> pWalkTargetAwayFromMemory,
            float pSpeedModifier,
            int pDesiredDistance,
            boolean pHasTarget
    ) {
        return SetWalkTargetAwayFrom.entity(pWalkTargetAwayFromMemory, pSpeedModifier, pDesiredDistance, pHasTarget);
    }

    @Info(value = "Creates a behavior which sets the entity's attack target to its walk target if the target is out of reach", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<Mob> setWalkTargetFromAttackTargetIfTargetOutOfReach(Function<LivingEntity, Float> speedModifier) {
        return SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(speedModifier); // One hell of a name
    }

    @Info(value = "Creates a `SetWalkTargetFromLookTarget` behavior", params = {
            @Param(name = "predicate", value = "The predicate for setting the walk target"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "closeEnoughDistance", value = "The distance that is close enough to the target to stop walking")
    })
    public OneShot<LivingEntity> setWalkTargetFromLookTarget(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> speedModifier, int closeEnoughDistance) {
        return SetWalkTargetFromLookTarget.create(predicate, speedModifier, closeEnoughDistance);
    }

    @Info(value = "Creates a `SleepInBed` behavior")
    public SleepInBed sleepInBed() {
        return new SleepInBed();
    }

    @Info(value = "Creates a `SocializeAtBell` behavior")
    public OneShot<LivingEntity> socializeAtBell() {
        return SocializeAtBell.create();
    }

    @Info(value = "Creates a `StartAttacking` behavior, only applicable to **mob** entities", params = {
            @Param(name = "canAttackPredicate", value = "A predicate for if the mob can attack"),
            @Param(name = "targetFinder", value = "A function that finds a target to attack"),
            @Param(name = "duration", value = "The number of ticks that the behavior should be active for")
    })
    public <E extends Mob> BehaviorControl<E> startAttacking(Predicate<E> canAttackPredicate, Function<E, @Nullable LivingEntity> targetFinder) {
        return StartAttacking.create(canAttackPredicate, e -> Optional.ofNullable(targetFinder.apply(e)));
    }

    @Info(value = "Creates a `StartCelebratingIfTargetDead` behavior", params = {
            @Param(name = "celebrationDuration", value = "The number of ticks the entity should celebrate for"),
            @Param(name = "dancePredicate", value = "A predicate for if the entity should dance. The first entity provided is the entity that will dance, the second is the target")
    })
    public BehaviorControl<LivingEntity> startCelebratingIfTargetDead(int celebrationDuration, BiPredicate<LivingEntity, LivingEntity> dancePredicate) {
        return StartCelebratingIfTargetDead.create(celebrationDuration, dancePredicate);
    }

    @Info(value = "Creates a `BlockPosTracker` for use in `.stayCloseToTarget()`", params = {
            @Param(name = "pos", value = "THe position that is to be tracked")
    })
    public BlockPosTracker blockPosTracker(BlockPos pos) {
        return new BlockPosTracker(pos);
    }

    @Info(value = "Creates an `EntityTracker` for use in `.stayCloseToTarget()`", params = {
            @Param(name = "entity", value = "The target entity"),
            @Param(name = "trackEyeHeight", value = "If the eye height of the target should be considered")
    })
    public EntityTracker entityPosTracker(Entity entity, boolean trackEyeHeight) {
        return new EntityTracker(entity, trackEyeHeight);
    }

    @Info(value = "Creates a `StayCloseToTarget` behavior", params = {
            @Param(name = "targetPositionTracker", value = "A function that returns the position tracker for the entity, the returned tracker may be null, see `.blockPosTracker()` and `.entityPosTracker()`"),
            @Param(name = "pPredicate", value = "The predicate to use with the living Entity as an argument"),
            @Param(name = "closeEnough", value = "The distance that is close enough to the target"),
            @Param(name = "tooFar", value = "The distance that is too far from the target"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<LivingEntity> stayCloseToTarget(Function<LivingEntity, @Nullable PositionTracker> targetPositionGetter, Predicate<LivingEntity> pPredicate, int closeEnough, int tooFar, float speedModifier) {
        return StayCloseToTarget.create(e -> Optional.ofNullable(targetPositionGetter.apply(e)), pPredicate, closeEnough, tooFar, speedModifier);
    }

    @Info(value = "Creates a `StopAttackingIfTargetInvalid` behavior, only applicable to **mob** entities", params = {
            @Param(name = "stopAttackingWhen", value = "A predicate for when the target is no longer valid"),
            @Param(name = "onTargetErased", value = "Actions that should be performed when the attack target is cleared, the first entity is the attacker and the second is the target"),
            @Param(name = "canGetTiredOfTryingToReachTarget", value = "If the attacker can get tired of trying to reach its target")
    })
    public <E extends Mob> BehaviorControl<E> stopAttackingIfTargetInvalid(Predicate<LivingEntity> stopAttackingWhen, BiConsumer<E, LivingEntity> onTargetErased, boolean canGetTiredOfTryingToReachTarget) {
        return StopAttackingIfTargetInvalid.create(stopAttackingWhen, onTargetErased, canGetTiredOfTryingToReachTarget);
    }

    @Info(value = "Creates a `StopBeingAngryIfTargetDead` behavior, only applicable to **mob** entities")
    public BehaviorControl<LivingEntity> stopBeingAngryIfTargetDead() {
        return StopBeingAngryIfTargetDead.create();
    }

    @Info(value = "Creates a `StrollAroundPoi` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "memoryType", value = "The memory that is used for the poi"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "maxDistanceFromPoi", value = "The maximum distance away from the poi that the mob may go while strolling")
    })
    public OneShot<PathfinderMob> strollAroundPoi(MemoryModuleType<GlobalPos> memoryType, float speedModifier, int maxDistanceFromPoi) {
        return StrollAroundPoi.create(memoryType, speedModifier, maxDistanceFromPoi);
    }

    @Info(value = "Creates a `StrollToPoi` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "memoryType", value = "The memory that is used for the poi"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "closeEnoughDist", value = "The distance that is considered close enough to the poi"),
            @Param(name = "maxDistanceFromPoi", value = "The maximum distance away from the poi that this behavior will apply")
    })
    public BehaviorControl<PathfinderMob> strollToPoi(MemoryModuleType<GlobalPos> memoryType, float speedModifier, int closeEnoughDist, int maxDistanceFromPoi) {
        return StrollToPoi.create(memoryType, speedModifier, closeEnoughDist, maxDistanceFromPoi);
    }

    @Info(value = "Creates a `Swim` behavior, only applicable to **mob** entities", params = {
            @Param(name = "chance", value = "The chance the mob will move upwards during a tick. Range: [0, 1]")
    })
    public Swim swim(float chance) {
        return new Swim(chance);
    }

    @Info(value = "Creates a `TryFindLand` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "range", value = "The range, in all directions, at which the mob will search for land"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<PathfinderMob> tryFindLand(int range, float speedModifier) {
        return TryFindLand.create(range, speedModifier);
    }

    @Info(value = "Creates a `TryFindLandNearWater` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "range", value = "The range, in all directions, at which the mob will search for land"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<PathfinderMob> tryFindLandNearWater(int range, float speedModifier) {
        return TryFindLandNearWater.create(range, speedModifier);
    }

    @Info(value = "Creates a `TryFindWater` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "range", value = "The range, in all directions, at which the mob will search for land"),
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active")
    })
    public BehaviorControl<PathfinderMob> tryFindWater(int range, float speedModifier) {
        return TryFindWater.create(range, speedModifier);
    }

    @Info(value = "Creates a `UpdateActivityFromSchedule` behavior")
    public BehaviorControl<LivingEntity> updateActivityFromSchedule() {
        return UpdateActivityFromSchedule.create();
    }

    @Info(value = "Creates a `ValidateNearbyPoi` behavior", params = {
            @Param(name = "poiPredicate", value = "The predicate that is used to validate the poi"),
            @Param(name = "memoryType", value = "The memory that is used for the poi")
    })
    public BehaviorControl<LivingEntity> validateNearbyPoi(Predicate<Holder<PoiType>> poiPredicate, MemoryModuleType<GlobalPos> memoryType) {
        return ValidateNearbyPoi.create(poiPredicate, memoryType);
    }

    @Info(value = "Creates a `VillageBoundRandomStroll` behavior, only applicable to **pathfinder** mobs", params = {
            @Param(name = "speedModifier", value = "The modifier to the mob's speed when this behavior is active"),
            @Param(name = "radius", value = "The radius around the village the mob will stroll"),
            @Param(name = "maxyDist", value = "The vertical range the mob will wander in")
    })
    public OneShot<PathfinderMob> villageBoundRandomStroll(float speedModifier, int radius, int maxYDist) {
        return VillageBoundRandomStroll.create(speedModifier, radius, maxYDist);
    }

    @Info(value = "Creates a `WakeUp` behavior")
    public BehaviorControl<LivingEntity> wakeUp() {
        return WakeUp.create();
    }
}
