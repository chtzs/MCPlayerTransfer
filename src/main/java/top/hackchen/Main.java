package top.hackchen;

import net.querz.nbt.tag.ByteTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.Tag;

import java.util.function.BiConsumer;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        CompoundTag ct = new CompoundTag();

        ct.put("byte", new ByteTag((byte) 1));
        ct.put("double", new DoubleTag(1.234));
        ct.putString("string", "stringValue");
        ct.forEach((s, tag) -> System.out.println(s));
        System.out.println(ct);
    }
}
