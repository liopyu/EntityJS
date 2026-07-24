accessWidener	v1	named
accessible field net/minecraft/world/entity/Mob targetSelector Lnet/minecraft/world/entity/ai/goal/GoalSelector;
accessible field net/minecraft/world/entity/Mob goalSelector Lnet/minecraft/world/entity/ai/goal/GoalSelector;
accessible method net/minecraft/world/entity/LivingEntity brainProvider ()Lnet/minecraft/world/entity/ai/Brain$Provider;
accessible method net/minecraft/client/renderer/entity/EntityRenderDispatcher renderHitbox (Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;F)V
accessible field net/minecraft/client/renderer/entity/EntityRenderer shadowStrength F
accessible field net/minecraft/client/renderer/entity/EntityRenderer shadowRadius F
accessible field net/minecraft/client/renderer/entity/EntityRenderDispatcher renderHitBoxes Z
accessible field net/minecraft/client/renderer/entity/EntityRenderDispatcher shouldRenderShadow Z
accessible field net/minecraft/client/renderer/entity/EntityRenderDispatcher level Lnet/minecraft/world/level/Level;
accessible method net/minecraft/client/renderer/entity/EntityRenderDispatcher renderFlame (Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;)V
accessible method net/minecraft/client/renderer/entity/EntityRenderDispatcher renderShadow (Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V
accessible method net/minecraft/world/level/Level getEntities ()Lnet/minecraft/world/level/entity/LevelEntityGetter;
accessible field net/minecraft/world/entity/ai/control/JumpControl mob Lnet/minecraft/world/entity/Mob;
extendable method net/minecraft/world/entity/ai/control/MoveControl isWalkable (FF)Z
accessible field net/minecraft/world/entity/projectile/ThrownTrident tridentItem Lnet/minecraft/world/item/ItemStack;
accessible field net/minecraft/world/entity/projectile/ThrownTrident dealtDamage Z
accessible field net/minecraft/world/entity/projectile/ThrownTrident ID_LOYALTY Lnet/minecraft/network/syncher/EntityDataAccessor;
accessible field net/minecraft/world/entity/projectile/ThrownTrident ID_FOIL Lnet/minecraft/network/syncher/EntityDataAccessor;

accessible field net/minecraft/world/entity/projectile/EyeOfEnder tx D
accessible field net/minecraft/world/entity/projectile/EyeOfEnder ty D
accessible field net/minecraft/world/entity/projectile/EyeOfEnder tz D
accessible field net/minecraft/world/entity/projectile/EyeOfEnder life I
accessible field net/minecraft/world/entity/projectile/EyeOfEnder surviveAfterDeath Z
accessible method net/minecraft/world/entity/projectile/Projectile lerpRotation (FF)F

accessible field net/minecraft/world/entity/boss/wither/WitherBoss destroyBlocksTick I
accessible field net/minecraft/world/entity/boss/wither/WitherBoss nextHeadUpdate [I
accessible field net/minecraft/world/entity/boss/wither/WitherBoss idleHeadUpdates [I
accessible field net/minecraft/world/entity/boss/wither/WitherBoss bossEvent Lnet/minecraft/server/level/ServerBossEvent;
accessible field net/minecraft/world/entity/boss/wither/WitherBoss TARGETING_CONDITIONS Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;
accessible method net/minecraft/world/entity/boss/wither/WitherBoss getHeadX (I)D
accessible method net/minecraft/world/entity/boss/wither/WitherBoss getHeadY (I)D
accessible method net/minecraft/world/entity/boss/wither/WitherBoss getHeadZ (I)D


# Fields
accessible field net/minecraft/world/entity/animal/Bee beePollinateGoal Lnet/minecraft/world/entity/animal/Bee$BeePollinateGoal;
accessible field net/minecraft/world/entity/animal/Bee goToHiveGoal Lnet/minecraft/world/entity/animal/Bee$BeeGoToHiveGoal;
accessible field net/minecraft/world/entity/animal/Bee goToKnownFlowerGoal Lnet/minecraft/world/entity/animal/Bee$BeeGoToKnownFlowerGoal;

# Inner Classes
accessible class net/minecraft/world/entity/animal/Bee$BeePollinateGoal
accessible class net/minecraft/world/entity/animal/Bee$BeeGoToHiveGoal
accessible class net/minecraft/world/entity/animal/Bee$BeeGoToKnownFlowerGoal

# Constructors (require Bee outer instance)
accessible method net/minecraft/world/entity/animal/Bee$BeePollinateGoal <init> (Lnet/minecraft/world/entity/animal/Bee;)V
accessible method net/minecraft/world/entity/animal/Bee$BeeGoToHiveGoal <init> (Lnet/minecraft/world/entity/animal/Bee;)V
accessible method net/minecraft/world/entity/animal/Bee$BeeGoToKnownFlowerGoal <init> (Lnet/minecraft/world/entity/animal/Bee;)V
