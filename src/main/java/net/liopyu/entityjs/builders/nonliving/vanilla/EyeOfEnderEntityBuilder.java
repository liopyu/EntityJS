package net.liopyu.entityjs.builders.nonliving.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.NonAnimatableEntityTypeBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IProjectileEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class EyeOfEnderEntityBuilder<T extends Entity & IProjectileEntityJS> extends BaseNonAnimatableEntityBuilder<T> {
    public transient Function<T, Object> textureLocation;
    public static final List<EyeOfEnderEntityBuilder<?>> thisList = new ArrayList<>();
    public transient Float pX;
    public transient Float pY;
    public transient Float pZ;
    public transient Float vX;
    public transient Float vY;
    public transient Float vZ;

    public EyeOfEnderEntityBuilder(ResourceLocation i) {
        super(i);
        textureLocation = t -> t.getProjectileBuilder().newID("textures/entity/projectiles/", ".png");
        thisList.add(this);
    }

    @Override
    public EntityType<T> createObject() {
        return new NonAnimatableEntityTypeBuilder<>(this).get();
    }

    //Throwable Projectile Overrides
    @Info(value = """
            Sets the scale for rendering the projectile entity.
                        
            @param pX The X-axis scale.
                        
            @param pY The Y-axis scale.
                        
            @param pZ The Z-axis scale.
                        
            Example usage:
            ```javascript
            projectileEntityBuilder.renderScale(1.5, 1.5, 1.5);
            ```
            """)
    public EyeOfEnderEntityBuilder<T> renderScale(Float pX, Float pY, Float pZ) {
        this.pX = pX;
        this.pY = pY;
        this.pZ = pZ;
        return this;
    }


    @Info(value = """
            Sets the offset for rendering the projectile entity.
                        
            @param vX The X-axis offset.
                        
            @param vY The Y-axis offset.
                        
            @param vZ The Z-axis offset.
                        
            Example usage:
            ```javascript
            projectileEntityBuilder.renderOffset(0.5, 1.0, -0.5);
            ```
            """)
    public EyeOfEnderEntityBuilder<T> renderOffset(Float vX, Float vY, Float vZ) {
        this.vX = vX;
        this.vY = vY;
        this.vZ = vZ;
        return this;
    }

    @Info(value = """
            Sets a function to determine the texture resource for the entity.
            The provided Function accepts a parameter of type T (the entity),
            allowing changing the texture based on information about the entity.
            The default behavior returns <namespace>:textures/entity/projectiles/<path>.png.
                        
            Example usage:
            ```javascript
            projectileBuilder.textureResource(entity => {
                // Define logic to determine the texture resource for the entity
                // Use information about the entity provided by the context.
                return // Some ResourceLocation representing the texture resource;
            });
            ```
            """)
    public EyeOfEnderEntityBuilder<T> textureLocation(Function<T, Object> function) {
        textureLocation = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String) {
                return ResourceLocation.parse((String) obj);
            } else if (obj instanceof ResourceLocation) {
                return (ResourceLocation) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("Invalid texture resource in projectile builder: " + obj + "Defaulting to " + entity.getProjectileBuilder().newID("textures/entity/projectiles/", ".png"));
                return entity.getProjectileBuilder().newID("textures/entity/projectiles/", ".png");
            }
        };
        return this;
    }
}
