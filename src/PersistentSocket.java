import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PersistentSocket {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isRunning;

    public PersistentSocket(String host, int port) throws IOException {
        // Initialize the socket and streams
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        isRunning = true;

        // Start a listener thread for incoming messages
        new Thread(() -> listenForMessages()).start();
    }

    // Send a message to the server
    public void sendMessage(String msg) {
        if (isRunning) {
            out.println(msg);
        } else {
            System.out.println("Socket is closed. Cannot send message.");
        }
    }

    // Listen for incoming messages from the server
    private void listenForMessages() {
        try {
            String incomingMessage;
            while (isRunning && (incomingMessage = in.readLine()) != null) {
                System.out.println("Server: " + incomingMessage);
            }
        } catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        }
    }

    // Close the socket and release resources
    public void close() {
        try {
            isRunning = false;
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}