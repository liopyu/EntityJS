package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.entities.ProjectileEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class ProjectileItemBuilder extends ItemBuilder {
    public transient final ProjectileEntityJSBuilder parent;
    public transient boolean canThrow;
    public transient float projectileZ;
    public transient float projectileVelocity;
    public transient float projectileInaccuracy;
    public transient String texture;

    public ProjectileItemBuilder(ResourceLocation i, ProjectileEntityJSBuilder parent) {
        super(i);
        this.parent = parent;
        canThrow = false;
        projectileZ = 0.0F;
        projectileVelocity = 1.5F;
        projectileInaccuracy = 1.0F;
        texture = parent.id.getNamespace() + ":item/" + parent.id.getPath();
    }

    @Override
    public Item createObject() {
        return new Item(createItemProperties()) {
            @Override
            public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
                if (canThrow) {
                    ItemStack $$3 = pPlayer.getItemInHand(pUsedHand);
                    if (!pLevel.isClientSide) {
                        float pZ = projectileZ;
                        float pVelocity = projectileVelocity;
                        float pInaccuracy = projectileInaccuracy;
                        ProjectileEntityJS $$4 = new ProjectileEntityJS(parent.get(), pPlayer, pLevel);
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
            }
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

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        if (modelJson != null) {
            generator.json(AssetJsonGenerator.asItemModelLocation(id), modelJson);
            return;
        }
        generator.itemModel(id, m -> {
            if (!parentModel.isEmpty()) {
                m.parent(parentModel);

                if (textureJson.size() == 0) {
                    texture(newID("item/", "").toString());
                }
                m.textures(textureJson);
            } else {
                m.parent("item/generated");

                if (textureJson.size() != 0) {
                    m.textures(textureJson);
                }
            }
        });
    }
}
