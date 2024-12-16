import java.io.IOException;

public class StartClient {
        public static void main(String[] args) throws IOException {
        Client client = new Client(3000);
        new Thread(client).start();
    }
}
