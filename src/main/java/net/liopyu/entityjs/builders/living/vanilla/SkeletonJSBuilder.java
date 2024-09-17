package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.SkeletonEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.ZombieEntityJS;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Function;


public class SkeletonJSBuilder extends PathfinderMobBuilder<SkeletonEntityJS> {
    public transient Boolean defaultGoals;
    public transient SoundEvent shootSound;
    public transient Function<LivingEntity, Object> isSunBurnTick;
    public transient boolean canConvert;
    public transient EntityType<? extends Mob> conversionType;
    public transient ItemStack setArrow;

    public SkeletonJSBuilder(ResourceLocation i) {
        super(i);
        defaultGoals = true;
        canConvert = true;
        shootSound = SoundEvents.SKELETON_SHOOT;
        setArrow = new ItemStack(Items.ARROW);
    }

    @Info(value = """  
            @param setArrow Sets the arrow entity to be fired.
            Defaults to "minecraft:arrow".
                        
            Example usage:
            ```javascript
            builder.setArrow("minecraft:arrow");
            ```
            """)
    public SkeletonJSBuilder setArrow(ItemStack setArrow) {
        this.setArrow = setArrow;
        return this;
    }

    @Info(value = """  
            @param setShootSound Sets the mobs shooting sound
                        
            Example usage:
            ```javascript
            builder.setShootSound("entity.skeleton.shoot");
            ```
            """)
    public SkeletonJSBuilder setShootSound(SoundEvent shootSound) {
        this.shootSound = shootSound;
        return this;
    }

    @Info(value = """  
            @param setConversionType Sets what mob the entity should convert to after freezing. Must be a Mob.
            Defaults to "minecraft:stray".
                        
            Example usage:
            ```javascript
            builder.setConversionType("minecraft:stray");
            ```
            """)
    public SkeletonJSBuilder setConversionType(EntityType<? extends Mob> conversionType) {
        this.conversionType = conversionType;
        return this;
    }

    @Info(value = """  
            @param canConvert Sets whether the mob should convert while freezing to another mob
            Defaults to true.
                        
            Example usage:
            ```javascript
            builder.canConvert(false);
            ```
            """)
    public SkeletonJSBuilder canConvert(boolean canConvert) {
        this.canConvert = canConvert;
        return this;
    }

    @Info(value = """  
            @param isSunBurnTick Sets whether the mob should burn in daylight
                        
            Example usage:
            ```javascript
            builder.isSunBurnTick(entity => {
                return false
            });
            ```
            """)
    public SkeletonJSBuilder isSunBurnTick(Function<LivingEntity, Object> isSunBurnTick) {
        this.isSunBurnTick = isSunBurnTick;
        return this;
    }

    @Info(value = """  
            @param defaultGoals Sets whether the mob should inherit it's goals from it's superclass
            Defaults to true.
                        
            Example usage:
            ```javascript
            builder.defaultGoals(false);
            ```
            """)
    public SkeletonJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }


    @Override
    public EntityType.EntityFactory<SkeletonEntityJS> factory() {
        return (type, level) -> new SkeletonEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return SkeletonEntityJS.createAttributes();
    }
}