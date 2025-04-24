import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            breadthFirstSearch(ETN, depth, i);
        }
    }

    public void breadthFirstSearch(GraphSequential ETN, int depth, int root){
        Queue<SimpleEntry<Integer, Integer>> q=new LinkedList<>();
        int currentDepth=0;
        SimpleEntry<Integer, Integer> rootPair= new SimpleEntry<>(root, currentDepth);
        q.add(rootPair);
        while (!q.isEmpty() && currentDepth<=depth){
            SimpleEntry<Integer, Integer> currentPair=q.poll();
            Integer parent=currentPair.getKey();
            currentDepth=currentPair.getValue();

            for (int i = 0; i < ETN.adjacencyList.get(parent).getValue().size(); i++) {
                if(ETN.adjacencyList.get(parent).getValue().get(i)!=null) {
                    if (currentDepth > 0) {
                        if (addEdge(root, ETN.adjacencyList.get(parent).getValue().get(i).getKey(), currentDepth)) {
                            try {
                                bw.write(ETN.adjacencyList.get(root).getKey() + "," + ETN.adjacencyList.get(parent).getValue().get(i).getKey() + "," + currentDepth + "\n");
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    int child = returnHash(ETN.adjacencyList.get(parent).getValue().get(i).getKey());
                    if (currentDepth + 1 <= depth) {
                        SimpleEntry<Integer, Integer> newPair = new SimpleEntry<>(child, currentDepth + 1);
                        q.add(newPair);
                    }
                }
            }
        }

    }
}
