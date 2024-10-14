package net.liopyu.entityjs.builders.nonliving.vanilla;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.EntityTypeBuilder;
import net.liopyu.entityjs.entities.nonliving.vanilla.TridentEntityJS;
import net.liopyu.entityjs.item.TridentItemBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.function.Consumer;
import java.util.function.Function;

public class TridentJSBuilder extends BaseEntityBuilder<TridentEntityJS> {
    public transient Consumer<ContextUtils.ProjectileEntityHitContext> onHitEntity;
    public transient Consumer<ContextUtils.ProjectileBlockHitContext> onHitBlock;
    public transient Function<Entity, Object> canHitEntity;
    public transient Consumer<ContextUtils.CollidingProjectileEntityContext> onEntityCollision;
    public transient TridentItemBuilder item;
    public transient boolean noItem;
    public transient SoundEvent defaultHitGroundSoundEvent;
    public transient SoundEvent defaultTridentHitSound;

    public transient SoundEvent thunderHitSound;
    public transient float thunderHitVolume;
    public transient Function<TridentEntityJS, Object> isChanneling;
    public transient DamageSource damageSource;
    public transient boolean alwaysThunder;

    public TridentJSBuilder(ResourceLocation i) {
        super(i);
        this.item = (TridentItemBuilder) new TridentItemBuilder(id, this)
                .canThrow(false)
                .texture(i.getNamespace() + ":item/" + i.getPath());
        this.defaultHitGroundSoundEvent = SoundEvents.TRIDENT_HIT_GROUND;
        this.thunderHitSound = SoundEvents.TRIDENT_THUNDER;
        this.defaultTridentHitSound = SoundEvents.TRIDENT_HIT;
        alwaysThunder = false;
        thunderHitVolume = 5.0F;
    }

    @Info(value = """
            @param alwaysThunder A boolean value determining if the trident always causes thunder on hit, regardless of weather.
                            
                Example usage:
                ```javascript
                tridentBuilder.setAlwaysThunder(true);
                ```
            """)
    public TridentJSBuilder setAlwaysThunder(boolean alwaysThunder) {
        this.alwaysThunder = alwaysThunder;
        return this;
    }

    @Info(value = """
            @param damageSource The source of damage caused by the trident.
                            
                Example usage:
                ```javascript
                tridentBuilder.setDamageSource(DamageSource.thrownProjectile);
                ```
            """)
    public TridentJSBuilder setDamageSource(DamageSource damageSource) {
        this.damageSource = damageSource;
        return this;
    }

    @Info(value = """
            @param defaultTridentHitSound The sound event to be played when the trident hits an entity by default.
                            
                Example usage:
                ```javascript
                tridentBuilder.setDefaultTridentHitSound(SoundEvents.TRIDENT_HIT);
                ```
            """)
    public TridentJSBuilder setDefaultTridentHitSound(SoundEvent defaultTridentHitSound) {
        this.defaultTridentHitSound = defaultTridentHitSound;
        return this;
    }

    @Info(value = """
            @param thunderHitSound The sound event to be played when the trident hits an entity during a thunderstorm.
                            
                Example usage:
                ```javascript
                tridentBuilder.setThunderHitSound(SoundEvents.THUNDER);
                ```
            """)
    public TridentJSBuilder setThunderHitSound(SoundEvent thunderHitSound) {
        this.thunderHitSound = thunderHitSound;
        return this;
    }


    @Info(value = """
            @param isChanneling A function that determines whether the trident entity has the channeling enchantment.
                            
                Example usage:
                ```javascript
                tridentBuilder.setIsChanneling(tridentEntity => {
                    return false;
                });
                ```
            """)
    public TridentJSBuilder setIsChanneling(Function<TridentEntityJS, Object> isChanneling) {
        this.isChanneling = isChanneling;
        return this;
    }

    @Info(value = """
            @param defaultHitGroundSoundEvent The sound event to be played when the trident hits the ground by default.
                            
                Example usage:
                ```javascript
                tridentBuilder.setDefaultHitGroundSoundEvent(SoundEvents.GENERIC_HIT);
                ```
            """)
    public TridentJSBuilder setDefaultHitGroundSoundEvent(SoundEvent defaultHitGroundSoundEvent) {
        this.defaultHitGroundSoundEvent = defaultHitGroundSoundEvent;
        return this;
    }


    @Info(value = "Indicates that no projectile item should be created for this entity type")
    public TridentJSBuilder noItem() {
        this.noItem = true;
        return this;
    }

    @Info(value = "Creates the arrow item for this entity type")
    @Generics(value = BaseEntityBuilder.class)
    public TridentJSBuilder item(Consumer<TridentItemBuilder> item) {
        this.item = new TridentItemBuilder(id, this);
        item.accept(this.item);
        return this;
    }

    @Override
    public void createAdditionalObjects() {
        if (!noItem) {
            RegistryInfo.ITEM.addBuilder(item);
        }
    }

    @Override
    public EntityType.EntityFactory<TridentEntityJS> factory() {
        return (type, level) -> new TridentEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        //final AttributeSupplier.Builder builder = BaseEntityJS.createLivingAttributes();
        return null; //BaseEntityJS.createLivingAttributes();
    }

    @Override
    public EntityType<TridentEntityJS> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }

    //Projectile Overrides

    @Info(value = """
            Sets a callback function to be executed when the projectile
            collides with an entity.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.onEntityCollision(context => {
                const { entity, target } = context
                console.log(entity)
            });
            ```
            """)
    public TridentJSBuilder onEntityCollision(Consumer<ContextUtils.CollidingProjectileEntityContext> consumer) {
        onEntityCollision = consumer;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the projectile hits an entity.
            The provided Consumer accepts a {@link ContextUtils.ProjectileEntityHitContext} parameter,
            representing the context of the projectile's interaction with a specific entity.
                        
            Example usage:
            ```javascript
            projectileBuilder.onHitEntity(context -> {
                // Custom logic to handle the projectile hitting an entity.
                // Access information about the entity and projectile using the provided context.
            });
            ```
            """)
    public TridentJSBuilder onHitEntity(Consumer<ContextUtils.ProjectileEntityHitContext> consumer) {
        onHitEntity = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the projectile hits a block.
            The provided Consumer accepts a {@link ContextUtils.ProjectileBlockHitContext} parameter,
            representing the context of the projectile's interaction with a specific block.
                        
            Example usage:
            ```javascript
            projectileBuilder.onHitBlock(context -> {
                // Custom logic to handle the projectile hitting a block.
                // Access information about the block and projectile using the provided context.
            });
            ```
            """)
    public TridentJSBuilder onHitBlock(Consumer<ContextUtils.ProjectileBlockHitContext> consumer) {
        onHitBlock = consumer;
        return this;
    }

    @Info(value = """
            Sets a function to determine if the projectile entity can hit a specific entity.
                        
            @param canHitEntity The predicate to check if the arrow can hit the entity.
                        
            Example usage:
            ```javascript
            projectileEntityBuilder.canHitEntity(entity -> {
                // Custom logic to determine if the projectile can hit the specified entity
                // Return true if the arrow can hit, false otherwise.
            });
            ```
            """)
    public TridentJSBuilder canHitEntity(Function<Entity, Object> function) {
        canHitEntity = function;
        return this;
    }
}