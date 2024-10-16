import java.io.*; 
import java.net.*; 

public class ChatClient {
    public static void main(String[] args) {
        // Try to establish a connection to the server
        try (Socket socket = new Socket("localhost", 12345);  // Connect to the server running on localhost at port 12345
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // Input stream to receive messages from the server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);  // Output stream to send messages to the server
             BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {  // Input from the console (user input)

            // Start a new thread to listen for incoming messages from the server
            new Thread(() -> {
                try {
                    String message;
                    // Continuously listen for messages from the server and print them to the console
                    while ((message = input.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Send messages from the console (user input) to the server
            String userInput;
            while ((userInput = console.readLine()) != null) {
                out.println(userInput);  // Send the input to the server
                if ("bye".equalsIgnoreCase(userInput)) {  // If the user types "bye", disconnect from the server
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
