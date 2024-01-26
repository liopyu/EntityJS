package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AddGoalTargetsEventJS<T extends Mob> extends GoalEventJS<T> {

    public AddGoalTargetsEventJS(T mob, GoalSelector selector) {
        super(mob, selector);
    }

    @Info(value = """
            Enables the addition of arbitrary goals to an entity
                        
            It is the responsibility of the user to ensure the goal is
            compatible with the entity
                        
            Example of usage:
            =====================================
            builder.arbitraryTargetGoal(3, entity -> new $DefendVillageTargetGoal(entity))
            =====================================
                       
            Note in the example the entity must be an instance of IronGolem
            """, params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "goalSupplier", value = "The goal supplier, a function that takes a Mob and returns a Goal")
    })
    public void arbitraryTargetGoal(int priority, Function<T, Goal> goalSuppler) {
        selector.addGoal(priority, goalSuppler.apply(mob));
    }

    @Info(value = "Adds a `NearestAttackableTargetGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "targetClass", value = "The entity class that should be targeted"),
            @Param(name = "randomInterval", value = "The interval at which the goal amy be 'refreshed'"),
            @Param(name = "mustSee", value = "If the mob must have line of sight at all times"),
            @Param(name = "mustReach", value = "If the mob must be able to reach the target to attack"),
            @Param(name = "targetConditions", value = "The conditions under which the targeted entity will be targeted, may be null")
    })
    public <E extends LivingEntity> void nearestAttackableTarget(int priority, Class<E> targetClass, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetConditions) {
        selector.addGoal(priority, new NearestAttackableTargetGoal<>(mob, targetClass, randomInterval, mustSee, mustReach, targetConditions));
    }

    @Info(value = "Adds s `HurtByTargetGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "toIgnoreDamage", value = "The classes that damage should be ignored from"), // This thing genuinely warps m mind, I do not understand why it's so generalized for its purpose
            @Param(name = "alertOthers", value = "If other mobs should be alerted when this mob is damaged"),
            @Param(name = "toIgnoreAlert", value = "The entity classes that should not be alerted")
    })
    public void hurtByTarget(int priority, List<Class<?>> toIgnoreDamage, boolean alertOthers, List<Class<?>> toIgnoreAlert) {
        if (isPathFinder) {
            var goal = new HurtByTargetGoal((PathfinderMob) mob, toIgnoreDamage.toArray(new Class<?>[0]));
            if (alertOthers) {
                goal.setAlertOthers(toIgnoreAlert.toArray(new Class<?>[0]));
            }
            selector.addGoal(priority, goal);
        }
    }

    @Info(value = "Adds a `NonTameRandomTargetGoal` to the entity, only applicable to **tamable** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "targetClass", value = "The entity class that should be targeted"),
            @Param(name = "mustSee", value = "If the mob must have line of sight at all times"),
            @Param(name = "targetConditions", value = "The conditions under which the targeted entity will be targeted, may be null")
    })
    public <E extends LivingEntity> void nonTameRandomTarget(int priority, Class<E> targetClass, boolean mustSee, @Nullable Predicate<LivingEntity> targetCondition) {
        if (isTamable) {
            selector.addGoal(priority, new NonTameRandomTargetGoal<>((TamableAnimal) mob, targetClass, mustSee, targetCondition));
        }
    }

    @Info(value = "Adds a `OwnerHurtByTargetGoal` to the entity, only applicable to **tamable** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public void ownerHurtByTarget(int priority) {
        if (isTamable) {
            selector.addGoal(priority, new OwnerHurtByTargetGoal((TamableAnimal) mob));
        }
    }

    @Info(value = "Adds a `ResetUniversalAngerTargetGoal` to the entity, only applicable to **neutral** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "alertOthersOfSameType", value = "If other mobs of the same type should be alerted")
    })
    public <E extends Mob & NeutralMob> void resetUniversalAngerTarget(int priority, boolean alertOthersOfSameType) {
        if (isNeutral) {
            selector.addGoal(priority, new ResetUniversalAngerTargetGoal<>((E) mob, alertOthersOfSameType));
        }
    }
}