package newSlaves;

import java.io.IOException;

public class starSlaveB {
    public static void main(String[] args) {
        try {
            AbstractSlave slaveB = new SlaveB(3000);
            (new Thread(slaveB)).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
