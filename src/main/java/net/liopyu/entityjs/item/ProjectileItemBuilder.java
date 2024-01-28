package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
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
    public transient String texture;
    public transient boolean canThrow;
    public transient float projectileZ;
    public transient float projectileVelocity;
    public transient float projectileInaccuracy;

    public ProjectileItemBuilder(ResourceLocation i, ProjectileEntityJSBuilder parent) {
        super(i);
        this.parent = parent;
        texture = "kubejs:item/" + i.getPath();
        canThrow = false;
        projectileZ = 0.0F;
        projectileVelocity = 1.5F;
        projectileInaccuracy = 1.0F;
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

    public ItemBuilder canThrow(boolean canThrow) {
        this.canThrow = canThrow;
        return this;
    }

    public ItemBuilder projectileZ(float projectileZ) {

        this.projectileZ = projectileZ;
        return this;
    }

    public ItemBuilder projectileVelocity(float projectileVelocity) {

        this.projectileVelocity = projectileVelocity;
        return this;
    }

    public ItemBuilder projectileInaccuracy(float projectileInaccuracy) {

        this.projectileInaccuracy = projectileInaccuracy;
        return this;
    }

    @Override
    public ItemBuilder texture(String tex) {
        this.texture = tex;
        return this;
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        if (modelJson != null) {
            generator.json(AssetJsonGenerator.asItemModelLocation(id), modelJson);
            return;
        }

        generator.itemModel(id, m -> {
            m.parent(id.getPath());

            if (!parentModel.isEmpty()) {
                m.parent(parentModel);

                if (texture.isEmpty()) {
                    texture(newID("item/", "").toString());
                }
                m.texture("layer0", texture);
            } else {
                m.parent("item/generated");

                if (texture.isEmpty()) {
                    texture(newID("item/", "").toString());
                }
                m.texture("layer0", texture);
            }
        });
    }

}
