package newSlaves;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class AbstractSlave implements Runnable {
    protected final int PORT;
    protected Queue<SlaveJob> jobs;
    protected char slaveType;
    protected BufferedReader in;
    protected PrintWriter out;
    protected Socket socket;

    public AbstractSlave(int port, char type) throws IOException {
        this.PORT = port;
        this.slaveType = type;
        this.jobs = new ConcurrentLinkedDeque<>(); // Initialize the queue for each slave

        // Each slave creates its own socket connection
        this.socket = new Socket("localhost", PORT);
    }

    /**
     * 
     */
    protected void listenForJobs() {
        new Thread(() -> {
            try {
                while (true) {
                    String line = in.readLine();
                    if (line != null) {
                        char jobType = line.charAt(0);
                        String id = line.substring(1);
                        jobs.add(new SlaveJob(Integer.parseInt(id), jobType));
                        System.out.println(slaveType + " accepting job #" + line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    protected void processJobs() {
        new Thread(() -> {
            while (true) {
                if (!jobs.isEmpty()) {
                    SlaveJob job = jobs.poll();
                    try {
                        // Determine processing time based on job type
                        if (job.jobType.equals(slaveType)) {
                            Thread.sleep(2000); // Optimal time for same type
                        } else {
                            Thread.sleep(10000); // Non-optimal time for non-same type
                        }
                        out.println("FIN" + job.jobType + job.jobNumber); // Send completion message
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }).start();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("S" + slaveType); // Notify master of the slave type
            listenForJobs();
            processJobs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
