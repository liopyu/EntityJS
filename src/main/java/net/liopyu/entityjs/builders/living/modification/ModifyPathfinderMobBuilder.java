package net.liopyu.entityjs.builders.living.modification;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.function.Function;

public class ModifyPathfinderMobBuilder extends ModifyMobBuilder {
    public transient Function<Mob, Object> shouldStayCloseToLeashHolder;
    public transient Double followLeashSpeed;
    public transient Function<ContextUtils.EntityBlockPosLevelContext, Object> walkTargetValue;

    public ModifyPathfinderMobBuilder(Entity entity) {
        super(entity);
    }

    @Info(value = """
            Sets the function to determine whether the entity should stay close to its leash holder.
                        
            @param predicate A Function accepting a {@link Mob} parameter,
                             defining the condition for the entity to stay close to its leash holder.
                        
            Example usage:
            ```javascript
            mobBuilder.shouldStayCloseToLeashHolder(entity => {
                // Custom logic to determine whether the entity should stay close to its leash holder.
                return true;
            });
            ```
            """)
    public ModifyPathfinderMobBuilder shouldStayCloseToLeashHolder(Function<Mob, Object> predicate) {
        this.shouldStayCloseToLeashHolder = predicate;
        return this;
    }

    @Info(value = """
            Sets the follow leash speed for the entity.
                        
            @param speed The follow leash speed.
                        
            Example usage:
            ```javascript
            mobBuilder.followLeashSpeed(1.5);
            ```
            """)
    public ModifyPathfinderMobBuilder followLeashSpeed(double speed) {
        this.followLeashSpeed = speed;
        return this;
    }

    @Info(value = """
            Sets the walk target value function for the entity.
                        
            @param function A Function accepting a {@link ContextUtils.EntityBlockPosLevelContext} parameter,
                            defining the walk target value based on the entity's interaction with a specific block.
                        
            Example usage:
            ```javascript
            mobBuilder.walkTargetValue(context => {
                // Custom logic to calculate the walk target value based on the provided context.
                // Access information about the block position and level using the provided context.
                return 10;
            });
            ```
            """)
    public ModifyPathfinderMobBuilder walkTargetValue(Function<ContextUtils.EntityBlockPosLevelContext, Object> function) {
        this.walkTargetValue = function;
        return this;
    }
}
