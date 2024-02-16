package net.liopyu.entityjs.builders;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.ArrowEntityJS;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.liopyu.entityjs.entities.ProjectileEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.BiConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public abstract class ProjectileEntityBuilder<T extends ThrowableItemProjectile & IProjectileEntityJS> extends BaseEntityBuilder<T> {
    public transient Function<T, ResourceLocation> textureLocation;
    public static final List<ProjectileEntityBuilder<?>> thisList = new ArrayList<>();


    public transient Consumer<ContextUtils.ProjectileEntityHitContext> onHitEntity;
    public transient Consumer<ContextUtils.ProjectileBlockHitContext> onHitBlock;

    public transient Predicate<Entity> canHitEntity;

    public transient Float pX;
    public transient Float pY;
    public transient Float pZ;

    public transient Float vX;
    public transient Float vY;
    public transient Float vZ;


    public ProjectileEntityBuilder(ResourceLocation i) {
        super(i);
        textureLocation = t -> t.getProjectileBuilder().newID("textures/entity/projectiles/", ".png");
        thisList.add(this);
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }


    @Info(value = """
            Sets a function to determine the texture resource for the entity.
            The provided Function accepts a parameter of type T (the entity),
            allowing changing the texture based on information about the entity.
            The default behavior returns <namespace>:textures/model/entity/<path>.png.
                        
            Example usage:
            ```javascript
            entityBuilder.textureLocation(entity => {
                // Define logic to determine the texture resource for the entity
                // Use information about the entity provided by the context.
                return // Some ResourceLocation representing the texture resource;
            });
            ```
            """)
    public BaseEntityBuilder<T> textureLocation(Function<T, ResourceLocation> textureCallback) {
        textureLocation = textureCallback;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the projectile hits an entity.
            The provided Consumer accepts a {@link ContextUtils.ProjectileEntityHitContext} parameter,
            representing the context of the projectile's interaction with a specific entity.
                        
            Example usage:
            ```javascript
            projectileBuilder.onHitEntity(context -> {
                // Custom logic to handle the projectile hitting an entity.
                // Access information about the entity and projectile using the provided context.
            });
            ```
            """)
    public ProjectileEntityBuilder<T> onHitEntity(Consumer<ContextUtils.ProjectileEntityHitContext> consumer) {
        onHitEntity = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the projectile hits a block.
            The provided Consumer accepts a {@link ContextUtils.ProjectileBlockHitContext} parameter,
            representing the context of the projectile's interaction with a specific block.
                        
            Example usage:
            ```javascript
            projectileBuilder.onHitBlock(context -> {
                // Custom logic to handle the projectile hitting a block.
                // Access information about the block and projectile using the provided context.
            });
            ```
            """)
    public ProjectileEntityBuilder<T> onHitBlock(Consumer<ContextUtils.ProjectileBlockHitContext> consumer) {
        onHitBlock = consumer;
        return this;
    }

    @Info(value = """
            Sets a predicate to determine if the projectile can hit a specific entity.
                        
            @param predicate A predicate accepting an {@link Entity} parameter,
                             defining the condition for the projectile to hit the entity.
                        
            Example usage:
            ```javascript
            projectileBuilder.canHitEntity(entity -> {
                // Custom logic to determine if the projectile can hit the given entity.
                // Return true if the projectile can hit, false otherwise.
            });
            ```
            """)
    public ProjectileEntityBuilder<T> canHitEntity(Predicate<Entity> predicate) {
        canHitEntity = predicate;
        return this;
    }

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
    public ProjectileEntityBuilder<T> renderScale(Float pX, Float pY, Float pZ) {
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
    public ProjectileEntityBuilder<T> renderOffset(Float vX, Float vY, Float vZ) {
        this.vX = vX;
        this.vY = vY;
        this.vZ = vZ;
        return this;
    }


}
