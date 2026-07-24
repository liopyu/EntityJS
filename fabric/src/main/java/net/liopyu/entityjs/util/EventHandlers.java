package net.liopyu.entityjs.util;

import dev.latvian.mods.kubejs.event.*;
import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.events.*;
import net.liopyu.entityjs.common.events.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.Map;

public class EventHandlers {

    public static final EventGroup EntityJSEvents = EventGroup.of("EntityJSEvents");
    // KubeJS 2001 doesn't transform extra IDs in hasListeners(Object). Callers with String type IDs
    // must use hasListeners() and let post(...) transform and filter the targeted listener instead.

    public static final EventHandler addGoalTargets = EntityJSEvents.server("addGoals", () -> AddGoalTargetsEventJS.class).extra(Extra.REQUIRES_ID); // Possibly a modify goals event for editing other entities
    public static final EventHandler addGoalSelectors = EntityJSEvents.server("addGoalSelectors", () -> AddGoalSelectorsEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrain = EntityJSEvents.server("buildBrain", () -> BuildBrainEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler buildBrainProvider = EntityJSEvents.server("buildBrainProvider", () -> BuildBrainProviderEventJS.class).extra(Extra.REQUIRES_ID);
    public static final EventHandler editAttributes = EntityJSEvents.startup("attributes", () -> ModifyAttributeEventJS.class);
    public static final EventHandler modifyEntity = EntityJSEvents.startup("modifyEntity", () -> EntityModificationEventJS.class);


    public static void init() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            RegistryEntryAddedCallback.event(BuiltInRegistries.ENTITY_TYPE).register((rawId, id, type) -> {
                BaseLivingEntityBuilder<?> hit = null;
                for (BaseLivingEntityBuilder<?> b : BaseLivingEntityBuilder.thisList) {
                    if (b.get() == type) {
                        hit = b; break;
                    }
                }
                if (hit != null) {
                    FabricDefaultAttributeRegistry.register(UtilsJS.cast(type), hit.getAttributeBuilder());
                    return;
                }
                for (CustomEntityJSBuilder candidate : CustomEntityBuilder.thisList) {
                    if (!(candidate instanceof CustomEntityBuilder builder)) continue;
                    if (builder.isLivingEntityClass() && builder.get() == type) {
                        FabricDefaultAttributeRegistry.register(UtilsJS.cast(type), builder.getAttributeBuilder());
                        return;
                    }
                }
            });
        }

        DynamicRegistrySetupCallback.EVENT.register(Event.DEFAULT_PHASE, ctx -> {

            if (editAttributes.hasListeners()) {
                editAttributes.post(new ModifyAttributeEventJS());
            }
            for (BaseLivingEntityBuilder<?> b : BaseLivingEntityBuilder.thisList) {
                EntityType<? extends LivingEntity> type = UtilsJS.cast(EntityJSUtils.getRegisteredEntityType(b, "attribute"));
                if (type == null) continue;
                var id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                AttributeSupplier.Builder merged = b.getAttributeBuilder();

                Map<Attribute, Double> adds = ModifyAttributeEventJS.pendingAdds.get(type);
                int n = adds == null ? 0 : adds.size();

                if (n > 0) {
                    for (Map.Entry<Attribute, Double> e : adds.entrySet()) {
                        Attribute a = e.getKey();
                        Double v = e.getValue();
                        if (v == null || v.isNaN()) {
                            merged.add(a);
                        } else {
                            merged.add(a, v);
                        }
                    }
                }
                FabricDefaultAttributeRegistry.register(type, merged);
            }
            for (CustomEntityJSBuilder candidate : CustomEntityBuilder.thisList) {
                if (!(candidate instanceof CustomEntityBuilder builder)) continue;
                if (!builder.isLivingEntityClass()) continue;
                EntityType<? extends LivingEntity> type = UtilsJS.cast(EntityJSUtils.getRegisteredEntityType(builder, "attribute"));
                if (type == null) continue;
                AttributeSupplier.Builder merged = builder.getAttributeBuilder();
                Map<Attribute, Double> adds = ModifyAttributeEventJS.pendingAdds.get(type);
                if (adds != null) {
                    for (Map.Entry<Attribute, Double> entry : adds.entrySet()) {
                        Double value = entry.getValue();
                        if (value == null || value.isNaN()) merged.add(entry.getKey());
                        else merged.add(entry.getKey(), value);
                    }
                }
                FabricDefaultAttributeRegistry.register(type, merged);
            }
        });
    }
    public static void postDataEvent(VirtualKubeJSDataPack pack, MultiPackResourceManager multiManager) {
        if (pack != null && multiManager != null) {
            // Unused
        }
    }
}
