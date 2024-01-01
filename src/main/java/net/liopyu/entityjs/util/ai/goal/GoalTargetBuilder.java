package net.liopyu.entityjs.util.ai.goal;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This is how {@link TargetGoal}s are added to a mob's {@code targetSelector}.
 * Some basic goals are provided as well as the ability to use arbitrary goals.<br><br>
 *
 * If you wish to add more goal types see the comment in {@link GoalSelectorBuilder}
 */
public class GoalTargetBuilder<T extends Mob> {

    private final List<Pair<Integer, Function<T, Goal>>> targetSuppliers;

    public GoalTargetBuilder() {
        targetSuppliers = new ArrayList<>();
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
    public GoalTargetBuilder<T> arbitraryTargetGoal(int priority, Function<T, Goal> goalSupplier) {
        targetSuppliers.add(new Pair<>(priority, goalSupplier));
        return this;
    }

    @Info(value = "Adds a `NearestAttackableTargetGoal` to the entity", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "targetClass", value = "The entity class that should be targeted"),
            @Param(name = "randomInterval", value = "The interval at which the goal amy be 'refreshed'"),
            @Param(name = "mustSee", value = "If the mob must have line of sight at all times"),
            @Param(name = "mustReach", value = "If the mob must be able to reach the target to attack"),
            @Param(name = "targetConditions", value = "The conditions under which the targeted entity will be targeted, may be null")
    })
    public <E extends LivingEntity> GoalTargetBuilder<T> nearestAttackableTarget(int priority, Class<E> targetClass, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetConditions) {
        targetSuppliers.add(new Pair<>(priority, t -> new NearestAttackableTargetGoal<>(t, targetClass, randomInterval, mustSee, mustReach, targetConditions)));
        return this;
    }

    @Info(value = "Adds s `HurtByTargetGoal` to the entity, only applicable to **pathfinder** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "toIgnoreDamage", value = "The classes that damage should be ignored from"), // This thing genuinely warps m mind, I do not understand why it's so generalized for its purpose
            @Param(name = "alertOthers", value = "If other mobs should be alerted when this mob is damaged"),
            @Param(name = "toIgnoreAlert", value = "The entity classes that should not be alerted")
    })
    public GoalTargetBuilder<T> hurtByTarget(int priority, List<Class<?>> toIgnoreDamage, boolean alertOthers, List<Class<?>> toIgnoreAlert) {
        targetSuppliers.add(new Pair<>(priority, t -> {
            var goal = new HurtByTargetGoal((PathfinderMob) t, toIgnoreDamage.toArray(new Class<?>[0]));
            if (alertOthers) {
                goal.setAlertOthers(toIgnoreAlert.toArray(new Class<?>[0]));
            }
            return goal;
        }));
        return this;
    }

    @Info(value = "Adds a `NonTameRandomTargetGoal` to the entity, only applicable to **tamable** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "targetClass", value = "The entity class that should be targeted"),
            @Param(name = "mustSee", value = "If the mob must have line of sight at all times"),
            @Param(name = "targetConditions", value = "The conditions under which the targeted entity will be targeted, may be null")
    })
    public <E extends LivingEntity> GoalTargetBuilder<T> nonTameRandomTarget(int priority, Class<E> targetClass, boolean mustSee, @Nullable Predicate<LivingEntity> targetCondition) {
        targetSuppliers.add(new Pair<>(priority, t -> new NonTameRandomTargetGoal<>((TamableAnimal) t, targetClass, mustSee, targetCondition)));
        return this;
    }

    @Info(value = "Adds a `OwnerHurtByTargetGoal` to the entity, only applicable to **tamable** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal")
    })
    public GoalTargetBuilder<T> ownerHurtByTarget(int priority) {
        targetSuppliers.add(new Pair<>(priority, t -> new OwnerHurtByTargetGoal((TamableAnimal) t)));
        return this;
    }

    @Info(value = "Adds a `ResetUniversalAngerTargetGoal` to the entity, only applicable to **neutral** mobs", params = {
            @Param(name = "priority", value = "The priority of the goal"),
            @Param(name = "alertOthersOfSameType", value = "If other mobs of the same type should be alerted")
    })
    public <E extends Mob & NeutralMob> GoalTargetBuilder<T> resetUniversalAngerTarget(int priority, boolean alertOthersOfSameType) {
        targetSuppliers.add(new Pair<>(priority, t -> new ResetUniversalAngerTargetGoal<>((E) t, alertOthersOfSameType)));
        return this;
    }

    @HideFromJS
    public void apply(GoalSelector targetSelector, T entity) {
        for (Pair<Integer, Function<T, Goal>> targetSupplier : targetSuppliers) {
            targetSelector.addGoal(targetSupplier.getFirst(), targetSupplier.getSecond().apply(entity));
        }
    }
}
