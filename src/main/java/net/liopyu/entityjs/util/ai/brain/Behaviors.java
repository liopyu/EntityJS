package net.liopyu.entityjs.util.ai.brain;

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

// These are all so awful to work with and expect things to exist,
//   something which is not guaranteed in modded due to deferred
//                         registration!
//
//        I hate every part of this system with a passion
//
//                   Brains were a mistake


// I am not wrapping all of those supplier arguments with ResourceLocation backed getters
@SuppressWarnings("unused")
public enum Behaviors {
    INSTANCE;

    public Supplier<AcquirePoi> aquirePoi(Predicate<Holder<PoiType>> poiType, Supplier<MemoryModuleType<GlobalPos>> entryCondition, Supplier<MemoryModuleType<GlobalPos>> memoryToAcquire, boolean onlyIfAdult, @Nullable Byte onPoiAcquisitionEvent) {
        return () -> new AcquirePoi(poiType, entryCondition.get(), memoryToAcquire.get(), onlyIfAdult, Optional.ofNullable(onPoiAcquisitionEvent));
    }

    public Supplier<AnimalMakeLove> animalMakeLove(Supplier<EntityType<? extends Animal>> partnerType, float speedModifier) {
        return () -> new AnimalMakeLove(partnerType.get(), speedModifier);
    }

    public Supplier<AnimalPanic> animalPanic(float speedMultiplier) {
        return () -> new AnimalPanic(speedMultiplier);
    }

    public <E extends AgeableMob> Supplier<BabyFollowAdult<E>> babyFollowAdult(int minFollowRange, int maxFollowRange, Function<LivingEntity, Float> speedModifier) {
        return () -> new BabyFollowAdult<>(UniformInt.of(minFollowRange, maxFollowRange), speedModifier);
    }

    public Supplier<CountDownCooldownTicks> countDownCooldownTicks(Supplier<MemoryModuleType<Integer>> coolDownTicks) {
        return () -> new CountDownCooldownTicks(coolDownTicks.get());
    }

    public <E extends LivingEntity, T extends Entity> Supplier<DismountOrSkipMounting<E, T>> dismountOrSkipMounting(int maxWalkDistToRideTarget, BiPredicate<E, Entity> dontRideIf) {
        return () -> new DismountOrSkipMounting<>(maxWalkDistToRideTarget, dontRideIf);
    }

    public Supplier<FlyingRandomStroll> flyingRandomStroll(float speedModifier, boolean mayStrollFromWater) {
        return () -> new FlyingRandomStroll(speedModifier, mayStrollFromWater);
    }

    public Supplier<FollowTemptation> followTemptation(Function<LivingEntity, Float> speedModifier) {
        return () -> new FollowTemptation(speedModifier);
    }

    public Supplier<ForceUnmount> forceUnmount() {
        return ForceUnmount::new;
    }

    public Supplier<MoveToSkySeeingSpot> moveToSkySeeingSpot(float speedModifier) {
        return () -> new MoveToSkySeeingSpot(speedModifier);
    }

    public <E extends Mob> Supplier<GoToTargetLocation<E>> gotoTargetLocation(Supplier<MemoryModuleType<BlockPos>> locationMemory, int closeEnoughDistance, float speedModifier) {
        return () -> new GoToTargetLocation<>(locationMemory.get(), closeEnoughDistance, speedModifier);
    }

    public <E extends LivingEntity> Supplier<GoToWantedItem<E>> goToWantedItem(Predicate<E> predicate, float speedModifier, int maxDistToWalk, boolean hasWalkTargetMemoryModuleType) {
        return () -> new GoToWantedItem<>(predicate, speedModifier, hasWalkTargetMemoryModuleType, maxDistToWalk);
    }

    public Supplier<InsideBrownianWalk> insideBrownianWalk(float speedModifier) {
        return () -> new InsideBrownianWalk(speedModifier);
    }

    public <E extends LivingEntity, T extends LivingEntity> Supplier<InteractWith<E, T>> interactWith(
            Supplier<EntityType<? extends T>> typeToInteractWith,
            int interactionRange,
            Predicate<E> selfFilter,
            Predicate<T> targetFilter,
            Supplier<MemoryModuleType<T>> memory,
            float speedModifier,
            int maxDistance
    ) {
        return () -> new InteractWith<>(
                typeToInteractWith.get(),
                interactionRange,
                selfFilter,
                targetFilter,
                memory.get(),
                speedModifier,
                maxDistance
        );
    }

    public Supplier<InteractWithDoor> interactWithDoor() {
        return InteractWithDoor::new;
    }

    public Supplier<JumpOnBed> jumpOnBed(float speedModifier) {
        return () -> new JumpOnBed(speedModifier);
    }

    public Supplier<LocateHidingPlace> locateHidingPlace(int radius, float speedModifier, int closeEnoughDistance) {
        return () -> new LocateHidingPlace(radius, speedModifier, closeEnoughDistance);
    }

    public Supplier<LongJumpMidJump> longJumpMidJump(int minTicksBetweenJumps, int maxTicksBetweenJumps, Supplier<SoundEvent> landingSound) {
        return () -> new LongJumpMidJump(UniformInt.of(minTicksBetweenJumps, maxTicksBetweenJumps), landingSound.get());
    }

    public Supplier<BecomePassiveIfMemoryPresent> becomePassiveIfMemoryPresent(Supplier<MemoryModuleType<?>> memoryType, int pacifyDuration) {
        return () -> new BecomePassiveIfMemoryPresent(memoryType.get(), pacifyDuration);
    }

    public Supplier<DoNothing> doNothing(int minTime, int maxTime) {
        return () -> new DoNothing(minTime, maxTime);
    }

    public <E extends LivingEntity> Supplier<EraseMemoryIf<E>> eraseMemoryIf(Predicate<E> predicate, Supplier<MemoryModuleType<?>> memoryType) {
        return () -> new EraseMemoryIf<>(predicate, memoryType.get());
    }

    public <E extends Mob> Supplier<BackUpIfTooClose<E>> backUpIfTooClose(int tooCloseDistance, float strafeSpeed) {
        return () -> new BackUpIfTooClose<>(tooCloseDistance, strafeSpeed);
    }

    public <E extends Mob> Supplier<LongJumpToPreferredBlock<E>> longJumpToPreferredBlock(int minTimeBetweenJumps, int maxTimeBetweenJumps, int maxJumpHeight, int maxJumpWidth, float maxJumpVelocity, Function<E, SoundEvent> jumpSound, ResourceLocation preferredBlockTag, float preferredBlockChance, Predicate<BlockState> acceptableLandingSpot) {
        return () -> new LongJumpToPreferredBlock<>(UniformInt.of(minTimeBetweenJumps, maxTimeBetweenJumps), maxJumpHeight, maxJumpWidth, maxJumpVelocity, jumpSound, TagKey.create(Registry.BLOCK_REGISTRY, preferredBlockTag), preferredBlockChance, acceptableLandingSpot);
    }

    public <E extends Mob> Supplier<LongJumpToRandomPos<E>> longJumpToRandomPos(int minTimeBetweenJumps, int maxTimeBetweenJumps, int maxJumpHeight, int maxJumpWidth, float maxJumpVelocity, Function<E, SoundEvent> jumpSound, Predicate<BlockState> acceptableLandingSpot) {
        return () -> new LongJumpToRandomPos<>(UniformInt.of(minTimeBetweenJumps, maxTimeBetweenJumps), maxJumpHeight, maxJumpWidth, maxJumpVelocity, jumpSound, acceptableLandingSpot);
    }

    public Supplier<LookAtTargetSink> lookAtTargetSink(int minDuration, int maxDuration) {
        return () -> new LookAtTargetSink(minDuration, maxDuration);
    }

    public Supplier<MeleeAttack> meleeAttack(int attackCooldown) {
        return () -> new MeleeAttack(attackCooldown);
    }

    public <E extends LivingEntity> Supplier<Mount<E>> mount(float speedModifier) {
        return () -> new Mount<>(speedModifier);
    }

    public Supplier<MoveToTargetSink> moveToTargetSink(int minDuration, int maxDuration) {
        return () -> new MoveToTargetSink(minDuration, maxDuration);
    }

    public Supplier<PlayTagWithOtherKids> playTagWithOtherKids() {
        return PlayTagWithOtherKids::new;
    }
}
