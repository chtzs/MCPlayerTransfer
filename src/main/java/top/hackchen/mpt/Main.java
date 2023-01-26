package top.hackchen.mpt;

import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.nbt.tag.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class Main {
    private static final int[] MY_UUID = {-1349816769, 858278922, -1439278174, -1147289869};
    private static final int[] WEIRED_UUID = {-1862471028, 141373501, -1287581019, 282416614};

    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");
        String[] list = list("G:/Temp/entities");
        System.out.println(list[0]);
        MCAFile mcaFile = MCAUtil.read(list[0]);
        MCAUtil.write("G:/Temp/test.mca", mcaFile);
        MCAFile read = MCAUtil.read("G:/Temp/test.mca");
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
            ((CompoundTag) root).forEach((s, tag) -> printName(tag));
        } else if (root instanceof IntArrayTag) {
            IntArrayTag maybeUUID = (IntArrayTag) root;
            if (Arrays.equals(maybeUUID.getValue(), WEIRED_UUID)) {
                System.out.println("Found");
            }
        }
    }
}
