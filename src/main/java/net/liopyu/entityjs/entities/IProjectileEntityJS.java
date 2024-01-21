package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Interface;

public interface IProjectileEntityJS {
    ProjectileEntityBuilder<?> getBuilder();

}
