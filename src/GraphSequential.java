import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GraphSequential {
    int availableId=0;
    ArrayList<SimpleEntry<String,HashMap<Integer,SimpleEntry<String, Integer>>>> adjacencyList;
    HashMap<String,Integer> hash;
    HashSet<String> irrelevantAddresses=new HashSet<>();
    public GraphSequential(){
        adjacencyList = new ArrayList<>();
        hash = new HashMap<>();
    }
    public GraphSequential(File blacklist, File ETNExample, int columnFromETN, int columnToETN){
        adjacencyList = new ArrayList<>();
        hash = new HashMap<>();
        createBlacklist(blacklist);
        readFromFile(ETNExample, columnFromETN, columnToETN);
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
    public void createBlacklist(File blacklist){
        File[] blacklistFiles = blacklist.listFiles();
        if(blacklistFiles!=null) {
            for (File file : blacklistFiles) {
                String content;
                try {
                    content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                content=content.substring(1,content.length()-1).replaceAll("\\s+","");
                String[] addressArray = content.split(",");
                for (String addressToRemove : addressArray ) {
                    irrelevantAddresses.add(addressToRemove.substring(1,addressToRemove.length()-1));
                }
            }
        }
    }
    public void addEdge(String from, String to, int weight){
        if (!irrelevantAddresses.contains(from) && !irrelevantAddresses.contains(to)){
            int fromID=returnHash(from);
            int toID=returnHash(to);
            adjacencyList.get(fromID).getValue().put(toID,new SimpleEntry<>(to, weight));
        }
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
