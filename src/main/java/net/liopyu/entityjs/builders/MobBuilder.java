package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.Object2FloatFunction;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.*;

/**
 * A helper class that acts as a base for all mob-based entity types<br><br>
 * <p>
 * Has methods for spawn eggs, goal selectors, goal targets, and anything else
 * in {@link Mob} that is not present in/related to {@link net.minecraft.world.entity.LivingEntity LivignEntity}
 */
public abstract class MobBuilder<T extends PathfinderMob & IAnimatableJS> extends BaseLivingEntityBuilder<T> {
    //pathfinder mob
    public transient Consumer<ContextUtils.PlayerEntityContext> tickLeash;
    //pathfinder mob
    public transient Function<PathfinderMob, Object> shouldStayCloseToLeashHolder;
    //pathfinder mob
    public transient Double followLeashSpeed;
    //pathfinder mob
    public transient Function<ContextUtils.EntityBlockPosLevelContext, Object> walkTargetValue;


    public transient SpawnEggItemBuilder eggItem;
    public transient Function<ContextUtils.EntityBlockPathTypeContext, Object> canCutCorner;

    public transient Consumer<ContextUtils.TargetChangeContext> onTargetChanged;
    public transient Ingredient canFireProjectileWeapon;
    public transient Function<ContextUtils.EntityProjectileWeaponContext, Object> canFireProjectileWeaponPredicate;
    public transient Consumer<LivingEntity> ate;
    public transient Object getAmbientSound;
    public transient Function<ContextUtils.EntityItemStackContext, Object> canHoldItem;
    public transient Boolean shouldDespawnInPeaceful;
    public transient Function<PathfinderMob, Object> canPickUpLoot;
    public transient Boolean isPersistenceRequired;

    public transient Function<PathfinderMob, Object> meleeAttackRangeSqr;
    public transient Boolean canJump;

    public MobBuilder(ResourceLocation i) {
        super(i);
        canJump = true;
    }

    @Info(value = "Creates a spawn egg item for this entity type")
    @Generics(value = {Mob.class, SpawnEggItemBuilder.class})
    public MobBuilder<T> eggItem(Consumer<SpawnEggItemBuilder> eggItem) {
        this.eggItem = new SpawnEggItemBuilder(id, this);
        eggItem.accept(this.eggItem);
        return this;
    }

    @HideFromJS
    @Override
    public void createAdditionalObjects() {
        if (eggItem != null) {
            RegistryInfo.ITEM.addBuilder(eggItem);
        }
    }


    @Info(value = """
            Sets a function to determine if the entity can cut corners when navigating paths.
                        
            @param canCutCorner A Function accepting a ContextUtils.EntityBlockPathTypeContext parameter,
                                defining the logic to determine if the entity can cut corners.
                        
            Example usage:
            ```javascript
            mobBuilder.canCutCorner(context => {
                // Custom logic to determine if the entity can cut corners based on the provided context.
                // Return true if the entity can cut corners, false otherwise.
            });
            ```
            """)
    public MobBuilder<T> canCutCorner(Function<ContextUtils.EntityBlockPathTypeContext, Object> canCutCorner) {
        this.canCutCorner = canCutCorner;
        return this;
    }


    @Info(value = """
            Sets whether the entity can jump.
                        
            @param canJump A boolean indicating whether the entity can jump.
                        
            Example usage:
            ```javascript
            mobBuilder.canJump(true);
            ```
            """)
    public MobBuilder<T> canJump(boolean canJump) {
        this.canJump = canJump;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity's target changes.
                        
            @param setTarget A Consumer accepting a ContextUtils.TargetChangeContext parameter,
                             defining the behavior to be executed when the entity's target changes.
                        
            Example usage:
            ```javascript
            mobBuilder.onTargetChanged(context -> {
                // Custom logic to handle the entity's target change
                // Access information about the target change using the provided context.
            });
            ```
            """)
    public MobBuilder<T> onTargetChanged(Consumer<ContextUtils.TargetChangeContext> setTarget) {
        this.onTargetChanged = setTarget;
        return this;
    }


    @Info(value = """
            Sets the ingredient required for the entity to fire a projectile weapon.
                        
            @param canFireProjectileWeapon An Ingredient representing the required item for firing a projectile weapon.
                        
            Example usage:
            ```javascript
            mobBuilder.canFireProjectileWeapon([
            'minecraft:bow',
            'minecraft:crossbow'
            ]);
            ```
            """)
    public MobBuilder<T> canFireProjectileWeapon(Ingredient canFireProjectileWeapon) {
        this.canFireProjectileWeapon = canFireProjectileWeapon;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the entity can fire a projectile weapon.
                        
            @param canFireProjectileWeaponPredicate A Predicate accepting a
                       ContextUtils.EntityProjectileWeaponContext parameter,
                       defining the condition under which the entity can fire a projectile weapon.
                        
            Example usage:
            ```javascript
            mobBuilder.canFireProjectileWeaponPredicate(context -> {
                // Custom logic to determine whether the entity can fire a projectile weapon
                // Access information about the entity and the projectile weapon using the provided context.
                return true; // Replace with your specific condition.
            });
            ```
            """)
    public MobBuilder<T> canFireProjectileWeaponPredicate(Function<ContextUtils.EntityProjectileWeaponContext, Object> canFireProjectileWeaponPredicate) {
        this.canFireProjectileWeaponPredicate = canFireProjectileWeaponPredicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs an eating action.
                        
            @param ate A Consumer accepting a LivingEntity parameter,
                       defining the behavior to be executed when the entity eats.
                        
            Example usage:
            ```javascript
            mobBuilder.ate(entity -> {
                // Custom logic to handle the entity's eating action
                // Access information about the entity using the provided parameter.
            });
            ```
            """)
    public MobBuilder<T> ate(Consumer<LivingEntity> ate) {
        this.ate = ate;
        return this;
    }


    @Info(value = """
            Sets the sound to play when the entity is ambient using either a string representation or a ResourceLocation object.
                        
            Example usage:
            ```javascript
            mobBuilder.getAmbientSound("minecraft:entity.zombie.ambient");
            ```
            """)
    public MobBuilder<T> getAmbientSound(Object ambientSound) {
        if (ambientSound instanceof String) {
            this.getAmbientSound = new ResourceLocation((String) ambientSound);
        } else if (ambientSound instanceof ResourceLocation resourceLocation) {
            this.getAmbientSound = resourceLocation;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for getAmbientSound. Value: " + ambientSound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.zombie.ambient\"");
            this.getAmbientSound = new ResourceLocation("minecraft", "entity/zombie/ambient");
        }
        return this;
    }


    @Info(value = """
            Sets the predicate to determine whether the entity can hold an item.
                        
            @param canHoldItem A Predicate accepting a {@link ContextUtils.EntityItemStackContext} parameter,
                               defining the condition for the entity to hold an item.
                        
            Example usage:
            ```javascript
            mobBuilder.canHoldItem(context => {
                // Custom logic to determine whether the entity can hold an item based on the provided context.
                return someCondition;
            });
            ```
            """)
    public MobBuilder<T> canHoldItem(Function<ContextUtils.EntityItemStackContext, Object> canHoldItem) {
        this.canHoldItem = canHoldItem;
        return this;
    }


    @Info(value = """
            Sets whether the entity should despawn in peaceful difficulty.
                        
            @param shouldDespawnInPeaceful A boolean indicating whether the entity should despawn in peaceful difficulty.
                        
            Example usage:
            ```javascript
            mobBuilder.shouldDespawnInPeaceful(true);
            ```
            """)
    public MobBuilder<T> shouldDespawnInPeaceful(boolean shouldDespawnInPeaceful) {
        this.shouldDespawnInPeaceful = shouldDespawnInPeaceful;
        return this;
    }


    @Info(value = """
            Sets the predicate to determine whether the entity can pick up loot.
                        
            @param canPickUpLoot A Predicate accepting a {@link PathfinderMob} parameter,
                                 defining the condition for the entity to pick up loot.
                        
            Example usage:
            ```javascript
            mobBuilder.canPickUpLoot(mob -> {
                // Custom logic to determine whether the entity can pick up loot based on the provided mob.
                return someCondition;
            });
            ```
            """)
    public MobBuilder<T> canPickUpLoot(Function<PathfinderMob, Object> canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
        return this;
    }


    @Info(value = """
            Sets whether persistence is required for the entity.
                        
            @param isPersistenceRequired A boolean indicating whether persistence is required.
                        
            Example usage:
            ```javascript
            mobBuilder.isPersistenceRequired(true);
            ```
            """)
    public MobBuilder<T> isPersistenceRequired(boolean isPersistenceRequired) {
        this.isPersistenceRequired = isPersistenceRequired;
        return this;
    }


    @Info(value = """
            Sets the function to determine the squared melee attack range for the entity.
                        
            @param meleeAttackRangeSqr A Function accepting a {@link PathfinderMob} parameter,
                                      defining the squared melee attack range based on the entity's state.
                                      Returns a 'Double' value representing the squared melee attack range.
            Example usage:
            ```javascript
            mobBuilder.meleeAttackRangeSqr(mob -> {
                // Custom logic to calculate the squared melee attack range based on the provided mob.
                return someCalculatedValue;
            });
            ```
            """)
    public MobBuilder<T> meleeAttackRangeSqr(Function<PathfinderMob, Object> meleeAttackRangeSqr) {
        this.meleeAttackRangeSqr = meleeAttackRangeSqr;
        return this;
    }


    @Info(value = """
            Sets the callback function to be executed when the entity ticks while leashed.
                        
            @param consumer A Consumer accepting a {@link ContextUtils.PlayerEntityContext} parameter,
                            defining the behavior to be executed when the entity ticks while leashed.
                        
            Example usage:
            ```javascript
            mobBuilder.tickLeash(context -> {
                // Custom logic to handle the entity's behavior while leashed.
                // Access information about the player entity using the provided context.
            });
            ```
            """)
    public MobBuilder<T> tickLeash(Consumer<ContextUtils.PlayerEntityContext> consumer) {
        this.tickLeash = consumer;
        return this;
    }


    @Info(value = """
            Sets the predicate to determine whether the entity should stay close to its leash holder.
                        
            @param predicate A Predicate accepting a {@link PathfinderMob} parameter,
                             defining the condition for the entity to stay close to its leash holder.
                        
            Example usage:
            ```javascript
            mobBuilder.shouldStayCloseToLeashHolder(mob -> {
                // Custom logic to determine whether the entity should stay close to its leash holder.
                return someCondition;
            });
            ```
            """)
    public MobBuilder<T> shouldStayCloseToLeashHolder(Function<PathfinderMob, Object> predicate) {
        this.shouldStayCloseToLeashHolder = predicate;
        return this;
    }


    @Info(value = """
            Sets the follow leash speed for the entity.
                        
            @param speed The follow leash speed.
                        
            Example usage:
            ```javascript
            mobBuilder.followLeashSpeed(1.5);
            ```
            """)
    public MobBuilder<T> followLeashSpeed(double speed) {
        this.followLeashSpeed = speed;
        return this;
    }


    @Info(value = """
            Sets the walk target value function for the entity.
                        
            @param function A Function accepting a {@link ContextUtils.EntityBlockPosLevelContext} parameter,
                            defining the walk target value based on the entity's interaction with a specific block.
                        
            Example usage:
            ```javascript
            mobBuilder.walkTargetValue(context -> {
                // Custom logic to calculate the walk target value based on the provided context.
                // Access information about the block position and level using the provided context.
                return someCalculatedValue;
            });
            ```
            """)
    public MobBuilder<T> walkTargetValue(Function<ContextUtils.EntityBlockPosLevelContext, Object> function) {
        this.walkTargetValue = function;
        return this;
    }


}
