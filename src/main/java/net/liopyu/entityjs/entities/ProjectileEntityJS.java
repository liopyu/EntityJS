package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityJSBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ProjectileEntityJS extends ThrowableItemProjectile implements IProjectileEntityJS, ItemSupplier {


    public ProjectileEntityJSBuilder builder;

    public ProjectileEntityJS(ProjectileEntityJSBuilder builder, EntityType<? extends ThrowableItemProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    public ProjectileEntityJS(EntityType<? extends ThrowableItemProjectile> pEntityType, LivingEntity pShooter, Level pLevel) {
        super(pEntityType, pShooter, pLevel);

    }

    @Override
    public ProjectileEntityBuilder<?> getProjectileBuilder() {
        return builder;
    }

    /*@Override
    public void shootFromRotation(Entity pShooter, float pX, float pY, float pZ, float pVelocity, float pInaccuracy) {

        if (builder != null) {
            // Ensure builder is not null before accessing it
            super.shootFromRotation(pShooter, pX, pY, pZ, pVelocity, pInaccuracy);
        } else {
            // Handle the case where builder is null (throw an exception, log a message, etc.)
            // Example: throw new IllegalStateException("ProjectileEntityBuilder is not set.");
        }

    }*/

    @Override
    protected Item getDefaultItem() {
        return null;
    }

    @Override
    public ItemStack getItem() {
        return null;
    }
}
