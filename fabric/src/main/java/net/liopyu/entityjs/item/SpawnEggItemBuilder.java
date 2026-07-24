package net.liopyu.entityjs.item;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;


public class SpawnEggItemBuilder extends ItemBuilder {

    public transient int backgroundColor;
    public transient int highlightColor;
    public transient final BuilderBase<?> parent;

    public SpawnEggItemBuilder(ResourceLocation i, BuilderBase<?> parent) {
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
    @SuppressWarnings("unchecked")
    public Item createObject() {
        return new SpawnEggItem((EntityType<? extends Mob>) parent.get(), backgroundColor, highlightColor, createItemProperties());
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
