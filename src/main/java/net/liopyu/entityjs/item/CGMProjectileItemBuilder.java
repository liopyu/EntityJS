package net.liopyu.entityjs.item;

import com.mrcrayfish.guns.item.AmmoItem;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.modded.CGMProjectileEntityJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileEntityJS;
import net.liopyu.entityjs.entities.nonliving.modded.CGMProjectileEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class CGMProjectileItemBuilder extends ItemBuilder {
    public transient final CGMProjectileEntityJSBuilder parent;
    public transient boolean canThrow;
    public transient float projectileZ;
    public transient float projectileVelocity;
    public transient float projectileInaccuracy;

    public CGMProjectileItemBuilder(ResourceLocation i, CGMProjectileEntityJSBuilder parent) {
        super(i);
        this.parent = parent;
        canThrow = false;
        projectileZ = 0.0F;
        projectileVelocity = 1.5F;
        projectileInaccuracy = 1.0F;
        texture = i.getNamespace() + ":item/" + i.getPath();
    }

    @Override
    public AmmoItem createObject() {
        return new AmmoItem(createItemProperties()) {

            /*@Override
            public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
                if (canThrow) {
                    ItemStack $$3 = pPlayer.getItemInHand(pUsedHand);
                    if (!pLevel.isClientSide) {
                        float pZ = projectileZ;
                        float pVelocity = projectileVelocity;
                        float pInaccuracy = projectileInaccuracy;
                        CGMProjectileEntityJS $$4 = new CGMProjectileEntityJS(parent, parent.get(), pLevel, pPlayer, this.getDefaultInstance(), null, null);
                        $$4.setItem($$3);
                        $$4.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), pZ, pVelocity, pInaccuracy);
                        pLevel.addFreshEntity($$4);
                    }
                    pPlayer.awardStat(Stats.ITEM_USED.get(this));
                    if (!pPlayer.getAbilities().instabuild) {
                        $$3.shrink(1);
                    }
                    return InteractionResultHolder.sidedSuccess($$3, pLevel.isClientSide());
                }
                return super.use(pLevel, pPlayer, pUsedHand);
            }*/
        };
    }

    @Info(value = """
            Sets whether the item can be thrown.
                        
            @param canThrow True if the item can be thrown, false otherwise.
                        
            Example usage:
            ```javascript
            itemBuilder.canThrow(true);
            ```
            """)
    public ItemBuilder canThrow(boolean canThrow) {
        this.canThrow = canThrow;
        return this;
    }


    @Info(value = """
            Sets the Z offset for the projectile.
                        
            @param projectileZ The Z offset for the projectile.
                        
            Example usage:
            ```javascript
            itemBuilder.projectileZ(0.5f);
            ```
            """)
    public ItemBuilder projectileZ(float projectileZ) {
        this.projectileZ = projectileZ;
        return this;
    }

    @Info(value = """
            Sets the velocity of the projectile.
                        
            @param projectileVelocity The velocity of the projectile.
                        
            Example usage:
            ```javascript
            itemBuilder.projectileVelocity(1.5f);
            ```
            """)
    public ItemBuilder projectileVelocity(float projectileVelocity) {
        this.projectileVelocity = projectileVelocity;
        return this;
    }

    @Info(value = """
            Sets the inaccuracy of the projectile.
                        
            @param projectileInaccuracy The inaccuracy of the projectile.
                        
            Example usage:
            ```javascript
            itemBuilder.projectileInaccuracy(0.1f);
            ```
            """)
    public ItemBuilder projectileInaccuracy(float projectileInaccuracy) {
        this.projectileInaccuracy = projectileInaccuracy;
        return this;
    }
}
