import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class accepts jobs from clients, delegates them to slaves,
 * and informs clients once slaves have completed their work.
 *
 */
public class Master  implements Runnable {
    private final Map<Socket,Integer> a_slaves = new ConcurrentHashMap<Socket,Integer>();
    private final Map<Socket,Integer> b_slaves = new ConcurrentHashMap<Socket,Integer>();
    private final Map<Job,Socket> clientJobs = new ConcurrentHashMap<Job,Socket>();
    private final int PORT;
    private final AtomicInteger numClients = new AtomicInteger(0);
    private final ServerSocket serverSocket;
    public Master(int port) throws IOException {
        PORT = port;
        serverSocket = new ServerSocket(PORT);
    }

    /**
     * Listens for news of job completion from slaves and sends it to clients.
     */
    private class SlaveHandler implements Runnable{
        private final Socket slave;
        private final BufferedReader in;
        private final PrintWriter out;
        public SlaveHandler(Socket slave) throws IOException {
            this.slave = slave;
            out = new PrintWriter(slave.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(slave.getInputStream()));
            slave.setKeepAlive(true);
        }
        @Override
        public void run() {
            while(true){
                String line = null;
                while(line == null) {
                    try{
                        line = in.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(line.startsWith("FIN")){
                    char jobType = line.charAt(3);
                    String id = line.substring(4);
                    Job job = new Job(Integer.parseInt(id),jobType);
                    Socket client = clientJobs.get(job);
                    try{
                        PrintWriter clientWriter = new PrintWriter(client.getOutputStream());
                        clientWriter.println(line);
                        clientWriter.flush();
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                    if(a_slaves.containsKey(slave)){
                        a_slaves.put(slave,a_slaves.get(slave) - 1);
                    }
                    else{
                        b_slaves.put(slave,b_slaves.get(slave) - 1);
                    }
                }
            }
        }
    }

    /**
     * Listens for jobs from clients and assigns them to slaves.
     */
    private class ClientHandler implements Runnable{
        private final Socket client;
        private final BufferedReader in;
        private final PrintWriter out;
        public ClientHandler(Socket client) throws IOException {
            this.client = client;
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }
        @Override
        public void run() {
            while(true){
                String line;
                try{
                    line = in.readLine();
                    while(a_slaves.isEmpty() && b_slaves.isEmpty()){
                        Thread.sleep(50);
                    }
                    if(line.startsWith("A")){
                        clientJobs.put(new Job(Integer.parseInt(line.substring(1)),'A'),client);
                        int minA = Integer.MAX_VALUE;
                        Socket minASocket = null;
                        for(Socket slave: a_slaves.keySet()){
                            int currentJobs = a_slaves.get(slave);
                            if(currentJobs < minA){
                                minA = currentJobs;
                                minASocket = slave;
                            }
                        }
                        int minB = Integer.MAX_VALUE;
                        Socket minBSocket = null;
                        for(Socket slave: b_slaves.keySet()){
                            int currentJobs = b_slaves.get(slave);
                            if(currentJobs < minB){
                                minB = currentJobs;
                                minBSocket = slave;
                            }
                        }

                        if(minA >= 5 * minB || minASocket == null){
                            PrintWriter slaveOut = new PrintWriter(minBSocket.getOutputStream(),true);
                            slaveOut.println(line);
                            slaveOut.flush();
                            b_slaves.put(minBSocket,b_slaves.get(minBSocket) + 1);
                        }
                        else{
                            PrintWriter slaveOut = new PrintWriter(minASocket.getOutputStream(),true);
                            slaveOut.println(line);
                            slaveOut.flush();
                            a_slaves.put(minASocket,a_slaves.get(minASocket) + 1);
                        }
                    }
                    else if(line.startsWith("B")){
                        clientJobs.put(new Job(Integer.parseInt(line.substring(1)),'B'),client);
                        int minA = Integer.MAX_VALUE;
                        Socket minASocket = null;
                        for(Socket slave: a_slaves.keySet()){
                            int currentJobs = a_slaves.get(slave);
                            if(currentJobs < minA){
                                minA = currentJobs;
                                minASocket = slave;
                            }
                        }
                        int minB = Integer.MAX_VALUE;
                        Socket minBSocket = null;
                        for(Socket slave: b_slaves.keySet()){
                            int currentJobs = b_slaves.get(slave);
                            if(currentJobs < minB){
                                minB = currentJobs;
                                minBSocket = slave;
                            }
                        }
                        if(minB >= 5 * minA || minBSocket == null){
                            try {
                                PrintWriter slaveOut = new PrintWriter(minASocket.getOutputStream(),true);
                                slaveOut.println(line);
                                slaveOut.flush();
                                a_slaves.put(minASocket,a_slaves.get(minASocket) + 1);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        else{
                            try
                            {
                                PrintWriter slaveOut = new PrintWriter(minBSocket.getOutputStream(),true);
                                slaveOut.println(line);
                                slaveOut.flush();
                                b_slaves.put(minBSocket,b_slaves.get(minBSocket) + 1);
                            }catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Identifies whether the incoming connection is a slave or client and passes it to the appropriate handler
     */
    @Override
    public void run(){
        try{
            System.out.println("Server listening on port " + PORT);
            while (true) {
                Socket newSocket = serverSocket.accept();
                newSocket.setKeepAlive(true);
                BufferedReader in = new BufferedReader(new InputStreamReader(newSocket.getInputStream()));
                try {
                    String inputLine = in.readLine();
                    if(inputLine != null && inputLine.startsWith("SA")){
                        a_slaves.put(newSocket,0);
                        System.out.println("Master Accepting A slave #" + a_slaves.size());
                        SlaveHandler handle = new SlaveHandler(newSocket);
                        Thread newThread = new Thread(handle);
                        newThread.setDaemon(false);
                        newThread.start();
                    }
                    if(inputLine != null && inputLine.startsWith("SB")){
                        b_slaves.put(newSocket,0);
                        System.out.println("Master Accepting B slave #" + b_slaves.size());
                        SlaveHandler handle = new SlaveHandler(newSocket);
                        Thread newThread = new Thread(handle);
                        newThread.setDaemon(false);
                        newThread.start();
                    }
                    if(inputLine != null && inputLine.startsWith("C")){
                        numClients.incrementAndGet();
                        ClientHandler handle = new ClientHandler(newSocket);
                        System.out.println("Master Accepting Client #" + numClients.get());
                        Thread newThread = new Thread(handle);
                        newThread.setDaemon(false);
                        newThread.start();
                    }
                } catch (IOException e) {
                    System.err.println("Error handling client: " + e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
