import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphSequential {
    int availableId=0;
    ArrayList<SimpleEntry<String,HashMap<Integer,SimpleEntry<String, Integer>>>> adjacencyList;
    HashMap<String,Integer> hash;

    public GraphSequential(){
        adjacencyList = new ArrayList<>();
        hash = new HashMap<>();

    }
    public void readFromFile(File f, int from, int to){
        String line;
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine()) != null)
            {
                String[] values = line.split(",");
                addEdge(values[from],values[to], 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean addEdge(String from, String to, int weight){
        int fromID=returnHash(from);
        int toID=returnHash(to);

            adjacencyList.get(fromID).getValue().put(toID,new SimpleEntry<>(to, weight));
            return true;

    }

    public int returnHash(String address) {
        if(!hash.containsKey(address)){
            hash.put(address, availableId);
            adjacencyList.add(new SimpleEntry<>(address, new HashMap<>()));
            int value=availableId;
            availableId++;
            return value;
        }
        return hash.get(address);
    }
}
