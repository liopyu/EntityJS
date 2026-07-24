package net.liopyu.entityjs.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.liopyu.entityjs.common.EntityJSMod;
import net.liopyu.entityjs.common.util.overrides.data.SavedDataJS;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;

/** Fabric transport for EntityJS's server-authoritative dynamic synced data. */
public final class FabricSyncedData {
    public static final ResourceLocation SYNC_ALL = EntityJSMod.identifier("synced_data_all");
    public static final ResourceLocation SET_VALUE = EntityJSMod.identifier("synced_data_set");

    private FabricSyncedData() {
    }

    public static void init() {
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> sendAll(player, entity));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendAll(handler.getPlayer(), handler.getPlayer()));
    }

    public static void sendAll(ServerPlayer player, Entity entity) {
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        SavedDataJS data = SavedDataJS.get(level);
        Map<String, net.minecraft.nbt.Tag> values = data.getAll(entity.getUUID());
        Map<String, Integer> types = data.getTypes(entity.getUUID());
        if (values.isEmpty() && types.isEmpty()) return;
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeUUID(entity.getUUID());
        buffer.writeVarInt(values.size());
        for (Map.Entry<String, net.minecraft.nbt.Tag> entry : values.entrySet()) {
            buffer.writeUtf(entry.getKey(), 128);
            buffer.writeNbt(wrap(entry.getValue()));
            buffer.writeVarInt(types.getOrDefault(entry.getKey(), -1));
        }
        ServerPlayNetworking.send(player, SYNC_ALL, buffer);
    }

    public static void sendValue(Entity entity, String name, net.minecraft.nbt.Tag value, int type) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            sendValue(player, entity.getUUID(), name, value, type);
        }
        if (entity instanceof ServerPlayer player) {
            sendValue(player, entity.getUUID(), name, value, type);
        }
    }

    private static void sendValue(ServerPlayer player, UUID entityId, String name, net.minecraft.nbt.Tag value, int type) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeUUID(entityId);
        buffer.writeUtf(name, 128);
        buffer.writeNbt(wrap(value));
        buffer.writeVarInt(type);
        ServerPlayNetworking.send(player, SET_VALUE, buffer);
    }

    private static net.minecraft.nbt.CompoundTag wrap(net.minecraft.nbt.Tag value) {
        net.minecraft.nbt.CompoundTag wrapped = new net.minecraft.nbt.CompoundTag();
        wrapped.put("v", value.copy());
        return wrapped;
    }
}
