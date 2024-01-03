package net.liopyu.entityjs.util.ai.goal;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.RangedAttackMob;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Base utility class where some things can be shared
 * between {@link GoalSelectorBuilder} and {@link GoalTargetBuilder}
 */
public abstract class GoalBuilder<T extends Mob> {

    protected final List<Pair<Integer, Function<T, Goal>>> suppliers;
    private final T entity;
    protected final boolean isPathFinder;
    protected final boolean isAnimal;
    protected final boolean isTamable;
    protected final boolean isRangedAttack;
    protected final boolean isHorse;
    protected final boolean isNeutral;

    public GoalBuilder(T entity) {
        suppliers = new ArrayList<>();
        this.entity = entity;
        isPathFinder = entity instanceof PathfinderMob;
        isAnimal = entity instanceof Animal;
        isTamable = entity instanceof TamableAnimal;
        isRangedAttack = entity instanceof RangedAttackMob;
        isHorse = entity instanceof AbstractHorse;
        isNeutral = entity instanceof NeutralMob;
    }

    /**
     * This assumes the goal builder won't be passed around,
     * <strong>don't</strong> make me regret that assumption
     */
    @HideFromJS
    public void apply(GoalSelector goalSelector) {
        for (Pair<Integer, Function<T, Goal>> goalSupplier : suppliers) {
            goalSelector.addGoal(goalSupplier.getFirst(), goalSupplier.getSecond().apply(entity));
        }
    }
}
