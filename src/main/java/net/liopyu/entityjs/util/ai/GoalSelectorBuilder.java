package net.liopyu.entityjs.util.ai;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class GoalSelectorBuilder<T extends Mob> {

    private final List<Pair<Integer, Function<T, Goal>>> goalSuppliers;


    public GoalSelectorBuilder() {
        this.goalSuppliers = new ArrayList<>();
    }

    @Info(value = """
            Enables the addition of arbitrary goals to an entity
            
            It is the responsibility of the user to ensure the goal is
            compatible with the entity
            
            Example of usage:
            =====================================
            builder.arbitraryGoal(3, entity -> new $PathFindToRaidGoal(entity))
            =====================================
            
            Note in the example the entity must be an instance of Raider
            """, params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "goalSupplier", value = "The goal supplier, a function that takes a Mob and returns a Goal")
    })
    public GoalSelectorBuilder<T> arbitraryGoal(int priority, Function<T, Goal> goalSupplier) {
        goalSuppliers.add(new Pair<>(priority, goalSupplier));
        return this;
    }

    // Only usable by pathfinder mobs
    public <E extends LivingEntity> GoalSelectorBuilder<T> avoidEntity(int priority, Class<E> entityClassToAvoid, Predicate<LivingEntity> avoidPredicate, float maxDist, double walkSpeedModifier, double sprintSpeedModifier, Predicate<LivingEntity> onAvoidEntityPredicate) {
        goalSuppliers.add(new Pair<>(priority, t -> new AvoidEntityGoal<>((PathfinderMob) t, entityClassToAvoid, avoidPredicate, maxDist, walkSpeedModifier, sprintSpeedModifier, onAvoidEntityPredicate)));
        return this;
    }

    public GoalSelectorBuilder<T> breakDoor(int priority, int doorBreakTime, Predicate<Difficulty> validDifficulties) {
        goalSuppliers.add(new Pair<>(priority, t -> new BreakDoorGoal(t, doorBreakTime, validDifficulties)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> breathAir(int priority) {
        goalSuppliers.add(new Pair<>(priority, t -> new BreathAirGoal((PathfinderMob) t)));
        return this;
    }

    // Only usable by animal mobs
    public GoalSelectorBuilder<T> breed(int priority, double speedModifier, @Nullable Class<? extends Animal> partnerClass) {
        goalSuppliers.add(new Pair<>(priority, t -> new BreedGoal((Animal) t, speedModifier, partnerClass != null ? partnerClass : (Class<? extends Animal>) t.getClass())));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> removeBlock(int priority, ResourceLocation block, double speedModifier, int verticalSearchRange) {
        goalSuppliers.add(new Pair<>(priority, t -> new RemoveBlockGoal(Registry.BLOCK.get(block), (PathfinderMob) t, speedModifier, verticalSearchRange)));
        return this;
    }

    // This requires a Level in is constructor WTF mojank
    // public GoalSelectorBuilder<T> climbOnTopOfPowderedSnow(int priority, )

    public <E extends LivingEntity> GoalSelectorBuilder<T> nearestAttackableTarget(int priority, Class<E> targetClass, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetConditions) {
        goalSuppliers.add(new Pair<>(priority, t -> new NearestAttackableTargetGoal<>(t, targetClass, randomInterval, mustSee, mustReach, targetConditions)));
        return this;
    }

    public GoalSelectorBuilder<T> eatGrass(int priority) {
        goalSuppliers.add(new Pair<>(priority, EatBlockGoal::new)); // This is only used by sheep and is hardcoded to grass blocks
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> meleeAttack(int priority, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        goalSuppliers.add(new Pair<>(priority, t -> new MeleeAttackGoal((PathfinderMob) t, speedModifier, followingTargetEvenIfNotSeen)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> fleeSun(int priority, double speedModifier) {
        goalSuppliers.add(new Pair<>(priority, t -> new FleeSunGoal((PathfinderMob) t, speedModifier)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> followBoat(int priority) {
        goalSuppliers.add(new Pair<>(priority, t -> new FollowBoatGoal((PathfinderMob) t)));
        return this;
    }

    public GoalSelectorBuilder<T> followMob(int priority, double speedModifier, float stopDistance, float areaSize) {
        goalSuppliers.add(new Pair<>(priority, t -> new FollowMobGoal(t, speedModifier, stopDistance, areaSize)));
        return this;
    }

    // Only usable by tamable animals
    public GoalSelectorBuilder<T> followOwner(int priority, double speedModifier, float startDistance, float stopDistance, boolean canFly) {
        goalSuppliers.add(new Pair<>(priority, t -> new FollowOwnerGoal((TamableAnimal) t, speedModifier, startDistance, stopDistance, canFly)));
        return this;
    }

    // Only usable by animal mobs
    public GoalSelectorBuilder<T> followParent(int priority, double speedModifier) {
        goalSuppliers.add(new Pair<>(priority, t -> new FollowParentGoal((Animal) t, speedModifier)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> hurtByTarget(int priority, List<Class<?>> toIgnoreDamage, boolean alertOthers, List<Class<?>> toIgnoreAlert) {
        goalSuppliers.add(new Pair<>(priority, t -> {
            var goal = new HurtByTargetGoal((PathfinderMob) t, toIgnoreDamage.toArray(new Class<?>[0]));
            if (alertOthers) {
                goal.setAlertOthers(toIgnoreAlert.toArray(new Class<?>[0]));
            }
            return goal;
        }));
        return this;
    }

    public <E extends LivingEntity> GoalSelectorBuilder<T> lookAtEntity(int priority, Class<E> targetClass, float lookDistance, float probability, boolean onlyHorizontal) {
        goalSuppliers.add(new Pair<>(priority, t -> new LookAtPlayerGoal(t, targetClass, lookDistance, probability, onlyHorizontal)));
        return this;
    }

    public GoalSelectorBuilder<T> leapAtTarget(int priority, float deltaY) {
        goalSuppliers.add(new Pair<>(priority, t -> new LeapAtTargetGoal(t, deltaY)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> randomStroll(int priority, double speedModifier, int interval, boolean checkNoActionTime) {
        goalSuppliers.add(new Pair<>(priority, t -> new RandomStrollGoal((PathfinderMob) t, speedModifier, interval, checkNoActionTime)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> moveBackToVillage(int priority, double speedModifier, boolean checkNoActionTime) {
        goalSuppliers.add(new Pair<>(priority, t -> new MoveBackToVillageGoal((PathfinderMob) t, speedModifier, checkNoActionTime)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> moveThroughVillage(int priority, double speedModifier, boolean onlyAtNight, int distanceToPoi, Supplier<Boolean> canDealWithDoors) {
        goalSuppliers.add(new Pair<>(priority, t -> new MoveThroughVillageGoal((PathfinderMob) t, speedModifier, onlyAtNight, distanceToPoi, canDealWithDoors::get))); // BooleanSupplier is not nice for generalized supplier options (Forge config values / ConfigJS)
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> moveTowardsRestriction(int priority, double speedModifier) {
        goalSuppliers.add(new Pair<>(priority, t -> new MoveTowardsRestrictionGoal((PathfinderMob) t, speedModifier)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> moveTowardsTarget(int priority, double speedModifier, float distanceWithin) {
        goalSuppliers.add(new Pair<>(priority, t -> new MoveTowardsTargetGoal((PathfinderMob) t, speedModifier, distanceWithin)));
        return this;
    }

    public <E extends LivingEntity> GoalSelectorBuilder<T> nonTameRandomTarget(int priority, Class<E> targetClass, boolean mustSee, @Nullable Predicate<LivingEntity> targetCondition) {
        goalSuppliers.add(new Pair<>(priority, t -> new NonTameRandomTargetGoal<>((TamableAnimal) t, targetClass, mustSee, targetCondition)));
        return this;
    }

    public GoalSelectorBuilder<T> ocelotAttack(int priority) {
        goalSuppliers.add(new Pair<>(priority, OcelotAttackGoal::new)); // Its named that way for legacy reasons I assume
        return this;
    }

    public GoalSelectorBuilder<T> openDoor(int priority, boolean closeDoor) {
        goalSuppliers.add(new Pair<>(priority, t -> new OpenDoorGoal(t, closeDoor)));
        return this;
    }

    // Only usable by tamable mobs
    public GoalSelectorBuilder<T> ownerHurtByTarget(int priority) {
        goalSuppliers.add(new Pair<>(priority, t -> new OwnerHurtByTargetGoal((TamableAnimal) t)));
        return this;
    }

    // Only usable by tamable mobs
    public GoalSelectorBuilder<T> ownerHurtTarget(int priority) {
        goalSuppliers.add(new Pair<>(priority, t -> new OwnerHurtTargetGoal((TamableAnimal) t)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> panic(int priority, double speedModifier) {
        goalSuppliers.add(new Pair<>(priority, t -> new PanicGoal((PathfinderMob) t, speedModifier)));
        return this;
    }

    // Must implement NeutralMob
    public <E extends Mob & NeutralMob> GoalSelectorBuilder<T> resetUniversalAngerTarget(int priority, boolean alertOthersOfSameType) {
        goalSuppliers.add(new Pair<>(priority, t -> new ResetUniversalAngerTargetGoal<>((E) t, alertOthersOfSameType)));
        return this;
    }

    public GoalSelectorBuilder<T> randomLookAround(int priority) {
        goalSuppliers.add(new Pair<>(priority, RandomLookAroundGoal::new));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> randomSwimming(int priority, double speedModifier, int interval) {
        goalSuppliers.add(new Pair<>(priority, t -> new RandomSwimmingGoal((PathfinderMob) t, speedModifier, interval)));
        return this;
    }

    // Must implement RangedAttackMob
    public <E extends Mob & RangedAttackMob> GoalSelectorBuilder<T> rangedAttack(int priority, double speedModifier, int attackIntervalMin, int attackIntervalMax, float attackRadius) {
        goalSuppliers.add(new Pair<>(priority, t -> new RangedAttackGoal((E) t, speedModifier, attackIntervalMin, attackIntervalMax, attackIntervalMax)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> restrictSun(int priority) {
        goalSuppliers.add(new Pair<>(priority, t -> new RestrictSunGoal((PathfinderMob) t)));
        return this;
    }

    // Only usable by horses, for reasons...
    public GoalSelectorBuilder<T> horseRunAroundLikeCrazy(int priority, double speedModifier) {
        goalSuppliers.add(new Pair<>(priority, t -> new RunAroundLikeCrazyGoal((AbstractHorse) t, speedModifier)));
        return this;
    }

    // Only usable by tamable mobs
    public GoalSelectorBuilder<T> sitWhenOrdered(int priority) {
        goalSuppliers.add(new Pair<>(priority, t -> new SitWhenOrderedToGoal((TamableAnimal) t)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> strollThroughVillage(int priority, int interval) {
        goalSuppliers.add(new Pair<>(priority, t -> new StrollThroughVillageGoal((PathfinderMob) t, interval)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> tempt(int priority, double speedModifier, Ingredient temptItems, boolean canScare) {
        goalSuppliers.add(new Pair<>(priority, t -> new TemptGoal((PathfinderMob) t, speedModifier, temptItems, canScare)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> tryFindWater(int priority) {
        goalSuppliers.add(new Pair<>(priority, t -> new TryFindWaterGoal((PathfinderMob) t)));
        return this;
    }

    public GoalSelectorBuilder<T> useItem(int priority, ItemStack itemToUse, @Nullable ResourceLocation soundEvent, Predicate<T> canUseSelector) {
        goalSuppliers.add(new Pair<>(priority, t -> new UseItemGoal<>(t, itemToUse, soundEvent == null ? null : Registry.SOUND_EVENT.get(soundEvent), canUseSelector))); // I like this one, interesting function and not stupidly restricted, mojang please more of these :)
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> waterAvoidingRandomFlying(int priority, double speedModifier) {
        goalSuppliers.add(new Pair<>(priority, t -> new WaterAvoidingRandomFlyingGoal((PathfinderMob) t,speedModifier)));
        return this;
    }

    // Only usable by pathfinder mobs
    public GoalSelectorBuilder<T> waterAvoidingRandomStroll(int priority, double speedModifier, float probability) {
        goalSuppliers.add(new Pair<>(priority, t -> new WaterAvoidingRandomStrollGoal((PathfinderMob) t, speedModifier, probability)));
        return this;
    }

    @HideFromJS
    public void apply(GoalSelector goalSelector, T entity) {
        for (Pair<Integer, Function<T, Goal>> goalSupplier : goalSuppliers) {
            goalSelector.addGoal(goalSupplier.getFirst(), goalSupplier.getSecond().apply(entity));
        }
    }
}
