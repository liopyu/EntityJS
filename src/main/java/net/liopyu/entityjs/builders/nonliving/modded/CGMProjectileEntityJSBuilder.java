package net.liopyu.entityjs.builders.nonliving.modded;

import com.mrcrayfish.guns.common.ProjectileManager;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.EntityTypeBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.PartBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileEntityJS;
import net.liopyu.entityjs.entities.nonliving.modded.CGMProjectileEntityJS;
import net.liopyu.entityjs.item.CGMProjectileItemBuilder;
import net.liopyu.entityjs.item.ProjectileItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.function.Consumer;


public class CGMProjectileEntityJSBuilder extends BaseEntityBuilder<CGMProjectileEntityJS> {
    public transient CGMProjectileItemBuilder item;
    public transient Item stack;
    public transient boolean noItem;

    public transient ItemStack setItem;
    public transient ItemStack setWeapon;
    public transient Consumer<CGMProjectileEntityJS.ShotContext> onHitEntity;
    public transient Consumer<CGMProjectileEntityJS.HitBlockContext> onHitBlock;

    public transient Consumer<Entity> onProjectileTick;
    public transient boolean explosionEnabled;

    public CGMProjectileEntityJSBuilder(ResourceLocation i) {
        super(i);
        explosionEnabled = false;
        noItem = false;
        this.item = (CGMProjectileItemBuilder) new CGMProjectileItemBuilder(id, this)
                .canThrow(true)
                .texture(i.getNamespace() + ":item/" + i.getPath());
        this.stack = this.item.get();
        ProjectileManager.getInstance().registerFactory(stack, (worldIn, entity, weapon, item, modifiedGun) -> new CGMProjectileEntityJS(this, this.get(), worldIn, entity, weapon, item, modifiedGun));
    }

    public transient Level level;

    @Info(value = "Indicates that no projectile item should be created for this entity type")
    public CGMProjectileEntityJSBuilder noItem() {
        this.noItem = true;
        return this;
    }

    @Info(value = "Creates the projectile item for this entity type")
    public CGMProjectileEntityJSBuilder item(Consumer<CGMProjectileItemBuilder> item) {

        this.item = new CGMProjectileItemBuilder(id, this);
        item.accept(this.item);

        return this;
    }

    @Override
    public void createAdditionalObjects() {
        if (!noItem) {
            RegistryInfo.ITEM.addBuilder(item);
        }
    }


    @Info(value = """       
            @param explosionEnabled A boolean deciding whether or not default explosion behavior is enabled.
            Defaults to false.
                        
            Example usage:
            ```javascript
            entityBuilder.explosionEnabled(true);
            ```
            """)
    public CGMProjectileEntityJSBuilder explosionEnabled(boolean b) {
        explosionEnabled = b;
        return this;
    }

    @Info(value = """
            Sets a callback function to override the onProjectileTick param.
                        
            @param onProjectileTick A Consumer accepting a {@link Entity} parameter, defining the behavior to be executed on each tick.
                        
            Example usage:
            ```javascript
            entityBuilder.onProjectileTick(entity => {
                // This overrides the onProjectileTick method giving scriptors
                // a chance to override the custom particle behavior ect.
            });
            ```
            """)
    public CGMProjectileEntityJSBuilder onProjectileTick(Consumer<Entity> consumer) {
        onProjectileTick = consumer;
        return this;
    }

    @Info(value = """
            Sets the weapon related to the entity.
                        
            @param setWeapon A resourcelocation for an itemstack
                        
            Example usage:
            ```javascript
            entityBuilder.setWeapon("kubejs:missile");
            ```
            """)
    public CGMProjectileEntityJSBuilder setWeapon(ResourceLocation item) {

        setItem = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)).getDefaultInstance();
        return this;
    }

    @Info(value = """
            Sets the item related to the entity.
                        
            @param setItem A resourcelocation for an itemstack
                        
            Example usage:
            ```javascript
            entityBuilder.setItem("kubejs:missile");
            ```
            """)
    public CGMProjectileEntityJSBuilder setItem(ResourceLocation item) {

        setItem = Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(item)).getDefaultInstance();
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the projectile hits an entity.
                        
            @param onHitEntity A Consumer accepting a {@link CGMProjectileEntityJS.ShotContext} parameter.
                        
            Example usage:
            ```javascript
            entityBuilder.onHitEntity(entity => {
                // Custom logic to be executed when the projectile hits an entity
            });
            ```
            """)
    public CGMProjectileEntityJSBuilder onHitEntity(Consumer<CGMProjectileEntityJS.ShotContext> consumer) {
        onHitEntity = consumer;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the projectile hits a block.
                        
            @param onHitBlock A Consumer accepting a {@link CGMProjectileEntityJS.HitBlockContext} parameter.
                        
            Example usage:
            ```javascript
            entityBuilder.onHitBlock(entity => {
                // Custom logic to be executed when the projectile hits a block
            });
            ```
            """)
    public CGMProjectileEntityJSBuilder onHitBlock(Consumer<CGMProjectileEntityJS.HitBlockContext> consumer) {
        onHitBlock = consumer;
        return this;
    }

    @Override
    public EntityType.EntityFactory<CGMProjectileEntityJS> factory() {
        return (type, level) -> new CGMProjectileEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return null;
    }
}
