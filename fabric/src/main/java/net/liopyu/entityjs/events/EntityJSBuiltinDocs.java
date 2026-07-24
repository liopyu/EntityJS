package net.liopyu.entityjs.events;

import net.liopyu.entityjs.common.events.*;

import com.probejs.docs.DocCompiler;
import com.probejs.features.plugin.DocGenerationEventJS;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.common.util.overrides.catalog.DynamicOverrideMethodCatalog;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

/**
 * Fabric's ProbeJS additions intentionally describe only the portable custom-entity API.
 * Loader-specific biome and spawn facilities stay out of the generated common surface.
 */
public final class EntityJSBuiltinDocs {
    private static final Set<DocGenerationEventJS> REGISTERED_EVENTS = java.util.Collections.newSetFromMap(new WeakHashMap<>());

    private EntityJSBuiltinDocs() {
    }

    public static void register(DocGenerationEventJS event) {
        if (!REGISTERED_EVENTS.add(event)) {
            return;
        }

        DocCompiler.CapturedClasses.capturedJavaClasses.add(CustomEntityBuilder.class);
        event.specialType("EntityJSOverrideMethodKey", overrideKeys());
        event.specialType("EntityJSDynamicOverrideContext", List.of(
                "{ entity: Internal.Entity; methodKey: Special.EntityJSOverrideMethodKey; args: any[]; superCall(...args: any[]): any; cancel(): void; setReturnValue(value: any): void }"
        ));
        event.transformDocument(CustomEntityBuilder.class, EntityJSBuiltinDocs::patchCustomEntityBuilder);
    }

    private static List<Object> overrideKeys() {
        Set<String> keys = new LinkedHashSet<>();
        Stream.of(Entity.class, LivingEntity.class, Mob.class, PathfinderMob.class)
                .flatMap(type -> DynamicOverrideMethodCatalog.overrideKeys(type.asSubclass(Entity.class)).stream())
                .sorted()
                .forEach(key -> keys.add("'" + key.replace("'", "\\\\'") + "'"));
        return keys.isEmpty() ? List.of("never") : keys.stream().map(Object.class::cast).toList();
    }

    private static void patchCustomEntityBuilder(DocumentClass documentClass) {
        for (DocumentMethod method : documentClass.methods) {
            if (!"override".equals(method.name) || method.params.isEmpty()) {
                continue;
            }
            boolean varArg = method.params.get(0).isVarArg();
            method.params.set(0, new PropertyParam(
                    "methodKey",
                    new PropertyType.Native("Special.EntityJSOverrideMethodKey"),
                    varArg
            ));
        }
    }
}
