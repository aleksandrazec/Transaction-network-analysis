import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BlacklistParallel implements Runnable {
    File file;
    GraphParallel graph;
    public BlacklistParallel(File file, GraphParallel graph) {
        this.file = file;
        this.graph = graph;
    }
    @Override
    public void run() {

        System.out.println("in thread "+file.getName());

        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONArray addressArray = new JSONArray(content);
        for (int i = 0; i < addressArray.length(); i++) {
            String addressToRemove = addressArray.getString(i);
            graph.addIrrelevantAddress(addressToRemove);
        }
        GraphParallel.blacklistSemaphore.release();
        System.out.println("out thread "+file.getName());
    }
}
