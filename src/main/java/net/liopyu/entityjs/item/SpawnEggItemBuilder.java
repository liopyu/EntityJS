package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.MobBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;


public class SpawnEggItemBuilder extends ItemBuilder {

    public transient int backgroundColor;
    public transient int highlightColor;
    public transient final MobBuilder<?> parent;

    public SpawnEggItemBuilder(ResourceLocation i, MobBuilder<?> parent) {
        super(i);
        backgroundColor = 0xFFFFFFFF;
        highlightColor = 0XFFFFFFFF;
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
        return new ForgeSpawnEggItem(parent, backgroundColor, highlightColor, createItemProperties());
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        if (modelJson != null) {
            generator.json(AssetJsonGenerator.asItemModelLocation(id), modelJson);
            return;
        }

        generator.itemModel(id, m -> {
            if (!parentModel.isEmpty()) {
                m.parent(parentModel);

                if (textureJson.size() == 0) {
                    texture(newID("item/", "").toString());
                }
                m.textures(textureJson);
            } else {
                m.parent("item/template_spawn_egg");

                if (textureJson.size() != 0) {
                    m.textures(textureJson);
                }
            }
        });
    }
}
