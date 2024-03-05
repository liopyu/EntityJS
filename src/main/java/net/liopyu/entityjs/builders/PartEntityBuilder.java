package net.liopyu.entityjs.builders;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.entities.PartEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.entity.PartEntity;

import java.util.ArrayList;
import java.util.List;

public abstract class PartEntityBuilder<T extends PartEntity<PartEntityJS>> extends BaseEntityBuilder<T> {
    private final T parent;
    public static final List<BaseLivingEntityBuilder<?>> thisList = new ArrayList<>();

    public PartEntityBuilder(ResourceLocation i, T parent) {
        super(i);
        this.parent = parent;
    }

    @Override
    abstract public EntityType.EntityFactory<T> factory();

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }
}
