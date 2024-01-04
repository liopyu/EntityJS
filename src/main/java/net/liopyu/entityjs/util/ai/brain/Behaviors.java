package net.liopyu.entityjs.util.ai.brain;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BecomePassiveIfMemoryPresent;
import net.minecraft.world.entity.ai.behavior.DoNothing;
import net.minecraft.world.entity.ai.behavior.EraseMemoryIf;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.function.Predicate;

// These are all so awful to work with and expect things to exist,
//   something which is not guaranteed in modded due to deferred
//                         registration!
//
//        I hate every part of this system with a passion
//
//                   Brains were a mistake
public enum Behaviors {
    INSTANCE;

    public BecomePassiveIfMemoryPresent becomePassiveIfMemoryPresent(MemoryModuleType<?> memoryType, int pacifyDuration) {
        return new BecomePassiveIfMemoryPresent(memoryType, pacifyDuration);
    }

    public DoNothing doNothing(int minTime, int maxTime) {
        return new DoNothing(minTime, maxTime);
    }

    public <E extends LivingEntity> EraseMemoryIf<E> eraseMemoryIf(Predicate<E> predicate, MemoryModuleType<?> memoryType) {
        return new EraseMemoryIf<>(predicate, memoryType);
    }
}
