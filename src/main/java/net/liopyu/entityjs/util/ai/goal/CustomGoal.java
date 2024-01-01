package net.liopyu.entityjs.util.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class CustomGoal<T extends Mob> extends Goal {

    private final String name;
    private final T mob;
    private final Predicate<T> canUse;
    @Nullable
    private final Predicate<T> canContinueToUse;
    private final boolean isInterruptable;
    private final Consumer<T> start;
    private final Consumer<T> stop;
    private final boolean requiresUpdateEveryTick;
    private final Consumer<T> tick;

    public CustomGoal(
            String name,
            T mob,
            Predicate<T> canUse,
            @Nullable Predicate<T> canContinueToUse,
            boolean isInterruptable,
            Consumer<T> start,
            Consumer<T> stop,
            boolean requiresUpdateEveryTick,
            Consumer<T> tick
    ) {
        this.name = name;
        this.mob = mob;
        this.canUse = canUse;
        this.canContinueToUse = canContinueToUse;
        this.isInterruptable = isInterruptable;
        this.start = start;
        this.stop = stop;
        this.requiresUpdateEveryTick = requiresUpdateEveryTick;
        this.tick = tick;
    }
    @Override
    public boolean canUse() {
        return canUse.test(mob);
    }

    @Override
    public boolean canContinueToUse() {
        return canContinueToUse == null ? super.canContinueToUse() : canContinueToUse.test(mob);
    }

    @Override
    public boolean isInterruptable() {
        return isInterruptable;
    }

    @Override
    public void start() {
        start.accept(mob);
    }

    @Override
    public void stop() {
        stop.accept(mob);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return requiresUpdateEveryTick;
    }

    @Override
    public void tick() {
        tick.accept(mob);
    }

    @Override
    public String toString() {
        return "CustomGoal[" + name + "]";
    }
}
