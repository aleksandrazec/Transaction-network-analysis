package Distributed;

import mpi.MPI;

import java.io.File;

public class Main {
    static File blacklist=new File("./linkabilityNetworksData/blacklist");
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        GraphDistributed ETN= new GraphDistributed(blacklist);
        MPI.Finalize();
    }
}
