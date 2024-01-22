package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.entities.ProjectileEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;


public class ProjectileItemBuilder extends ItemBuilder {
    public transient final ProjectileEntityJSBuilder parent;
    public transient String texture;
    public transient InteractionResultHolder<ItemStack> use;

    public ProjectileItemBuilder(ResourceLocation i, ProjectileEntityJSBuilder parent) {
        super(i);
        this.parent = parent;
        texture = "kubejs:item/" + i.getPath();
        use = null;
    }

    @Override
    public Item createObject() {
        return new Item(createItemProperties()) {
            @Override
            public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
                ItemStack $$3 = pPlayer.getItemInHand(pHand);
                /*
                pLevel.playSound((Player) null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
                if (!pLevel.isClientSide) {
                    ProjectileEntityJS $$4 = new ProjectileEntityJS(parent.get(), pPlayer, pLevel);
                    $$4.setItem($$3);
                    $$4.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), 0.0F, 1.5F, 1.0F);
                    pLevel.addFreshEntity($$4);
                }

                pLevel.explode(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), 1.0F, false, Explosion.BlockInteraction.BREAK);
                if (!pPlayer.getAbilities().instabuild) {
                    $$3.shrink(1);
                }*/
                if (use != null) {

                    return use;
                }

                return InteractionResultHolder.sidedSuccess($$3, pLevel.isClientSide());
            }
        };
    }

    public ProjectileItemBuilder use(InteractionResultHolder<ItemStack> use) {
        this.use = use;
        return this;
    }

    @Override
    public ItemBuilder use(UseCallback use) {
        return super.use(use);
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
