import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

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
    private class Listen implements Runnable {
        @Override
        public void run() {
            while(true) {
                System.out.println("running");
                try {
                    String line = null;
                    while(line == null) {
                        line = in.readLine();
                    }
                    System.out.println("Finished job: " + line.substring(3));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    @Override
    public void run() {
        try{
            out.println("C");
            Thread listener = new Thread(new Listen());
            listener.setDaemon(false);
            listener.start();
            int currentJob = lowestJob;
            while(currentJob <= highestJob){
                Thread.sleep(100);
                String type = (rand.nextBoolean()) ? "A" : "B";
                out.println(type + currentJob);
                currentJob++;
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
