import java.io.*;  // For input/output
import java.net.*; // For networking classes (ServerSocket, Socket)
import java.util.ArrayList;  // For storing multiple clients
import java.util.List;  // List interface for managing clients

public class ChatServer {
    // List to store all connected clients (ClientHandler objects)
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Create a ServerSocket that listens on port 12345
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server started...");

        // Continuously accept new clients
        while (true) {
            // Accept incoming client connection and create a socket
            Socket clientSocket = serverSocket.accept();
            
            // Create a new ClientHandler for the connected client
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            
            // Add the new client to the list of clients
            clients.add(clientHandler);
            
            // Start a new thread for this client so multiple clients can connect simultaneously
            new Thread(clientHandler).start();
        }
    }

    // Broadcast a message to all connected clients except the sender
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            // Send the message to all clients except the one who sent it
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Nested class to handle each connected client
    static class ClientHandler implements Runnable {
        private Socket socket;  // Client socket
        private PrintWriter out;  // Output stream to send messages to the client
        private BufferedReader in;  // Input stream to receive messages from the client
        private String name;  // Name of the client

        // Constructor that initializes the client socket
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Initialize input and output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Ask the client for their name
                out.println("Enter your name:");
                name = in.readLine();
                
                // Notify all clients that a new client has joined
                ChatServer.broadcast(name + " joined the room", this);

                // Continuously read messages from the client and broadcast them to others
                String message;
                while ((message = in.readLine()) != null) {
                    // If the client types "bye", break the loop and disconnect
                    if ("bye".equalsIgnoreCase(message)) {
                        break;
                    }
                    // Broadcast the received message to all other clients
                    ChatServer.broadcast(name + ": " + message, this);
                }

                // Notify others when the client leaves the room
                ChatServer.broadcast(name + " left the room", this);
                socket.close();  // Close the connection after the client leaves
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Method to send a message to the client
        public void sendMessage(String message) {
            out.println(message);
        }
    }
}
