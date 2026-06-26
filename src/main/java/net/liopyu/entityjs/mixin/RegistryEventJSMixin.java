package net.liopyu.entityjs.mixin;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryKubeEvent;
import dev.latvian.mods.kubejs.registry.RegistryObjectStorage;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.SourceLine;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.EntityReflection;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.util.overrides.CallbackInvoker;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.IRegistryJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Consumer;

import static net.liopyu.entityjs.events.EntityModificationEventJS.createCustomMap;

@Mixin(RegistryKubeEvent.class)
public class RegistryEventJSMixin<T> implements IRegistryJS {
    @Final
    @Shadow
    private ResourceKey<Registry<EntityType<?>>> registryKey;
    @Final
    @Shadow
    public List<BuilderBase<? extends T>> created;

    @Shadow
    private <R> void addBuilder(BuilderBase<? extends R> builder) {
        if (builder == null) {
            throw new IllegalArgumentException("Can't add null builder in registry '" + builder.registryKey.location() + "'!");
        }

        if (DevProperties.get().logRegistryEventObjects) {
            ConsoleJS.STARTUP.info("~ " + builder.registryKey.location() + " | " + builder.id);
        }

        var objStorage = RegistryObjectStorage.of(builder.registryKey);

        if (objStorage.objects.containsKey(builder.id)) {
            throw new IllegalArgumentException("Duplicate key '" + builder.id + "' in registry '" + builder.registryKey.location() + "'!");
        }

        objStorage.objects.put(builder.id, (BuilderBase) builder);
        RegistryObjectStorage.ALL_BUILDERS.add(builder);

        // registry.deferredRegister.register()
    }

    @Info(value = """
            Creates a new custom entity based on an existing entity class.
            This allows extending or modifying behavior of vanilla or modded entities dynamically.
            
            The builder provided in the callback can be used to directly access the respective entity's modification builder.
            
            Example usage:
            ```javascript
            let Villager = Java.loadClass("net.minecraft.world.entity.npc.Villager")
            event.createCustom('wyrm', Villager, modifyBuilder => {
                modifyBuilder.tick(entity => {
                    console.log(entity.type)
                })
            })
            ```
            """
    )
    public CustomEntityBuilder entityJs$createCustom(KubeResourceLocation id, Class<? extends Entity> entityClass, Consumer<ModifyEntityBuilder> consumer) {
        return entityjs$createCustomResolved(id, entityClass, consumer);
    }

    @Info(value = """
            Creates a new custom entity based on an existing entity class name.
            This is the same as createCustom(id, Class, callback), but accepts a fully qualified class name string for scripter convenience.

            Example usage:
            ```javascript
            event.createCustom('wyrm', 'net.minecraft.world.entity.monster.Zombie', modifyBuilder => {
                modifyBuilder.tick(entity => {
                    console.log(entity.type)
                })
            })
            ```
            """
    )
    public CustomEntityBuilder entityJs$createCustom(KubeResourceLocation id, String entityClassName, Consumer<ModifyEntityBuilder> consumer) {
        Class<? extends Entity> entityClass = entityjs$resolveEntityClass(id, entityClassName);
        if (entityClass == null) {
            return null;
        }
        return entityjs$createCustomResolved(id, entityClass, consumer);
    }

    @Unique
    private CustomEntityBuilder entityjs$createCustomResolved(KubeResourceLocation id, Class<? extends Entity> entityClass, Consumer<ModifyEntityBuilder> consumer) {
        if (!Entity.class.isAssignableFrom(entityClass)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a class that does not extend Entity. Id: " + id);
            return null;
        }
        if (EntityReflection.isMixinClass(entityClass)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a Mixin class, which is intentionally hidden from createCustom(). Id: " + id + ", class: " + entityClass.getName());
            return null;
        }
        var rl = id.wrapped();
        CustomEntityBuilder b = null;
        if (Entity.class.isAssignableFrom(entityClass)) {
            b = new CustomEntityBuilder(rl, (Class<? extends Entity>) entityClass);
        }
        if (b == null) {
            EntityJSHelperClass.logErrorMessageOnce("CustomEntityBuilder is null for entity id: " + id);
            return null;
        }
        b.sourceLine = SourceLine.UNKNOWN;
        b.registryKey = registryKey;
        this.addBuilder((BuilderBase<? extends T>) b);
        created.add((BuilderBase<? extends T>) b);
        Consumer<ModifyEntityBuilder> wrappedConsumer = CallbackInvoker.wrapConsumer(consumer);
        createCustomMap.put(rl, builder -> {
            wrappedConsumer.accept(builder);
            CallbackInvoker.wrapCallbackFields(builder);
        });
        return b;
    }

    @Unique
    private Class<? extends Entity> entityjs$resolveEntityClass(KubeResourceLocation id, String entityClassName) {
        if (entityClassName == null || entityClassName.isBlank()) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a blank class name. Id: " + id);
            return null;
        }
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = RegistryEventJSMixin.class.getClassLoader();
            }
            Class<?> entityClass = Class.forName(entityClassName, false, classLoader);
            if (!Entity.class.isAssignableFrom(entityClass)) {
                EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a class name that does not extend Entity. Id: " + id + ", class: " + entityClassName);
                return null;
            }
            return entityClass.asSubclass(Entity.class);
        } catch (ClassNotFoundException e) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from an unknown class name. Id: " + id + ", class: " + entityClassName);
            return null;
        } catch (LinkageError e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("Tried to create entity from a class name that could not be loaded. Id: " + id + ", class: " + entityClassName, e);
            return null;
        }
    }
}
