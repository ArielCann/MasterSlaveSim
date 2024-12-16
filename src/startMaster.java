import java.io.IOException;
public class startMaster {
    public static void main(String[] args) throws IOException {
        Master master = new Master(3000);
        (new Thread(master)).start();
    }
}
