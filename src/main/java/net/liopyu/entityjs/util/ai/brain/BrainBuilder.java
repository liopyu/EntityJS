package net.liopyu.entityjs.util.ai.brain;

import com.google.common.collect.ImmutableList;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/*
 * Wholly untested, DO NOT USE
 */
public class BrainBuilder {

    private final ResourceLocation entityTypeId;
    private final List<ActivitySupplier> suppliers;

    private final List<String> behaviorNames;

    public final Behaviors behaviors = Behaviors.INSTANCE;

    public BrainBuilder(ResourceLocation entityTypeId) {
        this.entityTypeId = entityTypeId;
        suppliers = new ArrayList<>();
        behaviorNames = new ArrayList<>();
    }

    public BrainBuilder coreActivity(int i, List<Behavior<? super LivingEntity>> behaviors) {
        suppliers.add(new BrainBuilderSupplier(Activity.CORE, null, i, behaviors));
        behaviorNames.add("minecraft:core");
        return this;
    }

    public BrainBuilder idleActivity(int i, List<Supplier<Behavior<? super LivingEntity>>> behaviors) {
        suppliers.add(new ActivitySupplier(Activity.IDLE, null, i, behaviors));
        behaviorNames.add("minecraft:idle");
        return this;
    }

    public BrainBuilder activity(ResourceLocation activity, int i, List<Supplier<Behavior<? super LivingEntity>>> behaviors) {
        suppliers.add(new ActivitySupplier(null, activity, i, behaviors));
        behaviorNames.add(activity.toString());
        return this;
    }

    @HideFromJS
    public <T extends LivingEntity> Brain<T> build(Brain<T> brain) {
        for (ActivitySupplier supplier : suppliers) {
            supplier.get(brain);
        }
        brain.useDefaultActivity();
        return brain;
    }

    @Override
    public String toString() {
        return "BrainBuilder{name=" + entityTypeId + ", activities=" + behaviorNames + "}";
    }

    public record ActivitySupplier(@Nullable Activity activity, @Nullable ResourceLocation activityName, int i, List<Supplier<Behavior<? super LivingEntity>>> behaviors) {

        public void get(Brain<?> brain) {
            if (activity != null) {
                brain.addActivity(activity, i, realizeBehaviors());
                if (activity == Activity.CORE) {
                    brain.setCoreActivities(Set.of(Activity.CORE));
                } else if (activity == Activity.IDLE) {
                    brain.setDefaultActivity(Activity.IDLE);
                }
            } else {
                assert activityName != null;
                brain.addActivity(Registry.ACTIVITY.get(activityName), i, realizeBehaviors());
            }
        }

        private ImmutableList<? extends Behavior<? super LivingEntity>> realizeBehaviors() {
            final List<Behavior<? super LivingEntity>> finalList = new ArrayList<>();
            behaviors.forEach(supplier -> finalList.add(supplier.get()));
            return ImmutableList.copyOf(finalList);
        }
    }
}
