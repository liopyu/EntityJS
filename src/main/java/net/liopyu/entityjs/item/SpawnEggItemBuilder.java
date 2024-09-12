package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;


public class SpawnEggItemBuilder extends ItemBuilder {

    public transient int backgroundColor = 0xFFFFFFFF;
    public transient int highlightColor = 0xFFFFFFFF;
    public transient final MobBuilder<?> parent;

    public SpawnEggItemBuilder(ResourceLocation i, MobBuilder<?> parent) {
        super(i);
        this.parent = parent;
    }

    @Info(value = "Sets the background color of the egg item")
    public SpawnEggItemBuilder backgroundColor(int i) {
        backgroundColor = i;
        return this;
    }

    @Info(value = "Sets the highlight color of the egg item")
    public SpawnEggItemBuilder highlightColor(int i) {
        highlightColor = i;
        return this;
    }

    @Override
    public Item createObject() {
        return new DeferredSpawnEggItem(parent, backgroundColor, highlightColor, createItemProperties());
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        generator.itemModel(id, m -> {
            if (modelGenerator != null) {
                modelGenerator.accept(m);
                return;
            }
            if (parentModel != null) {
                m.parent(parentModel);

                if (textures.isEmpty()) {
                    texture(newID("item/", "").toString());
                }
                m.textures(textures);
            } else {
                m.parent(ResourceLocation.parse("minecraft:item/template_spawn_egg"));

                if (!textures.isEmpty()) {
                    m.textures(textures);
                }
            }
        });
    }
}
