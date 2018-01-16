import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class WriteToFile {
    public static void write(String pathFile, String content, boolean append) {
        File file = new File(pathFile);
        if(!file.getParentFile().exists()){
            if(!file.getParentFile().mkdirs()){
                System.out.println("Unable to create file " + pathFile);
                return;
            }
        }
        BufferedWriter bWriter = null;
        FileWriter fWriter = null;
        try {
            fWriter = new FileWriter(pathFile, append);
            bWriter = new BufferedWriter(fWriter);
            bWriter.write(content);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (bWriter != null)
                    bWriter.close();
                if (fWriter != null)
                    fWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
