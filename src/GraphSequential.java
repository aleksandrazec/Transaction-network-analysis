import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphSequential {
    int availableId=0;
    ArrayList<SimpleEntry<String,ArrayList<SimpleEntry<String, Integer>>>> adjacencyList;
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
    public void addEdge(String from, String to, int weight){
        if(!hash.containsKey(from)){
            hash.put(from, availableId);
            availableId++;
            adjacencyList.add(hash.get(from), new SimpleEntry<>(from, new ArrayList<>()));
        }
        if(!hash.containsKey(to)){
            hash.put(to,availableId);
            availableId++;
            adjacencyList.add(hash.get(to), new SimpleEntry<>(to, new ArrayList<>()));
        }


        if(adjacencyList.get(hash.get(from)).getValue().size()<=hash.get(to)){
            for (int i = adjacencyList.get(hash.get(from)).getValue().size(); i <= hash.get(to); i++) {
                adjacencyList.get(hash.get(from)).getValue().add(i, null);
            }
        }
        if (adjacencyList.get(hash.get(from)).getValue().get(hash.get(to))==null){
            adjacencyList.get(hash.get(from)).getValue().add(hash.get(to),new SimpleEntry<>(to, weight));
        }
    }
    public boolean addEdge(int from, String to, int weight){
        if(!hash.containsKey(to)){
            hash.put(to,availableId);
            availableId++;
            adjacencyList.add(hash.get(to), new SimpleEntry<>(to, new ArrayList<>()));
        }

        if(adjacencyList.get(from).getValue().size()<hash.get(to)){
            for (int i = adjacencyList.get(from).getValue().size(); i <= hash.get(to); i++) {
                adjacencyList.get(from).getValue().add(i, null);
            }
        }

        if (adjacencyList.get(from).getValue().get(hash.get(to))==null){
            adjacencyList.get(from).getValue().add(hash.get(to),new SimpleEntry<>(to, weight));
            return true;
        }
        return false;
    }
    public int returnHash(String address){
            return hash.get(address);
    }
}
