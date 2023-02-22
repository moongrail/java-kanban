package util;

import exceptions.ManagerSaveException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileDataCreatorUtil {
    private static final  Path RESOURCE_PATH = Path.of("resources", "history.csv");

    private FileDataCreatorUtil() {

    }

    public static File getOrCreateFileAndDir() {
        File dir = Path.of("resources", "test").toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Path path = null;
        File file = null;

        if (!RESOURCE_PATH.toFile().exists()) {
            try {
                path = Files.createFile(RESOURCE_PATH);
            } catch (IOException e) {
                throw new ManagerSaveException(e.getMessage());
            }
            file = path.toFile();
        } else {
            file = RESOURCE_PATH.toFile();
        }
        return file;
    }
}
