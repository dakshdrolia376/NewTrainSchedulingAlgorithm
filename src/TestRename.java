import java.io.File;

@SuppressWarnings("unused")
public class TestRename {
    public static void renameStationDetails(){
        File[] listOfFiles = new File("data/temp/databaseStation").listFiles();
        if(listOfFiles==null) {
            System.out.println("No old trains found");
        }

        for (File file: listOfFiles) {
            System.out.print(" "+ file.getName());
            if (file.isFile() && file.getName().startsWith("station_detail") && file.getName().endsWith("..html")) {

                String name1 = file.getName();
                name1 = name1.substring(0,name1.length()-6);
                name1 = name1+".html";
                File newFile = new File("data/temp/databaseStation/"+ name1);
                if(file.renameTo(newFile)){
                    System.out.println("Successful:" + name1);
                }
                else{
                    System.out.println("Unsuccessful:" + name1);
                }
            }
        }
    }
}
