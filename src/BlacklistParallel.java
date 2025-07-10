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

        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        content=content.substring(1,content.length()-1).replaceAll("\\s+","");
        String[] addressArray = content.split(",");
        for (String addressToRemove : addressArray ) {
            graph.addIrrelevantAddress(addressToRemove.substring(1,addressToRemove.length()-1));
        }
        GraphParallel.blacklistSemaphore.release();
    }
}
