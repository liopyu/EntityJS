package net.liopyu.entityjs.builders.living;

import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;

public class LivingEntityTypeBuilderJS<B extends LivingEntity & IAnimatableJS> {
    private final BaseLivingEntityBuilder<?> builder;

    public <T extends BaseLivingEntityBuilder<B>> LivingEntityTypeBuilderJS(T builder) {
        this.builder = builder;
    }

    public EntityType<B> get() {
        var js = this.builder;

        var builder = EntityType.Builder.of(js.factory(), js.mobCategory);
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
            final Block[] blocks = new Block[js.immuneTo.length];
            for (int i = 0; i < js.immuneTo.length; i++) {
                blocks[i] = BuiltInRegistries.BLOCK.get(js.immuneTo[i]);
            }
            builder.immuneTo(blocks);
        }
        if (!js.summonable) {
            builder.noSummon();
        }

        return (EntityType<B>) builder.build(js.id.toString()); // If this fails, uh... do better?
    }
}
