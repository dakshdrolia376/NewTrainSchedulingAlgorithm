package iitp.naman.newtrainschedulingalgorithm.util;

import java.io.File;

import static java.util.Objects.requireNonNull;

/**
 * Helper class for folder.
 */
public class FolderHelper {

    /**
     * Creates folder for given directory.
     * Eg. if path is data/test/folder, the created folder will be data/test/folder.
     *
     * @param path path.
     * @return true if successful.
     */
    public static boolean createFolder(String path) {
        requireNonNull(path, "path is null");
        File file = new File(path);
        return file.exists() || file.mkdirs();
    }

    /**
     * Creates folder for given file parent.
     * Eg. if path is data/test/folder/file, the created folder will be data/test/folder
     *
     * @param path path of file/folder.
     * @return true if successful.
     */
    public static boolean createParentFolder(String path) {
        requireNonNull(path, "path is null");
        File file = new File(path);
        return createFolder(file.getParentFile().getPath());
    }

    /**
     * Deletes the content of a folder.
     *
     * @param folderPath path.
     */
    public static void deleteFolderContent(String folderPath) {
        File file = new File(folderPath);
        if (!file.exists()) {
            return;
        }

        try {
            for (File childFile : file.listFiles()) {

                if (childFile.isDirectory()) {
                    deleteFolderContent(childFile.getPath());
                } else {
                    if (!childFile.delete()) {
                        throw new RuntimeException("Unable to delete file: " + childFile.getPath());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
