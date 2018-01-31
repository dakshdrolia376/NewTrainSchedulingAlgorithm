import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import static java.util.Objects.requireNonNull;

public class WriteToFile {
    public void write(String pathFile, String content, boolean append) {
        requireNonNull(pathFile, "The file path is null.");
        requireNonNull(content, "The content is null.");
        try {
            File file = new File(pathFile);
            System.out.println("Writing data to file: " + file.getName());
            if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()){
                System.out.println("Unable to create file located at " + pathFile);
                return;
            }
            FileWriter fWriter = new FileWriter(file, append);
            BufferedWriter bWriter = new BufferedWriter(fWriter);
            bWriter.write(content);
            bWriter.close();
            fWriter.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
