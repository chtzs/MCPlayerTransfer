package top.hackchen.mcpt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {

    /*
     * Where does UUID exist?
     * In my research, UUID is existed in those files/folders (relative to save folder):
     * 1. level.dat and level.dat_old
     * 2. advancements/<uuid>.json
     * 3. stats/<uuid>.json
     * 4. entities/*.mca
     * 5. DIM1/entities/*.mca (entities in The End, the world dimension is 1)
     * 6. DIM-1/entities/*.mca (entities in The Nether, the world dimension is -1)
     * 7. playerdata/<uuid>.dat and playerdata/<uuid>.dat_old
     *
     * For json files, we just need rename.
     * For playerdata/<uuid>.dat and playerdata/<uuid>.dat_old, we need rename and do replacement.
     * For the rest, we just need to replace old uuid with new uuid.
     */
    private final static String[] DO_NOT_COPY = {
            "advancements",
            "level.dat",
            "level.dat_old",
            "stats",
            "entities",
            "DIM1/entities/",
            "DIM-1/entities/",
            "playerdata"
    };

    public static void main(String[] args) throws IOException {
        // Print usage
        if (args.length != 8) {
            printUsage();
            return;
        }

        // Parse arguments
        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < 4; i++) {
            argMap.put(args[i * 2], args[i * 2 + 1]);
        }
        String saveFolder = argMap.get("-s");
        String outputFolder = argMap.get("-o");
        MinecraftUUID target;
        MinecraftUUID replacement;

        // Check folder
        if (saveFolder == null || outputFolder == null) {
            System.out.println("Please enter the right folder.");
            return;
        }
        File s = new File(saveFolder);
        File o = new File(Paths.get(outputFolder, s.getName()).toString());
        if (!s.exists() || !s.isDirectory()) {
            System.out.println("The argument save_folder is not a directory!");
            return;
        }
        if (o.exists()) {
            System.out.printf("The output folder of world: %s is already exist. Do you want to delete it? (Y/n):", o);
            try (Scanner in = new Scanner(System.in)) {
                String answer = in.next().toLowerCase(Locale.ROOT).trim();
                if (answer.equals("yes") || answer.equals("y")) {
                    try {
                        FileUtil.deleteDirectory(o.toString());
                    } catch (RuntimeException e) {
                        System.out.println(e.getMessage());
                        return;
                    }
                } else {
                    System.out.println("Transfer abort. Make sure that the output folder of world is empty.");
                    return;
                }
            }
        }

        outputFolder = o.toString();
        FileUtil.createDirectoryIfNotExist(outputFolder);

        // Check UUID
        try {
            target = autoIdentify(argMap.get("-t"));
            replacement = autoIdentify(argMap.get("-r"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }


        // Do the transfer
        System.out.println("Begin transferring...");
        System.out.printf("User: %s --> %s\n", target.getFullUUID(), replacement.getFullUUID());
        System.out.printf("Path: %s --> %s\n", saveFolder, outputFolder);
        beginTransferring(saveFolder, outputFolder, target, replacement);
        System.out.println("Done. Enjoy your game :D");
    }

    private static MinecraftUUID autoIdentify(String arg) throws IOException {
        File file = new File(arg);
        if (file.exists()) {
            if (file.getName().equals("level.dat")) {
                return MinecraftUUID.fromLevelData(arg);
            } else {
                return MinecraftUUID.fromPlayerData(arg);
            }
        } else {
            return MinecraftUUID.fromHexString(arg);
        }
    }

    private static void beginTransferring(String saveFolder,
                                          String outputFolder,
                                          MinecraftUUID target,
                                          MinecraftUUID replacement) throws IOException {
        // advancements
        FileUtil.rename(saveFolder + "/advancements/" + target.getFullUUID() + ".json",
                outputFolder + "/advancements/", target, replacement);
        // stats
        FileUtil.rename(saveFolder + "/stats/" + target.getFullUUID() + ".json",
                outputFolder + "/stats/", target, replacement);

        // level.dat and level.dat_old
        FileUtil.replace(saveFolder + "/level.dat", outputFolder, target, replacement);
        FileUtil.replace(saveFolder + "/level.dat_old", outputFolder, target, replacement);

        // entities
        for (String path : list(saveFolder + "/entities")) {
            FileUtil.replace(path, outputFolder + "/entities", target, replacement);
        }

        // DM 1 entities
        for (String path : list(saveFolder + "/DIM1/entities")) {
            FileUtil.replace(path, outputFolder + "/DIM1/entities", target, replacement);
        }

        // DM -1 entities
        for (String path : list(saveFolder + "/DIM-1/entities")) {
            FileUtil.replace(path, outputFolder + "/DIM-1/entities", target, replacement);
        }

        // playerdata/<uuid>.dat and playerdata/<uuid>.dat_old
        FileUtil.renameAndReplace(saveFolder + "/playerdata/" + target.getFullUUID() + ".dat",
                outputFolder + "/playerdata/", target, replacement);
        FileUtil.renameAndReplace(saveFolder + "/playerdata/" + target.getFullUUID() + ".dat_old",
                outputFolder + "/playerdata/", target, replacement);

        // copy rest
        copyFolder(Paths.get(saveFolder), Paths.get(outputFolder));
    }

    private static void copyFolder(Path src, Path dest) throws IOException {
        Path[] banList = makeBanList(src);
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(source -> copy(banList, source, dest.resolve(src.relativize(source))));
        }
    }

    private static Path[] makeBanList(Path root) {
        Path[] result = new Path[DO_NOT_COPY.length];
        for (int i = 0; i < DO_NOT_COPY.length; i++) {
            result[i] = Paths.get(root.toString(), DO_NOT_COPY[i]).toAbsolutePath();
        }
        return result;
    }

    private static void copy(Path[] banList, Path source, Path dest) {
        try {
            Path absolutePath = source.toAbsolutePath();
            for (Path ban : banList) {
                if (absolutePath.startsWith(ban)) return;
            }
            if (Files.exists(dest)) return;
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println(source);
            System.out.println(dest);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String[] list(String dir) throws IOException {
        try (Stream<Path> list = Files.list(Paths.get(dir))) {
            return list.map(Path::toString).toArray(String[]::new);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar mcpt.jar -t <target_uuid> -r <replacement_uuid> -s <save_folder> -o <output_folder>");
        System.out.println("       A tool for replace world's player data with a new account.");
        System.out.println("       uuid: unique id of player");
        System.out.println("             You could leave a path to level.dat or /path/to/save/playerdata/<player_uuid>.dat,");
        System.out.println("             and the program will automatically find the uuid.");
        System.out.println("             You can also directly paste a player uuid, which could be found in https://mcuuid.net/.");
        System.out.println("       save_folder: Path to your world's save folder. They usually located in ./minecraft/saves/YOUR_WORLD_NAME");
        System.out.println("       output_folder: The ROOT FOLDER where translated folder exist. The usually be your new 'saves' folder.\n");
        System.out.println("       Example: java mpt.jar -t saves/MyWorld/level.dat -r YOUR_NEW_UUID -s saves/MyWorld -o new_saves");
    }
}
