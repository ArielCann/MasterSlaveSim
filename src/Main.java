import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Master master = new Master(3000);
        DummySlave aSlave = new DummySlave(3000,'A');
        DummySlave bSlave = new DummySlave(3000,'B');
        DummySlave aSlave2 = new DummySlave(3000,'A');
        DummyClient client = new DummyClient(3000,1,10);
        DummyClient client2 = new DummyClient(3000,11,20);
        new Thread(aSlave).start();
        new Thread(aSlave2).start();
        new Thread(bSlave).start();
        new Thread(master).start();
        new Thread(client).start();
        new Thread(client2).start();
    }
}
