package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ProjectileEntityJS extends AbstractHurtingProjectile implements IProjectileEntityJS {


    public ProjectileEntityJSBuilder builder;

    public ProjectileEntityJS(ProjectileEntityJSBuilder builder, EntityType<? extends AbstractHurtingProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    public ProjectileEntityJS(EntityType<? extends AbstractHurtingProjectile> pEntityType, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, Level pLevel, ProjectileEntityJSBuilder builder) {
        super(pEntityType, pShooter, pOffsetX, pOffsetY, pOffsetZ, pLevel);
        this.builder = builder;
        super.setOwner(pShooter);
        super.setRot(pShooter.getYRot(), pShooter.getXRot());
        super.setPos(pShooter.getX(), pShooter.getY(), pShooter.getZ());
        super.reapplyPosition();
    }


    @Override
    public ProjectileEntityBuilder<?> getProjectileBuilder() {
        return builder;
    }


    @Override
    protected void defineSynchedData() {

    }

}
