package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.ArrayList;
import java.util.List;


public abstract class ArrowEntityBuilder<T extends AbstractArrow & IArrowEntityJS> extends ProjectileEntityBuilder<T> {
    public static final List<ArrowEntityBuilder<?>> thisList = new ArrayList<>();

    public ArrowEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
    }

    @Override
    public EntityType<T> createObject() {
        return new ProjectileEntityTypeBuilder<>(this).get();
    }
}

