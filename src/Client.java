import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

/**
 * Dummy Client for testing purposes; has one thread for sending jobs and another listening for completion.
 */
public class Client implements Runnable {
    private final int PORT;
    private PrintWriter out;
    private BufferedReader in;
    private Socket master;
    private int numJobs = 10;
    public Client(int port) throws IOException {
        PORT = port;
        master = new Socket("localhost",PORT);
        master.setKeepAlive(true);
        in = new BufferedReader(new InputStreamReader(master.getInputStream()));
        out = new PrintWriter(master.getOutputStream(), true);
    }
    private Random rand = new Random();

    /**
     * Listens for news of job completion from the master.
     */
    private class Listen implements Runnable {
        @Override
        public void run() {
            System.out.println("Client listening for job completion.");
            while(true) {
                try {
                    String line = in.readLine();
                    while(line == null) {
                        line = in.readLine();
                    }
                    System.out.println("Client Acknowledging job completion: " + line.substring(3));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Sends jobs to the master.
     */
    @Override
    public void run() {
        System.out.println("Client Running jobs ");
        //ensure clinet is registered with the master before sending other jobs
        out.println("C"); 
        out.flush();
        Thread listener = new Thread(new Listen());
        listener.start();
        for (int i = 0; i< numJobs; i++){
            String type = (rand.nextBoolean()) ? "A" : "B";
            out.println(type + i);
        }
    }
}
