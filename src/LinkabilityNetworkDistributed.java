import mpi.MPI;

import java.io.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;


public class LinkabilityNetworkDistributed extends GraphDistributed{
    int[] weights;
    HashSet<String> relevantAddresses=null;
    BufferedWriter bw;
    StringWriter sw;
    @SuppressWarnings("unchecked")
    public void buildLinkabilityNetworkDistributed(GraphDistributed ETN, int depth, File f, File NFT, int from, int to) {
        byte[] buffer= null;
        int[] size=new int[1];
        if(MPI.COMM_WORLD.Rank()==MPIMain.ROOT){
            identifyRelevantAddresses(NFT, from, to);
            buffer= MPIMain.serializeObject(relevantAddresses);
            size[0]=buffer.length;
        }
        MPI.COMM_WORLD.Bcast(size, 0, 1, MPI.INT, 0);
        if(MPI.COMM_WORLD.Rank()!=MPIMain.ROOT){
            buffer=new byte[size[0]];
        }
        MPI.COMM_WORLD.Bcast(buffer, 0, size[0], MPI.BYTE, 0);
        if (MPI.COMM_WORLD.Rank()!=MPIMain.ROOT){
            relevantAddresses= (HashSet<String>) MPIMain.deserializeObject(buffer);
        }
        weights=new int[depth+1];
        int[] finalWeights=new int[depth+1];
        for (int i = 0; i < depth+1; i++) {
            weights[i]=0;
        }
        sw = new StringWriter();

        int total=ETN.adjacencyList.size();
        int fromAddress=MPI.COMM_WORLD.Rank()*total/MPI.COMM_WORLD.Size();
        int toAddress=(MPI.COMM_WORLD.Rank()+1)*total/MPI.COMM_WORLD.Size();
        if (MPI.COMM_WORLD.Rank()==MPI.COMM_WORLD.Size()-1){
            toAddress=total;
        }

        breadthFirstSearchLoop(ETN, depth, fromAddress, toAddress);
        MPI.COMM_WORLD.Barrier();

        String partToBeWritten = sw.toString();
        byte[] partTobeWrittenBytes = MPIMain.serializeObject(partToBeWritten);
        byte[][] tobeWrittenBytes = new byte[MPI.COMM_WORLD.Size()][];
        String[] toBeWritten = new String[MPI.COMM_WORLD.Size()];
        if(MPI.COMM_WORLD.Size()!=1){
            if (MPI.COMM_WORLD.Rank() == MPIMain.ROOT) {
                for (int i = 0; i < MPI.COMM_WORLD.Size(); i++) {
                    if (i == MPIMain.ROOT) {
                        tobeWrittenBytes[i] = partTobeWrittenBytes;
                    } else {
                        int[] recvSize = new int[1];
                        MPI.COMM_WORLD.Recv(recvSize, 0, 1, MPI.INT, i, 0);
                        byte[] recvBuffer = new byte[recvSize[0]];
                        MPI.COMM_WORLD.Recv(recvBuffer, 0, recvSize[0], MPI.BYTE, i, 1);
                        tobeWrittenBytes[i] = recvBuffer;
                    }
                }
            } else {
                int[] sendSize = new int[1];
                sendSize[0]=partTobeWrittenBytes.length;
                MPI.COMM_WORLD.Send(sendSize, 0, 1, MPI.INT, MPIMain.ROOT, 0);
                MPI.COMM_WORLD.Send(partTobeWrittenBytes, 0, partTobeWrittenBytes.length, MPI.BYTE, MPIMain.ROOT, 1);
            }
        }else {
            tobeWrittenBytes[0] = partTobeWrittenBytes;
        }

        MPI.COMM_WORLD.Reduce(weights, 0, finalWeights, 0, depth+1, MPI.INT, MPI.SUM, MPIMain.ROOT);
        if (MPI.COMM_WORLD.Rank()==MPIMain.ROOT) {
            try {
                bw = new BufferedWriter(new FileWriter(f));
                bw.write("addressFrom,addressTo,weight\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (int i = 0; i < MPI.COMM_WORLD.Size(); i++) {
                toBeWritten[i] = (String) MPIMain.deserializeObject(tobeWrittenBytes[i]);
                try {
                    bw.write(toBeWritten[i]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                bw.flush();
                bw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (int i = 1; i < finalWeights.length; i++) {
                System.out.println("Number of links of weight " + i + " is: " + finalWeights[i]);
            }
        }
    }

    public void breadthFirstSearchLoop(GraphDistributed ETN, int depth, int from, int to){
        for (int i = from; i < to; i++) {
            if(relevantAddresses.contains(ETN.adjacencyList.get(i).getKey())){
                breadthFirstSearch(ETN, depth,ETN.adjacencyList.get(i).getKey());
            }
        }
    }

    public void breadthFirstSearch(GraphDistributed ETN, int depth, String rootAddress){
        Queue<SimpleEntry<String, Integer>> q = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();
        visited.add(rootAddress);
        int currentDepth=0;
        SimpleEntry<String, Integer> rootPair= new SimpleEntry<>(rootAddress,currentDepth);
        q.add(rootPair);
        while (!q.isEmpty() && currentDepth<=depth){
            SimpleEntry<String, Integer> currentPair = q.poll();
            String parent=currentPair.getKey();
            int parentID=ETN.returnHash(parent);
            currentDepth=currentPair.getValue();
            for (HashMap.Entry<Integer, SimpleEntry<String, Integer>> entry : ETN.adjacencyList.get(parentID).getValue().entrySet()) {
                String child=entry.getValue().getKey();
                if(currentDepth>0){
                    if(relevantAddresses.contains(child)){
                        try {
                            sw.write(rootAddress + "," + child + "," + currentDepth + "\n");
                        }catch (Exception e){
                            throw new RuntimeException(e);
                        }
                        weights[currentDepth]++;
                    }
                }
                if (!visited.contains(child)){
                    if (currentDepth+1<=depth){
                        SimpleEntry<String, Integer> newPair = new SimpleEntry<>(child, currentDepth + 1);
                        q.add(newPair);
                        visited.add(child);
                    }
                }
            }
        }
    }


    public void identifyRelevantAddresses(File f, int from, int to){
        String line;
        relevantAddresses=new HashSet<>();
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
}
