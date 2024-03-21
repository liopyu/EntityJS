package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityBuilder;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.AllayEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.AxolotlEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Function;

public class AxolotlJSBuilder extends PathfinderMobBuilder<AxolotlEntityJS> {
    public transient Function<LivingEntity, Object> bucketItemStack;

    public AxolotlJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Info(value = """
            @param bucketItemStack Function returning the itemstack to receive when bucketed
            Defaults to Axolotl Bucket
            Example usage:
            ```javascript
            builder.bucketItemStack(entity => {
                // Use information about the entity to return an ItemStack.
                return Item.of('minecraft:diamond')
            })
            ```
            """)
    public AxolotlJSBuilder bucketItemStack(Function<LivingEntity, Object> function) {
        this.bucketItemStack = function;
        return this;
    }

    @Override
    public EntityType.EntityFactory<AxolotlEntityJS> factory() {
        return (type, level) -> new AxolotlEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return AxolotlEntityJS.createAttributes();
    }
}
