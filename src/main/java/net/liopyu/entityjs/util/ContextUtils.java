package net.liopyu.entityjs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

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

    public static class DeathContext {
        public final LivingEntity entity;
        public final DamageSource damageSource;

        public DeathContext(LivingEntity entity, DamageSource damageSource) {
            this.entity = entity;
            this.damageSource = damageSource;
        }
    }


    public static class EntityDamageContext {
        public final DamageSource damageSource;
        public final float damageAmount;
        public final LivingEntity livingEntity;

        public EntityDamageContext(DamageSource damageSource, float damageAmount, LivingEntity livingEntity) {
            this.damageSource = damageSource;
            this.damageAmount = damageAmount;
            this.livingEntity = livingEntity;
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

    public static class AutoAttackContext {
        public final LivingEntity target;
        public final LivingEntity entity;

        public AutoAttackContext(LivingEntity target, LivingEntity entity) {
            this.entity = entity;
            this.target = target;
        }
    }

    public static class EntityFloatContext {
        public final LivingEntity livingEntity;
        public final float absorbtionAmount;

        public EntityFloatContext(LivingEntity livingEntity, float absorbtionAmount) {
            this.livingEntity = livingEntity;
            this.absorbtionAmount = absorbtionAmount;
        }
    }

    public static class EntityHealContext {
        public final LivingEntity livingEntity;
        public final float healAmount;

        public EntityHealContext(LivingEntity livingEntity, float healAmount) {
            this.livingEntity = livingEntity;
            this.healAmount = healAmount;
        }
    }


    public static class EntityItemEntityContext {
        public final LivingEntity livingEntity;
        public final ItemEntity itemEntity;

        public EntityItemEntityContext(LivingEntity livingEntity, ItemEntity itemEntity) {
            this.livingEntity = livingEntity;
            this.itemEntity = itemEntity;
        }
    }

    public static class EntityTypeEntityContext {
        public final LivingEntity livingEntity;
        public final EntityType<?> targetType;

        public EntityTypeEntityContext(LivingEntity livingEntity, EntityType<?> targetType) {
            this.livingEntity = livingEntity;
            this.targetType = targetType;
        }
    }

    public static class EntityFluidStateContext {
        public final LivingEntity livingEntity;
        public final FluidState fluidState;

        public EntityFluidStateContext(LivingEntity livingEntity, FluidState fluidState) {
            this.livingEntity = livingEntity;
            this.fluidState = fluidState;
        }
    }

    public static class EntityFallDamageContext {
        public final LivingEntity livingEntity;
        public final float distance;
        public final float damageMultiplier;
        public final DamageSource damageSource;

        public EntityFallDamageContext(LivingEntity livingEntity, float distance, float damageMultiplier, DamageSource damageSource) {
            this.livingEntity = livingEntity;
            this.distance = distance;
            this.damageMultiplier = damageMultiplier;
            this.damageSource = damageSource;
        }
    }

    public static class EntityLootContext {
        public final DamageSource damageSource;
        public final int lootingMultiplier;
        public final boolean allowDrops;
        public final LivingEntity entity;

        public EntityLootContext(DamageSource damageSource, int lootingMultiplier, boolean allowDrops, LivingEntity entity) {
            this.damageSource = damageSource;
            this.lootingMultiplier = lootingMultiplier;
            this.allowDrops = allowDrops;
            this.entity = entity;
        }
    }

    public static class EntityEquipmentContext {
        public final EquipmentSlot slot;
        public final ItemStack previousStack;
        public final ItemStack currentStack;
        public final LivingEntity livingEntity;

        public EntityEquipmentContext(EquipmentSlot slot, ItemStack previousStack, ItemStack currentStack, LivingEntity livingEntity) {
            this.slot = slot;
            this.previousStack = previousStack;
            this.currentStack = currentStack;
            this.livingEntity = livingEntity;
        }
    }

    public static class EntityPoseDimensionsContext {
        public final Pose pose;
        public final EntityDimensions dimensions;
        public final LivingEntity livingEntity;

        public EntityPoseDimensionsContext(Pose pose, EntityDimensions dimensions, LivingEntity livingEntity) {
            this.pose = pose;
            this.dimensions = dimensions;
            this.livingEntity = livingEntity;
        }
    }

    public static class EntityProjectileWeaponContext {
        public final ProjectileWeaponItem projectileWeapon;
        public final LivingEntity livingEntity;

        public EntityProjectileWeaponContext(ProjectileWeaponItem projectileWeapon, LivingEntity livingEntity) {
            this.projectileWeapon = projectileWeapon;
            this.livingEntity = livingEntity;
        }
    }

    public static class EntityBlockPathTypeContext {
        public final BlockPathTypes blockPathType;
        public final LivingEntity livingEntity;

        public EntityBlockPathTypeContext(BlockPathTypes blockPathType, LivingEntity livingEntity) {
            this.blockPathType = blockPathType;
            this.livingEntity = livingEntity;
        }
    }

    public static class EntityAnimalContext {
        public final Animal animal;
        public final Animal otherAnimal;

        public EntityAnimalContext(Animal animal, Animal otherAnimal) {
            this.animal = animal;
            this.otherAnimal = otherAnimal;
        }
    }

    public static class EntityDistanceToPlayerContext {
        public final double distanceToClosestPlayer;
        public final LivingEntity entity;

        public EntityDistanceToPlayerContext(double distanceToClosestPlayer, LivingEntity entity) {
            this.distanceToClosestPlayer = distanceToClosestPlayer;
            this.entity = entity;
        }
    }

    public static class EntityBlockPosLevelContext {
        public final BlockPos pos;
        public final LevelReader levelReader;
        public final LivingEntity entity;

        public EntityBlockPosLevelContext(BlockPos pos, LevelReader levelReader, LivingEntity entity) {
            this.pos = pos;
            this.levelReader = levelReader;
            this.entity = entity;
        }
    }

    public static class ProjectilePlayerContext {
        public final ThrowableItemProjectile entity;
        public final Player player;

        public ProjectilePlayerContext(Player player, ThrowableItemProjectile entity) {
            this.entity = entity;
            this.player = player;
        }
    }

    public static class ProjectileEntityHitContext {
        public final ThrowableItemProjectile entity;
        public final EntityHitResult result;

        public ProjectileEntityHitContext(EntityHitResult result, ThrowableItemProjectile entity) {
            this.entity = entity;
            this.result = result;
        }
    }

    public static class ProjectileBlockHitContext {
        public final ThrowableItemProjectile entity;
        public final BlockHitResult result;

        public ProjectileBlockHitContext(BlockHitResult result, ThrowableItemProjectile entity) {
            this.entity = entity;
            this.result = result;
        }
    }
}

