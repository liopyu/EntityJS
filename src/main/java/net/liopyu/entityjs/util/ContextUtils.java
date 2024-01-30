package net.liopyu.entityjs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ContextUtils {
    public static class PlayerEntityContext {
        public final LivingEntity entity;
        public final Player player;

        public PlayerEntityContext(Player player, LivingEntity entity) {
            this.entity = entity;
            this.player = player;
        }
    }

    public static class EntityBlockPosContext {
        public final LivingEntity livingEntity;
        public final BlockPos blockPos;

        public EntityBlockPosContext(LivingEntity livingEntity, BlockPos blockPos) {
            this.livingEntity = livingEntity;
            this.blockPos = blockPos;
        }
    }

    public static class EntityItemLevelContext {
        public final LivingEntity livingEntity;
        public final ItemStack itemStack;
        public final Level level;

        public EntityItemLevelContext(LivingEntity livingEntity, ItemStack itemStack, Level level) {
            this.livingEntity = livingEntity;
            this.itemStack = itemStack;
            this.level = level;
        }
    }

    public static class DamageContext {
        public final LivingEntity targetEntity;
        public final DamageSource damageSource;

        public DamageContext(LivingEntity targetEntity, DamageSource damageSource) {
            this.targetEntity = targetEntity;
            this.damageSource = damageSource;
        }
    }

    public static class MayInteractContext {
        public final Level level;
        public final BlockPos pos;
        public final LivingEntity entity;

        public MayInteractContext(Level level, BlockPos pos, LivingEntity entity) {
            this.level = level;
            this.pos = pos;
            this.entity = entity;
        }
    }

    public static class CanTrampleContext {
        public final BlockState state;
        public final BlockPos pos;
        public final float fallDistance;
        public final LivingEntity entity;

        public CanTrampleContext(BlockState state, BlockPos pos, float fallDistance, LivingEntity entity) {
            this.state = state;
            this.pos = pos;
            this.fallDistance = fallDistance;
            this.entity = entity;
        }
    }

    public static class MobInteractContext {
        public final LivingEntity entity;
        public final Player player;
        public final InteractionHand hand;

        public MobInteractContext(LivingEntity entity, Player player, InteractionHand hand) {
            this.entity = entity;
            this.player = player;
            this.hand = hand;
        }
    }

    public static class OnEffectContext {
        public final LivingEntity entity;
        public final MobEffectInstance effect;

        public OnEffectContext(MobEffectInstance effect, LivingEntity entity) {
            this.entity = entity;
            this.effect = effect;
        }
    }

    public static class ThunderHitContext {
        public final LivingEntity entity;
        public final ServerLevel level;
        public final LightningBolt lightningBolt;

        public ThunderHitContext(ServerLevel level, LightningBolt lightningBolt, LivingEntity entity) {
            this.level = level;
            this.lightningBolt = lightningBolt;
            this.entity = entity;
        }
    }

    public static class TargetChangeContext {
        public final LivingEntity target;
        public final PathfinderMob entity;

        public TargetChangeContext(LivingEntity target, PathfinderMob entity) {
            this.entity = entity;
            this.target = target;
        }
    }
}

