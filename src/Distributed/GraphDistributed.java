package Distributed;

import mpi.MPI;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class GraphDistributed {
    public GraphDistributed(File blacklist) {
        createBlacklist(blacklist);
    }

    public void createBlacklist(File blacklist) {
        byte[] buffer= null;
        BlacklistDistributed irrelevantAddresses = null;
        int[] size=new int[1];
        if(MPI.COMM_WORLD.Rank()==0){
            File[] blacklistFiles = blacklist.listFiles();
            irrelevantAddresses = new BlacklistDistributed();
            if(blacklistFiles!=null) {
                for (File file : blacklistFiles) {
                    String content;
                    try {
                        content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    content=content.substring(1,content.length()-1).replaceAll("\\s+","");
                    String[] addressArray = content.split(",");
                    for (String addressToRemove : addressArray ) {
                       irrelevantAddresses.addBlacklistAddress(addressToRemove);
                    }
                }
            }
            
            buffer= serializeObject(irrelevantAddresses);
            size[0]=buffer.length;
        }
        MPI.COMM_WORLD.Bcast(size, 0, 1, MPI.INT, 0);
        if(MPI.COMM_WORLD.Rank()!=0){
            buffer=new byte[size[0]];
        }
        MPI.COMM_WORLD.Bcast(buffer, 0, size[0], MPI.BYTE, 0);
        if (MPI.COMM_WORLD.Rank()!=0){
            irrelevantAddresses= (BlacklistDistributed) deserializeObject(buffer);
        }
        irrelevantAddresses.printAddresses();
    }
    
    public byte[] serializeObject(Object object){
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
    public Object deserializeObject(byte[] bytes){
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
