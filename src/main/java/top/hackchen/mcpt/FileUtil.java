package top.hackchen.mcpt;

import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.IntArrayTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileUtil {
    public static void deleteDirectory(String dir) throws IOException {
        Path path = Paths.get(dir);
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (!file.delete()) {
                            throw new RuntimeException("Failed to delete file: " + file);
                        }
                    });
        }
    }

    public static void createDirectoryIfNotExist(String directory) throws IOException {
        File file = new File(directory);
        if (!file.exists()) {
            Files.createDirectories(Paths.get(directory));
        }
    }

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

    private static String getNewName(String file, String outputDir, MinecraftUUID target, MinecraftUUID replacement) {
        File f = new File(file);
        String name = f.getName().split("\\.")[0];
        // To ensure that we rename the right file.
        if (target.getFullUUID().equals(name)) {
            String suffix = f.getName().split("\\.")[1];
            return Paths.get(outputDir, replacement.getFullUUID() + "." + suffix).toString();
        }
        throw new RuntimeException("Why???");
    }

    // rename file
    public static void rename(String file, String outputDir, MinecraftUUID target, MinecraftUUID replacement) throws IOException {
        Path output = Paths.get(getNewName(file, outputDir, target, replacement));
        createDirectoryIfNotExist(output.toString());
        Files.copy(Paths.get(file),
                output,
                StandardCopyOption.REPLACE_EXISTING);
    }

    // replace uuid
    public static void replace(String file, String outputDir, MinecraftUUID target, MinecraftUUID replacement) throws IOException {
        createDirectoryIfNotExist(outputDir);
        File f = new File(file);
        String filename = f.getName();
        String output = Paths.get(outputDir, f.getName()).toString();
        // *.dat
        if (filename.endsWith("dat") || filename.endsWith("dat_old")) {
            NamedTag root = NBTUtil.read(file);
            replaceUUIDReclusive(root.getTag(), target, replacement);
            NBTUtil.write(root, output);
        }
        // *.mca
        else if (filename.endsWith("mca")) {
            MCAFile mcaFile = MCAUtil.read(file);
            mcaFile.replaceUUID(target, replacement);
            MCAUtil.write(output, mcaFile);
        }
        // just copy
        else {
            Files.copy(Paths.get(file), Paths.get(outputDir, f.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void renameAndReplace(String file, String outputDir, MinecraftUUID target, MinecraftUUID replacement) throws IOException {
        replace(file, outputDir, target, replacement);
        File f = new File(file);
        String output = Paths.get(outputDir, f.getName()).toString();
        File outputFile = new File(output);
        if (!outputFile.renameTo(new File(getNewName(file, outputDir, target, replacement)))) {
            throw new RuntimeException("Can't be that...");
        }
    }
}
