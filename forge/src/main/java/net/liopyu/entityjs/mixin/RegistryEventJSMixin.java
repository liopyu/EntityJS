package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.EntityReflection;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
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

import static net.liopyu.entityjs.events.EntityModificationEventJS.createCustomMap;

@Mixin(value = RegistryEventJS.class, remap = false)
public class RegistryEventJSMixin<T> implements IRegistryJS {
    @Final
    @Shadow
    private RegistryInfo<T> registry;
    @Final
    @Shadow
    public List<BuilderBase<? extends T>> created;

    @Info(value = """
            Creates a new custom entity based on an existing entity class.
            This allows extending or modifying behavior of vanilla or modded entities dynamically.
            """)
    public CustomEntityBuilder entityJs$createCustom(String id, Class<? extends Entity> entityClass, Consumer<ModifyEntityBuilder> consumer) {
        return entityjs$createCustomResolved(id, entityClass, consumer);
    }

    @Info(value = """
            Creates a new custom entity based on an existing entity class name.
            This is the same as createCustom(id, Class, callback), but accepts a fully qualified class name string for scripter convenience.
            """)
    public CustomEntityBuilder entityJs$createCustom(String id, String entityClassName, Consumer<ModifyEntityBuilder> consumer) {
        Class<? extends Entity> entityClass = entityjs$resolveEntityClass(id, entityClassName);
        if (entityClass == null) {
            return null;
        }
        return entityjs$createCustomResolved(id, entityClass, consumer);
    }

    @Unique
    private CustomEntityBuilder entityjs$createCustomResolved(String id, Class<? extends Entity> entityClass, Consumer<ModifyEntityBuilder> consumer) {
        if (!Entity.class.isAssignableFrom(entityClass)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a class that does not extend Entity. Id: " + id);
            return null;
        }
        if (EntityReflection.isMixinClass(entityClass)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a Mixin class, which is intentionally hidden from createCustom(). Id: " + id + ", class: " + entityClass.getName());
            return null;
        }
        var rl = UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, KubeJS.appendModId(id));
        CustomEntityBuilder b = new CustomEntityBuilder(rl, entityClass);
        registry.addBuilder((BuilderBase<? extends T>) b);
        created.add((BuilderBase<? extends T>) b);
        Consumer<ModifyEntityBuilder> wrappedConsumer = CallbackInvoker.wrapConsumer(consumer);
        createCustomMap.put(rl, builder -> {
            wrappedConsumer.accept(builder);
            CallbackInvoker.wrapCallbackFields(builder);
        });
        return b;
    }

    @Unique
    private Class<? extends Entity> entityjs$resolveEntityClass(String id, String entityClassName) {
        Class<?> entityClass = EntityReflection.resolveClassName(id, "entity", entityClassName, true);
        if (entityClass == null) {
            return null;
        }
        if (!Entity.class.isAssignableFrom(entityClass)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a class name that does not extend Entity. Id: " + id + ", class: " + entityClass.getName());
            return null;
        }
        return entityClass.asSubclass(Entity.class);
    }
}
