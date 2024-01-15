package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityTypeBuilderJS<B extends LivingEntity & IAnimatableJS> {

    private final BaseEntityBuilder<?> builder;

    public <T extends BaseEntityBuilder<B>> EntityTypeBuilderJS(T builder) {
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
                blocks[i] = ForgeRegistries.BLOCKS.getValue(js.immuneTo[i]);
            }
            builder.immuneTo(blocks);
        }
        if (!js.summonable) {
            builder.noSummon();
        }

        return UtilsJS.cast(builder.build(js.id.toString())); // If this fails, uh... do better?
    }
}
