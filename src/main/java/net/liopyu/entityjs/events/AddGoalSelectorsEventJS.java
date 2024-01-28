package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.util.ai.CustomGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class AddGoalSelectorsEventJS<T extends Mob> extends GoalEventJS<T> {

    public AddGoalSelectorsEventJS(T mob, GoalSelector selector) {
        super(mob, selector);
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
    public void arbitraryGoal(int priority, Function<T, Goal> goalSupplier) {
        selector.addGoal(priority, goalSupplier.apply(mob));
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
    public void customGoal(
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
        selector.addGoal(priority, new CustomGoal<>(name, mob, canUse, canContinueToUse, isInterruptable, start, stop, requiresUpdateEveryTick, tick));
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
    public <E extends LivingEntity> void avoidEntity(int priority, Class<E> entityClassToAvoid, Predicate<LivingEntity> avoidPredicate, float maxDist, double walkSpeedModifier, double sprintSpeedModifier, Predicate<LivingEntity> onAvoidEntityPredicate) {
        if (isPathFinder) {
            selector.addGoal(priority, new AvoidEntityGoal<>((PathfinderMob) mob, entityClassToAvoid, avoidPredicate, maxDist, walkSpeedModifier, sprintSpeedModifier, onAvoidEntityPredicate));
        }
    }

    @Info(value = "Adds a `BreakDoorGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "doorBreakTime", value = "The time it takes to break a door, limited to 240 ticks"), // I think that's what it is
            @Param(name = "validDifficulties", value = "Determines what difficulties are valid for the goal")
    })
    public void breakDoor(int priority, int doorBreakTime, Predicate<Difficulty> validDifficulties) {
        selector.addGoal(priority, new BreakDoorGoal(mob, doorBreakTime, validDifficulties));
    }

    @Info(value = "Adds a `BreathAirGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void breathAir(int priority) {
        if (isPathFinder) {
            selector.addGoal(priority, new BreathAirGoal((PathfinderMob) mob));
        }
    }

    @Info(value = "Adds a `BreedGoal` to the entity, only applicable to **animal** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "partnerClass", value = "The class of animal that this entity breeds with, may be null to specify it be the same class as this entity")
    })
    public void breed(int priority, double speedModifier, @Nullable Class<? extends Animal> partnerClass) {
        if (isAnimal) {
            selector.addGoal(priority, new BreedGoal((Animal) mob, speedModifier, partnerClass != null ? partnerClass : UtilsJS.cast(mob.getClass())));
        }
    }

    public void floatGoal(int priority) {
        selector.addGoal(priority, new FloatGoal(mob));
    }

    @Info(value = "Adds a `RemoveBlockGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "block", value = "The registry name of a block, the block to be removed"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "verticalSearchRange", value = "The vertical range the mob will search for the block")
    })
    public void removeBlock(int priority, ResourceLocation block, double speedModifier, int verticalSearchRange) {
        if (isPathFinder) {
            selector.addGoal(priority, new RemoveBlockGoal(Registry.BLOCK.get(block), (PathfinderMob) mob, speedModifier, verticalSearchRange));
        }
    }

    @Info(value = "Adds a `ClimbOnTopOfPowderSnowGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void climbOnTopOfPowderedSnow(int priority) {
        selector.addGoal(priority, new ClimbOnTopOfPowderSnowGoal(mob, mob.level));
    }

    @Info(value = "Adds a `EatBlockGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void eatGrass(int priority) {
        selector.addGoal(priority, new EatBlockGoal(mob));
    }

    @Info(value = "Adds a `MeleeAttackGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "followTargetEventIfNotSeen", value = "Determines if the entity should follow the target even if it doesn't see it")
    })
    public void meleeAttack(int priority, double speedModifier, boolean followTargetEvenIfNotSeen) {
        if (isPathFinder) {
            selector.addGoal(priority, new MeleeAttackGoal((PathfinderMob) mob, speedModifier, followTargetEvenIfNotSeen));
        }
    }

    @Info(value = "Adds a `FleeSunGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public void fleeSun(int priority, double speedModifier) {
        if (isPathFinder) {
            selector.addGoal(priority, new FleeSunGoal((PathfinderMob) mob, speedModifier));
        }
    }

    @Info(value = "Adds a `FollowBoatGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void followBoat(int priority) {
        if (isPathFinder) {
            selector.addGoal(priority, new FollowBoatGoal((PathfinderMob) mob));
        }
    }

    // How the hell does this not require a pathfinder mob
    @Info(value = "Adds a `FollowMobGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "stopDistance", value = "The distance away from the target the mob will stop"),
            @Param(name = "areaSize", value = "The distance away from the mob, that will be searched for mobs to follow")
    })
    public void followMob(int priority, double speedModifier, float stopDistance, float areaSize) {
        selector.addGoal(priority, new FollowMobGoal(mob, speedModifier, stopDistance, areaSize));
    }

    @Info(value = "Adds a `FollowOwnerGoal` to the entity, only applicable to **tamable** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "startDistance", value = "The distance away from the owner the mob will start moving"),
            @Param(name = "stopDistance", value = "The distance away from the owner the mob will stop moving"),
            @Param(name = "canFly", value = "If the mob can teleport into leaves") // Yes, this is what it means
    })
    public void followOwner(int priority, double speedModifier, float startDistance, float stopDistance, boolean canFly) {
        if (isTamable) {
            selector.addGoal(priority, new FollowOwnerGoal((TamableAnimal) mob, speedModifier, startDistance, stopDistance, canFly));
        }
    }

    // Only usable by animal mobs
    @Info(value = "Adds a `FollowParentGoal` to the entity, only applicable to **animal** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public void followParent(int priority, double speedModifier) {
        if (isAnimal) {
            selector.addGoal(priority, new FollowParentGoal((Animal) mob, speedModifier));
        }
    }

    @Info(value = "Adds a `LookAtPlayerGoal` to the entity", params = { // Should we lie and say adds a `LookAtEntityGoal`?
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "targetClass", value = "The entity class that should be looked at"),
            @Param(name = "lookDistance", value = "How far away the entity should be looked at"),
            @Param(name = "probability", value = "The probability, in the range [0, 1], that the goal may be used"),
            @Param(name = "onlyHorizontal", value = "Determines if the eye level must be the same to follow the target entity")
    })
    public <E extends LivingEntity> void lookAtEntity(int priority, Class<E> targetClass, float lookDistance, float probability, boolean onlyHorizontal) {
        selector.addGoal(priority, new LookAtPlayerGoal(mob, targetClass, lookDistance, probability, onlyHorizontal));
    }

    @Info(value = "Adds a `LeapAtTargetGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "deltaY", value = "Sets the delta movement of the animal in the y-axis")
    })
    public void leapAtTarget(int priority, float deltaY) {
        selector.addGoal(priority, new LeapAtTargetGoal(mob, deltaY));
    }

    @Info(value = "Adds a `RandomStrollGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "interval", value = "Sets the interval at which the goal will be 'refreshed, any values below 1 will be 1.'"), // I think? It indirectly determines when #canUse() returns false
            @Param(name = "checkNoActionTime", value = "Determines if the mob's noActionTime property should be checked")
    })
    public void randomStroll(int priority, double speedModifier, int interval, boolean checkNoActionTime) {
        if (isPathFinder) {
            selector.addGoal(priority, new RandomStrollGoal((PathfinderMob) mob, speedModifier, Math.max(1, interval), checkNoActionTime));
        }

    }

    /*public void moveToBlock(int priority, double speedModifier, int interval, boolean checkNoActionTime) {
        if (isPathFinder) {
            selector.addGoal(priority, new MoveToBlockGoal(priority, (PathfinderMob) mob, speedModifier, Math.max(1, interval), checkNoActionTime) {
                @Override
                protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
                    return false;
                }
            });
            })
        }

    }*/

    @Info(value = "Adds a `MoveBackToVillageGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "checkNoActionTime", value = "Determines if the mob's noActionTime property should be checked")
    })
    public void moveBackToVillage(int priority, double speedModifier, boolean checkNoActionTime) {
        if (isPathFinder) {
            selector.addGoal(priority, new MoveBackToVillageGoal((PathfinderMob) mob, speedModifier, checkNoActionTime));
        }
    }

    @Info(value = "Adds a `MoveThroughVillageGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "onlyAtNight", value = "If this goal should only apply at night"),
            @Param(name = "distanceToPoi", value = "The minimum distance to a poi the mob must be to have it be considered 'visited'"),
            @Param(name = "canDealWithDoors", value = "If doors can be opened to navigate as part of this goal") // Mention supplier somehow?
    })
    public void moveThroughVillage(int priority, double speedModifier, boolean onlyAtNight, int distanceToPoi, Supplier<Boolean> canDealWithDoors) {
        if (isPathFinder) {
            selector.addGoal(priority, new MoveThroughVillageGoal((PathfinderMob) mob, speedModifier, onlyAtNight, distanceToPoi, canDealWithDoors::get)); // BooleanSupplier is not nice for generalized supplier options (Forge config values / ConfigJS)
        }
    }

    @Info(value = "Adds a `MoveTowardsRestrictionGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public void moveTowardsRestriction(int priority, double speedModifier) {
        if (isPathFinder) {
            selector.addGoal(priority, new MoveTowardsRestrictionGoal((PathfinderMob) mob, speedModifier));
        }
    }

    @Info(value = "Adds a `MoveTowardsTargetGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "distanceWithin", value = "The distance the target must be within to move towards it")
    })
    public void moveTowardsTarget(int priority, double speedModifier, float distanceWithin) {
        if (isPathFinder) {
            selector.addGoal(priority, new MoveTowardsTargetGoal((PathfinderMob) mob, speedModifier, distanceWithin));
        }
    }

    @Info(value = "Adds a `OcelotAttackGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void ocelotAttack(int priority) {
        selector.addGoal(priority, new OcelotAttackGoal(mob)); // It's named that way for legacy reasons I assume
    }

    @Info(value = "Adds a `OpenDoorGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "closeDoor", value = "If the entity should also close doors")
    })
    public void openDoor(int priority, boolean closeDoor) {
        selector.addGoal(priority, new OpenDoorGoal(mob, closeDoor));
    }

    @Info(value = "Adds a `PanicGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public void panic(int priority, double speedModifier) {
        if (isPathFinder) {
            selector.addGoal(priority, new PanicGoal((PathfinderMob) mob, speedModifier));
        }
    }

    @Info(value = "Adds a `RandomLookAroundGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void randomLookAround(int priority) {
        selector.addGoal(priority, new RandomLookAroundGoal(mob));
    }

    @Info(value = "Adds a `RandomSwimmingGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "interval", value = "Sets the interval at which the goal will be refreshed") // the randomStroll method's comment
    })
    public void randomSwimming(int priority, double speedModifier, int interval) {
        if (isPathFinder) {
            selector.addGoal(priority, new RandomSwimmingGoal((PathfinderMob) mob, speedModifier, interval));
        }
    }

    @Info(value = "Adds a `RangedAttackGoal` to the entity, only applicable to **ranged attack** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "attackIntervalMin", value = "The minimum interval between attacks"),
            @Param(name = "attackIntervalMax", value = "The maximum interval between attacks"),
            @Param(name = "attackRadius", value = "The maximum distance something can be attacked from") // I think?
    })
    public <E extends Mob & RangedAttackMob> void rangedAttack(int priority, double speedModifier, int attackIntervalMin, int attackIntervalMax, float attackRadius) {
        if (isRangedAttack) {
            selector.addGoal(priority, new RangedAttackGoal((E) mob, speedModifier, attackIntervalMin, attackIntervalMax, attackRadius));
        }
    }

    @Info(value = "Adds a `RestrictSunGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void restrictSun(int priority) {
        if (isPathFinder) {
            selector.addGoal(priority, new RestrictSunGoal((PathfinderMob) mob));
        }
    }

    @Info(value = "Adds a `RunAroundLikeCrazyGoal` to the entity, only applicable to **horse** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public void horseRunAroundLikeCrazy(int priority, double speedModifier) {
        if (isHorse) {
            selector.addGoal(priority, new RunAroundLikeCrazyGoal((AbstractHorse) mob, speedModifier));
        }
    }

    @Info(value = "Adds a `SitWhenOrderedToGoal` to the entity, only applicable to **tamable** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void sitWhenOrdered(int priority) {
        if (isTamable) {
            selector.addGoal(priority, new SitWhenOrderedToGoal((TamableAnimal) mob));
        }
    }

    @Info(value = "Adds a `StrollThroughVillageGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "interval", value = "Sets how often the goal 'refreshes'")
    })
    public void strollThroughVillage(int priority, int interval) {
        if (isPathFinder) {
            selector.addGoal(priority, new StrollThroughVillageGoal((PathfinderMob) mob, interval));
        }
    }

    @Info(value = "Adds a `TemptGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "temptItems", value = "The ingredient that determines what items tempt the mob"),
            @Param(name = "canScare", value = "If the mob can be scared by getting to close to the tempter")
    })
    public void tempt(int priority, double speedModifier, Ingredient temptItems, boolean canScare) {
        if (isPathFinder) {
            selector.addGoal(priority, new TemptGoal((PathfinderMob) mob, speedModifier, temptItems, canScare));
        }
    }

    @Info(value = "Adds a `TryFindWaterGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void tryFindWater(int priority) {
        if (isPathFinder) {
            selector.addGoal(priority, new TryFindWaterGoal((PathfinderMob) mob));
        }
    }

    @Info(value = "Adds a `UseItemGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "itemToUse", value = "The item that will be used"),
            @Param(name = "soundEvent", value = "The registry name of a sound event that should play when the item is used, may be null to indicate not sound event should play"),
            @Param(name = "canUseSelector", value = "Determines when the item may be used")
    })
    public void useItem(int priority, ItemStack itemToUse, @Nullable ResourceLocation soundEvent, Predicate<T> canUseSelector) {
        selector.addGoal(priority, new UseItemGoal<>(mob, itemToUse, soundEvent == null ? null : Registry.SOUND_EVENT.get(soundEvent), canUseSelector)); // I like this one, interesting function and not stupidly restricted, Mojang please more of these :)
    }

    @Info(value = "Adds a `WaterAvoidingRandomFlyingGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move")
    })
    public void waterAvoidingRandomFlying(int priority, double speedModifier) {
        if (isPathFinder) {
            selector.addGoal(priority, new WaterAvoidingRandomFlyingGoal((PathfinderMob) mob, speedModifier));
        }
    }

    @Info(value = "Adds a `WaterAvoidRandomStrollingGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "speedModifier", value = "Sets the speed at which the mob should try to move"),
            @Param(name = "probability", value = "The probability, in the range [0, 1], that the entity picks a new position")
    })
    public void waterAvoidingRandomStroll(int priority, double speedModifier, float probability) {
        if (isPathFinder) {
            selector.addGoal(priority, new WaterAvoidingRandomStrollGoal((PathfinderMob) mob, speedModifier, probability));
        }
    }
}