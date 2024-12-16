import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Dummy Client for testing purposes; has one thread for sending jobs and another listening for completion.
 */
public class DummyClient implements Runnable {
    private final int PORT;
    private PrintWriter out;
    private BufferedReader in;
    private int lowestJob;
    private int highestJob;
    private Socket master;
    public DummyClient(int port,int lowestJob,int highestJob) throws IOException {
        PORT = port;
        this.lowestJob = lowestJob;
        this.highestJob = highestJob;
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
            System.out.println("Client " + lowestJob + ": " + highestJob + " listening for job completion.");
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
        System.out.println("Client Running jobs " + lowestJob + ":" + highestJob);
        out.println("C");
        Thread listener = new Thread(new Listen());
        listener.start();
        int currentJob = lowestJob;
        while(currentJob <= highestJob){
            String type = (rand.nextBoolean()) ? "A" : "B";
            out.println(type + currentJob);currentJob++;
        }
    }
}
