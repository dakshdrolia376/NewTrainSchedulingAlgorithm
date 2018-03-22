import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String... s){

        FileReader fReader;
        BufferedReader bReader;
        String fileName1 = "abc1.txt";
        String fileName2 = "abc2.txt";
        List<String> st1 = new ArrayList<>();
        List<String> st2 = new ArrayList<>();
        String line;
        try {
            fReader = new FileReader(fileName1);
            bReader = new BufferedReader(fReader);
            while((line = bReader.readLine()) != null) {
                String data = line.split("\\s+")[1];
                st2.add(data);
            }

            fReader = new FileReader(fileName2);
            bReader = new BufferedReader(fReader);
            while((line = bReader.readLine()) != null) {
                String data = line.split("\\s+")[0];
                st1.add(Scheduler.getStationIdFromName(data));
            }

            for(int i=0;i<st1.size();i++){
                for(int j=i+1;j<st1.size();j++){
                    if(st1.get(i).equalsIgnoreCase(st1.get(j))){
                        System.out.println("Duplicate: " + st1.get(i));
                    }
                }

                boolean found =false;
                for(String stTemp: st2){
                    if(st1.get(i).equalsIgnoreCase(stTemp)){
                        found = true;
                    }
                }
                if(!found){
                    System.out.println("Not found: "+st1.get(i));
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
