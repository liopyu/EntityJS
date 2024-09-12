package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileEntityJS;
import net.liopyu.entityjs.entities.nonliving.vanilla.EyeOfEnderEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;

public class EyeOfEnderItemBuilder extends ItemBuilder {
    public transient final EyeOfEnderJSBuilder parent;
    public transient boolean triggersCriteria;
    public transient Player sPlayer;
    public transient SoundEvent soundEvent;
    public transient SoundSource soundSource;
    public transient float soundVolume;
    public transient float soundPitch;
    public transient boolean overrideSound;
    public transient Function<ContextUtils.ItemUseContext, Object> signalTo;

    public EyeOfEnderItemBuilder(ResourceLocation i, EyeOfEnderJSBuilder parent) {
        super(i);
        this.parent = parent;
        baseTexture = i.getNamespace() + ":item/" + i.getPath();
        this.triggersCriteria = true;
        this.overrideSound = false;
    }

    @Info(value = """
            A function to determine where the thrown ender eye item will head towards.
                        
            Example usage:
            ```javascript
            builder.signalTo(context => {
                const { level, player, hand } = context
                return // Some BlockPos for the eye to navigate to when thrown
            });
            ```
            """)
    public EyeOfEnderItemBuilder signalTo(Function<ContextUtils.ItemUseContext, Object> f) {
        this.signalTo = f;
        return this;
    }

    @Info(value = """
            Sets the sound to play when the eye item is thrown at the coordinates of the player
                       
            @param sPlayer The player to play the sound to, can be null.
            @param soundEvent The sound to play when the eye item is thrown
            @param soundSource The source of the sound in the mixer.
            @param soundVolume The volume of the sound.
            @param soundPitch The pitch of the sound.
                        
            ```javascript
            item.playSoundOverride(null,"ambient.basalt_deltas.additions","ambient",1,1)
            ```
            """)
    public EyeOfEnderItemBuilder playSoundOverride(@Nullable Player player, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch) {
        this.sPlayer = player;
        this.soundEvent = soundEvent;
        this.soundSource = soundSource;
        this.soundVolume = volume;
        this.soundPitch = pitch;
        this.overrideSound = true;
        return this;
    }

    @Info(value = "Sets if the eye of ender triggers the USED_ENDER_EYE Criteria")
    public EyeOfEnderItemBuilder triggersCriteria(boolean triggersCriteria) {
        this.triggersCriteria = triggersCriteria;
        return this;
    }

    @Override
    public Item createObject() {
        return new EnderEyeItem(createItemProperties()) {
            @Override
            public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
                ItemStack $$3 = pPlayer.getItemInHand(pHand);
                BlockHitResult $$4 = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.NONE);
                if ($$4.getType() == HitResult.Type.BLOCK && pLevel.getBlockState($$4.getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
                    return InteractionResultHolder.pass($$3);
                } else {
                    pPlayer.startUsingItem(pHand);
                    if (pLevel instanceof ServerLevel) {
                        ServerLevel $$5 = (ServerLevel) pLevel;
                        BlockPos $$6 = $$5.findNearestMapStructure(StructureTags.EYE_OF_ENDER_LOCATED, pPlayer.blockPosition(), 100, false);
                        if (signalTo != null) {
                            final ContextUtils.ItemUseContext context = new ContextUtils.ItemUseContext(pLevel, pPlayer, pHand);
                            Object obj = signalTo.apply(context);
                            if (obj instanceof BlockPos b) {
                                EyeOfEnderEntityJS $$7 = new EyeOfEnderEntityJS(parent, pLevel, parent.get(), pPlayer.getX(), pPlayer.getY(0.5), pPlayer.getZ());
                                $$7.setItem($$3);
                                $$7.signalTo(b);
                                pLevel.gameEvent(GameEvent.PROJECTILE_SHOOT, $$7.position(), GameEvent.Context.of(pPlayer));
                                pLevel.addFreshEntity($$7);
                            } else {
                                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for signalTo in ender eye item builder: " + obj + ". Must be a BlockPos. Defaulting to null.");
                                return InteractionResultHolder.consume($$3);
                            }
                            if (pPlayer instanceof ServerPlayer) {
                                if (triggersCriteria) {
                                    CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer) pPlayer, $$6);
                                }
                            }
                            if (overrideSound) {
                                pLevel.playSound(sPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), soundEvent, soundSource, soundVolume, soundPitch);
                            } else {
                                pLevel.playSound((Player) null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
                            }
                            pLevel.levelEvent((Player) null, 1003, pPlayer.blockPosition(), 0);
                            if (!pPlayer.getAbilities().instabuild) {
                                $$3.shrink(1);
                            }
                            pPlayer.awardStat(Stats.ITEM_USED.get(this));
                            pPlayer.swing(pHand, true);
                            return InteractionResultHolder.success($$3);
                        } else if ($$6 != null) {
                            EyeOfEnderEntityJS $$7 = new EyeOfEnderEntityJS(parent, pLevel, parent.get(), pPlayer.getX(), pPlayer.getY(0.5), pPlayer.getZ());
                            $$7.setItem($$3);
                            $$7.signalTo($$6);
                            pLevel.gameEvent(GameEvent.PROJECTILE_SHOOT, $$7.position(), GameEvent.Context.of(pPlayer));
                            pLevel.addFreshEntity($$7);
                            if (pPlayer instanceof ServerPlayer) {
                                if (triggersCriteria) {
                                    CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayer) pPlayer, $$6);
                                }
                            }
                            if (overrideSound) {
                                pLevel.playSound(sPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), soundEvent, soundSource, soundVolume, soundPitch);
                            } else {
                                pLevel.playSound((Player) null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
                            }
                            pLevel.levelEvent((Player) null, 1003, pPlayer.blockPosition(), 0);
                            if (!pPlayer.getAbilities().instabuild) {
                                $$3.shrink(1);
                            }
                            pPlayer.awardStat(Stats.ITEM_USED.get(this));
                            pPlayer.swing(pHand, true);
                            return InteractionResultHolder.success($$3);
                        }
                    }

                    return InteractionResultHolder.consume($$3);
                }
            }


        };
    }
}

