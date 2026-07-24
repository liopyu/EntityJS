package net.liopyu.entityjs.builders.nonliving.vanilla;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.NonAnimatableEntityTypeBuilder;
import net.liopyu.entityjs.entities.nonliving.vanilla.EyeOfEnderEntityJS;
import net.liopyu.entityjs.item.EyeOfEnderItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.EyeOfEnder;

import java.util.function.Consumer;
import java.util.function.Function;

public class EyeOfEnderJSBuilder extends EyeOfEnderEntityBuilder<EyeOfEnderEntityJS> {

    public transient Function<EyeOfEnder, Object> getItem;
    public transient EyeOfEnderItemBuilder item;
    public transient boolean noItem;
    public transient boolean disableTrailParticles;
    public transient boolean disableDefaultDeathLogic;
    public transient Float survivalChance;

    public EyeOfEnderJSBuilder(ResourceLocation i) {
        super(i);
        this.item = (EyeOfEnderItemBuilder) new EyeOfEnderItemBuilder(id, this)
                .texture(i.getNamespace() + ":item/" + i.getPath());
        this.disableTrailParticles = false;
        this.disableDefaultDeathLogic = false;
    }

    @Info(value = """
            @param survivalChance A float value from 0 to 1 representing the chance that the Eye of Ender will not break after use.
                            
                Example usage:
                ```javascript
                eyeOfEnderBuilder.setSurvivalChance(0.8);
                ```
            """)
    public EyeOfEnderJSBuilder setSurvivalChance(float survivalChance) {
        this.survivalChance = survivalChance;
        return this;
    }

    @Info(value = "Disables the default ender eye break sound as well as the death particles.")
    public EyeOfEnderJSBuilder disableDefaultDeathLogic() {
        this.disableDefaultDeathLogic = true;
        return this;
    }

    @Info(value = "The default trail particles will be disabled")
    public EyeOfEnderJSBuilder disableTrailParticles() {
        this.disableTrailParticles = true;
        return this;
    }

    @Override
    public EntityType<EyeOfEnderEntityJS> createObject() {
        return new NonAnimatableEntityTypeBuilder<>(this).get();
    }

    @Info(value = """
            Sets a function to determine the itemstack the entity drops when it
            turns back into an item
            Defaults to eye of ender.
            Example usage:
            ```javascript
            builder.getItem(entity => {
                // Use information about the entity provided by the context.
                return Item.of('kubejs:eye_of_ender')// Some ItemStack
            });
            ```
            """)
    public EyeOfEnderJSBuilder getItem(Function<EyeOfEnder, Object> function) {
        this.getItem = function;
        return this;
    }

    @Info(value = "Indicates that no item should be created for this entity type")
    public EyeOfEnderJSBuilder noItem() {
        this.noItem = true;
        return this;
    }

    @Info(value = "Creates the item for this entity type")
    @Generics(value = BaseEntityBuilder.class)
    public EyeOfEnderJSBuilder item(Consumer<EyeOfEnderItemBuilder> item) {
        this.item = new EyeOfEnderItemBuilder(id, this);
        item.accept(this.item);
        return this;
    }

    @Override
    public EntityType.EntityFactory<EyeOfEnderEntityJS> factory() {
        return (type, level) -> new EyeOfEnderEntityJS(this, type, level);
    }

    @Override
    public void createAdditionalObjects() {
        if (!noItem) {
            RegistryInfo.ITEM.addBuilder(item);
        }
    }
}

