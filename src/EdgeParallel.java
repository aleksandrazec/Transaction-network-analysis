public class EdgeParallel implements Runnable {
    String from;
    String to;
    GraphParallel graph;
    public EdgeParallel(String from, String to, GraphParallel graph) {
        this.from = from;
        this.to = to;
        this.graph = graph;
    }
    @Override
    public void run() {
        if (graph.isRelevantAddress(from) && graph.isRelevantAddress(to)) {
            int fromID = graph.returnHash(from);
            int toID = graph.returnHash(to);
            graph.addEdge(fromID, toID, to);
        }
        GraphParallel.graphSemaphore.release();
    }
}
