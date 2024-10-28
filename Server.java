
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private PrintWriter out;
    private ExecutorService threadpool; //so we dont need a new thread everytime (reuses)
    private ArrayList<String> clientNames = new ArrayList<>(); //List with all names already in use 
    private final int MIN_PLAYERS = 2;
    private final int MAX_PLAYERS = 4;
    private boolean chatStarted = false;  // to indicate if the chat has started

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run(){
         try {
             server = new ServerSocket(9999);
             threadpool = Executors.newCachedThreadPool(); //creates as many threads as possible 

             while(!done){
                Socket client = server.accept();
                if (connections.size() < MAX_PLAYERS && !chatStarted) {
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                threadpool.execute(handler);
                } else {
                    PrintWriter ChatFull = new PrintWriter(client.getOutputStream(), true);
                    if (chatStarted){
                        ChatFull.println("Chat has already Started!");
                    }
                    ChatFull.println("Chat is full, please try again later.");
                    client.close();
                }
             }

         } catch (IOException e) {
            quit();
         }   
 
    }
    
    public void sendMessage(String message, ConnectionHandler receiver) {
            receiver.out.println(message);
        }

    public void broadcast(String message, ConnectionHandler sender){
        for (ConnectionHandler ch : connections){
            if (ch != null && ch!= sender) {
                ch.out.println(message);
            }
        }
    }

    public void quit(){ //function to shut down the server
        try{
        done = true;
        if (!server.isClosed()){
            server.close();
        }
        for (ConnectionHandler ch : connections){
            ch.quit();
        }
        }catch(Exception e){

        }
    }

    class ConnectionHandler implements Runnable{ //handles client connections 
        
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;
        private boolean chatStarted = false;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));


                while (true) {
                    System.out.println("Server Started...");
                   out.println("Enter your Name:");

                    name = in.readLine();
                 //here code should read the name still didnt check if theres one like it using if

                 if (name == null || name.isEmpty()) {
                            out.println("Invalid name. Please choose another.");
                        } if (clientNames.contains(name)) {
                            out.println("Name already in use. Please choose another.");
                             } else {
                                clientNames.add(name);
                                break; // Add the name to the list of client names
                                }
                }

               out.println("Welcome " + name + "!"); //in order to send a message that he connected to server we need an ArrayList
                broadcast(name + " joined the room", this);
                
                 if (connections.size() < MIN_PLAYERS && !chatStarted) {
                        out.println("Waiting for more players to join...");
                    }

                    while (connections.size() < MIN_PLAYERS) {
                        // Wait for enough players to join
                        synchronized (connections) {
                            connections.wait(); // Wait until more players connect
                        }
                    }

                    if (connections.size() <= MAX_PLAYERS) {
                        synchronized (connections) {
                            out.println("You can type 'Start' to begin the chat.");
                            connections.notifyAll();  // Notify all waiting clients to start chatting
                        }
                    String startMessage = in.readLine();
                    if (startMessage != null && startMessage.equalsIgnoreCase("Start")) {
                        synchronized(connections){
                        chatStarted = true;
                        broadcast("The chat has started!", this);
                        connections.notifyAll();
                        }
                    }else{
                        out.println("You cannot start the chat yet. Please wait for more players or type 'Start'.");
                    }
                }

                String message;
                while ((message = in.readLine()) !=null){
                   if(message.startsWith("bye")) {
                    broadcast(name + " left the Chat!", this);
                    quit();
                   } else if (message.startsWith("/dm")) {
                    handleDirectMessage(message);  // Handle direct message
                    }else{
                    broadcast(name + ": " + message, this);
                   }
                }
            } catch (Exception e) {
                    quit();
            }
        }
        public void quit(){
            try{
                in.close();
                out.close();
                synchronized (connections) {
                    clientNames.remove(name);
                    connections.remove(this);
                } //removes client from the Arraylist when he quits, do i have to write name.ch or smth or is that enough
            if (!client.isClosed()){
                client.close();
                } connections.notifyAll(); // Notify waiting threads if players disconnect
            }catch(IOException e){

            }
    }

    public ConnectionHandler connectionByName( String name){
            for (ConnectionHandler ch : connections) {
        if (ch != null && ch.name.equals(name)) {
            return ch;  // Return the matching ConnectionHandler
        }
    }
    return null;
    }

    public void handleDirectMessage(String message) {
            String[] splitMessage = message.split(" ", 3);
             if (splitMessage.length < 3) {
        out.println("Invalid DM format. /dm name message");
        return;
    }
    String recipientName = splitMessage[1];
    String dmMessage = splitMessage[2];
    boolean recipientFound = false;

    for (ConnectionHandler ch : connections) {
        if(ch != null && ch.name.equals(recipientName)){
             ch.out.println("[DM from " + name + "]: " + dmMessage);
             recipientFound = true;
            break;
        }
        }
    if (!recipientFound) {
        out.println("User " + recipientName + " not found.");
    }else{
        ConnectionHandler dmName = connectionByName(recipientName);
        sendMessage(message, dmName);
    }//converted the String name to CH so server can search for it and send a message using sendMessage function

 }
    }

public static void main(String[] args){
        Server server = new Server(); 
        server.run();
    }

}