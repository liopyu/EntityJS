package net.liopyu.entityjs.util.overrides.data;

import net.liopyu.entityjs.EntityJSMod;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public final class Net {
    private static final String PROTO = "1";
    private static final int MAX_NAME_LENGTH = 128;
    private static final int MAX_MAP_ENTRIES = 1024;
    private static int packetId;
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(EntityJSMod.MOD_ID, "synced_data"),
            () -> PROTO,
            PROTO::equals,
            PROTO::equals
    );

    private Net() {
    }

    public static void register() {
        CHANNEL.messageBuilder(SyncAllS2C.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncAllS2C::encode)
                .decoder(SyncAllS2C::decode)
                .consumerMainThread(SyncAllS2C::handle)
                .add();
        CHANNEL.messageBuilder(SetTypedValueS2C.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SetTypedValueS2C::encode)
                .decoder(SetTypedValueS2C::decode)
                .consumerMainThread(SetTypedValueS2C::handle)
                .add();
        CHANNEL.messageBuilder(SetTypeS2C.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SetTypeS2C::encode)
                .decoder(SetTypeS2C::decode)
                .consumerMainThread(SetTypeS2C::handle)
                .add();
        CHANNEL.messageBuilder(SetValueS2C.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SetValueS2C::encode)
                .decoder(SetValueS2C::decode)
                .consumerMainThread(SetValueS2C::handle)
                .add();
        CHANNEL.messageBuilder(DeleteValueS2C.class, nextId(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DeleteValueS2C::encode)
                .decoder(DeleteValueS2C::decode)
                .consumerMainThread(DeleteValueS2C::handle)
                .add();
    }

    private static int nextId() {
        return packetId++;
    }

    private static void writeTagMap(FriendlyByteBuf buf, Map<String, Tag> map) {
        int size = Math.min(map.size(), MAX_MAP_ENTRIES);
        buf.writeVarInt(size);
        int written = 0;
        for (Map.Entry<String, Tag> entry : map.entrySet()) {
            if (written++ >= size) break;
            buf.writeUtf(entry.getKey(), MAX_NAME_LENGTH);
            writeTag(buf, entry.getValue());
        }
    }

    private static Map<String, Tag> readTagMap(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Tag> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String name = buf.readUtf(MAX_NAME_LENGTH);
            Tag tag = readTag(buf);
            if (i < MAX_MAP_ENTRIES) {
                map.put(name, tag);
            }
        }
        return map;
    }

    private static void writeTag(FriendlyByteBuf buf, Tag tag) {
        net.minecraft.nbt.CompoundTag wrapper = new net.minecraft.nbt.CompoundTag();
        wrapper.put("v", tag.copy());
        buf.writeNbt(wrapper);
    }

    private static Tag readTag(FriendlyByteBuf buf) {
        net.minecraft.nbt.CompoundTag wrapper = buf.readNbt();
        return wrapper == null ? net.minecraft.nbt.EndTag.INSTANCE : wrapper.get("v");
    }

    private static void writeTypeMap(FriendlyByteBuf buf, Map<String, Integer> map) {
        int size = Math.min(map.size(), MAX_MAP_ENTRIES);
        buf.writeVarInt(size);
        int written = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (written++ >= size) break;
            buf.writeUtf(entry.getKey(), MAX_NAME_LENGTH);
            buf.writeVarInt(entry.getValue());
        }
    }

    private static Map<String, Integer> readTypeMap(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String name = buf.readUtf(MAX_NAME_LENGTH);
            int ordinal = buf.readVarInt();
            if (i < MAX_MAP_ENTRIES) {
                map.put(name, ordinal);
            }
        }
        return map;
    }

    private static void handled(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().setPacketHandled(true);
    }

    public static void sendTypedValueTracking(Entity e, UUID id, String name, int ord, Tag value) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> e), new SetTypedValueS2C(id, name, ord, value));
    }

    public static void sendAllTo(ServerPlayer p, UUID id, Map<String, Tag> values, Map<String, Integer> types) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new SyncAllS2C(id, values, types));
    }

    public static void sendAllTracking(Entity e, UUID id, Map<String, Tag> values, Map<String, Integer> types) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> e), new SyncAllS2C(id, values, types));
    }

    public static void sendValueTracking(Entity e, UUID id, String name, Tag value) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> e), new SetValueS2C(id, name, value));
    }

    public static void sendTypeTracking(Entity e, UUID id, String name, int ord) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> e), new SetTypeS2C(id, name, ord));
    }

    public static void sendDeleteTracking(Entity e, UUID id, String name) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> e), new DeleteValueS2C(id, name));
    }

    public record SyncAllS2C(UUID entityId, Map<String, Tag> values, Map<String, Integer> types) {
        private static void encode(SyncAllS2C msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.entityId);
            writeTagMap(buf, msg.values);
            writeTypeMap(buf, msg.types);
        }

        private static SyncAllS2C decode(FriendlyByteBuf buf) {
            return new SyncAllS2C(buf.readUUID(), readTagMap(buf), readTypeMap(buf));
        }

        private static void handle(SyncAllS2C msg, Supplier<NetworkEvent.Context> ctx) {
            ClientCache.setAll(msg.entityId, msg.values, msg.types);
            handled(ctx);
        }
    }

    public record SetTypedValueS2C(UUID entityId, String name, int ord, Tag value) {
        private static void encode(SetTypedValueS2C msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.entityId);
            buf.writeUtf(msg.name);
            buf.writeVarInt(msg.ord);
            writeTag(buf, msg.value);
        }

        private static SetTypedValueS2C decode(FriendlyByteBuf buf) {
            return new SetTypedValueS2C(buf.readUUID(), buf.readUtf(MAX_NAME_LENGTH), buf.readVarInt(), readTag(buf));
        }

        private static void handle(SetTypedValueS2C msg, Supplier<NetworkEvent.Context> ctx) {
            if (ClientCache.setType(msg.entityId, msg.name, msg.ord)) {
                ClientCache.set(msg.entityId, msg.name, msg.value);
            }
            handled(ctx);
        }
    }

    public record SetValueS2C(UUID entityId, String name, Tag value) {
        private static void encode(SetValueS2C msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.entityId);
            buf.writeUtf(msg.name);
            writeTag(buf, msg.value);
        }

        private static SetValueS2C decode(FriendlyByteBuf buf) {
            return new SetValueS2C(buf.readUUID(), buf.readUtf(MAX_NAME_LENGTH), readTag(buf));
        }

        private static void handle(SetValueS2C msg, Supplier<NetworkEvent.Context> ctx) {
            ClientCache.set(msg.entityId, msg.name, msg.value);
            handled(ctx);
        }
    }

    public record SetTypeS2C(UUID entityId, String name, int ord) {
        private static void encode(SetTypeS2C msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.entityId);
            buf.writeUtf(msg.name);
            buf.writeVarInt(msg.ord);
        }

        private static SetTypeS2C decode(FriendlyByteBuf buf) {
            return new SetTypeS2C(buf.readUUID(), buf.readUtf(MAX_NAME_LENGTH), buf.readVarInt());
        }

        private static void handle(SetTypeS2C msg, Supplier<NetworkEvent.Context> ctx) {
            ClientCache.setType(msg.entityId, msg.name, msg.ord);
            handled(ctx);
        }
    }

    public record DeleteValueS2C(UUID entityId, String name) {
        private static void encode(DeleteValueS2C msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.entityId);
            buf.writeUtf(msg.name);
        }

        private static DeleteValueS2C decode(FriendlyByteBuf buf) {
            return new DeleteValueS2C(buf.readUUID(), buf.readUtf(MAX_NAME_LENGTH));
        }

        private static void handle(DeleteValueS2C msg, Supplier<NetworkEvent.Context> ctx) {
            ClientCache.remove(msg.entityId, msg.name);
            handled(ctx);
        }
    }

}
