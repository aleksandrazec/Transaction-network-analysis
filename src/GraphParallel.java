import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class GraphParallel {
    static int availableId=0;
    static ArrayList<SimpleEntry<String, HashMap<Integer, SimpleEntry<String, Integer>>>> adjacencyList;
    static HashMap<String,Integer> hash;
    static HashSet<String> irrelevantAddresses=new HashSet<>();
    static ExecutorService threadPool = Executors.newCachedThreadPool();
    static Semaphore blacklistSemaphore;
    int blacklistSize;
    public GraphParallel(){
        adjacencyList = new ArrayList<>();
        hash = new HashMap<>();
    }
    public GraphParallel(File blacklist){
        adjacencyList = new ArrayList<>();
        hash = new HashMap<>();
        createBlacklist(blacklist);
    }
    public void createBlacklist(File blacklist) {
        File[] blacklistFiles = blacklist.listFiles();
        blacklistSize=blacklistFiles.length;
        blacklistSemaphore = new Semaphore(blacklistSize);
        for (File file : blacklistFiles) {
            threadPool.submit(new BlacklistParallel(file));
        }
    }
    public void readFromFile(File f, int from, int to){
        try {
            blacklistSemaphore.acquire(blacklistSize);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String line;
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine()) != null)
            {
                String[] values = line.split(",");
                threadPool.submit(new EdgeParallel(values[from],values[to],0));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static public int returnHash(String address) {
        if(!hash.containsKey(address)){
            synchronized (hash) {
                hash.put(address, availableId);
                synchronized (adjacencyList) {
                    adjacencyList.add(new SimpleEntry<>(address, new HashMap<>()));
                }
                int value = availableId;
                availableId++;
                return value;
            }
        }
        return hash.get(address);
    }
}
