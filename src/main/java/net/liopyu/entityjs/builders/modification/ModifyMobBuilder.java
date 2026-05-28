package net.liopyu.entityjs.builders.modification;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ModifyMobBuilder extends ModifyLivingEntityBuilder {
    public transient Consumer<ContextUtils.PlayerEntityContext> tickLeash;
    public transient Consumer<ContextUtils.TargetChangeContext> onTargetChanged;
    public transient Consumer<LivingEntity> ate;
    public transient Object setAmbientSound;
    public transient Predicate<ContextUtils.EntityItemStackContext> canHoldItem;
    public transient Boolean shouldDespawnInPeaceful;
    public transient Predicate<Mob> canPickUpLoot;
    public transient Predicate<ContextUtils.EntityItemLevelContext> canTakeItem;
    public transient Boolean isPersistenceRequired;
    public transient Function<Mob, Object> getAttackBoundingBox;
    public transient Object ambientSoundInterval;
    public transient Predicate<ContextUtils.EntityDistanceToPlayerContext> removeWhenFarAway;
    public transient Predicate<Mob> canBeLeashed;
    public transient Function<ContextUtils.EntityLevelContext, Object> createNavigation;
    public transient Consumer<ContextUtils.MobInteractContext> onMobInteract;
    public transient Predicate<LivingEntity> isSunBurnTick;

    public ModifyMobBuilder(EntityType<?> entity) {
        super(entity);
    }

    @Info(value = """
            Sets a predicate function to determine whether the entity can take an item.
            The provided Predicate accepts a {@link ContextUtils.EntityItemLevelContext} parameter,
            representing the context of the entity potentially taking an item.
            
            Example usage:
            ```javascript
            modifyBuilder.canTakeItem(context => {
                // Define conditions for the entity to be able to take an item
                // Use information about the EntityItemLevelContext provided by the context.
                return // Some boolean condition indicating if the entity can take the item;
            });
            ```
            """)
    public ModifyMobBuilder canTakeItem(Predicate<ContextUtils.EntityItemLevelContext> predicate) {
        canTakeItem = predicate;
        return this;
    }

    @Info(value = """  
            @param isSunBurnTick Sets whether the mob should burn in daylight
            
            Example usage:
            ```javascript
            modifyBuilder.isSunBurnTick(entity => {
                return false
            });
            ```
            """)
    public ModifyMobBuilder isSunBurnTick(Predicate<LivingEntity> isSunBurnTick) {
        this.isSunBurnTick = isSunBurnTick;
        return this;
    }

    @Info(value = """
            Sets a consumer to handle the interaction with the entity.
            The provided Consumer accepts a {@link ContextUtils.MobInteractContext} parameter,
            representing the context of the interaction
            
            Example usage:
            ```javascript
            modifyBuilder.onMobInteract(context => {
                // Define custom logic for the interaction with the entity
                // Use information about the MobInteractContext provided by the context.
                if (context.player.isShiftKeyDown()) return
                context.player.startRiding(context.entity);
            });
            ```
            """)
    public ModifyMobBuilder onMobInteract(Consumer<ContextUtils.MobInteractContext> c) {
        onMobInteract = c;
        return this;
    }

    @Info(value = """
            Sets a function to determine the PathNavigation of the entity.
            
            @param createNavigation A Function accepting an EntityLevelContext parameter
            
            Example usage:
            ```javascript
            modifyBuilder.createNavigation(context => {
                const {entity, level} = context
                return EntityJSUtils.createWallClimberNavigation(entity, level) // Return some path navigation
            });
            ```
            """)
    public ModifyMobBuilder createNavigation(Function<ContextUtils.EntityLevelContext, Object> createNavigation) {
        this.createNavigation = createNavigation;
        return this;
    }

    @Info(value = """
            Sets a function to determine if the entity can be leashed.
            
            @param canBeLeashed A Function accepting a Mob parameter
            
            Example usage:
            ```javascript
            modifyBuilder.canBeLeashed(context => {
                return true // Return true if the entity can be leashed, false otherwise.
            });
            ```
            """)
    public ModifyMobBuilder canBeLeashed(Predicate<Mob> canBeLeashed) {
        this.canBeLeashed = canBeLeashed;
        return this;
    }

    @Info(value = """
            Sets a predicate to determine if the entity should be removed when far away from the player.
            
            @param removeWhenFarAway A Function accepting a ContextUtils.EntityDistanceToPlayerContext parameter,
                                     defining the condition for the entity to be removed when far away.
            
            Example usage:
            ```javascript
            modifyBuilder.removeWhenFarAway(context => {
                // Custom logic to determine if the entity should be removed when far away
                // Return true if the entity should be removed based on the provided context.
            });
            ```
            """)
    public ModifyMobBuilder removeWhenFarAway(Predicate<ContextUtils.EntityDistanceToPlayerContext> removeWhenFarAway) {
        this.removeWhenFarAway = removeWhenFarAway;
        return this;
    }

    @Info(value = """
            Sets the interval in ticks between ambient sounds for the mob entity.
            
            @param ambientSoundInterval The interval in ticks between ambient sounds.
            Defaults to 120.
            
            Example usage:
            ```javascript
            modifyBuilder.ambientSoundInterval(100);
            ```
            """)
    public ModifyMobBuilder ambientSoundInterval(int ambientSoundInterval) {
        this.ambientSoundInterval = ambientSoundInterval;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity's target changes.
            
            @param setTarget A Consumer accepting a ContextUtils.TargetChangeContext parameter,
                             defining the behavior to be executed when the entity's target changes.
            
            Example usage:
            ```javascript
            modifyBuilder.onTargetChanged(context => {
                // Custom logic to handle the entity's target change
                // Access information about the target change using the provided context.
            });
            ```
            """)
    public ModifyMobBuilder onTargetChanged(Consumer<ContextUtils.TargetChangeContext> setTarget) {
        this.onTargetChanged = setTarget;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs an eating action.
            
            @param ate A Consumer accepting a LivingEntity parameter,
                       defining the behavior to be executed when the entity eats.
            
            Example usage:
            ```javascript
            modifyBuilder.ate(entity => {
                // Custom logic to handle the entity's eating action
                // Access information about the entity using the provided parameter.
            });
            ```
            """)
    public ModifyMobBuilder ate(Consumer<LivingEntity> ate) {
        this.ate = ate;
        return this;
    }

    @Info(value = """
            Sets the sound to play when the entity is ambient using either a string representation or a Identifier object.
            
            Example usage:
            ```javascript
            modifyBuilder.setAmbientSound("minecraft:entity.zombie.ambient");
            ```
            """)
    public ModifyMobBuilder setAmbientSound(Object ambientSound) {
        if (ambientSound instanceof String) {
            this.setAmbientSound = Identifier.parse((String) ambientSound);
        } else if (ambientSound instanceof Identifier resourceLocation) {
            this.setAmbientSound = resourceLocation;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setAmbientSound. Value: " + ambientSound + ". Must be a Identifier or String. Example: \"minecraft:entity.zombie.ambient\"");
            this.setAmbientSound = null;
        }
        return this;
    }

    @Info(value = """
            Sets the function to determine whether the entity can hold an item.
            
            @param canHoldItem A Function accepting a {@link ContextUtils.EntityItemStackContext} parameter,
                               defining the condition for the entity to hold an item.
            
            Example usage:
            ```javascript
            modifyBuilder.canHoldItem(context => {
                // Custom logic to determine whether the entity can hold an item based on the provided context.
                return true;
            });
            ```
            """)
    public ModifyMobBuilder canHoldItem(Predicate<ContextUtils.EntityItemStackContext> canHoldItem) {
        this.canHoldItem = canHoldItem;
        return this;
    }

    @Info(value = """
            Sets whether the entity should despawn in peaceful difficulty.
            
            @param shouldDespawnInPeaceful A boolean indicating whether the entity should despawn in peaceful difficulty.
            
            Example usage:
            ```javascript
            modifyBuilder.shouldDespawnInPeaceful(true);
            ```
            """)
    public ModifyMobBuilder shouldDespawnInPeaceful(boolean shouldDespawnInPeaceful) {
        this.shouldDespawnInPeaceful = shouldDespawnInPeaceful;
        return this;
    }

    @Info(value = """
            Sets the function to determine whether the entity can pick up loot.
            
            @param canPickUpLoot A Function accepting a {@link Mob} parameter,
                                 defining the condition for the entity to pick up loot.
            
            Example usage:
            ```javascript
            modifyBuilder.canPickUpLoot(entity => {
                // Custom logic to determine whether the entity can pick up loot based on the provided mob.
                return true;
            });
            ```
            """)
    public ModifyMobBuilder canPickUpLoot(Predicate<Mob> canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
        return this;
    }

    @Info(value = """
            Sets whether persistence is required for the entity.
            
            @param isPersistenceRequired A boolean indicating whether persistence is required.
            
            Example usage:
            ```javascript
            modifyBuilder.isPersistenceRequired(true);
            ```
            """)
    public ModifyMobBuilder isPersistenceRequired(boolean isPersistenceRequired) {
        this.isPersistenceRequired = isPersistenceRequired;
        return this;
    }

    @Info(value = """
            @param getAttackBoundingBox A Function accepting a {@link Mob} parameter,
                                      defining the bounding box to check for target intersection attacks.
                                      Returns an 'AABB' value representing the melee attack range.
            Example usage:
            ```javascript
            modifyBuilder.getAttackBoundingBox(entity => {
                // Custom logic to calculate the squared melee attack range based on the provided mob.
                return entity;
            });
            ```
            """)
    public ModifyMobBuilder getAttackBoundingBox(Function<Mob, Object> getAttackBoundingBox) {
        this.getAttackBoundingBox = getAttackBoundingBox;
        return this;
    }

    @Info(value = """
            Sets the callback function to be executed when the entity ticks while leashed.
            
            @param consumer A Consumer accepting a {@link ContextUtils.PlayerEntityContext} parameter,
                            defining the behavior to be executed when the entity ticks while leashed.
            
            Example usage:
            ```javascript
            modifyBuilder.tickLeash(context => {
                // Custom logic to handle the entity's behavior while leashed.
                // Access information about the player and entity using the provided context.
            });
            ```
            """)
    public ModifyMobBuilder tickLeash(Consumer<ContextUtils.PlayerEntityContext> consumer) {
        this.tickLeash = consumer;
        return this;
    }
}