import java.util.AbstractMap;

public class EdgeParallel implements Runnable {
    String from;
    String to;
    int weight;
    public EdgeParallel(String from, String to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
    @Override
    public void run() {
        if (!GraphParallel.irrelevantAddresses.contains(from) && !GraphParallel.irrelevantAddresses.contains(to)) {
            int fromID = GraphParallel.returnHash(from);
            int toID = GraphParallel.returnHash(to);
            synchronized (GraphParallel.addressLock) {
                GraphParallel.adjacencyList.get(fromID).getValue().put(toID, new AbstractMap.SimpleEntry<>(to, weight));
            }
        }
    }
}
