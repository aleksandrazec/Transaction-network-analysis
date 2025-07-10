package Distributed;

import mpi.MPI;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class GraphDistributed {
    static final int ROOT = 0;
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        System.out.println("Hi from <"+rank+"> | size: " + size);
        MPI.Finalize();
    }
    public void createBlacklist(File blacklist){
        String[] blacklistFileAddresses;
        String blacklistFileAddress;
        if (MPI.COMM_WORLD.Rank()==ROOT){
            File[] blacklistFiles = blacklist.listFiles();
            blacklistFileAddresses = new String[blacklistFiles.length];
            for (int i = 0; i < blacklistFiles.length; i++) {
                blacklistFileAddresses[i] = blacklistFiles[i].getPath();
            }
        }
        //nemam mozak trenutno ali u zavisnosti od broja masina napravi appropriately sized
        //recieve buffers za scatter i onda u loop radi svoj deo blekliste
    }
}
