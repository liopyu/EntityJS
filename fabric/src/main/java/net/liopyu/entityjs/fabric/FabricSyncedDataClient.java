package net.liopyu.entityjs.fabric;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.liopyu.entityjs.common.util.EntitySerializerType;
import net.liopyu.entityjs.common.util.overrides.data.ClientCache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FabricSyncedDataClient {
    private FabricSyncedDataClient() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(FabricSyncedData.SYNC_ALL, (client, handler, buffer, responseSender) -> {
            UUID id = buffer.readUUID();
            int count = Math.min(buffer.readVarInt(), 1024);
            Map<String, net.minecraft.nbt.Tag> values = new HashMap<>();
            Map<String, Integer> types = new HashMap<>();
            for (int i = 0; i < count; i++) {
                String name = buffer.readUtf(128);
                net.minecraft.nbt.CompoundTag wrapped = buffer.readNbt();
                int type = buffer.readVarInt();
                if (wrapped != null && wrapped.contains("v")) values.put(name, wrapped.get("v"));
                if (EntitySerializerType.byOrdinal(type).isPresent()) types.put(name, type);
            }
            client.execute(() -> ClientCache.setAll(id, values, types));
        });
        ClientPlayNetworking.registerGlobalReceiver(FabricSyncedData.SET_VALUE, (client, handler, buffer, responseSender) -> {
            UUID id = buffer.readUUID();
            String name = buffer.readUtf(128);
            net.minecraft.nbt.CompoundTag wrapped = buffer.readNbt();
            int type = buffer.readVarInt();
            client.execute(() -> {
                if (!EntitySerializerType.byOrdinal(type).isPresent()) return;
                ClientCache.setType(id, name, type);
                if (wrapped != null && wrapped.contains("v")) ClientCache.set(id, name, wrapped.get("v"));
            });
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientCache.clear());
    }
}
