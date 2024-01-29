package net.liopyu.entityjs.events;

import com.mojang.datafixers.util.Either;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.Weight;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class BiomeSpawnsEventJS extends EventJS {

    @HideFromJS
    public final List<Addition> additions = new ArrayList<>();
    @HideFromJS
    public final List<Removal> removals = new ArrayList<>();

    @Info(value = "Adds a spawn to the given entity type in the given biomes", params = {
            @Param(name = "entityType", value = "The entity type to add a spawn to"),
            @Param(name = "biomes", value = "A list of biomes and biome tags to spawn in"),
            @Param(name = "weight", value = "The spawn weight"),
            @Param(name = "minCount", value = "The minimum number of entities to spawn"),
            @Param(name = "maxCount", value = "The maximum number of entities to spawn")
    })
    @Generics(value = {Entity.class, String.class})
    public void addSpawn(EntityType<?> entityType, List<String> biomes, int weight, int minCount, int maxCount) {
        additions.add(new Addition(entityType, new MobSpawnSettings.SpawnerData(entityType, Weight.of(weight), minCount, maxCount), processBiomes(biomes)));
    }

    @Info(value = "Removes the given entity type spawns from the given biomes", params = {
            @Param(name = "entityType", value = "The entity type to remove spawns from"),
            @Param(name = "biomes", value = "A list of biomes and biome tags to remove the spawns from")
    })
    @Generics(value = {Entity.class, String.class})
    public void removeSpawn(EntityType<?> entityType, List<String> biomes) {
        removals.add(new Removal(entityType, processBiomes(biomes)));
    }

    @HideFromJS
    public static List<Either<ResourceLocation, TagKey<Biome>>> processBiomes(List<String> biomes) {
        final List<Either<ResourceLocation, TagKey<Biome>>> biomeList = new ArrayList<>();
        for (String biome : biomes) {
            if (biome.charAt(0) == '#') {
                biomeList.add(Either.left(new ResourceLocation(biome.substring(1))));
            } else {
                biomeList.add(Either.right(TagKey.create(ForgeRegistries.Keys.BIOMES, new ResourceLocation(biome))));
            }
        }
        return biomeList;
    }

    public record Addition(EntityType<?> entityType, MobSpawnSettings.SpawnerData spawnData, List<Either<ResourceLocation, TagKey<Biome>>> biomes) {}

    public record Removal(EntityType<?> entityType, List<Either<ResourceLocation, TagKey<Biome>>> biomes) {}
}
