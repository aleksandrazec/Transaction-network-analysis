import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BlacklistParallel implements Runnable {
    File file;
    BlacklistParallel(File file) {
        this.file = file;
    }
    @Override
    public void run() {
        try {
            GraphParallel.blacklistSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String content;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JSONArray addressArray = new JSONArray(content);
        for (int i = 0; i < addressArray.length(); i++) {
            String addressToRemove = addressArray.getString(i);
            synchronized (GraphParallel.irrelevantAddresses) {
                GraphParallel.irrelevantAddresses.add(addressToRemove);
            }
        }
        GraphParallel.blacklistSemaphore.release();
    }
}
