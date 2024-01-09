package net.liopyu.entityjs.util.ai;

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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.*;

@SuppressWarnings("unused")
public enum Behaviors {
    INSTANCE;

    public AcquirePoi acquirePoi(Predicate<Holder<PoiType>> poiType, MemoryModuleType<GlobalPos> entryCondition, MemoryModuleType<GlobalPos> memoryToAcquire, boolean onlyIfAdult, @Nullable Byte onPoiAcquisitionEvent) {
        return new AcquirePoi(poiType, entryCondition, memoryToAcquire, onlyIfAdult, Optional.ofNullable(onPoiAcquisitionEvent));
    }

    public AnimalMakeLove animalMakeLove(EntityType<? extends Animal> partnerType, float speedModifier) {
        return new AnimalMakeLove(partnerType, speedModifier);
    }

    public AnimalPanic animalPanic(float speedMultiplier) {
        return new AnimalPanic(speedMultiplier);
    }

    public <E extends AgeableMob> BabyFollowAdult<E> babyFollowAdult(int minFollowRange, int maxFollowRange, Function<LivingEntity, Float> speedModifier) {
        return new BabyFollowAdult<>(UniformInt.of(minFollowRange, maxFollowRange), speedModifier);
    }

    public CountDownCooldownTicks countDownCooldownTicks(MemoryModuleType<Integer> coolDownTicks) {
        return new CountDownCooldownTicks(coolDownTicks);
    }

    public <E extends LivingEntity, T extends Entity> DismountOrSkipMounting<E, T> dismountOrSkipMounting(int maxWalkDistToRideTarget, BiPredicate<E, Entity> dontRideIf) {
        return new DismountOrSkipMounting<>(maxWalkDistToRideTarget, dontRideIf);
    }

    public FlyingRandomStroll flyingRandomStroll(float speedModifier, boolean mayStrollFromWater) {
        return new FlyingRandomStroll(speedModifier, mayStrollFromWater);
    }

    public FollowTemptation followTemptation(Function<LivingEntity, Float> speedModifier) {
        return new FollowTemptation(speedModifier);
    }

    public ForceUnmount forceUnmount() {
        return new ForceUnmount();
    }

    public MoveToSkySeeingSpot moveToSkySeeingSpot(float speedModifier) {
        return new MoveToSkySeeingSpot(speedModifier);
    }

    public <E extends Mob> GoToTargetLocation<E> gotoTargetLocation(MemoryModuleType<BlockPos> locationMemory, int closeEnoughDistance, float speedModifier) {
        return new GoToTargetLocation<>(locationMemory, closeEnoughDistance, speedModifier);
    }

    public <E extends LivingEntity> GoToWantedItem<E> goToWantedItem(Predicate<E> predicate, float speedModifier, int maxDistToWalk, boolean hasWalkTargetMemoryModuleType) {
        return new GoToWantedItem<>(predicate, speedModifier, hasWalkTargetMemoryModuleType, maxDistToWalk);
    }

    public InsideBrownianWalk insideBrownianWalk(float speedModifier) {
        return new InsideBrownianWalk(speedModifier);
    }

    public <E extends LivingEntity, T extends LivingEntity> InteractWith<E, T> interactWith(
            EntityType<? extends T> typeToInteractWith,
            int interactionRange,
            Predicate<E> selfFilter,
            Predicate<T> targetFilter,
            MemoryModuleType<T> memory,
            float speedModifier,
            int maxDistance
    ) {
        return new InteractWith<>(
                typeToInteractWith,
                interactionRange,
                selfFilter,
                targetFilter,
                memory,
                speedModifier,
                maxDistance
        );
    }

    public InteractWithDoor interactWithDoor() {
        return new InteractWithDoor();
    }

    public JumpOnBed jumpOnBed(float speedModifier) {
        return new JumpOnBed(speedModifier);
    }

    public LocateHidingPlace locateHidingPlace(int radius, float speedModifier, int closeEnoughDistance) {
        return new LocateHidingPlace(radius, speedModifier, closeEnoughDistance);
    }

    public LongJumpMidJump longJumpMidJump(int minTicksBetweenJumps, int maxTicksBetweenJumps, SoundEvent landingSound) {
        return new LongJumpMidJump(UniformInt.of(minTicksBetweenJumps, maxTicksBetweenJumps), landingSound);
    }

    public BecomePassiveIfMemoryPresent becomePassiveIfMemoryPresent(MemoryModuleType<?> memoryType, int pacifyDuration) {
        return new BecomePassiveIfMemoryPresent(memoryType, pacifyDuration);
    }

    public DoNothing doNothing(int minTime, int maxTime) {
        return new DoNothing(minTime, maxTime);
    }

    public <E extends LivingEntity> EraseMemoryIf<E> eraseMemoryIf(Predicate<E> predicate, MemoryModuleType<?> memoryType) {
        return new EraseMemoryIf<>(predicate, memoryType);
    }

    public <E extends Mob> BackUpIfTooClose<E> backUpIfTooClose(int tooCloseDistance, float strafeSpeed) {
        return new BackUpIfTooClose<>(tooCloseDistance, strafeSpeed);
    }

    public <E extends Mob> LongJumpToPreferredBlock<E> longJumpToPreferredBlock(int minTimeBetweenJumps, int maxTimeBetweenJumps, int maxJumpHeight, int maxJumpWidth, float maxJumpVelocity, Function<E, SoundEvent> jumpSound, ResourceLocation preferredBlockTag, float preferredBlockChance, Predicate<BlockState> acceptableLandingSpot) {
        return new LongJumpToPreferredBlock<>(UniformInt.of(minTimeBetweenJumps, maxTimeBetweenJumps), maxJumpHeight, maxJumpWidth, maxJumpVelocity, jumpSound, TagKey.create(Registry.BLOCK_REGISTRY, preferredBlockTag), preferredBlockChance, acceptableLandingSpot);
    }

    public <E extends Mob> LongJumpToRandomPos<E> longJumpToRandomPos(int minTimeBetweenJumps, int maxTimeBetweenJumps, int maxJumpHeight, int maxJumpWidth, float maxJumpVelocity, Function<E, SoundEvent> jumpSound, Predicate<BlockState> acceptableLandingSpot) {
        return new LongJumpToRandomPos<>(UniformInt.of(minTimeBetweenJumps, maxTimeBetweenJumps), maxJumpHeight, maxJumpWidth, maxJumpVelocity, jumpSound, acceptableLandingSpot);
    }

    public LookAtTargetSink lookAtTargetSink(int minDuration, int maxDuration) {
        return new LookAtTargetSink(minDuration, maxDuration);
    }

    public MeleeAttack meleeAttack(int attackCooldown) {
        return new MeleeAttack(attackCooldown);
    }

    public <E extends LivingEntity> Mount<E> mount(float speedModifier) {
        return new Mount<>(speedModifier);
    }

    public MoveToTargetSink moveToTargetSink(int minDuration, int maxDuration) {
        return new MoveToTargetSink(minDuration, maxDuration);
    }

    public PlayTagWithOtherKids playTagWithOtherKids() {
        return new PlayTagWithOtherKids();
    }

    public <E extends PathfinderMob> PrepareRamNearestTarget<E> prepareRamNearestTarget(
            ToIntFunction<E> cooldownOnFall,
            int minRamDistance,
            int maxRamDistance,
            float walkSpeed,
            TargetingConditions targetingConditions,
            int ramPrepareTime,
            Function<E, SoundEvent> getPrepareRamSound
    ) {
        return new PrepareRamNearestTarget<>(
                cooldownOnFall,
                minRamDistance,
                maxRamDistance,
                walkSpeed,
                targetingConditions,
                ramPrepareTime,
                getPrepareRamSound
        );
    }

    public RandomStroll randomStroll(float speedModifier, int maxHorizontalDistance, int maxVerticalDistance, boolean mayStrollFromWater) {
        return new RandomStroll(speedModifier, maxHorizontalDistance, maxVerticalDistance, mayStrollFromWater);
    }

    public RandomSwim randomSwim(float speedModifier) {
        return new RandomSwim(speedModifier);
    }

    public ReactToBell reactToBell() {
        return new ReactToBell();
    }

    public ResetRaidStatus resetRaidStatus() {
        return new ResetRaidStatus();
    }

    public RingBell ringBell() {
        return new RingBell();
    }

    public <E extends LivingEntity> RunIf<E> runIf(Predicate<E> predicate, Behavior<? super E> wrappedbehavior, boolean checkWhileRunningAlso) {
        return new RunIf<>(predicate, wrappedbehavior, checkWhileRunningAlso);
    }

    // RunOne impl, requires a List<Pair<Behavior<? super LivingEntity>, Integer>>

    public <E extends LivingEntity> RunSometimes<E> runSometimes(Behavior<? super E> wrappedBehavior, boolean keepTicks, int minInterval, int maxInterval) {
        return new RunSometimes<>(wrappedBehavior, keepTicks, UniformInt.of(minInterval, maxInterval));
    }

    // These may cause problems with B E A N S but its a simple fix if so
    public SetClosestHomeAsWalkTarget setClosestHomeAsWalkTarget(float speedModifier) {
        return new SetClosestHomeAsWalkTarget(speedModifier);
    }

    public SetEntityLookTarget setEntityLookTarget(Predicate<LivingEntity> predicate, float maxDist) {
        return new SetEntityLookTarget(predicate, maxDist);
    }

    public SetHiddenState setHiddenState(int stayHiddenSeconds, int closeEnoughDist) {
        return new SetHiddenState(stayHiddenSeconds, closeEnoughDist);
    }

    public SetLookAndInteract setLookAndInteract(EntityType<?> type, int interactionRange, Predicate<LivingEntity> selfFilter, Predicate<LivingEntity> targetFilter) {
        return new SetLookAndInteract(type, interactionRange, selfFilter, targetFilter);
    }

    public SetRaidStatus setRaidStatus() {
        return new SetRaidStatus();
    }

    public <T> SetWalkTargetAwayFrom<T> setWalkTargetAwayFrom(MemoryModuleType<T> walkTargetAwayFromMemory, float speedModifier, int desiredDist, boolean hasTarget, Function<T, Vec3> toPosition) {
        return new SetWalkTargetAwayFrom<>(walkTargetAwayFromMemory, speedModifier, desiredDist, hasTarget, toPosition);
    }

    public SetWalkTargetFromAttackTargetIfTargetOutOfReach setWalkTargetFromAttackTargetIfTargetOutOfReach(Function<LivingEntity, Float> speedModifier) {
        return new SetWalkTargetFromAttackTargetIfTargetOutOfReach(speedModifier); // One hell of a name
    }

    public SetWalkTargetFromBlockMemory setWalkTargetFromBlockMemory(MemoryModuleType<GlobalPos> memoryType, float speedModifier, int closeEnoughDist, int tooFarDistance, int tooLongUnreachableDuration) {
        return new SetWalkTargetFromBlockMemory(memoryType, speedModifier, closeEnoughDist, tooFarDistance, tooLongUnreachableDuration);
    }

    public SetWalkTargetFromLookTarget setWalkTargetFromLookTarget(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> speedModifier, int closeEnoughDistance) {
        return new SetWalkTargetFromLookTarget(predicate, speedModifier, closeEnoughDistance);
    }

    public SleepInBed sleepInBed() {
        return new SleepInBed();
    }

    public SocializeAtBell socializeAtBell() {
        return new SocializeAtBell();
    }

    public <E extends Mob> StartAttacking<E> startAttacking(Predicate<E> canAttackPredicate, Function<E, @Nullable LivingEntity> targetFinder, int duration) {
        return new StartAttacking<>(canAttackPredicate, e -> Optional.ofNullable(targetFinder.apply(e)), duration);
    }

    public StartCelebratingIfTargetDead startCelebratingIfTargetDead(int celebrationDuration, BiPredicate<LivingEntity, LivingEntity> dancePredicate) {
        return new StartCelebratingIfTargetDead(celebrationDuration, dancePredicate);
    }

    public <E extends LivingEntity> StayCloseToTarget<E> stayCloseToTarget(Function<LivingEntity, @Nullable PositionTracker> targetPositionGetter, int closeEnough, int tooFar, float speedModifier) {
        return new StayCloseToTarget<>(e -> Optional.ofNullable(targetPositionGetter.apply(e)), closeEnough, tooFar, speedModifier);
    }

    public <E extends Mob> StopAttackingIfTargetInvalid<E> stopAttackingIfTargetInvalid(Predicate<LivingEntity> stopAttackingWhen, BiConsumer<E, LivingEntity> onTargetErased, boolean canGetTiredOfTryingToReachTarget) {
        return new StopAttackingIfTargetInvalid<>(stopAttackingWhen, onTargetErased, canGetTiredOfTryingToReachTarget);
    }

    public <E extends Mob> StopBeingAngryIfTargetDead<E> stopBeingAngryIfTargetDead() {
        return new StopBeingAngryIfTargetDead<>();
    }

    public StrollAroundPoi strollAroundPoi(MemoryModuleType<GlobalPos> memoryType, float speedModifier, int maxDistanceFromPoi) {
        return new StrollAroundPoi(memoryType, speedModifier, maxDistanceFromPoi);
    }

    public StrollToPoi strollToPoi(MemoryModuleType<GlobalPos> memoryType, float speedModifier, int closeEnoughDist, int maxDistanceFromPoi) {
        return new StrollToPoi(memoryType, speedModifier, closeEnoughDist, maxDistanceFromPoi);
    }

    public StrollToPoiList strollToPoiList(MemoryModuleType<List<GlobalPos>> strollMemoryType, float speedModifier, int closeEnoughDist, int maxDistanceFromPoi, MemoryModuleType<GlobalPos> mustBeCloseToMemoryType) {
        return new StrollToPoiList(strollMemoryType, speedModifier, closeEnoughDist, maxDistanceFromPoi, mustBeCloseToMemoryType);
    }

    public Swim swim(float chance) {
        return new Swim(chance);
    }
}
