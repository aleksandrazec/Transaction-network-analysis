import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    public void removeBlacklist(File blacklist){
        File[] blacklistFiles = blacklist.listFiles();
        if(blacklistFiles!=null) {
            for (File file : blacklistFiles) {
                String content;
                try {
                    content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                JSONArray addressArray = new JSONArray(content);
                for (int i = 0; i < addressArray.length(); i++) {
                    String addressToRemove = addressArray.getString(i);
                    int addressToRemoveID=returnHash(addressToRemove);
                    for (HashMap.Entry<Integer, SimpleEntry<String, Integer>> entry : adjacencyList.get(addressToRemoveID).getValue().entrySet()) {
                        removeEdge(addressToRemove, entry.getValue().getKey());
                    }
                    for (SimpleEntry<String,HashMap<Integer,SimpleEntry<String, Integer>>> address : adjacencyList) {
                        String currentAddress =address.getKey();
                        int currentAddressID = returnHash(currentAddress);
                        for (HashMap.Entry<Integer, SimpleEntry<String, Integer>> entry : adjacencyList.get(currentAddressID).getValue().entrySet()) {
                            if(entry.getKey()==addressToRemoveID){
                                removeEdge(currentAddress, entry.getValue().getKey());
                            }
                        }
                    }
                }
            }
        }
    }
    public void addEdge(String from, String to, int weight){
        int fromID=returnHash(from);
        int toID=returnHash(to);
        adjacencyList.get(fromID).getValue().put(toID,new SimpleEntry<>(to, weight));
    }
    public void removeEdge(String from, String to){
        int fromID=returnHash(from);
        int toID=returnHash(to);
        adjacencyList.get(fromID).getValue().remove(toID);

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
