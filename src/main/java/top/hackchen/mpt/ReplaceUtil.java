package top.hackchen.mpt;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntArrayTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

public class ReplaceUtil {
    public static void replaceUUIDReclusive(Tag<?> root, MinecraftUUID target, MinecraftUUID replacement) {
        if (root instanceof ListTag) {
            ListTag<?> tag = (ListTag<?>) root;
            tag.forEach((Consumer<Object>) o -> replaceUUIDReclusive((Tag<?>) o, target, replacement));
        } else if (root instanceof CompoundTag) {
            ((CompoundTag) root).forEach((s, tag) -> replaceUUIDReclusive(tag, target, replacement));
        } else if (root instanceof IntArrayTag) {
            IntArrayTag maybeUUID = (IntArrayTag) root;
            if (Arrays.equals(maybeUUID.getValue(), target.getUUID())) {
                maybeUUID.setValue(replacement.getUUID());
            }
        }
    }

    public static void testFind(String filename, MinecraftUUID target) throws IOException {
        NamedTag tag = NBTUtil.read(filename);
        testFindReclusive(tag.getTag(), target);
    }

    public static void testFindReclusive(Tag<?> root, MinecraftUUID target) {
        if (root instanceof ListTag) {
            ListTag<?> tag = (ListTag<?>) root;
            tag.forEach((Consumer<Object>) o -> testFindReclusive((Tag<?>) o, target));
        } else if (root instanceof CompoundTag) {
            ((CompoundTag) root).forEach((s, tag) -> testFindReclusive(tag, target));
        } else if (root instanceof IntArrayTag) {
            IntArrayTag maybeUUID = (IntArrayTag) root;
            if (Arrays.equals(maybeUUID.getValue(), target.getUUID())) {
                System.out.println("Found.");
            }
        }
    }
}
