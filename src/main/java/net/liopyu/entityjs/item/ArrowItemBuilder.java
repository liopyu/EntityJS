package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.ArrowEntityBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;


public class ArrowItemBuilder extends ItemBuilder {
    public transient final ArrowEntityBuilder<?> parent;
    public transient CreativeModeTab creativeModeTab;
    public transient int stacksTo;


    public ArrowItemBuilder(ResourceLocation i, ArrowEntityBuilder<?> parent) {
        super(i);
        stacksTo = 64;
        creativeModeTab = CreativeModeTab.TAB_SEARCH;
        this.parent = parent;
    }

    @Override
    public ArrowItem createObject() {

        final ArrowItem.Properties properties = new ArrowItem.Properties()
                .stacksTo(stacksTo)
                .tab(creativeModeTab);

        return new ArrowItem(properties);
    }

    @Info(value = "Sets the creative mode tab for the arrow item")
    public ArrowItemBuilder creativeModeTab(CreativeModeTab i) {
        creativeModeTab = i;
        return this;
    }

    @Info(value = "Sets the stacks to for the arrow item")
    public ArrowItemBuilder stacksTo(int i) {
        stacksTo = i;
        return this;
    }

    @Override
    public void generateAssetJsons(AssetJsonGenerator generator) {
        if (modelJson != null) {
            generator.json(AssetJsonGenerator.asItemModelLocation(id), modelJson);
            return;
        }

        generator.itemModel(id, m -> {
            m.parent(id.getPath());

            if (!parentModel.isEmpty()) {
                m.parent(parentModel);

                if (textureJson.size() == 0) {
                    texture(newID("item/", "").toString());
                }
                m.textures(textureJson);
            } else {
                m.parent("item/generated");

                if (textureJson.size() != 0) {
                    m.textures(textureJson);
                }
            }
        });
    }
}
