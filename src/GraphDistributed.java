import mpi.MPI;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


public class GraphDistributed {
    HashSet<String> irrelevantAddresses = null;
    ArrayList<SimpleEntry<String, HashMap<Integer, String>>> adjacencyList;
    HashMap<String,Integer> hash;
    int availableId=0;


    public GraphDistributed(){
        hash=new HashMap<>();
        adjacencyList=new ArrayList<>();
    }

    public void buildGraphDistributed(File blacklist, File ETNExample, int columnFromETN, int columnToETN) {
        createBlacklist(blacklist);
        readFromFile(ETNExample, columnFromETN, columnToETN);
    }

    public void readFromFile(File f, int from, int to){
        String line;
        try{
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine())!= null){
                String[] values = line.split(",");
                addEdge(values[from], values[to]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int returnHash(String address){
        if(!hash.containsKey(address)){
            hash.put(address,availableId);
            adjacencyList.add(new SimpleEntry<>(address,new HashMap<>()));
            int value=availableId;
            availableId++;
            return value;
        }
        return hash.get(address);
    }


    public void addEdge(String from, String to){
        if (!irrelevantAddresses.contains(from) && !irrelevantAddresses.contains(to)) {
            int fromID=returnHash(from);
            int toID=returnHash(to);
            adjacencyList.get(fromID).getValue().put(toID, to);
        }
    }
    @SuppressWarnings("unchecked")
    public void createBlacklist(File blacklist) {
        byte[] buffer= null;
        int[] size=new int[1];

        if(MPI.COMM_WORLD.Rank()==MPIMain.ROOT){
            File[] blacklistFiles = blacklist.listFiles();
            irrelevantAddresses = new HashSet<>();
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
                       irrelevantAddresses.add(addressToRemove.substring(1, addressToRemove.length() - 1));
                    }
                }
            }
            
            buffer= MPIMain.serializeObject(irrelevantAddresses);
            size[0]=buffer.length;
        }
        MPI.COMM_WORLD.Bcast(size, 0, 1, MPI.INT, 0);
        if(MPI.COMM_WORLD.Rank()!=MPIMain.ROOT){
            buffer=new byte[size[0]];
        }
        MPI.COMM_WORLD.Bcast(buffer, 0, size[0], MPI.BYTE, 0);
        if (MPI.COMM_WORLD.Rank()!=MPIMain.ROOT){
            irrelevantAddresses= (HashSet<String>) MPIMain.deserializeObject(buffer);
        }
    }
    

}
