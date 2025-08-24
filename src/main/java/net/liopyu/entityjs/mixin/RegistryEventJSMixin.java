package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryEventJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.util.implementation.IRegistryJS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Consumer;

import static net.liopyu.entityjs.events.EntityModificationEventJS.createCustomMap;

@Mixin(RegistryEventJS.class)
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
    public CustomEntityBuilder entityJs$createCustom(String id, Class<? extends Entity> entityClass, Consumer<ModifyEntityBuilder> consumer) {
        if (!Entity.class.isAssignableFrom(entityClass)) {
            throw new IllegalArgumentException("Tried to create entity from a class that does not extend Entity. Id: " + id);
        }
        var rl = UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, KubeJS.appendModId(id));
        CustomEntityBuilder b = null;
        if (LivingEntity.class.isAssignableFrom(entityClass)) {
            b = new CustomEntityBuilder(rl, (Class<? extends LivingEntity>) entityClass);
        }
        if (b == null) {
            throw new IllegalArgumentException("CustomEntityBuilder is null for entity id: " + id);
        }
        registry.addBuilder((BuilderBase<? extends T>) b);
        created.add((BuilderBase<? extends T>) b);
        createCustomMap.put(rl, consumer);
        return b;
    }
}
