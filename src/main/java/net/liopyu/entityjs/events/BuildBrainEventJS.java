package net.liopyu.entityjs.events;

import com.google.common.collect.ImmutableList;
import dev.latvian.mods.kubejs.event.KubeEvent;
import net.liopyu.entityjs.util.ai.Behaviors;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Set;

public class BuildBrainEventJS<T extends LivingEntity> implements KubeEvent {

    private final Brain<T> base;

    public final Behaviors behaviors = Behaviors.INSTANCE;

    public BuildBrainEventJS(Brain<T> base) {
        this.base = base;
    }

    public void coreActivity(int i, List<Behavior<? super LivingEntity>> behaviors) {
        base.addActivity(Activity.CORE, i, ImmutableList.copyOf(behaviors));
        base.setCoreActivities(Set.of(Activity.CORE));
    }

    public void idleActivity(int i, List<Behavior<? super LivingEntity>> behaviors) {
        base.addActivity(Activity.IDLE, i, ImmutableList.copyOf(behaviors));
        base.setDefaultActivity(Activity.IDLE);
    }

    public void addActivity(Activity activity, int i, List<Behavior<? super LivingEntity>> behaviors) {
        base.addActivity(activity, i, ImmutableList.copyOf(behaviors));
    }
}