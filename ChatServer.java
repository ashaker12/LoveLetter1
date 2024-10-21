import java.io.*;  
import java.net.*;
import java.util.ArrayList; 
import java.util.List;  

public class ChatServer {
    // List to store all connected clients (ClientHandler objects)
    private static List<ClientHandler> clients = new ArrayList<>();
    private static List<String> clientNames = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        //a ServerSocket that listens on port 12345
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server started...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            
            //a new ClientHandler for the connected client
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
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }
    
    /*public static void broadcast(String message, ClientHandler sender) { // Updated
        System.out.println("Broadcasting message: " + message); // New log for debugging
        for (ClientHandler client : clients) {
            client.sendMessage(message);  // Now sends message to all clients, including the sender
        }
    }
*/
    // Nested class to handle each connected client
    static class ClientHandler implements Runnable {
        private Socket socket;  // Client socket
        private PrintWriter out;  // Output stream to send messages to the client
        private BufferedReader in;  // Input stream to receive messages from the client
        private String name;  

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {

                // Initialize input and output streams
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                while(true){
                
                out.println("Enter your name:");
                name = in.readLine();

                synchronized (clientNames) {
                        if (name == null || name.isEmpty()) {
                            out.println("Invalid name. Please choose another.");
                        } if (clientNames.contains(name)) {
                            out.println("Name already in use. Please choose another.");
                        } else {
                            clientNames.add(name); // Add the name to the list of client names
                            break;
                        }
                    }
                }

                while (true) {
                    out.println("Enter your name:");
                    name = in.readLine();
                    System.out.println("Client attempting to use name: " + name);  // New log for debugging

                    synchronized (clientNames) {  // This ensures no duplicate names across threads
                        if (name == null || name.isEmpty()) {
                            out.println("Invalid name. Please choose another.");
                            System.out.println("Client provided an invalid name.");  // New log for debugging
                        } else if (clientNames.contains(name)) {
                            out.println("Name already in use. Please choose another.");
                            System.out.println("Client attempted to use a duplicate name: " + name);  // New log for debugging
                        } else {
                            clientNames.add(name); // Add the name to the list of client names
                            System.out.println("Client name accepted: " + name);  // New log for debugging
                            break;  // Exit the loop if the name is valid and unique
                        }
                    }
                }


                out.println("Welcome, " + name + "!");
            
                // Notify all clients that a new client has joined
                ChatServer.broadcast(name + " joined the room", this);

                // Continuously read messages from the client and broadcast them to others
                String message;
                while ((message = in.readLine()) != null) {
                    if ("bye".equalsIgnoreCase(message)) {
                        break;
                    }
                    ChatServer.broadcast(name + ": " + message, this);
                }

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