package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.vanilla.TridentJSBuilder;
import net.liopyu.entityjs.entities.nonliving.vanilla.TridentEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

public class TridentItemBuilder extends ProjectileItemBuilder {
    public transient SoundEvent throwSound;
    public transient SoundEvent riptide1Sound;
    public transient SoundEvent riptide2Sound;
    public transient SoundEvent riptide3Sound;

    public TridentItemBuilder(ResourceLocation i, BuilderBase<?> parent) {
        super(i, parent);
        this.maxDamage = 250;
        this.throwSound = SoundEvents.TRIDENT_THROW;
        this.projectileVelocity = 0;
        this.riptide1Sound = SoundEvents.TRIDENT_RIPTIDE_1;
        this.riptide2Sound = SoundEvents.TRIDENT_RIPTIDE_2;
        this.riptide3Sound = SoundEvents.TRIDENT_RIPTIDE_3;
        this.use = (p, l, h) -> true;
        this.useAnimation(UseAnim.SPEAR);
        this.maxStackSize = 1;
        var tag = Tags.Items.TOOLS_TRIDENTS.location();
        var tag2 = Tags.Items.TOOLS.location();
        this.tag(tag);
        this.tag(tag2);
    }

    @Info("Sets the sound event for the riptide level 3")
    public void setRiptide3Sound(SoundEvent riptide3Sound) {
        this.riptide3Sound = riptide3Sound;
    }

    @Info("Sets the sound event for the riptide level 2")
    public void setRiptide2Sound(SoundEvent riptide2Sound) {
        this.riptide2Sound = riptide2Sound;
    }

    @Info("Sets the sound event for the riptide level 1")
    public void setRiptide1Sound(SoundEvent riptide1Sound) {
        this.riptide1Sound = riptide1Sound;
    }

    @Info("Sets the sound event for throwing the item")
    public void setThrowSound(SoundEvent throwSound) {
        this.throwSound = throwSound;
    }


    @Override
    public Item createObject() {
        return new TridentItem(createItemProperties()) {
            @Override
            public int getUseDuration(ItemStack pStack) {
                return useDuration != null ? useDuration.applyAsInt(pStack) : 72000;
            }

            public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
                ItemStack $$3 = pPlayer.getItemInHand(pHand);
                if (canThrow) {
                    if (!pLevel.isClientSide) {
                        float pZ = projectileZ;
                        float pVelocity = projectileVelocity;
                        float pInaccuracy = projectileInaccuracy;
                        if (parent instanceof TridentJSBuilder builder) {
                            TridentEntityJS $$4 = new TridentEntityJS((builder), builder.get(), pPlayer, pLevel, $$3);
                            $$4.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), pZ, pVelocity, pInaccuracy);
                            pLevel.addFreshEntity($$4);
                        }
                    }
                    pPlayer.awardStat(Stats.ITEM_USED.get(this));
                    if (!pPlayer.getAbilities().instabuild) {
                        $$3.shrink(1);
                    }
                    return InteractionResultHolder.sidedSuccess($$3, pLevel.isClientSide());
                }
                if ($$3.getDamageValue() >= $$3.getMaxDamage() - 1) {
                    return InteractionResultHolder.fail($$3);
                } else if (EnchantmentHelper.getRiptide($$3) > 0 && !pPlayer.isInWaterOrRain()) {
                    return InteractionResultHolder.fail($$3);
                } else {
                    pPlayer.startUsingItem(pHand);
                    return InteractionResultHolder.consume($$3);
                }
            }

            @Override
            public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
                if (pEntityLiving instanceof Player $$4) {
                    int $$5 = this.getUseDuration(pStack) - pTimeLeft;
                    if ($$5 >= 10) {
                        int $$6 = EnchantmentHelper.getRiptide(pStack);
                        if ($$6 <= 0 || $$4.isInWaterOrRain()) {
                            if (!pLevel.isClientSide) {
                                pStack.hurtAndBreak(1, $$4, (p_43388_) -> {
                                    p_43388_.broadcastBreakEvent(pEntityLiving.getUsedItemHand());
                                });
                                if ($$6 == 0) {
                                    float pZ = projectileZ;
                                    float pVelocity = projectileVelocity;
                                    float pInaccuracy = projectileInaccuracy;
                                    TridentEntityJS $$7 = new TridentEntityJS((TridentJSBuilder) parent, (EntityType<? extends TridentEntityJS>) parent.get(), pEntityLiving, pLevel, pStack);
                                    $$7.shootFromRotation($$4, $$4.getXRot(), $$4.getYRot(), pZ, (2.5F + (float) $$6 * 0.5F) + pVelocity, pInaccuracy);
                                    if ($$4.getAbilities().instabuild) {
                                        $$7.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                                    }

                                    pLevel.addFreshEntity($$7);
                                    pLevel.playSound((Player) null, $$7, throwSound, SoundSource.PLAYERS, 1.0F, 1.0F);
                                    if (!$$4.getAbilities().instabuild) {
                                        $$4.getInventory().removeItem(pStack);
                                    }
                                }
                            }

                            $$4.awardStat(Stats.ITEM_USED.get(this));
                            if ($$6 > 0) {
                                float $$8 = $$4.getYRot();
                                float $$9 = $$4.getXRot();
                                float $$10 = -Mth.sin($$8 * 0.017453292F) * Mth.cos($$9 * 0.017453292F);
                                float $$11 = -Mth.sin($$9 * 0.017453292F);
                                float $$12 = Mth.cos($$8 * 0.017453292F) * Mth.cos($$9 * 0.017453292F);
                                float $$13 = Mth.sqrt($$10 * $$10 + $$11 * $$11 + $$12 * $$12);
                                float $$14 = 3.0F * ((1.0F + (float) $$6) / 4.0F);
                                $$10 *= $$14 / $$13;
                                $$11 *= $$14 / $$13;
                                $$12 *= $$14 / $$13;
                                $$4.push((double) $$10, (double) $$11, (double) $$12);
                                $$4.startAutoSpinAttack(20);
                                if ($$4.isOnGround()) {
                                    float $$15 = 1.1999999F;
                                    $$4.move(MoverType.SELF, new Vec3(0.0, 1.1999999284744263, 0.0));
                                }

                                SoundEvent $$18;
                                if ($$6 >= 3) {
                                    $$18 = riptide3Sound;
                                } else if ($$6 == 2) {
                                    $$18 = riptide2Sound;
                                } else {
                                    $$18 = riptide1Sound;
                                }

                                pLevel.playSound((Player) null, $$4, $$18, SoundSource.PLAYERS, 1.0F, 1.0F);
                            }

                        }
                    }
                }
            }
        };
    }

}
