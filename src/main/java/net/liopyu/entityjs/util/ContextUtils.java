package net.liopyu.entityjs.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.entityjs.PartBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class ContextUtils {
    public static class PreRenderContext<T> {
        @Info("The PoseStack")
        public final PoseStack poseStack;

        @Info("The animatable entity")
        public final T entity;

        @Info("The baked model")
        public final BakedGeoModel bakedModel;

        @Info("The render type")
        public final RenderType renderType;

        @Info("The buffer source")
        public final MultiBufferSource bufferSource;

        @Info("The vertex consumer buffer")
        public final VertexConsumer buffer;

        @Info("The partial tick")
        public final float partialTick;

        @Info("The packed light")
        public final int packedLight;

        @Info("The packed overlay")
        public final int packedOverlay;

        public PreRenderContext(PoseStack poseStack, T entity, BakedGeoModel bakedModel, RenderType renderType,
                                MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                                int packedLight, int packedOverlay) {
            this.poseStack = poseStack;
            this.entity = entity;
            this.bakedModel = bakedModel;
            this.renderType = renderType;
            this.bufferSource = bufferSource;
            this.buffer = buffer;
            this.partialTick = partialTick;
            this.packedLight = packedLight;
            this.packedOverlay = packedOverlay;
        }
    }

    public static class ScaleModelRenderContext<T extends LivingEntity> {
        public final float widthScale;
        public final float heightScale;
        public final PoseStack poseStack;
        public final T entity;
        public final BakedGeoModel model;
        public final boolean isReRender;
        public final float partialTick;
        public final int packedLight;
        public final int packedOverlay;

        public ScaleModelRenderContext(float widthScale, float heightScale, PoseStack poseStack, T entity, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
            this.widthScale = widthScale;
            this.heightScale = heightScale;
            this.poseStack = poseStack;
            this.entity = entity;
            this.model = model;
            this.isReRender = isReRender;
            this.partialTick = partialTick;
            this.packedLight = packedLight;
            this.packedOverlay = packedOverlay;
        }
    }

    public static class ScaleModelRenderContextNL<T extends Entity> {
        public final float widthScale;
        public final float heightScale;
        public final PoseStack poseStack;
        public final T entity;
        public final BakedGeoModel model;
        public final boolean isReRender;
        public final float partialTick;
        public final int packedLight;
        public final int packedOverlay;

        public ScaleModelRenderContextNL(float widthScale, float heightScale, PoseStack poseStack, T entity, BakedGeoModel model, boolean isReRender, float partialTick, int packedLight, int packedOverlay) {
            this.widthScale = widthScale;
            this.heightScale = heightScale;
            this.poseStack = poseStack;
            this.entity = entity;
            this.model = model;
            this.isReRender = isReRender;
            this.partialTick = partialTick;
            this.packedLight = packedLight;
            this.packedOverlay = packedOverlay;
        }
    }

    public static class PartEntityParams<T extends LivingEntity> {
        public final String name;
        public final float width;
        public final float height;
        public final PartBuilder<T> builder;

        public PartEntityParams(String name, float width, float height, PartBuilder<T> builder) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.builder = builder;
        }
    }


    public static class PlayerEntityContext {
        @Info("The living entity associated with the player")
        public final LivingEntity entity;

        @Info("The player entity")
        public final Player player;

        public PlayerEntityContext(Player player, LivingEntity entity) {
            this.entity = entity;
            this.player = player;
        }
    }

    public static class EntityLevelContext {
        @Info("The living entity")
        public final LivingEntity entity;

        @Info("The level")
        public final Level level;

        public EntityLevelContext(Level level, LivingEntity entity) {
            this.entity = entity;
            this.level = level;
        }
    }

    public static class CollidingProjectileEntityContext {
        @Info("The colliding entity")
        public final Entity entity;

        @Info("The the target being collided with")
        public final Entity target;

        public CollidingProjectileEntityContext(Entity entity, Entity target) {
            this.entity = entity;
            this.target = target;
        }
    }

    public static class CollidingEntityContext {
        @Info("The entity getting collided with")
        public final LivingEntity entity;

        @Info("The entity colliding")
        public final Entity collidingEntity;

        public CollidingEntityContext(LivingEntity entity, Entity collidingEntity) {
            this.entity = entity;
            this.collidingEntity = collidingEntity;
        }
    }

    public static class BreedableEntityContext {
        @Info("The entity who mated.")
        public final AgeableMob entity;
        @Info("The other parent the entity mated with.")
        public final AgeableMob mate;

        @Info("The Server Level")
        public final ServerLevel level;

        public BreedableEntityContext(AgeableMob entity, AgeableMob mate, ServerLevel level) {
            this.entity = entity;
            this.mate = mate;
            this.level = level;
        }
    }

    public static class EntityBlockPosContext {
        @Info("The living entity")
        public final LivingEntity entity;

        @Info("The block position")
        public final BlockPos blockPos;

        public EntityBlockPosContext(LivingEntity entity, BlockPos blockPos) {
            this.entity = entity;
            this.blockPos = blockPos;
        }
    }

    public static class PassengerEntityContext {

        public final Entity passenger;


        public final LivingEntity entity;

        public PassengerEntityContext(Entity passenger, LivingEntity entity) {
            this.passenger = passenger;
            this.entity = entity;
        }
    }

    public static class LivingEntityContext {

        public final LivingEntity entity;


        public final LivingEntity target;

        public LivingEntityContext(LivingEntity entity, LivingEntity target) {
            this.entity = entity;
            this.target = target;
        }
    }


    public static class EntityItemLevelContext {
        @Info("The living entity")
        public final LivingEntity entity;

        @Info("The item stack")
        public final ItemStack itemStack;

        @Info("The level")
        public final Level level;

        public EntityItemLevelContext(LivingEntity entity, ItemStack itemStack, Level level) {
            this.entity = entity;
            this.itemStack = itemStack;
            this.level = level;
        }
    }


    public static class DamageContext {
        @Info("The living entity that is the target of the damage")
        public final LivingEntity entity;

        @Info("The source of the damage")
        public final DamageSource damageSource;

        public DamageContext(LivingEntity entity, DamageSource damageSource) {
            this.entity = entity;
            this.damageSource = damageSource;
        }
    }


    public static class DeathContext {
        @Info("The living entity that has died")
        public final LivingEntity entity;

        @Info("The source of the damage causing the death")
        public final DamageSource damageSource;

        public DeathContext(LivingEntity entity, DamageSource damageSource) {
            this.entity = entity;
            this.damageSource = damageSource;
        }
    }

    public static class HurtContext {
        @Info("The living entity that was hurt")
        public final LivingEntity entity;

        @Info("The source of the damage")
        public final DamageSource damageSource;

        public HurtContext(LivingEntity entity, DamageSource damageSource) {
            this.entity = entity;
            this.damageSource = damageSource;
        }
    }

    public static class EntityHurtContext {
        @Info("The entity that was hurt")
        public final Entity entity;

        @Info("The source of the damage")
        public final DamageSource damageSource;
        @Info("The source of the damage")
        public final float damageAmount;

        public EntityHurtContext(Entity entity, DamageSource damageSource, float damageAmount) {
            this.entity = entity;
            this.damageSource = damageSource;
            this.damageAmount = damageAmount;
        }
    }

    public static class PartHurtContext<T extends LivingEntity> {
        @Info("The part of the entity that was hurt")
        public final PartEntity<T> part;
        @Info("The source of the damage")
        public final DamageSource source;
        @Info("The source of the damage")
        public final float amount;
        @Info("The parent of the part entity")
        public final LivingEntity entity;

        public PartHurtContext(PartEntity<T> part, DamageSource source, float amount, LivingEntity entity) {
            this.part = part;
            this.source = source;
            this.amount = amount;
            this.entity = entity;
        }
    }

    public static class EntityDamageContext {
        @Info("The source of the damage")
        public final DamageSource damageSource;

        @Info("The amount of damage inflicted")
        public final float damageAmount;

        @Info("The living entity receiving the damage")
        public final LivingEntity entity;

        public EntityDamageContext(DamageSource damageSource, float damageAmount, LivingEntity entity) {
            this.damageSource = damageSource;
            this.damageAmount = damageAmount;
            this.entity = entity;
        }
    }


    public static class MayInteractContext {
        @Info("The level where the interaction may occur")
        public final Level level;

        @Info("The position where the interaction may occur")
        public final BlockPos pos;

        @Info("The living entity involved in the interaction")
        public final LivingEntity entity;

        public MayInteractContext(Level level, BlockPos pos, LivingEntity entity) {
            this.level = level;
            this.pos = pos;
            this.entity = entity;
        }
    }


    public static class CanTrampleContext {
        @Info("The block state at the position")
        public final BlockState state;

        @Info("The position of the block being considered for trampling")
        public final BlockPos pos;

        @Info("The distance fallen before trampling (if applicable)")
        public final float fallDistance;

        @Info("The living entity attempting to trample the block")
        public final LivingEntity entity;

        public CanTrampleContext(BlockState state, BlockPos pos, float fallDistance, LivingEntity entity) {
            this.state = state;
            this.pos = pos;
            this.fallDistance = fallDistance;
            this.entity = entity;
        }
    }

    public static class ItemUseContext {
        @Info("The level of the entity using the item")
        public final Level level;

        @Info("The player interacting with the item")
        public final Player player;

        @Info("The hand used for interaction")
        public final InteractionHand hand;

        public ItemUseContext(Level level, Player player, InteractionHand hand) {
            this.level = level;
            this.player = player;
            this.hand = hand;
        }
    }

    public static class MobInteractContext {
        @Info("The living entity being interacted with")
        public final LivingEntity entity;

        @Info("The player interacting with the living entity")
        public final Player player;

        @Info("The hand used for interaction")
        public final InteractionHand hand;

        public MobInteractContext(LivingEntity entity, Player player, InteractionHand hand) {
            this.entity = entity;
            this.player = player;
            this.hand = hand;
        }
    }


    public static class OnEffectContext {
        @Info("The living entity affected by the mob effect")
        public final LivingEntity entity;

        @Info("The mob effect instance applied to the living entity")
        public final MobEffectInstance effect;

        public OnEffectContext(MobEffectInstance effect, LivingEntity entity) {
            this.entity = entity;
            this.effect = effect;
        }
    }

    public static class ArrowLivingEntityContext {
        @Info("The living entity hit by the arrow.")
        public final LivingEntity entity;

        @Info("The arrow being fired.")
        public final AbstractArrow arrow;

        public ArrowLivingEntityContext(AbstractArrow arrow, LivingEntity entity) {
            this.entity = entity;
            this.arrow = arrow;
        }
    }


    public static class ThunderHitContext {
        @Info("The server level where the lightning strike occurred")
        public final ServerLevel level;

        @Info("The lightning bolt that struck")
        public final LightningBolt lightningBolt;

        @Info("The living entity affected by the lightning strike")
        public final LivingEntity entity;

        public ThunderHitContext(ServerLevel level, LightningBolt lightningBolt, LivingEntity entity) {
            this.level = level;
            this.lightningBolt = lightningBolt;
            this.entity = entity;
        }
    }


    public static class TargetChangeContext {
        @Info("The new target entity")
        public final LivingEntity target;

        @Info("The entity whose target is changing")
        public final Mob entity;

        public TargetChangeContext(LivingEntity target, Mob entity) {
            this.target = target;
            this.entity = entity;
        }
    }


    public static class AutoAttackContext {
        @Info("The target entity that is being attacked")
        public final LivingEntity target;

        @Info("The attacking entity")
        public final LivingEntity entity;

        public AutoAttackContext(LivingEntity target, LivingEntity entity) {
            this.target = target;
            this.entity = entity;
        }
    }


    public static class CalculateFallDamageContext {
        @Info("The height from which the entity is falling")
        public final float fallHeight;

        @Info("The multiplier applied to calculate fall damage")
        public final float damageMultiplier;

        @Info("The living entity experiencing fall damage calculation")
        public final LivingEntity entity;

        public CalculateFallDamageContext(float fallHeight, float damageMultiplier, LivingEntity entity) {
            this.fallHeight = fallHeight;
            this.damageMultiplier = damageMultiplier;
            this.entity = entity;
        }
    }


    public static class EntityItemStackContext {
        @Info("The item stack")
        public final ItemStack item;

        @Info("The living entity")
        public final LivingEntity entity;

        public EntityItemStackContext(ItemStack item, LivingEntity entity) {
            this.item = item;
            this.entity = entity;
        }
    }


    public static class EntityHealContext {
        @Info("The living entity being healed")
        public final LivingEntity entity;

        @Info("The amount of healing applied to the living entity")
        public final float healAmount;

        public EntityHealContext(LivingEntity entity, float healAmount) {
            this.entity = entity;
            this.healAmount = healAmount;
        }
    }


    public static class EntityItemEntityContext {
        @Info("The living entity involved")
        public final LivingEntity entity;

        @Info("The item entity associated with the living entity")
        public final ItemEntity itemEntity;

        public EntityItemEntityContext(LivingEntity entity, ItemEntity itemEntity) {
            this.entity = entity;
            this.itemEntity = itemEntity;
        }
    }


    public static class EntityTypeEntityContext {
        @Info("The living entity")
        public final LivingEntity entity;

        @Info("The target entity type")
        public final EntityType<?> targetType;

        public EntityTypeEntityContext(LivingEntity entity, EntityType<?> targetType) {
            this.entity = entity;
            this.targetType = targetType;
        }
    }


    public static class EntityFluidStateContext {
        @Info("The living entity")
        public final LivingEntity entity;

        @Info("The fluid state associated with the living entity")
        public final FluidState fluidState;

        public EntityFluidStateContext(LivingEntity entity, FluidState fluidState) {
            this.entity = entity;
            this.fluidState = fluidState;
        }
    }


    public static class EntityFallDamageContext {
        @Info("The living entity experiencing fall damage")
        public final LivingEntity entity;

        @Info("The distance fallen by the living entity")
        public final float distance;

        @Info("The multiplier applied to calculate fall damage")
        public final float damageMultiplier;

        @Info("The source of the fall damage")
        public final DamageSource damageSource;

        public EntityFallDamageContext(LivingEntity entity, float damageMultiplier, float distance, DamageSource damageSource) {
            this.entity = entity;
            this.distance = distance;
            this.damageMultiplier = damageMultiplier;
            this.damageSource = damageSource;
        }
    }


    public static class EntityLootContext {
        @Info("The source of the damage causing the loot")
        public final DamageSource damageSource;

        @Info("The looting multiplier for the loot")
        public final int lootingMultiplier;

        @Info("Whether drops are allowed")
        public final boolean allowDrops;

        @Info("The living entity involved")
        public final LivingEntity entity;

        public EntityLootContext(DamageSource damageSource, int lootingMultiplier, boolean allowDrops, LivingEntity entity) {
            this.damageSource = damageSource;
            this.lootingMultiplier = lootingMultiplier;
            this.allowDrops = allowDrops;
            this.entity = entity;
        }
    }


    public static class EntityEquipmentContext {
        @Info("The equipment slot being modified")
        public final EquipmentSlot slot;

        @Info("The item stack previously in the equipment slot")
        public final ItemStack previousStack;

        @Info("The item stack currently in the equipment slot")
        public final ItemStack currentStack;

        @Info("The living entity associated with the equipment change")
        public final LivingEntity entity;

        public EntityEquipmentContext(EquipmentSlot slot, ItemStack previousStack, ItemStack currentStack, LivingEntity entity) {
            this.slot = slot;
            this.previousStack = previousStack;
            this.currentStack = currentStack;
            this.entity = entity;
        }
    }


    public static class EntityPoseDimensionsContext {
        @Info("The pose of the living entity")
        public final Pose pose;

        @Info("The dimensions of the living entity")
        public final EntityDimensions dimensions;

        @Info("The living entity associated with the pose and dimensions")
        public final LivingEntity entity;

        public EntityPoseDimensionsContext(Pose pose, EntityDimensions dimensions, LivingEntity entity) {
            this.pose = pose;
            this.dimensions = dimensions;
            this.entity = entity;
        }
    }


    public static class EntityProjectileWeaponContext {
        @Info("The projectile weapon being used")
        public final ProjectileWeaponItem projectileWeapon;

        @Info("The living entity using the projectile weapon")
        public final LivingEntity entity;

        public EntityProjectileWeaponContext(ProjectileWeaponItem projectileWeapon, LivingEntity entity) {
            this.projectileWeapon = projectileWeapon;
            this.entity = entity;
        }
    }

    public static class EntityBlockPathTypeContext {
        @Info("The type of block pathfinding being considered")
        public final BlockPathTypes blockPathType;

        @Info("The living entity associated with the block path type")
        public final LivingEntity entity;

        public EntityBlockPathTypeContext(BlockPathTypes blockPathType, LivingEntity entity) {
            this.blockPathType = blockPathType;
            this.entity = entity;
        }
    }


    public static class EntityAnimalContext {
        @Info("The main animal entity")
        public final Animal animal;

        @Info("The other animal entity")
        public final Animal otherAnimal;

        public EntityAnimalContext(Animal animal, Animal otherAnimal) {
            this.animal = animal;
            this.otherAnimal = otherAnimal;
        }
    }

    public static class LineOfSightContext {
        @Info("The target entity")
        public final Entity targetEntity;

        @Info("The living entity associated with the target entity")
        public final LivingEntity entity;

        public LineOfSightContext(Entity targetEntity, LivingEntity entity) {
            this.targetEntity = targetEntity;
            this.entity = entity;
        }
    }

    public static class VisualContext {
        @Info("The looking entity")
        public final Entity lookingEntity;

        @Info("Our entity which is looked at")
        public final LivingEntity entity;

        public VisualContext(Entity lookingEntity, LivingEntity entity) {
            this.lookingEntity = lookingEntity;
            this.entity = entity;
        }
    }


    public static class LerpToContext {
        @Info("The target x-coordinate for lerping")
        public final double x;

        @Info("The target y-coordinate for lerping")
        public final double y;

        @Info("The target z-coordinate for lerping")
        public final double z;

        @Info("The target yaw for lerping")
        public final float yaw;

        @Info("The target pitch for lerping")
        public final float pitch;

        @Info("The number of position rotation increments")
        public final int posRotationIncrements;

        @Info("Whether to teleport the entity")
        public final boolean teleport;

        @Info("The entity to lerp")
        public final Entity entity;

        public LerpToContext(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport, Entity entity) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.posRotationIncrements = posRotationIncrements;
            this.teleport = teleport;
            this.entity = entity;
        }
    }


    public static class LevelAnimalContext {
        @Info("The main animal entity")
        public final Animal entity;

        @Info("The mate animal entity")
        public final Animal mate;

        @Info("The server level where the animals reside")
        public final ServerLevel level;

        public LevelAnimalContext(Animal mate, Animal entity, ServerLevel level) {
            this.entity = entity;
            this.mate = mate;
            this.level = level;
        }
    }


    public static class EntityDistanceToPlayerContext {
        @Info("The distance to the closest player")
        public final double distanceToClosestPlayer;

        @Info("The living entity")
        public final LivingEntity entity;

        public EntityDistanceToPlayerContext(double distanceToClosestPlayer, LivingEntity entity) {
            this.distanceToClosestPlayer = distanceToClosestPlayer;
            this.entity = entity;
        }
    }


    public static class EntitySqrDistanceContext {
        @Info("The squared distance to the player")
        public final double distanceToPlayer;

        @Info("The entity")
        public final Entity entity;

        public EntitySqrDistanceContext(double distanceToPlayer, Entity entity) {
            this.distanceToPlayer = distanceToPlayer;
            this.entity = entity;
        }
    }


    public static class MovementContext {
        @Info("The type of mover responsible for the movement")
        public final MoverType moverType;

        @Info("The position to which the entity is moving")
        public final Vec3 position;

        @Info("The entity undergoing movement")
        public final Entity entity;

        public MovementContext(MoverType moverType, Vec3 position, Entity entity) {
            this.moverType = moverType;
            this.position = position;
            this.entity = entity;
        }
    }

    public static class Vec3Context {
        @Info("The Travel Vector of the entity.")
        public final Vec3 vec3;

        @Info("The entity undergoing movement")
        public final LivingEntity entity;

        public Vec3Context(Vec3 vec3, LivingEntity entity) {
            this.vec3 = vec3;
            this.entity = entity;
        }
    }

    public static class EntityBlockPosLevelContext {
        @Info("The block position")
        public final BlockPos pos;

        @Info("The level reader")
        public final LevelReader levelReader;

        @Info("The living entity")
        public final LivingEntity entity;

        public EntityBlockPosLevelContext(BlockPos pos, LevelReader levelReader, LivingEntity entity) {
            this.pos = pos;
            this.levelReader = levelReader;
            this.entity = entity;
        }
    }


    public static class EntityPlayerContext {
        @Info("The entity")
        public final Entity entity;

        @Info("The player")
        public final Player player;

        public EntityPlayerContext(Player player, Entity entity) {
            this.entity = entity;
            this.player = player;
        }
    }


    public static class ProjectileEntityHitContext {
        @Info("The projectile that was thrown")
        public final ThrowableItemProjectile entity;

        @Info("The result of the hit")
        public final EntityHitResult result;

        public ProjectileEntityHitContext(EntityHitResult result, ThrowableItemProjectile entity) {
            this.entity = entity;
            this.result = result;
        }
    }


    public static class ProjectileBlockHitContext {
        @Info("The throwable item projectile that hit the block")
        public final ThrowableItemProjectile entity;

        @Info("The result of the hit on the block")
        public final BlockHitResult result;

        public ProjectileBlockHitContext(BlockHitResult result, ThrowableItemProjectile entity) {
            this.entity = entity;
            this.result = result;
        }
    }


    public static class ArrowEntityHitContext {
        @Info("The abstract arrow entity")
        public final AbstractArrow entity;

        @Info("The result of the hit on the entity")
        public final EntityHitResult result;

        public ArrowEntityHitContext(EntityHitResult result, AbstractArrow entity) {
            this.entity = entity;
            this.result = result;
        }
    }


    public static class ArrowBlockHitContext {
        @Info("The abstract arrow entity")
        public final AbstractArrow entity;

        @Info("The result of the hit on the block")
        public final BlockHitResult result;

        public ArrowBlockHitContext(BlockHitResult result, AbstractArrow entity) {
            this.entity = entity;
            this.result = result;
        }
    }


    public static class ArrowPlayerContext {
        @Info("The abstract arrow entity")
        public final AbstractArrow entity;

        @Info("The player")
        public final Player player;

        public ArrowPlayerContext(Player player, AbstractArrow entity) {
            this.entity = entity;
            this.player = player;
        }
    }

    public static class RenderContext<T extends LivingEntity & IAnimatableJS> {
        @Info("The animatable entity being rendered")
        public final T entity;

        @Info("The yaw of the entity")
        public final float entityYaw;

        @Info("The partial tick")
        public final float partialTick;

        @Info("The pose stack for transformations")
        public final PoseStack poseStack;

        @Info("The buffer source for rendering")
        public final MultiBufferSource bufferSource;

        @Info("The packed light information")
        public final int packedLight;

        public RenderContext(T entity, float entityYaw, float partialTick,
                             PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
            this.entity = entity;
            this.entityYaw = entityYaw;
            this.partialTick = partialTick;
            this.poseStack = poseStack;
            this.bufferSource = bufferSource;
            this.packedLight = packedLight;
        }
    }

    public static class NLRenderContext<T extends Entity> {
        @Info("The animatable entity being rendered")
        public final T entity;

        @Info("The yaw of the entity")
        public final float entityYaw;

        @Info("The partial tick")
        public final float partialTick;

        @Info("The pose stack for transformations")
        public final PoseStack poseStack;

        @Info("The buffer source for rendering")
        public final MultiBufferSource bufferSource;

        @Info("The packed light information")
        public final int packedLight;

        public NLRenderContext(T entity, float entityYaw, float partialTick,
                               PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
            this.entity = entity;
            this.entityYaw = entityYaw;
            this.partialTick = partialTick;
            this.poseStack = poseStack;
            this.bufferSource = bufferSource;
            this.packedLight = packedLight;
        }
    }

    public static class EPassengerEntityContext {

        public final Entity passenger;


        public final Entity entity;

        public EPassengerEntityContext(Entity passenger, Entity entity) {
            this.passenger = passenger;
            this.entity = entity;
        }
    }

    public static class ECollidingEntityContext {
        @Info("The entity getting collided with")
        public final Entity entity;

        @Info("The entity colliding")
        public final Entity collidingEntity;

        public ECollidingEntityContext(Entity entity, Entity collidingEntity) {
            this.entity = entity;
            this.collidingEntity = collidingEntity;
        }
    }

    public static class ECanTrampleContext {
        @Info("The block state at the position")
        public final BlockState state;

        @Info("The position of the block being considered for trampling")
        public final BlockPos pos;

        @Info("The distance fallen before trampling (if applicable)")
        public final float fallDistance;

        @Info("The entity attempting to trample the block")
        public final Entity entity;

        public ECanTrampleContext(BlockState state, BlockPos pos, float fallDistance, Entity entity) {
            this.state = state;
            this.pos = pos;
            this.fallDistance = fallDistance;
            this.entity = entity;
        }
    }

    public static class EDamageContext {
        @Info("The entity that is the target of the damage")
        public final Entity entity;

        @Info("The source of the damage")
        public final DamageSource damageSource;

        public EDamageContext(Entity entity, DamageSource damageSource) {
            this.entity = entity;
            this.damageSource = damageSource;
        }
    }

    public static class EThunderHitContext {
        @Info("The server level where the lightning strike occurred")
        public final ServerLevel level;

        @Info("The lightning bolt that struck")
        public final LightningBolt lightningBolt;

        @Info("The entity affected by the lightning strike")
        public final Entity entity;

        public EThunderHitContext(ServerLevel level, LightningBolt lightningBolt, Entity entity) {
            this.level = level;
            this.lightningBolt = lightningBolt;
            this.entity = entity;
        }
    }

    public static class EEntityFallDamageContext {
        @Info("The entity experiencing fall damage")
        public final Entity entity;

        @Info("The distance fallen by the living entity")
        public final float distance;

        @Info("The multiplier applied to calculate fall damage")
        public final float damageMultiplier;

        @Info("The source of the fall damage")
        public final DamageSource damageSource;

        public EEntityFallDamageContext(Entity entity, float damageMultiplier, float distance, DamageSource damageSource) {
            this.entity = entity;
            this.distance = distance;
            this.damageMultiplier = damageMultiplier;
            this.damageSource = damageSource;
        }
    }

    public static class EMayInteractContext {
        @Info("The level where the interaction may occur")
        public final Level level;

        @Info("The position where the interaction may occur")
        public final BlockPos pos;

        @Info("The entity involved in the interaction")
        public final Entity entity;

        public EMayInteractContext(Level level, BlockPos pos, Entity entity) {
            this.level = level;
            this.pos = pos;
            this.entity = entity;
        }
    }
}
