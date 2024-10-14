package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.vanilla.TridentJSBuilder;
import net.liopyu.entityjs.entities.nonliving.vanilla.TridentEntityJS;
import net.minecraft.core.Holder;
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
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class TridentItemBuilder extends ProjectileItemBuilder {
    public transient Holder<SoundEvent> throwSound;
    public transient Holder<SoundEvent> riptide1Sound;
    public transient Holder<SoundEvent> riptide2Sound;
    public transient Holder<SoundEvent> riptide3Sound;

    public TridentItemBuilder(ResourceLocation i, BuilderBase<?> parent) {
        super(i, parent);
        this.maxDamage = 1000;
        this.throwSound = SoundEvents.TRIDENT_THROW;
        this.projectileVelocity = 0;
        this.riptide1Sound = SoundEvents.TRIDENT_RIPTIDE_1;
        this.riptide2Sound = SoundEvents.TRIDENT_RIPTIDE_2;
        this.riptide3Sound = SoundEvents.TRIDENT_RIPTIDE_3;
        this.use = (p, l, h) -> true;
        this.useAnimation(UseAnim.SPEAR);
        var tag = ItemTags.TRIDENT_ENCHANTABLE.location();
        this.tag(new ResourceLocation[]{tag});
    }

    @Info("Sets the sound event for the riptide level 3")
    public void setRiptide3Sound(SoundEvent riptide3Sound) {
        this.riptide3Sound = Holder.direct(riptide3Sound);
    }

    @Info("Sets the sound event for the riptide level 2")
    public void setRiptide2Sound(SoundEvent riptide2Sound) {
        this.riptide2Sound = Holder.direct(riptide2Sound);
    }

    @Info("Sets the sound event for the riptide level 1")
    public void setRiptide1Sound(SoundEvent riptide1Sound) {
        this.riptide1Sound = Holder.direct(riptide1Sound);
    }

    @Info("Sets the sound event for throwing the item")
    public void setThrowSound(SoundEvent throwSound) {
        this.throwSound = Holder.direct(throwSound);
    }

    @Override
    public Item createObject() {
        return new TridentItem(createItemProperties()) {

            @Override
            public void releaseUsing(ItemStack p_43394_, Level p_43395_, LivingEntity p_43396_, int p_43397_) {
                if (p_43396_ instanceof Player player) {
                    int i = this.getUseDuration(p_43394_, p_43396_) - p_43397_;
                    if (i >= 10) {
                        float f = EnchantmentHelper.getTridentSpinAttackStrength(p_43394_, player);
                        if (!(f > 0.0F) || player.isInWaterOrRain()) {
                            if (!isTooDamagedToUse(p_43394_)) {
                                Holder<SoundEvent> holder = EnchantmentHelper.pickHighestLevel(p_43394_, EnchantmentEffectComponents.TRIDENT_SOUND)
                                        .orElse(SoundEvents.TRIDENT_THROW);

                                Holder<SoundEvent> sound;
                                if (SoundEvents.TRIDENT_RIPTIDE_3 == holder.value()) {
                                    sound = riptide3Sound;
                                } else if (SoundEvents.TRIDENT_RIPTIDE_2 == holder.value()) {
                                    sound = riptide2Sound;
                                } else if (SoundEvents.TRIDENT_RIPTIDE_1 == holder.value()) {
                                    sound = riptide1Sound;
                                } else sound = throwSound;
                                if (!p_43395_.isClientSide) {
                                    p_43394_.hurtAndBreak(1, player, LivingEntity.getSlotForHand(p_43396_.getUsedItemHand()));
                                    if (f == 0.0F) {
                                        float pZ = projectileZ;
                                        float pVelocity = projectileVelocity;
                                        float pInaccuracy = projectileInaccuracy;
                                        TridentEntityJS throwntrident = new TridentEntityJS((TridentJSBuilder) parent, (EntityType<? extends TridentEntityJS>) parent.get(), p_43396_, p_43395_, p_43394_);
                                        throwntrident.shootFromRotation(player, player.getXRot(), player.getYRot(), pZ, 2.5F + pVelocity, pInaccuracy);
                                        if (player.hasInfiniteMaterials()) {
                                            throwntrident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                                        }

                                        p_43395_.addFreshEntity(throwntrident);

                                        p_43395_.playSound(null, throwntrident, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                                        if (!player.hasInfiniteMaterials()) {
                                            player.getInventory().removeItem(p_43394_);
                                        }
                                    }
                                }

                                player.awardStat(Stats.ITEM_USED.get(this));
                                if (f > 0.0F) {
                                    float f7 = player.getYRot();
                                    float f1 = player.getXRot();
                                    float f2 = -Mth.sin(f7 * (float) (Math.PI / 180.0)) * Mth.cos(f1 * (float) (Math.PI / 180.0));
                                    float f3 = -Mth.sin(f1 * (float) (Math.PI / 180.0));
                                    float f4 = Mth.cos(f7 * (float) (Math.PI / 180.0)) * Mth.cos(f1 * (float) (Math.PI / 180.0));
                                    float f5 = Mth.sqrt(f2 * f2 + f3 * f3 + f4 * f4);
                                    f2 *= f / f5;
                                    f3 *= f / f5;
                                    f4 *= f / f5;
                                    player.push((double) f2, (double) f3, (double) f4);
                                    player.startAutoSpinAttack(20, 8.0F, p_43394_);
                                    if (player.onGround()) {
                                        float f6 = 1.1999999F;
                                        player.move(MoverType.SELF, new Vec3(0.0, 1.1999999F, 0.0));
                                    }

                                    p_43395_.playSound(null, player, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                                }
                            }
                        }
                    }
                }

            }

            ;

            @Override
            public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand p_43407_) {
                ItemStack itemstack = pPlayer.getItemInHand(p_43407_);
                if (canThrow) {
                    if (!pLevel.isClientSide) {
                        float pZ = projectileZ;
                        float pVelocity = projectileVelocity;
                        float pInaccuracy = projectileInaccuracy;
                        if (parent instanceof TridentJSBuilder builder) {
                            TridentEntityJS $$4 = new TridentEntityJS((builder), builder.get(), pPlayer, pLevel, itemstack);
                            $$4.shootFromRotation(pPlayer, pPlayer.getXRot(), pPlayer.getYRot(), pZ, pVelocity, pInaccuracy);
                            pLevel.addFreshEntity($$4);
                        }
                    }
                    pPlayer.awardStat(Stats.ITEM_USED.get(this));
                    if (!pPlayer.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }
                    return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
                }
                return super.use(pLevel, pPlayer, p_43407_);
            }

        };
    }
}