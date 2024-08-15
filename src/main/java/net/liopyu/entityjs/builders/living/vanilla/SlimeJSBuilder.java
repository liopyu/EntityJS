package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.entities.living.vanilla.SlimeEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Consumer;

public class SlimeJSBuilder extends MobBuilder<SlimeEntityJS> {
    public transient Boolean defaultGoals;
    public transient ParticleOptions setParticleType;
    public transient Consumer<ContextUtils.LivingEntityContext> dealDamage;
    public transient SoundEvent setSquishSound;

    public SlimeJSBuilder(ResourceLocation i) {
        super(i);
        defaultGoals = true;
    }

    @Info(value = """  
            @param setSquishSound Sets the squish sound
                        
            Example usage:
            ```javascript
            builder.setSquishSound(false);
            ```
            """)
    public SlimeJSBuilder setSquishSound(SoundEvent sound) {
        this.setSquishSound = sound;
        return this;
    }

    @Info(value = """  
            @param dealDamage Overrides the way the slime deals damage
                        
            Example usage:
            ```javascript
            builder.dealDamage(ctx => {
            	const { entity, target } = ctx
            	// Determine how the slime deals damage
            });
            ```
            """)
    public SlimeJSBuilder dealDamage(Consumer<ContextUtils.LivingEntityContext> dealDamage) {
        this.dealDamage = dealDamage;
        return this;
    }

    @Info(value = """  
            @param setParticleType Sets the particles emitted off the slime
            Defaults to slime particles
                        
            Example usage:
            ```javascript
            builder.setParticleType(false);
            ```
            """)
    public SlimeJSBuilder setParticleType(ParticleType<?> type) {
        this.setParticleType = (ParticleOptions) type;
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
    public SlimeJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Override
    public EntityType.EntityFactory<SlimeEntityJS> factory() {
        return (type, level) -> new SlimeEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return SlimeEntityJS.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }
}

