package iitp.naman.newtrainschedulingalgorithm.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to write data ro file.
 */
public class WriteToFile {
    /**
     * Write data to file.
     *
     * @param pathFile path of the file.
     * @param content  data to be written to be file.
     * @param append   controls whether to append to file or replace the content.
     */
    public void write(String pathFile, String content, boolean append) {
        requireNonNull(pathFile, "The file path is null.");
        requireNonNull(content, "The content is null.");
        try {
            File file = new File(pathFile);
            System.out.println("Writing data to file: " + file.getName());
            if (file.getParentFile() != null && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                System.out.println("Unable to create file located at " + pathFile);
                return;
            }
            FileWriter fWriter = new FileWriter(file, append);
            BufferedWriter bWriter = new BufferedWriter(fWriter);
            bWriter.write(content);
            bWriter.close();
            fWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
