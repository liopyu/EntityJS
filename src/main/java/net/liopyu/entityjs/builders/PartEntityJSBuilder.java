package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.BaseLivingEntityJS;
import net.liopyu.entityjs.entities.PartEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.entity.PartEntity;

public class PartEntityJSBuilder extends PartEntityBuilder<PartEntity<PartEntityJS>> {
    public PartEntityJSBuilder(ResourceLocation i, PartEntityJS parent) {
        super(i, parent);
    }

    @Override
    public EntityType.EntityFactory<PartEntity<PartEntityJS>> factory() {
        return (parentMob, name, size) -> new PartEntityJS(this, parentMob, name, size);
    }
}
