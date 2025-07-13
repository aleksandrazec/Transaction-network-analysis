package Distributed;

import mpi.MPI;
import java.io.*;

public class MPIMain {
    static File ETNExample=new File("./linkabilityNetworksData/prog3ETNsample.csv");
    static File blacklist=new File("./linkabilityNetworksData/blacklist");
    static File NFTTransfers=new File("./linkabilityNetworksData/boredapeyachtclub.csv");
    static int columnFromETN=5;
    static int columnToETN=6;
    static int columnFromNFT=4;
    static int columnToNFT=5;
    static File fileToWrite=new File("linkabilityNetwork.csv");
    static int depth=3;
    static final int ROOT=0;
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        MPI.Init(args);
        GraphDistributed ETN= new GraphDistributed();
        ETN.createBlacklist(blacklist);
        ETN.readFromFile(ETNExample, columnFromETN, columnToETN);
        MPI.COMM_WORLD.Barrier();
        LinkabilityNetworkDistributed l= new LinkabilityNetworkDistributed();
        l.buildLinkabilityNetworkDistributed(ETN, depth, fileToWrite, NFTTransfers, columnFromNFT, columnToNFT);
        MPI.Finalize();

        long end = System.currentTimeMillis();
        if (MPI.COMM_WORLD.Rank() == ROOT) {
            System.out.println("Run-time: " + (end - start) + "ms");
        }
    }

    public static byte[] serializeObject(Object object){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.flush();
            oos.close();
            baos.flush();
            baos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
    public static Object deserializeObject(byte[] bytes){
        Object object=null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            object = ois.readObject();
            ois.close();
            bis.close();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return object;
    }
}
