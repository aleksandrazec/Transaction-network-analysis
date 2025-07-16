import java.io.*;
import java.util.HashSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class LinkabilityNetworkParallel{
    BufferedWriter bw;
    AtomicInteger[] weights;
    HashSet<String> relevantAddresses=new HashSet<>();
    static Semaphore linkabilitySemaphore;
    final Object bwLock=new Object();
    public void buildLinkabilityNetworkParallel(GraphParallel ETN, int depth, File f, File NFT, int from, int to) {
        identifyRelevantAddresses(NFT, from, to);

        try {
            bw=new BufferedWriter(new FileWriter(f));
            bw.write("addressFrom,addressTo,weight\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        weights=new AtomicInteger[depth+1];
        for (int i = 0; i < depth+1; i++) {
            weights[i]=new AtomicInteger(0);
        }

        linkabilitySemaphore=new Semaphore(ETN.adjacencyList.size());
        try {
            linkabilitySemaphore.acquire(ETN.adjacencyList.size());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        for(int i = 0; i< ETN.adjacencyList.size(); i++){
            if(relevantAddresses.contains(ETN.adjacencyList.get(i).getKey())){
                GraphParallel.threadPool.submit(new BreadthFirstSearchParallel(ETN, depth, ETN.adjacencyList.get(i).getKey(), this));
            }else {
                linkabilitySemaphore.release();
            }
        }

        try {
            linkabilitySemaphore.acquire(ETN.adjacencyList.size());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        printWeights();
    }

    public void printWeights(){
        for (int i = 1; i < weights.length; i++) {
            System.out.println("Number of links of weight "+i+" is: "+weights[i]);
        }
    }

    public void identifyRelevantAddresses(File f, int from, int to) {
        String line;
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(f));
            while ((line = br.readLine()) != null)
            {
                String[] values = line.split(",");
                relevantAddresses.add(values[from]);
                relevantAddresses.add(values[to]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRelevantAddress(String address) {
        return relevantAddresses.contains(address);
    }

    public void writeLine(String from, String to, int depth){
        synchronized (bwLock){
            try {
                bw.write(from + "," + to + "," + depth + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void incrementWeight(int index){
        weights[index].incrementAndGet();
    }
}
