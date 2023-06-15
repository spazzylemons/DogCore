package net.dumbdogdiner.dogcore.teleport;

import java.nio.ByteBuffer;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * An serializer and deserializer for {@link org.bukkit.Location}.
 */
public final class LocationDataType implements PersistentDataType<byte[], Location> {
    private LocationDataType() { }

    /** The size of the buffer. */
    private static final int BUFFER_SIZE = 48;

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<Location> getComplexType() {
        return Location.class;
    }

    @Override
    public byte @NotNull[] toPrimitive(
        @NotNull final Location complex,
        @NotNull final PersistentDataAdapterContext context
    ) {
        var bb = ByteBuffer.wrap(new byte[BUFFER_SIZE]);
        var worldId = complex.getWorld().getUID();
        bb.putLong(worldId.getMostSignificantBits());
        bb.putLong(worldId.getLeastSignificantBits());
        bb.putDouble(complex.getX());
        bb.putDouble(complex.getY());
        bb.putDouble(complex.getZ());
        bb.putFloat(complex.getYaw());
        bb.putFloat(complex.getPitch());
        return bb.array();
    }

    @Override
    public @NotNull Location fromPrimitive(
        final byte @NotNull[] primitive,
        @NotNull final PersistentDataAdapterContext context
    ) {
        var bb = ByteBuffer.wrap(primitive);
        var worldId1 = bb.getLong();
        var worldId2 = bb.getLong();
        var world = Bukkit.getWorld(new UUID(worldId1, worldId2));
        var x = bb.getDouble();
        var y = bb.getDouble();
        var z = bb.getDouble();
        var yaw = bb.getFloat();
        var pitch = bb.getFloat();
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * The instance of this class.
     */
    public static final LocationDataType INSTANCE = new LocationDataType();
}
