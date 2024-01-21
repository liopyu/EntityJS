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
    }


    @Override
    public ProjectileEntityBuilder<?> getBuilder() {
        return builder;
    }


    @Override
    protected void defineSynchedData() {

    }

    /*@Override
    protected boolean tryPickup(Player p_150121_) {
        if (builder.tryPickup != null) {
            if (builder.tryPickup.getAsBoolean()) {
                if (!p_150121_.getAbilities().instabuild) {
                    p_150121_.getInventory().add(this.getPickupItem());
                    return builder.tryPickup.getAsBoolean();
                }
            } else return builder.tryPickup.getAsBoolean();
        }
        return super.tryPickup(p_150121_);
    }*/

    /*@Override
    protected ItemStack getPickupItem() {
        return pickUpStack;
    }*/

}
