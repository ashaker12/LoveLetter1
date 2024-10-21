
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService threadpool;//so we dont need a new thread everytime (reuses)

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run(){
         try {
             server = new ServerSocket(9999);
             threadpool = Executors.newCachedThreadPool();

             while(!done){
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                threadpool.execute(handler);
             }

         } catch (IOException e) {
            quit();
         }   

    }
    
    public void sendMessage(String message){

        out.println(message); //this is a function that prints a message now we need a function to broadcast it to all 
    }

    public void broadcast(String message){
        for (ConnectionHandler ch : connections){
            if (ch!= null){
                sendMessage(message);
            }
        }
    }
    


    public void quit(){//function to shut down the server
        try{
        done = true;
        if (!server.isClosed()){
            server.close();
        }
        for (ConnectionHandler ch : connections){
            ch.quit();
        }
        }catch(IOException e){

        }
    }

    class ConnectionHandler implements Runnable{ //handles client connections 
        
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        public ConnectionHandler(Socket client){
            this.client = client;
        }
        
        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Enter your Name:");

                name = in.readLine();
                //here code should read the name still didnt check if theres one like it using if

                System.out.println("Welcome " + name + "!"); //in order to send a message that he connected to server we need an ArrayList
                broadcast(name + "joined the room");
                
                String message;
                while ((message = in.readLine()) !=null){
                   if(message.startsWith("/quit")) {
                    broadcast(name + " left the Chat!");
                    quit();
                   }else{
                    broadcast(name + ": " + message);
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
            if (!client.isClosed()){
                client.close();
                }
            }catch(IOException e){

            }
    }

 }
    public static void main(String[] args){
        Server server = new Server(); 
        server.run();
    }
}