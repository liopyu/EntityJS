package net.liopyu.entityjs.util.overrides.data;

import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.util.EntitySerializerType;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
        CHANNEL.messageBuilder(SetValueC2S.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetValueC2S::encode)
                .decoder(SetValueC2S::decode)
                .consumerMainThread(SetValueC2S::handle)
                .add();
        CHANNEL.messageBuilder(EnsureValueC2S.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(EnsureValueC2S::encode)
                .decoder(EnsureValueC2S::decode)
                .consumerMainThread(EnsureValueC2S::handle)
                .add();
    }

    private static int nextId() {
        return packetId++;
    }

    private static void writeTagMap(FriendlyByteBuf buf, Map<String, Tag> map) {
        buf.writeVarInt(map.size());
        map.forEach((name, tag) -> {
            buf.writeUtf(name);
            writeTag(buf, tag);
        });
    }

    private static Map<String, Tag> readTagMap(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Tag> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readUtf(), readTag(buf));
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
        buf.writeVarInt(map.size());
        map.forEach((name, type) -> {
            buf.writeUtf(name);
            buf.writeVarInt(type);
        });
    }

    private static Map<String, Integer> readTypeMap(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(buf.readUtf(), buf.readVarInt());
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

    public static void sendSetToServer(UUID id, String name, Tag value) {
        CHANNEL.sendToServer(new SetValueC2S(id, name, value));
    }

    public static void sendEnsureToServer(UUID id, String name, EntitySerializerType serType, Tag value) {
        CHANNEL.sendToServer(new EnsureValueC2S(id, name, serType, value));
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
            return new SetTypedValueS2C(buf.readUUID(), buf.readUtf(), buf.readVarInt(), readTag(buf));
        }

        private static void handle(SetTypedValueS2C msg, Supplier<NetworkEvent.Context> ctx) {
            ClientCache.setType(msg.entityId, msg.name, msg.ord);
            ClientCache.set(msg.entityId, msg.name, msg.value);
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
            return new SetValueS2C(buf.readUUID(), buf.readUtf(), readTag(buf));
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
            return new SetTypeS2C(buf.readUUID(), buf.readUtf(), buf.readVarInt());
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
            return new DeleteValueS2C(buf.readUUID(), buf.readUtf());
        }

        private static void handle(DeleteValueS2C msg, Supplier<NetworkEvent.Context> ctx) {
            ClientCache.remove(msg.entityId, msg.name);
            handled(ctx);
        }
    }

    public record SetValueC2S(UUID entityId, String name, Tag value) {
        private static void encode(SetValueC2S msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.entityId);
            buf.writeUtf(msg.name);
            writeTag(buf, msg.value);
        }

        private static SetValueC2S decode(FriendlyByteBuf buf) {
            return new SetValueC2S(buf.readUUID(), buf.readUtf(), readTag(buf));
        }

        private static void handle(SetValueC2S msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = ((ServerLevel) player.level()).getEntity(msg.entityId);
                if (entity != null) {
                    ServerCache.set(entity, msg.name, msg.value);
                }
            }
            handled(ctx);
        }
    }

    public record EnsureValueC2S(UUID entityId, String name, EntitySerializerType serType, Tag value) {
        private static void encode(EnsureValueC2S msg, FriendlyByteBuf buf) {
            buf.writeUUID(msg.entityId);
            buf.writeUtf(msg.name);
            buf.writeVarInt(msg.serType.ordinal());
            writeTag(buf, msg.value);
        }

        private static EnsureValueC2S decode(FriendlyByteBuf buf) {
            return new EnsureValueC2S(buf.readUUID(), buf.readUtf(), EntitySerializerType.values()[buf.readVarInt()], readTag(buf));
        }

        private static void handle(EnsureValueC2S msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                Entity entity = ((ServerLevel) player.level()).getEntity(msg.entityId);
                if (entity != null) {
                    ServerCache.ensure(entity, msg.name, msg.value, msg.serType);
                }
            }
            handled(ctx);
        }
    }
}
