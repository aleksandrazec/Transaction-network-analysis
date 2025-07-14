import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public static int depth=0;
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
        depth = 0;
        try {
            depth = s.nextInt();
        } catch (Exception e) {
            System.out.println("Unexpected error occurred.");
        }
        switch (currentMode){
            case Sequential -> {
                long start = System.currentTimeMillis();
                GraphSequential ETN = new GraphSequential();
                ETN.buildGraphSequential(blacklist,ETNExample, columnFromETN, columnToETN);
                LinkabilityNetworkSequential l = new LinkabilityNetworkSequential();
                l.buildLinkabilityNetworkSequential(ETN, depth, fileToWrite, NFTTransfers, columnFromNFT, columnToNFT);
                long end = System.currentTimeMillis();
                System.out.println("Run-time: " + (end - start) + "ms");
            }
            case Parallel -> {
                long start = System.currentTimeMillis();
                GraphParallel ETN = new GraphParallel();
                ETN.buildGraphParallel(blacklist, ETNExample, columnFromETN, columnToETN);
                LinkabilityNetworkParallel l=new LinkabilityNetworkParallel();
                l.buildLinkabilityNetworkParallel(ETN, depth,  fileToWrite, NFTTransfers, columnFromNFT, columnToNFT);
                long end = System.currentTimeMillis();
                System.out.println("Run-time: " + (end - start) + "ms");
            }
            case Distributed -> {

                ProcessBuilder pbComp = new ProcessBuilder(
                        "javac",
                        "-cp", "..\\mpj\\lib\\mpj.jar",
                        "-d", "out/production/Transaction network analasys 2.0",
                        "src/GraphDistributed.java",
                        "src/LinkabilityNetworkDistributed.java",
                        "src/MPIMain.java"
                );

                pbComp.redirectErrorStream(true);
                try {
                    Process process = pbComp.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ProcessBuilder pb = new ProcessBuilder(
                        "bash", "-c",
                        "mpjrun.sh -np 2 -cp 'out/production/Transaction network analasys 2.0' MPIMain "+depth
                );
                pb.redirectErrorStream(true);

                try {
                    Process process = pb.start();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

}
