package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseProjectileBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.item.ProjectileItemBuilder;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Interface;

public interface IProjectileEntityJS {
    ProjectileEntityBuilder<?> getProjectileBuilder();

}
