
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
//receives all the messages from the Sverver

private Socket client;
private BufferedReader in;
private PrintWriter out;
private boolean done;

    @Override
    public void run() {
       try {
           client = new Socket("127.0.0.1", 9999); //connecting to local IP
           out = new PrintWriter(client.getOutputStream(), true);
           in = new BufferedReader(new InputStreamReader(client.getInputStream()));

           InputHandler inHandler =  new InputHandler();
           Thread t = new Thread(inHandler);
           t.start();

           String inMessage;
           while((inMessage = in.readLine()) != null){
           System.out.println(inMessage);
           }
       } catch (IOException e) {
        quit();
       }
    }

    public void quit(){
        done = true;
        try {
            in.close();
            out.close();
            if (!client.isClosed()){
                client.close();
                }
        } catch (Exception e) {

        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
           try {
               BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
               while(!done){
                String message = inReader.readLine(); //waiting for new message input from user 
                if(message.equals("bye")){
                    out.println(message);
                    inReader.close();
                    quit();
                }else{
                out.println(message);
            }
               }
           } catch (IOException e) {

           }
        }

    }
    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

  }