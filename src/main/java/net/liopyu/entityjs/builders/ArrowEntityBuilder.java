package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public abstract class ArrowEntityBuilder<T extends AbstractArrow & IArrowEntityJS> extends BaseEntityBuilder<T> {
    public static final List<ArrowEntityBuilder<?>> thisList = new ArrayList<>();
    public transient Function<T, ResourceLocation> getTextureLocation;

    public ArrowEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        getTextureLocation = t -> t.getArrowBuilder().newID("textures/entity/projectiles/", ".png");
    }

    @Info(value = """
            Sets how the texture of the entity is determined, has access to the entity
            to allow changing the texture based on info about the entity
                        
            Defaults to returning <namespace>:textures/entity/projectiles/<path>.png
            """)
    public BaseEntityBuilder<T> getTextureLocation(Function<T, ResourceLocation> function) {
        getTextureLocation = function;
        return this;
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }
}

