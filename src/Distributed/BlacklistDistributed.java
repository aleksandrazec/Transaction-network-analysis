package Distributed;

import java.io.Serializable;
import java.util.HashSet;

public class BlacklistDistributed implements Serializable {
    HashSet<String> irrelevantAddresses;
    public BlacklistDistributed() {
        irrelevantAddresses = new HashSet<>();
    }
    public void addBlacklistAddress(String address) {
        irrelevantAddresses.add(address);
    }
    public void printAddresses() {
        System.out.println(irrelevantAddresses);
    }
}
