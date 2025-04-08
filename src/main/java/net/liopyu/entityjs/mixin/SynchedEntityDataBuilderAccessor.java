package net.liopyu.entityjs.mixin;

import net.minecraft.network.syncher.SynchedEntityData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchedEntityData.Builder.class)
public interface SynchedEntityDataBuilderAccessor {
    @Accessor("itemsById")
    SynchedEntityData.DataItem<?>[] entityJs$getItemsById();
}
