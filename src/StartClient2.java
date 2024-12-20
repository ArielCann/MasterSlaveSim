import java.io.IOException;

public class StartClient2 {
    public static void main(String[] args) throws IOException {
        Client client = new Client(3000, 1);
        new Thread(client).start();
    }
}
