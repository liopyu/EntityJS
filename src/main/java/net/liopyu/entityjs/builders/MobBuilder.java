package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.ai.goal.GoalSelectorBuilder;
import net.liopyu.entityjs.util.ai.goal.GoalTargetBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;

/**
 * A helper class that acts as a base for all mob-based entity types<br><br>
 *
 * Has methods for spawn eggs, goal selectors, goal targets, and anything else
 * in {@link Mob} that is not present in/related to {@link net.minecraft.world.entity.LivingEntity LivignEntity}
 */
public abstract class MobBuilder<T extends Mob & IAnimatableJS> extends BaseEntityBuilder<T> {

    public transient SpawnEggItemBuilder eggItem;
    public transient Consumer<GoalSelectorBuilder<T>> goalSelectorBuilder;
    public transient Consumer<GoalTargetBuilder<T>> goalTargetBuilder;

    public MobBuilder(ResourceLocation i) {
        super(i);
        goalSelectorBuilder = b -> {};
        goalTargetBuilder = b -> {};
    }

    @Info(value = "Creates a spawn egg item for this entity type")
    @Generics(value = SpawnEggItemBuilder.class)
    public MobBuilder<T> eggItem(Consumer<SpawnEggItemBuilder> eggItem) {
        this.eggItem = new SpawnEggItemBuilder(id, this);
        eggItem.accept(this.eggItem);
        return this;
    }

    public MobBuilder<T> goals(Consumer<GoalSelectorBuilder<T>> goals) {
        goalSelectorBuilder = goals;
        return this;
    }

    public MobBuilder<T> goalTargets(Consumer<GoalTargetBuilder<T>> targets) {
        goalTargetBuilder = targets;
        return this;
    }

    @Override
    public void createAdditionalObjects() {
        if (eggItem != null) {
            RegistryInfo.ITEM.addBuilder(eggItem);
        }
    }
}
