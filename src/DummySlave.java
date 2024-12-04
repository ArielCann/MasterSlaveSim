import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Simulates a slave for testing purposes.
 * Has two threads, one for accepting jobs and the other for 'doing' them.
 */
public class DummySlave implements Runnable {
    private final int PORT;
    private final Queue<Job> jobs = new ConcurrentLinkedDeque<>();
    private final char slaveType;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    public DummySlave(int port,boolean isTypeA) throws IOException {
        PORT = port;
        this.slaveType = isTypeA ? 'A':'B';
        socket = new Socket("localhost",PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println("S" + slaveType);
        socket.setKeepAlive(true);
    }
    private class Listen implements Runnable {
        @Override
        public void run() {
            while(true) {
                String line = null;
                while(line == null) {
                    try {
                        line = in.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                char jobType = line.charAt(0);
                String id = line.substring(1);
                jobs.add(new Job(Integer.parseInt(id), jobType));
                System.out.println(slaveType + "accepting job #" + line);
            }
        }
    }
    private class DoJob implements Runnable {
        @Override
        public void run() {
            while(true) {
                if(!jobs.isEmpty()) {
                    Job job = jobs.poll();
                    if(job.type == slaveType){
                        try {
                            Thread.sleep(2000);
                            out.println("FIN"+ job.type + job.id );
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    else{
                        try {
                            Thread.sleep(10000);
                            out.println("FIN" + job.type + job.id);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
    @Override
    public void run() {
        Thread listenThread = new Thread(new Listen());
        listenThread.setDaemon(false);
        listenThread.start();
        Thread jobThread = new Thread(new DoJob());
        jobThread.setDaemon(false);
        jobThread.start();
    }
}
