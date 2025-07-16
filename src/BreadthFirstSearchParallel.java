import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class BreadthFirstSearchParallel implements Runnable{
    GraphParallel ETN;
    LinkabilityNetworkParallel linkability;
    int depth;
    String rootAddress;
    Queue<SimpleEntry<String,Integer>> q= new LinkedList<>();
    int currentDepth=0;
    HashSet<String> visited = new HashSet<>();
    public BreadthFirstSearchParallel(GraphParallel ETN, int depth, String rootAddress, LinkabilityNetworkParallel linkability) {
        this.ETN = ETN;
        this.depth = depth;
        this.rootAddress = rootAddress;
        this.linkability = linkability;
        visited.add(rootAddress);
        SimpleEntry<String, Integer> rootPair= new SimpleEntry<>(rootAddress, currentDepth);
        q.offer(rootPair);
    }
    @Override
    public void run() {
        while(!q.isEmpty() && currentDepth<=depth){
            SimpleEntry<String,Integer> currentPair= q.poll();
            String parent=currentPair.getKey();
            int parentID=ETN.returnHash(parent);
            currentDepth=currentPair.getValue();

            for (HashMap.Entry<Integer, String> entry : ETN.adjacencyList.get(parentID).getValue().entrySet()) {
                String child=entry.getValue();
                if(currentDepth>0){
                    if(linkability.isRelevantAddress(child)){
                        linkability.writeLine(rootAddress, child, currentDepth);
                        linkability.incrementWeight(currentDepth);
                    }
                }
                if (!visited.contains(child)){
                    if(currentDepth+1<=depth){
                        SimpleEntry<String, Integer> newPair= new SimpleEntry<>(child, currentDepth+1);
                        q.add(newPair);
                        visited.add(child);
                    }
                }
            }
        }
        LinkabilityNetworkParallel.linkabilitySemaphore.release();
    }
}
