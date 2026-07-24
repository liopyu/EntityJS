package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.EntityReflection;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.IRegistryJS;
import net.liopyu.entityjs.common.util.overrides.CallbackInvoker;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Consumer;

@Mixin(value = RegistryEventJS.class, remap = false)
public class RegistryEventJSMixin<T> implements IRegistryJS {
    @Final @Shadow private RegistryInfo<T> registry;
    @Final @Shadow public List<BuilderBase<? extends T>> created;

    @Override
    @Info("Creates a new custom entity based on an existing entity class.")
    public CustomEntityBuilder entityJs$createCustom(String id, Class<? extends Entity> entityClass, Consumer<ModifyEntityBuilder> consumer) {
        return entityjs$createCustomResolved(id, entityClass, consumer);
    }

    @Override
    @Info("Creates a new custom entity based on a fully-qualified entity class name.")
    public CustomEntityBuilder entityJs$createCustom(String id, String entityClassName, Consumer<ModifyEntityBuilder> consumer) {
        Class<?> type = EntityReflection.resolveClassName(id, "entity", entityClassName, true);
        if (type == null) return null;
        if (!Entity.class.isAssignableFrom(type)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create an entity from a class that does not extend Entity. Id: " + id);
            return null;
        }
        return entityjs$createCustomResolved(id, type.asSubclass(Entity.class), consumer);
    }

    @Unique
    private CustomEntityBuilder entityjs$createCustomResolved(String id, Class<? extends Entity> entityClass, Consumer<ModifyEntityBuilder> consumer) {
        if (!Entity.class.isAssignableFrom(entityClass)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create an entity from a class that does not extend Entity. Id: " + id);
            return null;
        }
        if (EntityReflection.isMixinClass(entityClass)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a Mixin class, which is intentionally hidden from createCustom(). Id: " + id + ", class: " + entityClass.getName());
            return null;
        }
        var resourceLocation = UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, KubeJS.appendModId(id));
        CustomEntityBuilder builder = new CustomEntityBuilder(resourceLocation, entityClass);
        registry.addBuilder((BuilderBase<? extends T>) builder);
        created.add((BuilderBase<? extends T>) builder);
        if (consumer != null) {
            EntityModificationEventJS.createCustomMap.put(resourceLocation, customBuilder -> {
                CallbackInvoker.wrapConsumer(consumer).accept(customBuilder);
                CallbackInvoker.wrapCallbackFields(customBuilder);
            });
        }
        return builder;
    }
}
