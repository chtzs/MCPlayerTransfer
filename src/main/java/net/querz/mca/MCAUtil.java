package net.querz.mca;

import java.io.IOException;

public class MCAUtil {
    public static MCAFile read(String file) throws IOException {
        MCAFile mcaFile = new MCAFile();
        mcaFile.readMCA(file);
        return mcaFile;
    }

    public static void write(String file, MCAFile mcaFile) throws IOException {
        mcaFile.writeMCA(file);
    }
}
