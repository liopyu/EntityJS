package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.builders.modification.*;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.overrides.CallbackInvoker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class EntityModificationEventJS extends EventJS {
    public static final Map<EntityType<?>, EntityModificationEventJS> eventMap = new HashMap<>();
    private final Object builder;
    private final EntityType<?> entityType;
    private boolean posted;
    public static final Map<ResourceLocation, Consumer<ModifyEntityBuilder>> createCustomMap = new HashMap<>();
    private static final Set<ResourceLocation> appliedCustomModifiers = new HashSet<>();

    // Must use #getOrCreate
    private EntityModificationEventJS(EntityType<?> entityType, Entity entity) {
        this.entityType = entityType;
        this.builder = determineModificationType(entityType, entity);
    }

    public static EntityModificationEventJS getOrCreate(EntityType<?> entityType, Entity entity) {
        EntityModificationEventJS event = eventMap.get(entityType);
        if (event == null) {
            event = new EntityModificationEventJS(entityType, entity);
            eventMap.put(entityType, event);
        }
        return event;
    }

    @HideFromJS
    public Object getBuilder() {
        return builder;
    }

    @HideFromJS
    public void postModifyEventIfNeeded() {
        if (posted || !EventHandlers.modifyEntity.hasListeners()) {
            return;
        }
        posted = true;
        EventHandlers.modifyEntity.post(this);
    }

    @HideFromJS
    public static void resetPostedModifyEvents() {
        eventMap.values().forEach(event -> event.posted = false);
        appliedCustomModifiers.clear();
    }

    @HideFromJS
    public static void applyCustomModifierIfNeeded(ResourceLocation entityType, ModifyEntityBuilder builder) {
        Consumer<ModifyEntityBuilder> customConsumer = createCustomMap.get(entityType);
        if (customConsumer == null || !appliedCustomModifiers.add(entityType)) {
            return;
        }
        customConsumer.accept(builder);
        CallbackInvoker.wrapCallbackFields(builder);
    }


    @Info(value = """
            Entity type modification event. Allows modification of methods for any existing entity.\s
            \s
            This event determines the entity's type and uses the appropriate builder for modification.\s
            \s
            Builders:\s
                - ModifyPathfinderMobBuilder: For entities extending {@link PathfinderMob}\s
                - ModifyMobBuilder: For entities extending {@link Mob}\s
                - ModifyLivingEntityBuilder: For entities extending {@link LivingEntity}\s
                - ModifyEntityBuilder: For entities extending {@link Entity}\s
            \s
            Example usage:\s
            ```javascript
            EntityJSEvents.modifyEntity(event => {
                event.modify("minecraft:zombie", builder => {
                    builder.onRemovedFromWorld(entity => {
                        // Execute code when the zombie is removed from the world.
                    })
                })
            })
            ```
            """, params = {
            @Param(name = "entityType", value = "The entity type to modify"),
            @Param(name = "modifyBuilder", value = "A consumer to modify the entity type."),
    })
    public void modify(EntityType<?> entityType, Consumer<? extends ModifyEntityBuilder> modifyBuilder) {
        if (entityType != this.entityType) return;
        Consumer<?> wrappedModifyBuilder = CallbackInvoker.wrapConsumer(modifyBuilder);
        /*if (builder instanceof ModifyTamableAnimalBuilder) {
            ((Consumer<ModifyTamableAnimalBuilder>) wrappedModifyBuilder).accept((ModifyTamableAnimalBuilder) builder);
        } else if (builder instanceof ModifyAnimalBuilder) {
            ((Consumer<ModifyAnimalBuilder>) wrappedModifyBuilder).accept((ModifyAnimalBuilder) builder);
        } else if (builder instanceof ModifyAgeableMobBuilder) {
            ((Consumer<ModifyAgeableMobBuilder>) wrappedModifyBuilder).accept((ModifyAgeableMobBuilder) builder);
        } else */
        try {
            if (builder instanceof ModifyProjectileBuilder) {
                ((Consumer<ModifyProjectileBuilder>) wrappedModifyBuilder).accept((ModifyProjectileBuilder) builder);
            } else if (builder instanceof ModifyPathfinderMobBuilder) {
                ((Consumer<ModifyPathfinderMobBuilder>) wrappedModifyBuilder).accept((ModifyPathfinderMobBuilder) builder);
            } else if (builder instanceof ModifyMobBuilder) {
                ((Consumer<ModifyMobBuilder>) wrappedModifyBuilder).accept((ModifyMobBuilder) builder);
            } else if (builder instanceof ModifyLivingEntityBuilder) {
                ((Consumer<ModifyLivingEntityBuilder>) wrappedModifyBuilder).accept((ModifyLivingEntityBuilder) builder);
            } else if (builder instanceof ModifyEntityBuilder) {
                ((Consumer<ModifyEntityBuilder>) wrappedModifyBuilder).accept((ModifyEntityBuilder) builder);
            } else {
                throw new IllegalArgumentException("Unsupported builder type or consumer type.");
            }
        } finally {
            CallbackInvoker.wrapCallbackFields(builder);
        }
    }

    public ModifyEntityBuilder determineModificationType(EntityType<?> type, Entity entity) {
        /*if (entity instanceof TamableAnimal) {
            return new ModifyTamableAnimalBuilder(type);
        } else if (entity instanceof Animal) {
            return new ModifyAnimalBuilder(type);
        } else if (entity instanceof AgeableMob) {
            return new ModifyAgeableMobBuilder(type);
        } else */
        if (entity instanceof PathfinderMob) {
            return new ModifyPathfinderMobBuilder(type);
        } else if (entity instanceof Mob) {
            return new ModifyMobBuilder(type);
        } else if (entity instanceof LivingEntity) {
            return new ModifyLivingEntityBuilder(type);
        } else if (entity instanceof Projectile) {
            return new ModifyProjectileBuilder(type);
        } else {
            return new ModifyEntityBuilder(type);
        }
    }
}
