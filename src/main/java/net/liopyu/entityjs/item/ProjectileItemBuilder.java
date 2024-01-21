package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.generator.AssetJsonGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityJSBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;


public class ProjectileItemBuilder extends ItemBuilder {
    public transient final ProjectileEntityJSBuilder parent;
    public transient String texture;

    public ProjectileItemBuilder(ResourceLocation i, ProjectileEntityJSBuilder parent) {
        super(i);
        this.parent = parent;
        texture = "kubejs:item/" + i.getPath();
    }

    @Override
    public Item createObject() {
        return new Item(createItemProperties());
    }

    @Override
    public ItemBuilder texture(String tex) {
        this.texture = tex;
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

                if (texture.isEmpty()) {
                    texture(newID("item/", "").toString());
                }
                m.texture("layer0", texture);
            } else {
                m.parent("item/generated");

                if (texture.isEmpty()) {
                    texture(newID("item/", "").toString());
                }
                m.texture("layer0", texture);
            }
        });
    }

}
