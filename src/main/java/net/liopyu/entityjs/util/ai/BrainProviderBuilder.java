package net.liopyu.entityjs.util.ai;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;

import java.util.ArrayList;
import java.util.List;

/*
 * This is awful and I hate it, I am not convinced Brains are worthwhile
 *
 * In vanilla, there are like 9 entity types that implement an actual brain
 */
public class BrainProviderBuilder {

    private final ResourceLocation entityTypeId;
    private final List<ResourceLocation> memories;
    private final List<ResourceLocation> sensors;

    public BrainProviderBuilder(ResourceLocation entityTypeId) {
        memories = new ArrayList<>();
        sensors = new ArrayList<>();
        this.entityTypeId = entityTypeId;
    }

    @Info(value = "Adds a new memory module type to the brain provider's list of memory module types")
    public BrainProviderBuilder addMemoryModuleType(ResourceLocation memoryModule) {
        memories.add(memoryModule);
        return this;
    }

    @Info(value = "Adds a new sensor type to the brain provider's list of sensor types")
    public BrainProviderBuilder addSensorType(ResourceLocation sensor) {
        sensors.add(sensor);
        return this;
    }

    @HideFromJS
    public Brain.Provider<?> build() {
        final List<MemoryModuleType<?>> memoryModuleTypes = new ArrayList<>();
        for (ResourceLocation memoryType : memories) {
            memoryModuleTypes.add(Registry.MEMORY_MODULE_TYPE.get(memoryType));
        }
        final List<SensorType<? extends Sensor<? super LivingEntity>>> sensorTypes = new ArrayList<>();
        for (ResourceLocation sensorType : sensors) {
            sensorTypes.add(UtilsJS.cast(Registry.SENSOR_TYPE.get(sensorType)));
        }
        return Brain.provider(memoryModuleTypes, sensorTypes);
    }

    @Override
    public String toString() {
        return "BrainProviderBuilder[" + entityTypeId + "]{MemoryModuleTypes=" + memories + ", SensorTypes=" + sensors + "}";
    }
}
