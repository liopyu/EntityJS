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
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

// I am not wrapping all of those supplier arguments with ResourceLocation backed getters
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
}
