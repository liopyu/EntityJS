package net.liopyu.entityjs.builders.living.entityjs;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.*;

/**
 * A helper class that acts as a base for all mob-based entity types<br><br>
 * <p>
 * Has methods for spawn eggs, goal selectors, goal targets, and anything else
 * in {@link Mob} that is not present in/related to {@link net.minecraft.world.entity.LivingEntity LivignEntity}
 */
public abstract class MobBuilder<T extends Mob & IAnimatableJS> extends BaseLivingEntityBuilder<T> {

    public transient Consumer<ContextUtils.PlayerEntityContext> tickLeash;
    public transient SpawnEggItemBuilder eggItem;
    public transient Consumer<ContextUtils.TargetChangeContext> onTargetChanged;
    public transient Ingredient canFireProjectileWeapon;
    public transient Function<ContextUtils.EntityProjectileWeaponContext, Object> canFireProjectileWeaponPredicate;
    public transient Consumer<LivingEntity> ate;
    public transient Object setAmbientSound;
    public transient Function<ContextUtils.EntityItemStackContext, Object> canHoldItem;
    public transient Boolean shouldDespawnInPeaceful;
    public transient Function<Mob, Object> canPickUpLoot;
    public transient Boolean isPersistenceRequired;
    public transient Function<Mob, Object> meleeAttackRangeSqr;
    public transient Boolean canJump;
    public transient Function<LivingEntity, Object> myRidingOffset;
    public transient Object ambientSoundInterval;
    public transient Function<ContextUtils.EntityDistanceToPlayerContext, Object> removeWhenFarAway;
    public transient Function<ContextUtils.PlayerEntityContext, Object> canBeLeashed;
    public transient Function<ContextUtils.EntityLevelContext, Object> createNavigation;
    public transient boolean noEggItem = false;
    public transient Function<LivingEntity, Object> setLookControl;
    public transient Function<LivingEntity, Object> setMoveControl;
    public transient Function<LivingEntity, Object> setJumpControl;

    public MobBuilder(ResourceLocation i) {
        super(i);
        canJump = true;
        ambientSoundInterval = 120;
        canFireProjectileWeapon = Ingredient.of(Items.BOW);
        canFireProjectileWeaponPredicate = t -> t.projectileWeapon.getDefaultInstance().is(Items.BOW);
        this.eggItem = new SpawnEggItemBuilder(id, this)
                .backgroundColor(0)
                .highlightColor(0);
    }

    @Info(value = "Sets a function to define the entity's LookControl behavior")
    public MobBuilder<T> setLookControl(Function<LivingEntity, Object> setLookControl) {
        this.setLookControl = setLookControl;
        return this;
    }

    @Info(value = "Sets a function to define the entity's MoveControl behavior")
    public MobBuilder<T> setMoveControl(Function<LivingEntity, Object> setMoveControl) {
        this.setMoveControl = setMoveControl;
        return this;
    }

    @Info(value = "Sets a function to define the entity's JumpControl behavior")
    public MobBuilder<T> setJumpControl(Function<LivingEntity, Object> setJumpControl) {
        this.setJumpControl = setJumpControl;
        return this;
    }

    @Info(value = "Indicates that no egg item should be created for this entity type")
    public MobBuilder<T> noEggItem() {
        this.noEggItem = true;
        return this;
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
        if (noEggItem) return;
        RegistryInfo.ITEM.addBuilder(eggItem);
    }

    @Info(value = """
            Sets a function to determine the PathNavigation of the entity.
            
            @param createNavigation A Function accepting an EntityLevelContext parameter
            
            Example usage:
            ```javascript
            mobBuilder.createNavigation(context => {
                const {entity, level} = context
                return EntityJSUtils.createWallClimberNavigation(entity, level) // Return some path navigation
            });
            ```
            """)
    public MobBuilder<T> createNavigation(Function<ContextUtils.EntityLevelContext, Object> createNavigation) {
        this.createNavigation = createNavigation;
        return this;
    }

    @Info(value = """
            Sets a function to determine if the entity can be leashed.
            
            @param canBeLeashed A Function accepting a ContextUtils.PlayerEntityContext parameter
            
            Example usage:
            ```javascript
            mobBuilder.canBeLeashed(context => {
                return true // Return true if the entity can be leashed, false otherwise.
            });
            ```
            """)
    public MobBuilder<T> canBeLeashed(Function<ContextUtils.PlayerEntityContext, Object> canBeLeashed) {
        this.canBeLeashed = canBeLeashed;
        return this;
    }

    @Info(value = """
            Sets a predicate to determine if the entity should be removed when far away from the player.
            
            @param removeWhenFarAway A Function accepting a ContextUtils.EntityDistanceToPlayerContext parameter,
                                     defining the condition for the entity to be removed when far away.
            
            Example usage:
            ```javascript
            mobBuilder.removeWhenFarAway(context => {
                // Custom logic to determine if the entity should be removed when far away
                // Return true if the entity should be removed based on the provided context.
            });
            ```
            """)
    public MobBuilder<T> removeWhenFarAway(Function<ContextUtils.EntityDistanceToPlayerContext, Object> removeWhenFarAway) {
        this.removeWhenFarAway = removeWhenFarAway;
        return this;
    }

    @Info(value = """
            Sets the interval in ticks between ambient sounds for the mob entity.
            
            @param ambientSoundInterval The interval in ticks between ambient sounds.
            Defaults to 120.
            
            Example usage:
            ```javascript
            mobBuilder.ambientSoundInterval(100);
            ```
            """)
    public MobBuilder<T> ambientSoundInterval(int ambientSoundInterval) {
        this.ambientSoundInterval = ambientSoundInterval;
        return this;
    }

    @Info(value = """
            Function which sets the offset for riding on the mob entity.
            
            @param myRidingOffset The offset value for riding on the mob.
            Defaults to 0.0.
            
            Example usage:
            ```javascript
            mobBuilder.myRidingOffset(entity => {
                //Use the provided context about the entity to determine the riding offset of the passengers
                return 5 //Some double value;
            })
            ```
            """)
    public MobBuilder<T> myRidingOffset(Function<LivingEntity, Object> myRidingOffset) {
        this.myRidingOffset = myRidingOffset;
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
            mobBuilder.onTargetChanged(context => {
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
            mobBuilder.canFireProjectileWeaponPredicate(context => {
                // Custom logic to determine whether the entity can fire a projectile weapon
                // Access information about the entity and the projectile weapon using the provided context.
                return context.projectileWeapon.id == 'minecraft:bow'; // Replace with your specific condition.
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
            mobBuilder.ate(entity => {
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
            mobBuilder.setAmbientSound("minecraft:entity.zombie.ambient");
            ```
            """)
    public MobBuilder<T> setAmbientSound(Object ambientSound) {
        if (ambientSound instanceof String) {
            this.setAmbientSound = new ResourceLocation((String) ambientSound);
        } else if (ambientSound instanceof ResourceLocation resourceLocation) {
            this.setAmbientSound = resourceLocation;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setAmbientSound. Value: " + ambientSound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.zombie.ambient\"");
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
            mobBuilder.canHoldItem(context => {
                // Custom logic to determine whether the entity can hold an item based on the provided context.
                return true;
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
            Sets the function to determine whether the entity can pick up loot.
            
            @param canPickUpLoot A Function accepting a {@link Mob} parameter,
                                 defining the condition for the entity to pick up loot.
            
            Example usage:
            ```javascript
            mobBuilder.canPickUpLoot(entity => {
                // Custom logic to determine whether the entity can pick up loot based on the provided mob.
                return true;
            });
            ```
            """)
    public MobBuilder<T> canPickUpLoot(Function<Mob, Object> canPickUpLoot) {
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
            
            @param meleeAttackRangeSqr A Function accepting a {@link Mob} parameter,
                                      defining the squared melee attack range based on the entity's state.
                                      Returns a 'Double' value representing the squared melee attack range.
            Example usage:
            ```javascript
            mobBuilder.meleeAttackRangeSqr(entity => {
                // Custom logic to calculate the squared melee attack range based on the provided mob.
                return 2;
            });
            ```
            """)
    public MobBuilder<T> meleeAttackRangeSqr(Function<Mob, Object> meleeAttackRangeSqr) {
        this.meleeAttackRangeSqr = meleeAttackRangeSqr;
        return this;
    }

    @Info(value = """
            Sets the callback function to be executed when the entity ticks while leashed.
            
            @param consumer A Consumer accepting a {@link ContextUtils.PlayerEntityContext} parameter,
                            defining the behavior to be executed when the entity ticks while leashed.
            
            Example usage:
            ```javascript
            mobBuilder.tickLeash(context => {
                // Custom logic to handle the entity's behavior while leashed.
                // Access information about the player and entity using the provided context.
            });
            ```
            """)
    public MobBuilder<T> tickLeash(Consumer<ContextUtils.PlayerEntityContext> consumer) {
        this.tickLeash = consumer;
        return this;
    }


}
