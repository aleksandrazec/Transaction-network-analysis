public class EdgeParallel implements Runnable {
    String from;
    String to;
    int weight;
    GraphParallel graph;
    public EdgeParallel(String from, String to, int weight, GraphParallel graph) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.graph = graph;
    }
    @Override
    public void run() {
        if (graph.isRelevantAddress(from) && graph.isRelevantAddress(to)) {
            int fromID = graph.returnHash(from);
            int toID = graph.returnHash(to);
            graph.addEdge(fromID, toID, to, weight);
        }
        GraphParallel.graphSemaphore.release();
    }
}
