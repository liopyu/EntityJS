package net.liopyu.entityjs.builders.nonliving;

import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class NonAnimatableEntityTypeBuilder<B extends Entity> {
    private final BaseNonAnimatableEntityBuilder<?> builder;

    public <T extends BaseNonAnimatableEntityBuilder<B>> NonAnimatableEntityTypeBuilder(T builder) {
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
        if (js.immuneTo != null && js.immuneTo.length > 0) {
            final Block[] blocks = new Block[js.immuneTo.length];
            for (int i = 0; i < js.immuneTo.length; i++) {
                blocks[i] = BuiltInRegistries.BLOCK.get(js.immuneTo[i]);
            }
            builder.immuneTo(blocks);
        }
        if (!js.summonable) {
            builder.noSummon();
        }

        return Cast.to(builder.build(js.id.toString())); // If this fails, uh... do better?
    }

}


