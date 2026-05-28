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
import dev.latvian.mods.kubejs.util.KubeIdentifier;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.IRegistryJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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
            Creates a new custom entity based on an existing living entity class.
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
    public CustomEntityBuilder entityJs$createCustom(KubeIdentifier id, Class<? extends LivingEntity> entityClass, Consumer<ModifyEntityBuilder> consumer) {
        if (!LivingEntity.class.isAssignableFrom(entityClass)) {
            EntityJSHelperClass.logErrorMessageOnce("Tried to create entity from a class that does not extend LivingEntity. Id: " + id);
            return null;
        }
        var rl = id.wrapped();
        CustomEntityBuilder b = null;
        if (LivingEntity.class.isAssignableFrom(entityClass)) {
            b = new CustomEntityBuilder(rl, (Class<? extends LivingEntity>) entityClass);
        }
        if (b == null) {
            EntityJSHelperClass.logErrorMessageOnce("CustomEntityBuilder is null for entity id: " + id);
            return null;
        }
        b.sourceLine = SourceLine.UNKNOWN;
        b.registryKey = registryKey;
        this.addBuilder((BuilderBase<? extends T>) b);
        created.add((BuilderBase<? extends T>) b);
        createCustomMap.put(rl, consumer);
        return b;
    }
}