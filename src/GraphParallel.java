import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

public class GraphParallel {
    int availableId=0;
    public ArrayList<SimpleEntry<String, HashMap<Integer, SimpleEntry<String, Integer>>>> adjacencyList;
    HashMap<String,Integer> hash;
    final Object hashLock = new Object();
    final Object addressLock=new Object();
    HashSet<String> irrelevantAddresses=new HashSet<>();
    static ExecutorService threadPool = Executors.newWorkStealingPool();
    static Semaphore blacklistSemaphore;
    static Semaphore graphSemaphore;
    int lines = 0;

    int blacklistSize;
    public GraphParallel(){
        adjacencyList = new ArrayList<>();
        hash = new HashMap<>();
    }
    public GraphParallel(File blacklist, File ETNExample, int columnFromETN, int columnToETN){
        adjacencyList = new ArrayList<>();
        hash = new HashMap<>();
        System.out.println("Loading Blacklist");
        createBlacklist(blacklist);
        try {
            blacklistSemaphore.acquire(blacklistSize);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        readFromFile(ETNExample, columnFromETN, columnToETN);
    }
    public void createBlacklist(File blacklist) {
        System.out.println("Creating Blacklist");
        File[] blacklistFiles = blacklist.listFiles();
        blacklistSize= blacklistFiles != null ? blacklistFiles.length : 0;
        System.out.println("Blacklist size: " + blacklistSize);
        blacklistSemaphore = new Semaphore(0);
        if (blacklistFiles != null) {
            for (File file : blacklistFiles) {
                threadPool.submit(new BlacklistParallel(file, this));
            }
        }
    }
    public void readFromFile(File f, int from, int to){

        try (Stream<String> stream = Files.lines(f.toPath(), StandardCharsets.UTF_8)) {
            lines = Math.toIntExact(stream.count());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        graphSemaphore= new Semaphore(0);

        String line;

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine()) != null)
            {
                String[] values = line.split(",");
                threadPool.submit(new EdgeParallel(values[from],values[to],0, this));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            graphSemaphore.acquire(lines);
            System.out.println("largest id is : "+availableId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public int returnHash(String address) {
        synchronized (hashLock) {
            if(!hash.containsKey(address)){
                hash.put(address, availableId);
                synchronized (addressLock) {
                    adjacencyList.add(new SimpleEntry<>(address, new HashMap<>()));
                }
                int value = availableId;
                availableId++;
                return value;
            }
            return hash.get(address);
        }
    }
    public void addEdge(int fromID, int toID, String to, int weight){
        synchronized (addressLock){
            adjacencyList.get(fromID).getValue().put(toID, new SimpleEntry<>(to, weight));
//            System.out.println("edge "+ fromID+ " to "+ toID +" added");
        }
    }
    public void addIrrelevantAddress(String addressToRemove){
//        System.out.println("In function "+ addressToRemove);
        synchronized (addressLock){
//            System.out.println("Adding irrelevant address "+ addressToRemove);
            irrelevantAddresses.add(addressToRemove);
//            System.out.println(addressToRemove+" added to blacklist");
        }
    }
    public boolean isRelevantAddress(String address){
        return !irrelevantAddresses.contains(address);
    }
}
