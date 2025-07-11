import Distributed.GraphDistributed;

import java.io.File;
import java.util.Scanner;

public class Main {
    static File ETNExample=new File("./linkabilityNetworksData/prog3ETNsample.csv");
    static File blacklist=new File("./linkabilityNetworksData/blacklist");
    static File NFTTransfers=new File("./linkabilityNetworksData/boredapeyachtclub.csv");
    static int columnFromETN=5;
    static int columnToETN=6;
    static int columnFromNFT=4;
    static int columnToNFT=5;
    static File fileToWrite=new File("./linkabilityNetwork.csv");
    enum Mode{
        Sequential,
        Parallel,
        Distributed,
    }
    public static void main(String[] args) {
        System.out.println("TRANSACTION NETWORK ANALYSIS");
        System.out.println("Specify which mode you would prefer by typing in the corresponding number:");
        System.out.println("1 for sequential mode");
        System.out.println("2 for parallel mode");
        System.out.println("3 for distributed mode");
        Scanner s = new Scanner(System.in);
        int input = 0;
        try {
            input = s.nextInt();
        } catch (Exception e) {
            System.out.println("Unexpected error occurred.");
        }
        Mode currentMode=null;
        switch (input) {
            case 1 -> currentMode = Mode.Sequential;
            case 2 -> currentMode = Mode.Parallel;
            case 3 -> currentMode = Mode.Distributed;
            default -> {
                System.out.println("Please enter a valid mode.");
                System.exit(0);
            }
        }
        System.out.println("Specify maximum depth od ETN traversal:");
        int depth = 0;
        try {
            depth = s.nextInt();
        } catch (Exception e) {
            System.out.println("Unexpected error occurred.");
        }
        switch (currentMode){
            case Sequential -> {
                long start = System.currentTimeMillis();
                GraphSequential ETN = new GraphSequential(blacklist,ETNExample, columnFromETN, columnToETN);
                LinkabilityNetworkSequential l = new LinkabilityNetworkSequential(ETN, depth, fileToWrite, NFTTransfers, columnFromNFT, columnToNFT);
                long end = System.currentTimeMillis();
                System.out.println("Run-time: " + (end - start) + "ms");
            }
            case Parallel -> {
                long start = System.currentTimeMillis();
                GraphParallel ETN = new GraphParallel(blacklist, ETNExample, columnFromETN, columnToETN);
                LinkabilityNetworkParallel l=new LinkabilityNetworkParallel(ETN, depth,  fileToWrite, NFTTransfers, columnFromNFT, columnToNFT);
                long end = System.currentTimeMillis();
                System.out.println("Run-time: " + (end - start) + "ms");
            }
            case Distributed -> {
                long start = System.currentTimeMillis();
//                GraphDistributed ETN=new GraphDistributed(args, blacklist);
                long end = System.currentTimeMillis();
                System.out.println("Run-time: " + (end - start) + "ms");
            }
        }

    }
}
