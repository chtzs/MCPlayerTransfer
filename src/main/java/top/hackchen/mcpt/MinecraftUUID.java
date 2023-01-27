package top.hackchen.mcpt;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntArrayTag;

import java.io.IOException;
import java.util.Arrays;

public class MinecraftUUID {
    private final int[] uuid;

    public int[] getUUID() {
        return uuid;
    }

    private MinecraftUUID(int[] uuid) {
        this.uuid = Arrays.copyOf(uuid, 4);
    }

    public static MinecraftUUID fromIntArray(int[] uuid) {
        return new MinecraftUUID(uuid);
    }

    public static MinecraftUUID fromLevelData(String levelFile) throws IOException {
        NamedTag tag = NBTUtil.read(levelFile);
        CompoundTag root = (CompoundTag) tag.getTag();
        CompoundTag data = root.getCompoundTag("Data");
        CompoundTag player = data.getCompoundTag("Player");
        IntArrayTag uuid = player.getIntArrayTag("UUID");
        return new MinecraftUUID(uuid.getValue());
    }

    public static MinecraftUUID fromPlayerData(String playerFile) throws IOException {
        NamedTag tag = NBTUtil.read(playerFile);
        CompoundTag root = (CompoundTag) tag.getTag();
        IntArrayTag uuid = root.getIntArrayTag("UUID");
        return new MinecraftUUID(uuid.getValue());
    }

    public static MinecraftUUID fromHexString(String hex) {
        hex = hex.replace("-", "");
        if (hex.length() != 32) {
            throw new IllegalArgumentException("The length of UUID String must be 32");
        }
        int[] uuid = new int[4];
        for (int i = 0; i < 4; i++) {
            // trick: use long to allow overflow
            uuid[i] = (int) Long.parseLong(hex.substring(i * 8, i * 8 + 8), 16);
        }
        return new MinecraftUUID(uuid);
    }

    public String getTrimmedUUID() {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            // solve overflow problem
            hex.append(String.format("%08x", uuid[i] & 0xFFFFFFFFL));
        }
        return hex.toString();
    }

    public String getFullUUID() {
        String trimmed = getTrimmedUUID();
        return trimmed.substring(0, 8) +
                '-' +
                trimmed.substring(8, 12) +
                '-' +
                trimmed.substring(12, 16) +
                '-' +
                trimmed.substring(16, 20) +
                '-' +
                trimmed.substring(20, 32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftUUID that = (MinecraftUUID) o;
        return Arrays.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(uuid);
    }

    @Override
    public String toString() {
        String trimmed = getTrimmedUUID();
        String full = getFullUUID();
        return "MinecraftUUID {\n" +
                "   Int Array UUID = " + Arrays.toString(uuid) +
                ",\n   Full UUID = " + full +
                ",\n   Trimmed UUID = " + trimmed +
                "\n}";
    }
}
