package top.hackchen;

import net.querz.mca.CompressionType;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    private static final int[] MY_UUID = {-1349816769, 858278922, -1439278174, -1147289869};
    private static final int[] WEIRED_UUID = {-1862471028, 141373501, -1287581019, 282416614};

    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");
        // NamedTag level = NBTUtil.read("G:/Temp/level.dat");
        // CompoundTag data = (CompoundTag) level.getTag();
        // printName(data);
        // MCAUtil.read("").getChunk(0).getEntities()
        // MCAUtil.read("G:/Temp/entities/r.-1.0.mca");
        for (String file : list("G:/Temp/entities")) {
            try {
                for (CompoundTag tag : readMCA(file)) {
                    printName(tag);
                }
            } catch (Exception e) {
                System.out.println(file);
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static List<CompoundTag> readMCA(String filename) throws IOException {
        List<CompoundTag> tags = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(filename, "r")) {
            if (raf.length() == 0) return tags;
            for (int i = 0; i < 1024; ++i) {
                raf.seek(i * 4);
                int offset = raf.read() << 16;
                offset |= (raf.read() & 0xFF) << 8;
                offset |= raf.read() & 0xFF;
                if (raf.readByte() == 0) {
                    continue;
                }
                raf.seek(4096 + i * 4);
                int timestamp = raf.readInt();
                raf.seek(4096L * offset + 4); //+4: skip data size

                byte compressionTypeByte = raf.readByte();
                CompressionType compressionType = CompressionType.getFromID(compressionTypeByte);
                if (compressionType == null) {
                    throw new IOException("invalid compression type " + compressionTypeByte);
                } else {
                    BufferedInputStream dis = new BufferedInputStream(compressionType.decompress(new FileInputStream(raf.getFD())));
                    NamedTag tag = (new NBTDeserializer(false)).fromStream(dis);
                    if (tag != null && tag.getTag() instanceof CompoundTag) {
                        tags.add((CompoundTag) tag.getTag());
                    }
                }
            }
        }
        return tags;
    }

    private static String[] list(String dir) throws IOException {
        try (Stream<Path> list = Files.list(Paths.get(dir))) {
            return list.map(Path::toString).toArray(String[]::new);
        }
    }

    private static void printName(Tag<?> root) {
        if (root instanceof ListTag) {
            ListTag<?> tag = (ListTag<?>) root;
            tag.forEach(Main::printName);
        } else if (root instanceof CompoundTag) {
            ((CompoundTag) root).forEach((s, tag) -> {
                if (s.contains("Target")) {
                    // -1349816769  858278922  -1439278174  -1147289869
                    if (tag instanceof IntArrayTag) {
                        int[] value = ((IntArrayTag) tag).getValue();
                        if (Arrays.equals(value, WEIRED_UUID)) {
                            // System.out.println(s);
                            // System.out.println(tag);
                        }
                    }
                } else if(s.equals("id") &&  ((StringTag) tag).getValue().equals("villager")) {
                    System.out.println("Yes!");
                }
                if (tag instanceof CompoundTag || tag instanceof ListTag) {
                    printName(tag);
                }
            });
        }
        // else if (root instanceof StringTag) {
        //     if(((StringTag) root).getValue().equals("villager")){
        //         System.out.println("Yes!");
        //     }
        // }
    }
}
