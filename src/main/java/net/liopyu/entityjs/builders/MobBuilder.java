package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.entities.MobEntityJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.PathfinderMob;
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
public abstract class MobBuilder<T extends PathfinderMob & IAnimatableJS> extends BaseLivingEntityBuilder<T> {
    public transient Function<ContextUtils.MobInteractContext, @Nullable InteractionResult> mobInteract;
    public transient SpawnEggItemBuilder eggItem;
    public transient BiConsumer<BlockPathTypes, Float> setPathfindingMalus;
    public transient Function<BlockPathTypes, Boolean> canCutCorner;
    /* public transient Supplier<BodyRotationControl> createBodyControl;*/

    public transient Consumer<ContextUtils.TargetChangeContext> onTargetChanged;
    public transient Predicate<ProjectileWeaponItem> canFireProjectileWeapon;
    public transient Consumer<LivingEntity> ate;
    public transient Consumer<Object> getAmbientSound;
    public transient List<Object> canHoldItem;
    public transient Boolean shouldDespawnInPeaceful;
    public transient Boolean canPickUpLoot;
    public transient Boolean isPersistenceRequired;
    /*public transient Consumer<ContextUtils.PlayerEntityContext> onOffspringSpawnedFromEgg;*/

    public transient Function<LivingEntity, Double> meleeAttackRangeSqr;
    /*public transient Consumer<Mob> updateControlFlags;*/
    public transient Consumer<PathfinderMob> aiStep;
    public transient boolean canJump;
    public transient Consumer<PathfinderMob> onJump;

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

    @Override
    public void createAdditionalObjects() {
        if (eggItem != null) {
            RegistryInfo.ITEM.addBuilder(eggItem);
        }
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return Mob.createMobAttributes();
    }

    @Info(value = """
            Sets the custom behavior for the pathfinding malus of a specific node type for the mob in the builder.
            """)
    public MobBuilder<T> setPathfindingMalus(BiConsumer<BlockPathTypes, Float> setPathfindingMalus) {
        this.setPathfindingMalus = setPathfindingMalus;
        return this;
    }

    @Info(value = """
            Sets the aiStep property in the builder.
                        
            " +
            "Defaults to super-AgeableMob.
            """)
    public MobBuilder<T> aiStep(Consumer<PathfinderMob> aiStep) {
        this.aiStep = aiStep;
        return this;
    }


    @Info(value = """
            Sets the custom function for determining if the entity can cut corners for a specific path type for the mob in the builder.
            """)
    public MobBuilder<T> canCutCorner(Function<BlockPathTypes, Boolean> canCutCorner) {
        this.canCutCorner = canCutCorner;
        return this;
    }

    public MobBuilder<T> canJump(boolean canJump) {
        this.canJump = canJump;
        return this;
    }

    public MobBuilder<T> onJump(Consumer<PathfinderMob> onJump) {
        this.onJump = onJump;
        return this;
    }

    @Info(value = """
            Sets the custom consumer for the target of the entity for the mob in the builder.
            """)
    public MobBuilder<T> onTargetChanged(Consumer<ContextUtils.TargetChangeContext> setTarget) {
        this.onTargetChanged = setTarget;
        return this;
    }

    @Info(value = """
            Sets the custom predicate for determining if the entity can fire a projectile weapon for the mob in the builder.
            """)
    public MobBuilder<T> canFireProjectileWeapon(Predicate<ProjectileWeaponItem> canFireProjectileWeapon) {
        this.canFireProjectileWeapon = canFireProjectileWeapon;
        return this;
    }

    @Info(value = """
            Sets the custom runnable representing the eating behavior for the mob in the builder.
            """)
    public MobBuilder<T> ate(Consumer<LivingEntity> ate) {
        this.ate = ate;
        return this;
    }

    @Info(value = """
            Sets the custom logic for mob interaction using the provided function.
            """)
    public BaseLivingEntityBuilder<T> mobInteract(Function<ContextUtils.MobInteractContext, @Nullable InteractionResult> f) {
        mobInteract = f;
        return this;
    }

    @Info(value = """
            Sets the custom supplier for providing the ambient sound for the mob in the builder.
            """)
    public MobBuilder<T> getAmbientSound(Consumer<Object> getAmbientSound) {
        this.getAmbientSound = getAmbientSound;
        return this;
    }

    @Info(value = """
            Sets the list of custom items or resource locations representing the items the entity can hold for the mob in the builder.
            """)
    public MobBuilder<T> canHoldItem(List<Object> items) {
        this.canHoldItem = items;
        return this;
    }

    @Info(value = """
            Sets whether the entity should despawn in peaceful mode for the mob in the builder.
            """)
    public MobBuilder<T> shouldDespawnInPeaceful(Boolean shouldDespawnInPeaceful) {
        this.shouldDespawnInPeaceful = shouldDespawnInPeaceful;
        return this;
    }

    @Info(value = """
            Sets whether the entity can pick up loot for the mob in the builder.
            """)
    public MobBuilder<T> canPickUpLoot(Boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
        return this;
    }

    @Info(value = """
            Sets whether the entity's persistence is required for the mob in the builder.
            """)
    public MobBuilder<T> isPersistenceRequired(Boolean isPersistenceRequired) {
        this.isPersistenceRequired = isPersistenceRequired;
        return this;
    }

    /*@Info(value = """
            Sets the custom behavior when offspring is spawned from an egg for the mob in the builder.
            """)
    public MobBuilder<T> onOffspringSpawnedFromEgg(Consumer<ContextUtils.PlayerEntityContext> onOffspringSpawnedFromEgg) {
        this.onOffspringSpawnedFromEgg = onOffspringSpawnedFromEgg;
        return this;
    }*/

    @Info(value = "Sets the custom double representing the square of the melee attack range for the mob in the builder.")
    public MobBuilder<T> meleeAttackRangeSqr(Function<LivingEntity, Double> meleeAttackRangeSqr) {
        this.meleeAttackRangeSqr = meleeAttackRangeSqr;
        return this;
    }
}
