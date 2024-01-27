package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.MobInteractContext;
import net.liopyu.entityjs.util.PlayerEntityContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.*;

/**
 * A helper class that acts as a base for all mob-based entity types<br><br>
 * <p>
 * Has methods for spawn eggs, goal selectors, goal targets, and anything else
 * in {@link Mob} that is not present in/related to {@link net.minecraft.world.entity.LivingEntity LivignEntity}
 */
public abstract class MobBuilder<T extends Mob & IAnimatableJS> extends BaseLivingEntityBuilder<T> {
    public transient Function<MobInteractContext, @Nullable InteractionResult> mobInteract;
    public transient SpawnEggItemBuilder eggItem;
    public transient BiConsumer<BlockPathTypes, Float> setPathfindingMalus;
    public transient Function<BlockPathTypes, Boolean> canCutCorner;
    public transient Supplier<BodyRotationControl> createBodyControl;

    public transient Consumer<LivingEntity> setTarget;
    public transient Predicate<ProjectileWeaponItem> canFireProjectileWeapon;
    public transient Consumer<LivingEntity> ate;
    public transient Consumer<Object> getAmbientSound;
    public transient List<Object> canHoldItem;
    public transient Boolean shouldDespawnInPeaceful;
    public transient Boolean canPickUpLoot;
    public transient Boolean isPersistenceRequired;
    public transient Consumer<PlayerEntityContext> onOffspringSpawnedFromEgg;

    public transient Double meleeAttackRangeSqr;

    public MobBuilder(ResourceLocation i) {
        super(i);
    }

    @Info(value = "Creates a spawn egg item for this entity type")
    @Generics(value = {Mob.class, SpawnEggItemBuilder.class})
    public MobBuilder<T> eggItem(Consumer<SpawnEggItemBuilder> eggItem) {
        this.eggItem = new SpawnEggItemBuilder(id, this);
        eggItem.accept(this.eggItem);
        return this;
    }

    @Override
    public void createAdditionalObjects() {
        if (eggItem != null) {
            RegistryInfo.ITEM.addBuilder(eggItem);
        }
    }

    /**
     * Sets the pathfinding malus for a specific node type.
     *
     * @param setPathfindingMalus BiConsumer accepting pNodeType and pMalus.
     */
    public MobBuilder<T> setPathfindingMalus(BiConsumer<BlockPathTypes, Float> setPathfindingMalus) {
        this.setPathfindingMalus = setPathfindingMalus;
        return this;
    }

    /**
     * Determines if the entity can cut corners for a specific path type.
     *
     * @param canCutCorner Function accepting pPathType and returning a boolean.
     */
    public MobBuilder<T> canCutCorner(Function<BlockPathTypes, Boolean> canCutCorner) {
        this.canCutCorner = canCutCorner;
        return this;
    }

    /**
     * Creates a custom BodyRotationControl.
     *
     * @param createBodyControl Supplier returning a custom BodyRotationControl.
     */
    public MobBuilder<T> createBodyControl(Supplier<BodyRotationControl> createBodyControl) {
        this.createBodyControl = createBodyControl;
        return this;
    }


    /**
     * Sets the target for the entity.
     *
     * @param setTarget Consumer accepting a LivingEntity as the target.
     */
    public MobBuilder<T> setTarget(Consumer<LivingEntity> setTarget) {
        this.setTarget = setTarget;
        return this;
    }

    /**
     * Determines if the entity can fire a projectile weapon.
     *
     * @param canFireProjectileWeapon Predicate accepting a ProjectileWeaponItem and returning a boolean.
     */
    public MobBuilder<T> canFireProjectileWeapon(Predicate<ProjectileWeaponItem> canFireProjectileWeapon) {
        this.canFireProjectileWeapon = canFireProjectileWeapon;
        return this;
    }

    /**
     * Custom behavior when the entity eats.
     *
     * @param ate Runnable representing the custom eating behavior.
     */
    public MobBuilder<T> ate(Consumer<LivingEntity> ate) {
        this.ate = ate;
        return this;
    }

    @Info(value = "Sets the custom logic for mob interaction")
    public BaseLivingEntityBuilder<T> mobInteract(Function<MobInteractContext, @Nullable InteractionResult> f) {
        mobInteract = f;
        return this;
    }

    /**
     * Sets the ambient sound for the entity.
     *
     * @param getAmbientSound Supplier providing the ambient sound.
     */
    public MobBuilder<T> getAmbientSound(Consumer<Object> getAmbientSound) {
        this.getAmbientSound = getAmbientSound;
        return this;
    }


    /**
     * Sets the condition for whether the entity can hold specific items.
     *
     * @param items List of ItemStacks or ResourceLocations representing the items the entity can hold.
     */
    public MobBuilder<T> canHoldItem(List<Object> items) {
        this.canHoldItem = items;
        return this;
    }


    /**
     * Sets whether the entity should despawn in peaceful mode.
     *
     * @param shouldDespawnInPeaceful Boolean indicating whether the entity should despawn in peaceful mode.
     */
    public MobBuilder<T> shouldDespawnInPeaceful(Boolean shouldDespawnInPeaceful) {
        this.shouldDespawnInPeaceful = shouldDespawnInPeaceful;
        return this;
    }


    /**
     * Sets whether the entity can pick up loot.
     *
     * @param canPickUpLoot Boolean indicating whether the entity can pick up loot.
     */
    public MobBuilder<T> canPickUpLoot(Boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
        return this;
    }

    /**
     * Sets whether the entity's persistence is required.
     *
     * @param isPersistenceRequired Boolean indicating whether the entity's persistence is required.
     */
    public MobBuilder<T> isPersistenceRequired(Boolean isPersistenceRequired) {
        this.isPersistenceRequired = isPersistenceRequired;
        return this;
    }

    /**
     * Sets the behavior when offspring is spawned from an egg.
     *
     * @param onOffspringSpawnedFromEgg Consumer accepting a Pair of Player and Mob when offspring is spawned.
     */
    public MobBuilder<T> onOffspringSpawnedFromEgg(Consumer<PlayerEntityContext> onOffspringSpawnedFromEgg) {
        this.onOffspringSpawnedFromEgg = onOffspringSpawnedFromEgg;
        return this;
    }


    /**
     * Sets the square of the melee attack range for the entity.
     *
     * @param meleeAttackRangeSqr Double representing the square of the melee attack range.
     */
    public MobBuilder<T> meleeAttackRangeSqr(Double meleeAttackRangeSqr) {
        this.meleeAttackRangeSqr = meleeAttackRangeSqr;
        return this;
    }
}
