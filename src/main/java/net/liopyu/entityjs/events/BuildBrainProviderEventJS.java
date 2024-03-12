package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.allay.Allay;

import java.util.ArrayList;
import java.util.List;

@Info(value = """
        This event is fired during entity creation and is responsible
        for adding the `MemoryModuleType` and `SensorType`s the used
        by the entity.
                
        This is only posted for entities made through a builder
        """)
public class BuildBrainProviderEventJS<T extends LivingEntity> extends EventJS {

    private final List<MemoryModuleType<?>> memories;
    private final List<SensorType<? extends Sensor<? super LivingEntity>>> sensors;

    public BuildBrainProviderEventJS() {
        memories = new ArrayList<>();
        sensors = new ArrayList<>();
    }

    @Info(value = "Adds the provided `MemoryModuleType` to the entity type's memories")
    public void addMemory(MemoryModuleType<?> memory) {
        memories.add(memory);
    }

    @Info(value = "Adds the provided `SensorType` to the entity type's sensors")
    public void addSensor(SensorType<? extends Sensor<? super LivingEntity>> sensor) {
        sensors.add(sensor);
    }

    public Brain.Provider<T> provide() {
        return Brain.provider(memories, sensors);
    }
}