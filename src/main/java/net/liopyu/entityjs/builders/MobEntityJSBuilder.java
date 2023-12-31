package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.MobEntityJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.ai.GoalSelectorBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.Consumer;

public class MobEntityJSBuilder extends BaseEntityBuilder<MobEntityJS> {

    public transient SpawnEggItemBuilder eggItem;
    public transient Consumer<GoalSelectorBuilder<MobEntityJS>> goalBuilder;

    public MobEntityJSBuilder(ResourceLocation i) {
        super(i);
        goalBuilder = b -> {};
    }

    @Info(value = "Creates a spawn egg item for this entity type")
    @Generics(value = SpawnEggItemBuilder.class)
    public MobEntityJSBuilder eggItem(Consumer<SpawnEggItemBuilder> eggItem) {
        this.eggItem = new SpawnEggItemBuilder(id, this);
        eggItem.accept(this.eggItem);
        return this;
    }

    public MobEntityJSBuilder goals(Consumer<GoalSelectorBuilder<MobEntityJS>> goals) {
        goalBuilder = goals;
        return this;
    }

    @Override
    public EntityType.EntityFactory<MobEntityJS> factory() {
        return (type, level) -> new MobEntityJS(this, type ,level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        final AttributeSupplier.Builder builder = MobEntityJS.createMobAttributes();
        attributes.accept(builder);
        return builder;
    }

    @Override
    public void createAdditionalObjects() {
        if (eggItem != null) {
            RegistryInfo.ITEM.addBuilder(eggItem);
        }
    }
}
