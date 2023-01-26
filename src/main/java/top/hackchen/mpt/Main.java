package top.hackchen.mpt;

import net.querz.mca.MCAUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    public static void main(String[] args) throws IOException {
        printUsage();
        for (String path : list("G:\\PCL2\\.minecraft\\versions\\1.17.1\\saves\\新的开始___\\poi")) {
            MCAUtil.read(path).testFind(MinecraftUUID.fromHexString("90fcf28c-086d-303d-b341-12a510d555e6"));
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java mpt.jar -t <target_uuid> -r <replacement_uuid> -d <save_folder> -o <output_folder>");
        System.out.println("       A tool for replace world's player data with a new account.");
        System.out.println("       uuid: unique id of player");
        System.out.println("             You could leave a path to level.dat or /path/to/save/playerdata/<player_uuid>.dat,");
        System.out.println("             and the program will automatically find the uuid.");
        System.out.println("             You can also directly paste a player uuid, which could be found in https://mcuuid.net/.");
        System.out.println("       save_folder: Path to your world's save folder. They usually located in ./minecraft/saves/YOUR_WORLD_NAME");
        System.out.println("       output_folder: The folder where translated world exist. YOU SHOULD NEVER MAKE THIS FOLDER THE SAME AS SAVE_FOLDER!!!");
    }

    private static String[] list(String dir) throws IOException {
        try (Stream<Path> list = Files.list(Paths.get(dir))) {
            return list.map(Path::toString).toArray(String[]::new);
        }
    }
}
