package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.IAnimatableJS;
import dev.latvian.mods.kubejs.util.UtilsJS;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class EntityTypeBuilderJS<B extends Entity & IAnimatableJS> {

    private final BaseEntityBuilder<?> builder;

    public <T extends BaseEntityBuilder<B>> EntityTypeBuilderJS(T builder) {
        this.builder = builder;
    }


    public <E extends Entity & IAnimatableJS> EntityType<E> get() {
        var js = this.builder;
        var builder = EntityType.Builder.of((type, level) -> js.factory().get(js, type, level), js.mobCategory);
        builder
                .sized(js.width, js.height)
                .clientTrackingRange(js.clientTrackingRange)
                .updateInterval(js.updateInterval);

        if (js.spawnFarFromPlayer) {
            builder.canSpawnFarFromPlayer();
        }
        if (js.fireImmune) {
            builder.fireImmune();
        }
        if (!js.save) {
            builder.noSave();
        }
        if (js.immuneTo.length > 0) {
            builder.immuneTo(js.immuneTo);
        }
        if (!js.summonable) {
            builder.noSummon();
        }
        return UtilsJS.cast(builder.build(js.toString())); // If this fails, uh... do better?
    }

    @FunctionalInterface
    public interface Factory<E extends Entity & IAnimatableJS> {
        E get(BaseEntityBuilder<?> builder, EntityType<?> type, Level level);
    }
}
