package newSlaves;

public class startSlave {
    public static void main(String[] args) {
        try {
            SlaveA slaveA = new SlaveA(3000);
            SlaveB slaveB = new SlaveB(3000);
            (new Thread(slaveB)).start();
            (new Thread(slaveA)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
