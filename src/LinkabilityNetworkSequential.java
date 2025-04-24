import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.AbstractMap.SimpleEntry;

public class LinkabilityNetworkSequential extends GraphSequential {
    BufferedWriter bw;
    public LinkabilityNetworkSequential(GraphSequential ETN, int depth, File f){
        super();
        try {
            bw=new BufferedWriter(new FileWriter(f));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            bw.write("addressFrom,addressTo,weight\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        breadthFirstSearchLoop(ETN, depth);

        try {
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void breadthFirstSearchLoop(GraphSequential ETN,int depth) {
        for(int i = 0; i< ETN.adjacencyList.size(); i++){
            breadthFirstSearch(ETN, depth, ETN.adjacencyList.get(i).getKey());
        }
    }

    public void breadthFirstSearch(GraphSequential ETN, int depth, String rootAddress){
        Queue<SimpleEntry<String, Integer>> q=new LinkedList<>();
        int currentDepth=0;
        SimpleEntry<String, Integer> rootPair= new SimpleEntry<>(rootAddress, currentDepth);
        q.add(rootPair);
        while (!q.isEmpty() && currentDepth<=depth){
            SimpleEntry<String, Integer> currentPair=q.poll();
            String parent=currentPair.getKey();
            int parentID=ETN.returnHash(parent);
            currentDepth=currentPair.getValue();

            for (HashMap.Entry<Integer, SimpleEntry<String, Integer>> entry : ETN.adjacencyList.get(parentID).getValue().entrySet()) {
                if (currentDepth > 0) {
                    if (addEdge(rootAddress, entry.getValue().getKey(), currentDepth)) {
                        try {
                            bw.write(rootAddress + "," + entry.getValue().getKey() + "," + currentDepth + "\n");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                String child = entry.getValue().getKey();
                if (currentDepth + 1 <= depth) {
                    SimpleEntry<String, Integer> newPair = new SimpleEntry<>(child, currentDepth + 1);
                    q.add(newPair);
                }

            }
        }

    }
}
