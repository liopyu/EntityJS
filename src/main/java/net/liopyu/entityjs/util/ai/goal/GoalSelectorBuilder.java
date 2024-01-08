package net.liopyu.entityjs.util.ai.goal;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This is how {@link Goal}s are added to a mob's {@code goalSelector}. Some
 * basic goals are provided as well as the ability to arbitrary and custom
 * goals, but if you want to add more goal types, say those added by another
 * mod, then a mixin that adds a method(s) to this class is required. In
 * addition, if you wish to have the method documented with the {@link @Info}
 * annotation, a duck interface will be required.<br><br>
 *
 * An example of doing so:
 * <pre>{@code
 * @Mixin(GoalSelectorBuilder.class)
 * public abstract class GoalSelectorBuilderMixin<T extends Mob> extends GoalBuilder<T> implements IGoalSelectorBuilderMixin {
 *
 *     private GoalSelectorBuilder<T> self() {
 *         return (GoalSelectorBuilder) (Object) this;
 *     }
 *
 *     @Override
 *     public GoalSelectorBuilder<T> modid$moddedGoal(int priority, double distanceToSky, String chatter) {
 *         suppliers.add(new Pair<>(priority, t -> new CoolModdedGoal(t, distanceToSky, chatter)));
 *         return self();
 *     }
 * }
 *
 * public interface IGoalSelectorBuilderMixin {
 *
 *     @Info(value = "Adds a `CoolModdedGoal` to the entity", params = {
 *         @Param(name = "priority", value = "The priority of the goal"),
 *         @param(name = "distanceToSky", value = "The minimum amount of sky light required"),
 *         @Param(name = "chatter", value = "The messages that should be played around the entity")
 *     })
 *     @RemapForJS("coolModdedGoal")
 *     GoalSelectorBuilder<?> modid$moddedGoal(int priority, double distanceToSky, String chatter);
 * }
 * }</pre>
 */
@SuppressWarnings("unused")
public class GoalSelectorBuilder<T extends Mob> extends GoalBuilder<T> {

    public GoalSelectorBuilder(T entity) {
        super(entity);
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
        suppliers.add(new Pair<>(priority, goalSupplier));
        return this;
    }

    @Info(value = "Adds a custom goal to the entity", params = {
            @Param(name = "name", value = "The name of the custom goal"),
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "canUse", value = "Determines if the entity can use the goal"),
            @Param(name = "canContinueToUse", value = "Determines if the entity can continue to use the goal, may be null"),
            @Param(name = "isInterruptable", value = "If the goal may be interrupted"),
            @Param(name = "start", value = "The action to perform when the goal starts"),
            @Param(name = "stop", value = "The action to perform when the goal stops"),
            @Param(name = "requiresUpdateEveryTick", value = "If the goal needs to be updated every tick"),
            @Param(name = "tick", value = "The action to perform when the goal ticks")
    })
    public GoalSelectorBuilder<T> customGoal(
            String name,
            int priority,
            Predicate<T> canUse,
            @Nullable Predicate<T> canContinueToUse,
            boolean isInterruptable,
            Consumer<T> start,
            Consumer<T> stop,
            boolean requiresUpdateEveryTick,
            Consumer<T> tick
    ) {
        suppliers.add(new Pair<>(priority, t -> new CustomGoal<>(name, t, canUse, canContinueToUse, isInterruptable, start, stop, requiresUpdateEveryTick, tick)));
        return this;
    }

    @Info(value = "Adds a `AvoidEntityGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "entityClassToAvoid", value = "The class of entity to avoid"),
            @Param(name = "avoidPredicate", value = "The conditions under which an entity will be avoided"), // Maybe? It doesn't directly use this
            @Param(name = "maxDist", value = "The maximum distance from a entity the mob will detect and flee from it"),
            @Param(name = "walkSpeedModifier", value = "Modifies the mob's speed when avoiding an entity"),
            @Param(name = "sprintSpeedModifier", value = "Modifies the mob's speed when avoiding an entity at close range"),
            @Param(name = "onAvoidEntityPredicate", value = "An additional predicate for entity avoidance") // Again, maybe? its ANDed with the other one and processed so who knows!
    })
    public <E extends LivingEntity> GoalSelectorBuilder<T> avoidEntity(int priority, Class<E> entityClassToAvoid, Predicate<LivingEntity> avoidPredicate, float maxDist, double walkSpeedModifier, double sprintSpeedModifier, Predicate<LivingEntity> onAvoidEntityPredicate) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new AvoidEntityGoal<>((PathfinderMob) t, entityClassToAvoid, avoidPredicate, maxDist, walkSpeedModifier, sprintSpeedModifier, onAvoidEntityPredicate)));
        }
        return this;
    }

    @Info(value = "Adds a `BreakDoorGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "doorBreakTime", value = "The time it takes to break a door, limited to 240 ticks"), // I think that's what it is
            @Param(name = "validDifficulties", value = "Determines what difficulties are valid for the goal")
    })
    public GoalSelectorBuilder<T> breakDoor(int priority, int doorBreakTime, Predicate<Difficulty> validDifficulties) {
        suppliers.add(new Pair<>(priority, t -> new BreakDoorGoal(t, doorBreakTime, validDifficulties)));
        return this;
    }

    @Info(value = "Adds a `BreathAirGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> breathAir(int priority) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new BreathAirGoal((PathfinderMob) t)));
        }
        return this;
    }

    @Info(value = "Adds a `BreedGoal` to the entity, only applicable to **animal** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "partnerClass", value = "The class of animal that this entity breeds with, may be null to specify it be the same class as this entity")
    })
    public GoalSelectorBuilder<T> breed(int priority, double speedModifier, @Nullable Class<? extends Animal> partnerClass) {
        if (isAnimal) {
            suppliers.add(new Pair<>(priority, t -> new BreedGoal((Animal) t, speedModifier, partnerClass != null ? partnerClass : UtilsJS.cast(t.getClass()))));
        }
        return this;
    }

    @Info(value = "Adds a `RemoveBlockGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "block", value = "The registry name of a block, the block to be removed"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "verticalSearchRange", value = "The vertical range the mob will search for the block")
    })
    public GoalSelectorBuilder<T> removeBlock(int priority, ResourceLocation block, double speedModifier, int verticalSearchRange) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new RemoveBlockGoal(Registry.BLOCK.get(block), (PathfinderMob) t, speedModifier, verticalSearchRange)));
        }
        return this;
    }

    @Info(value = "Adds a `ClimbOnTopOfPowderSnowGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> climbOnTopOfPowderedSnow(int priority) {
        suppliers.add(new Pair<>(priority, t -> new ClimbOnTopOfPowderSnowGoal(t, t.level)));
        return this;
    }

    @Info(value = "Adds a `EatBlockGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> eatGrass(int priority) {
        suppliers.add(new Pair<>(priority, EatBlockGoal::new)); // This is only used by sheep and is hardcoded to grass blocks
        return this;
    }

    @Info(value = "Adds a `MeleeAttackGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "followTargetEventIfNotSeen", value = "Determines if the entity should follow the target even if it doesn't see it")
    })
    public GoalSelectorBuilder<T> meleeAttack(int priority, double speedModifier, boolean followTargetEvenIfNotSeen) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new MeleeAttackGoal((PathfinderMob) t, speedModifier, followTargetEvenIfNotSeen)));
        }
        return this;
    }

    @Info(value = "Adds a `FleeSunGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public GoalSelectorBuilder<T> fleeSun(int priority, double speedModifier) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new FleeSunGoal((PathfinderMob) t, speedModifier)));
        }
        return this;
    }

    @Info(value = "Adds a `FollowBoatGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> followBoat(int priority) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new FollowBoatGoal((PathfinderMob) t)));
        }
        return this;
    }

    // How the hell does this not require a pathfinder mob
    @Info(value = "Adds a `FollowMobGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "stopDistance", value = "The distance away from the target the mob will stop"),
            @Param(name = "areaSize", value = "The distance away from the mob, that will be searched for mobs to follow")
    })
    public GoalSelectorBuilder<T> followMob(int priority, double speedModifier, float stopDistance, float areaSize) {
        suppliers.add(new Pair<>(priority, t -> new FollowMobGoal(t, speedModifier, stopDistance, areaSize)));
        return this;
    }

    @Info(value = "Adds a `FollowOwnerGoal` to the entity, only applicable to **tamable** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "startDistance", value = "The distance away from the owner the mob will start moving"),
            @Param(name = "stopDistance", value = "The distance away from the owner the mob will stop moving"),
            @Param(name = "canFly", value = "If the mob can teleport into leaves") // Yes, this is what it means
    })
    public GoalSelectorBuilder<T> followOwner(int priority, double speedModifier, float startDistance, float stopDistance, boolean canFly) {
        if (isTamable) {
            suppliers.add(new Pair<>(priority, t -> new FollowOwnerGoal((TamableAnimal) t, speedModifier, startDistance, stopDistance, canFly)));
        }
        return this;
    }

    // Only usable by animal mobs
    @Info(value = "Adds a `FollowParentGoal` to the entity, only applicable to **animal** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public GoalSelectorBuilder<T> followParent(int priority, double speedModifier) {
        if (isAnimal) {
            suppliers.add(new Pair<>(priority, t -> new FollowParentGoal((Animal) t, speedModifier)));
        }
        return this;
    }

    @Info(value = "Adds a `LookAtPlayerGoal` to the entity", params = { // Should we lie and say adds a `LookAtEntityGoal`?
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "targetClass", value = "The entity class that should be looked at"),
            @Param(name = "lookDistance", value = "How far away the entity should be looked at"),
            @Param(name = "probability", value = "The probability, in the range [0, 1], that the goal may be used"),
            @Param(name = "onlyHorizontal", value = "Determines if the eye level must be the same to follow the target entity")
    })
    public <E extends LivingEntity> GoalSelectorBuilder<T> lookAtEntity(int priority, Class<E> targetClass, float lookDistance, float probability, boolean onlyHorizontal) {
        suppliers.add(new Pair<>(priority, t -> new LookAtPlayerGoal(t, targetClass, lookDistance, probability, onlyHorizontal)));
        return this;
    }

    @Info(value = "Adds a `LeapAtTargetGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "deltaY", value = "Sets the delta movement of the animal in the y-axis")
    })
    public GoalSelectorBuilder<T> leapAtTarget(int priority, float deltaY) {
        suppliers.add(new Pair<>(priority, t -> new LeapAtTargetGoal(t, deltaY)));
        return this;
    }

    @Info(value = "Adds a `RandomStrollGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "interval", value = "Sets the interval at which the goal will be 'refreshed'"), // I think? It indirectly determines when #canUse() returns false
            @Param(name = "checkNoActionTime", value = "Determines if the mob's noActionTime property should be checked")
    })
    public GoalSelectorBuilder<T> randomStroll(int priority, double speedModifier, int interval, boolean checkNoActionTime) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new RandomStrollGoal((PathfinderMob) t, speedModifier, interval, checkNoActionTime)));
        }
        return this;
    }

    @Info(value = "Adds a `MoveBackToVillageGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "checkNoActionTime", value = "Determines if the mob's noActionTime property should be checked")
    })
    public GoalSelectorBuilder<T> moveBackToVillage(int priority, double speedModifier, boolean checkNoActionTime) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new MoveBackToVillageGoal((PathfinderMob) t, speedModifier, checkNoActionTime)));
        }
        return this;
    }

    @Info(value = "Adds a `MoveThroughVillageGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "onlyAtNight", value = "If this goal should only apply at night"),
            @Param(name = "distanceToPoi", value = "The minimum distance to a poi the mob must be to have it be considered 'visited'"),
            @Param(name = "canDealWithDoors", value = "If doors can be opened to navigate as part of this goal") // Mention supplier somehow?
    })
    public GoalSelectorBuilder<T> moveThroughVillage(int priority, double speedModifier, boolean onlyAtNight, int distanceToPoi, Supplier<Boolean> canDealWithDoors) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new MoveThroughVillageGoal((PathfinderMob) t, speedModifier, onlyAtNight, distanceToPoi, canDealWithDoors::get))); // BooleanSupplier is not nice for generalized supplier options (Forge config values / ConfigJS)
        }
        return this;
    }

    @Info(value = "Adds a `MoveTowardsRestrictionGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public GoalSelectorBuilder<T> moveTowardsRestriction(int priority, double speedModifier) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new MoveTowardsRestrictionGoal((PathfinderMob) t, speedModifier)));
        }
        return this;
    }

    @Info(value = "Adds a `MoveTowardsTargetGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "distanceWithin", value = "The distance the target must be within to move towards it")
    })
    public GoalSelectorBuilder<T> moveTowardsTarget(int priority, double speedModifier, float distanceWithin) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new MoveTowardsTargetGoal((PathfinderMob) t, speedModifier, distanceWithin)));
        }
        return this;
    }

    @Info(value = "Adds a `OcelotAttackGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> ocelotAttack(int priority) {
        suppliers.add(new Pair<>(priority, OcelotAttackGoal::new)); // It's named that way for legacy reasons I assume
        return this;
    }

    @Info(value = "Adds a `OpenDoorGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "closeDoor", value = "If the entity should also close doors")
    })
    public GoalSelectorBuilder<T> openDoor(int priority, boolean closeDoor) {
        suppliers.add(new Pair<>(priority, t -> new OpenDoorGoal(t, closeDoor)));
        return this;
    }

    @Info(value = "Adds a `PanicGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public GoalSelectorBuilder<T> panic(int priority, double speedModifier) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new PanicGoal((PathfinderMob) t, speedModifier)));
        }
        return this;
    }

    @Info(value = "Adds a `RandomLookAroundGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> randomLookAround(int priority) {
        suppliers.add(new Pair<>(priority, RandomLookAroundGoal::new));
        return this;
    }

    @Info(value = "Adds a `RandomSwimmingGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "interval", value = "Sets the interval at which the goal will be refreshed") // the randomStroll method's comment
    })
    public GoalSelectorBuilder<T> randomSwimming(int priority, double speedModifier, int interval) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new RandomSwimmingGoal((PathfinderMob) t, speedModifier, interval)));
        }
        return this;
    }

    @Info(value = "Adds a `RangedAttackGoal` to the entity, only applicable to **ranged attack** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "attackIntervalMin", value = "The minimum interval between attacks"),
            @Param(name = "attackIntervalMax", value = "The maximum interval between attacks"),
            @Param(name = "attackRadius", value = "The maximum distance something can be attacked from") // I think?
    })
    public <E extends Mob & RangedAttackMob> GoalSelectorBuilder<T> rangedAttack(int priority, double speedModifier, int attackIntervalMin, int attackIntervalMax, float attackRadius) {
        if (isRangedAttack) {
            suppliers.add(new Pair<>(priority, t -> new RangedAttackGoal((E) t, speedModifier, attackIntervalMin, attackIntervalMax, attackIntervalMax)));
        }
        return this;
    }

    @Info(value = "Adds a `RestrictSunGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> restrictSun(int priority) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new RestrictSunGoal((PathfinderMob) t)));
        }
        return this;
    }

    @Info(value = "Adds a `RunAroundLikeCrazyGoal` to the entity, only applicable to **horse** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public GoalSelectorBuilder<T> horseRunAroundLikeCrazy(int priority, double speedModifier) {
        if (isHorse) {
            suppliers.add(new Pair<>(priority, t -> new RunAroundLikeCrazyGoal((AbstractHorse) t, speedModifier)));
        }
        return this;
    }

    @Info(value = "Adds a `SitWhenOrderedToGoal` to the entity, only applicable to **tamable** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> sitWhenOrdered(int priority) {
        if (isTamable) {
            suppliers.add(new Pair<>(priority, t -> new SitWhenOrderedToGoal((TamableAnimal) t)));
        }
        return this;
    }

    @Info(value = "Adds a `StrollThroughVillageGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "interval", value = "Sets how often the goal 'refreshes'")
    })
    public GoalSelectorBuilder<T> strollThroughVillage(int priority, int interval) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new StrollThroughVillageGoal((PathfinderMob) t, interval)));
        }
        return this;
    }

    @Info(value = "Adds a `TemptGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "temptItems", value = "The ingredient that determines what items tempt the mob"),
            @Param(name = "canScare", value = "If the mob can be scared by getting to close to the tempter")
    })
    public GoalSelectorBuilder<T> tempt(int priority, double speedModifier, Ingredient temptItems, boolean canScare) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new TemptGoal((PathfinderMob) t, speedModifier, temptItems, canScare)));
        }
        return this;
    }

    @Info(value = "Adds a `TryFindWaterGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalSelectorBuilder<T> tryFindWater(int priority) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new TryFindWaterGoal((PathfinderMob) t)));
        }
        return this;
    }

    @Info(value = "Adds a `UseItemGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "itemToUse", value = "The item that will be used"),
            @Param(name = "soundEvent", value = "The registry name of a sound event that should play when the itme is used, may be null to indicate not sound event should play"),
            @Param(name = "canUseSelector", value = "Determines when the item may be used")
    })
    public GoalSelectorBuilder<T> useItem(int priority, ItemStack itemToUse, @Nullable ResourceLocation soundEvent, Predicate<T> canUseSelector) {
        suppliers.add(new Pair<>(priority, t -> new UseItemGoal<>(t, itemToUse, soundEvent == null ? null : Registry.SOUND_EVENT.get(soundEvent), canUseSelector))); // I like this one, interesting function and not stupidly restricted, mojang please more of these :)
    }

    @Info(value = "Adds a `WaterAvoidingRandomFlyingGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public GoalSelectorBuilder<T> waterAvoidingRandomFlying(int priority, double speedModifier) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new WaterAvoidingRandomFlyingGoal((PathfinderMob) t, speedModifier)));
        }
        return this;
    }

    @Info(value = "Adds a `WaterAvoidRandomStrollingGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "probability", value = "The probability, in the range [0, 1], that the entity picks a new position")
    })
    public GoalSelectorBuilder<T> waterAvoidingRandomStroll(int priority, double speedModifier, float probability) {
        if (isPathFinder) {
            suppliers.add(new Pair<>(priority, t -> new WaterAvoidingRandomStrollGoal((PathfinderMob) t, speedModifier, probability)));
        }
        return this;
    }
}
