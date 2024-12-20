package newSlaves;

public class startSlaveA {
    public static void main(String[] args) {
        try {
            AbstractSlave slaveA = new SlaveA(3000);
            (new Thread(slaveA)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
