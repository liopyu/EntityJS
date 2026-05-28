package net.liopyu.entityjs.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
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
    public transient Identifier structure;
    public transient Identifier structureTag;
    public transient int chunkRadius;

    public EyeOfEnderItemBuilder(Identifier i, EyeOfEnderJSBuilder parent) {
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
            A function to determine which structure tag the thrown ender eye item will head towards in a certain chunk radius.
            
            Example usage:
            ```javascript
            builder.signalToStructureTag("minecraft:village", 100);
            ```
            """)
    public EyeOfEnderItemBuilder signalToStructureTag(Identifier resourceLocation, int chunkRadius) {
        this.structureTag = resourceLocation;
        this.chunkRadius = chunkRadius;
        return this;
    }

    @Info(value = """
            A function to determine which structure tag the thrown ender eye item will head towards in a 100 chunk radius.
            
            Example usage:
            ```javascript
            builder.signalToStructureTag("minecraft:village");
            ```
            """)
    public EyeOfEnderItemBuilder signalToStructureTag(Identifier resourceLocation) {
        this.structureTag = resourceLocation;
        this.chunkRadius = 100;
        return this;
    }

    @Info(value = """
            A function to determine structure the thrown ender eye item will head towards in a certain chunk radius.
            
            Example usage:
            ```javascript
            builder.signalToStructure("minecraft:village_plains", 100);
            ```
            """)
    public EyeOfEnderItemBuilder signalToStructure(Identifier resourceLocation, int chunkRadius) {
        this.structure = resourceLocation;
        this.chunkRadius = chunkRadius;
        return this;
    }

    @Info(value = """
            A function to determine structure the thrown ender eye item will head towards in a 100 chunk radius.
            
            Example usage:
            ```javascript
            builder.signalToStructure("minecraft:village_plains");
            ```
            """)
    public EyeOfEnderItemBuilder signalToStructure(Identifier resourceLocation) {
        this.structure = resourceLocation;
        this.chunkRadius = 100;
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
                if (use != null) {
                    if (use.use(pLevel, pPlayer, pHand)) {
                        ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand);
                    }
                }
                ItemStack $$3 = pPlayer.getItemInHand(pHand);
                BlockHitResult $$4 = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.NONE);
                if ($$4.getType() == HitResult.Type.BLOCK && pLevel.getBlockState($$4.getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
                    return InteractionResultHolder.pass($$3);
                } else {
                    pPlayer.startUsingItem(pHand);
                    if (pLevel instanceof ServerLevel) {
                        ServerLevel $$5 = (ServerLevel) pLevel;
                        BlockPos $$6 = $$5.findNearestMapStructure(StructureTags.EYE_OF_ENDER_LOCATED, pPlayer.blockPosition(), 100, false);
                        if (signalTo != null || structure != null || structureTag != null) {
                            if (signalTo != null) {
                                final ContextUtils.ItemUseContext context = new ContextUtils.ItemUseContext(pLevel, pPlayer, pHand);
                                Object obj = signalTo.apply(context);
                                if (obj != null) {
                                    EyeOfEnderEntityJS $$7 = new EyeOfEnderEntityJS(parent, pLevel, parent.get(), pPlayer.getX(), pPlayer.getY(0.5), pPlayer.getZ());
                                    $$7.setItem($$3);
                                    if (obj instanceof BlockPos b)
                                        if (b != null) {
                                            $$7.signalTo(b);
                                            pLevel.gameEvent(GameEvent.PROJECTILE_SHOOT, $$7.position(), GameEvent.Context.of(pPlayer));
                                            pLevel.addFreshEntity($$7);
                                        } else {
                                            return InteractionResultHolder.consume($$3);
                                        }
                                    pLevel.gameEvent(GameEvent.PROJECTILE_SHOOT, $$7.position(), GameEvent.Context.of(pPlayer));
                                    pLevel.addFreshEntity($$7);
                                } else {
                                    return InteractionResultHolder.consume($$3);
                                }
                            } else if (structureTag != null) {
                                EyeOfEnderEntityJS $$7 = new EyeOfEnderEntityJS(parent, pLevel, parent.get(), pPlayer.getX(), pPlayer.getY(0.5), pPlayer.getZ());
                                $$7.setItem($$3);
                                var structureTagKey = TagKey.create(Registries.STRUCTURE, structureTag);
                                BlockPos searchOrigin = pPlayer.blockPosition();
                                BlockPos location = ((ServerLevel) pLevel).findNearestMapStructure(structureTagKey, searchOrigin, chunkRadius, false);
                                if (location != null) {
                                    $$7.signalTo(location);
                                    pLevel.gameEvent(GameEvent.PROJECTILE_SHOOT, $$7.position(), GameEvent.Context.of(pPlayer));
                                    pLevel.addFreshEntity($$7);
                                } else {
                                    return InteractionResultHolder.consume($$3);
                                }
                            } else if (structure != null) {
                                Identifier structureId = structure;
                                Registry<Structure> structureRegistry = pLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);
                                Optional<Holder.Reference<Structure>> holder = structureRegistry.getHolder(ResourceKey.create(Registries.STRUCTURE, structureId));

                                if (holder.isPresent()) {
                                    HolderSet<Structure> holderSet = HolderSet.direct(holder.get());
                                    BlockPos origin = pPlayer.blockPosition();

                                    Pair<BlockPos, Holder<Structure>> result = ((ServerLevel) pLevel)
                                            .getChunkSource()
                                            .getGenerator()
                                            .findNearestMapStructure((ServerLevel) pLevel, holderSet, origin, chunkRadius, false);

                                    if (result != null) {
                                        BlockPos location = result.getFirst();
                                        EyeOfEnderEntityJS eye = new EyeOfEnderEntityJS(parent, pLevel, parent.get(), pPlayer.getX(), pPlayer.getY(0.5), pPlayer.getZ());
                                        eye.setItem($$3);
                                        eye.signalTo(location);
                                        pLevel.gameEvent(GameEvent.PROJECTILE_SHOOT, eye.position(), GameEvent.Context.of(pPlayer));
                                        pLevel.addFreshEntity(eye);
                                    } else {
                                        return InteractionResultHolder.consume($$3);
                                    }
                                } else {
                                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Could not find registry for structure: " + structureId + " in method signalToStructure");
                                    return InteractionResultHolder.consume($$3);
                                }
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

