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
    private Game game = new Game();

    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

@Override
public void run() {
    try {
        server = new ServerSocket(9999);
        threadpool = Executors.newCachedThreadPool(); // Creates as many threads as needed

        while (!done) {
            Socket client = server.accept();

            if (connections.size() >= MAX_PLAYERS) {
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                out.println("Chat is full, please try again later.");
                client.close();
                continue;
            }

            //Accept the new connection and add it to the chat
            ConnectionHandler handler = new ConnectionHandler(client);
            connections.add(handler);
            threadpool.execute(handler);
        }

    } catch (IOException e) {
        quit();
    }
    
}
    private void endRound(Player roundWinner) {
        game.determineRoundWinner(roundWinner);
        broadcast(roundWinner.getName() + " has won this round and earned a Token of Affection!", null);

        // Check if the game is over based on tokens collected
        if (!game.isGameStarted()) {
            broadcast("Game Over! " + roundWinner.getName() + " is the overall winner!", null);
            quit(); // End the game
        } else {
            game.startGame(); 
            broadcast("A new round has started! It's " + game.getCurrentPlayer().getName() + "'s turn.", null);
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
        private Player player;

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
                        } else if (clientNames.contains(name)) {
                            out.println("Name already in use. Please choose another.");
                             } else {
                                clientNames.add(name);
                                break; // Add the name to the list of client names
                                }
                                player = new Player(name);
                                game.addPlayer(player);
                }

               out.println("Welcome " + name + "!"); 
                broadcast(name + " joined the room", this);
                
                if (chatStarted){
                    out.println("Chat already Started!");
                }else if (connections.size() < MIN_PLAYERS && !chatStarted) {
                        out.println("Waiting for more players to join...");
                    }

                    while (connections.size() < MIN_PLAYERS) {
                        
                        synchronized (connections) {
                            connections.wait(); 
                        }
                    }

                    synchronized (connections) {
                        if (!Server.this.chatStarted && connections.size() >= MIN_PLAYERS) {
                            broadcast("You can type 'Start' to begin the chat.", this);
                            out.println("You can type 'Start' to begin the chat.");
                        } 
                    }

                        String startMessage = in.readLine();
                            if (startMessage != null && startMessage.equalsIgnoreCase("Start")) {
                            synchronized (connections) {
                                startGame();
                                Server.this.chatStarted = true;
                            }
                            
                            if (!Server.this.chatStarted) {  //to ensure it's not started by another user
                            broadcast("The chat has started!", this);
                            connections.notifyAll();
                            }
                        
                        }

                String message;
                while ((message = in.readLine()) !=null){
                   if(message.startsWith("bye")) {
                    broadcast(name + " left the Chat!", this);
                    quit();
                   } else if (message.startsWith("/dm")) {
                    handleDirectMessage(message);  
                    } else if (message.startsWith("/draw")) {
                        handleDrawCommand();
                        //out.println("you are drawing a card!");
                    } else if (message.startsWith("/play")){
                        handlePlayCommand(message);
                    }else {
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
                    connections.notifyAll(); 
                } 
            if (!client.isClosed()){
                client.close();
                } 
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
    String recipientNameString = splitMessage[1];
    String dmMessage = splitMessage[2];
    boolean recipientFound = false;
    ConnectionHandler recipientHandler = connectionByName(recipientNameString);

    /*for (ConnectionHandler ch : connections) {
        if(ch != null && ch.name.equals(recipientName)){
             ch.out.println("[DM from " + name + "]: " + dmMessage);
             recipientFound = true;
            break;
        }
        }*/
    if (recipientHandler!=null){
        recipientHandler.out.println("[DM from " + name + "]: " + dmMessage);
    }else if (!recipientFound) {
        out.println("User " + recipientNameString + " not found.");
    }else{
        out.println("Error sending message. Try again");
    } //converted the String name to CH so server can search for it and send a message using sendMessage function

 }
    private void handleDrawCommand() {
    if (game.getCurrentPlayer() != player) {
        out.println("It's not your turn! Please wait for your turn to draw.");
        return;
    }

    player.drawCard(game.getDeck());
    out.println("You drew a: " + player.showHand());
    broadcast(player.getName() + " drew a card.", this);
    broadcast("It's now " + game.getCurrentPlayer().getName() + "'s turn.", null);
}

    private void handlePlayCommand(String message) {
    if (game.getCurrentPlayer() != player) {
        out.println("It's not your turn! Please wait for your turn to play.");
        return;
    }

    try {
        String[] parts = message.split(" ");
        int cardInd = Integer.parseInt(parts[1]); // Index of the card to play
        Card playedCard = player.playCard(cardInd);    

        if (playedCard != null) {
            broadcast(player.getName() + " played " + playedCard.getName(), this);

            Player targetPlayer = null;
            String guessedCard = "";

            switch (playedCard.getName()) {
                case "Guard":
                    out.println("Choose a player to target with Guard effect:");
                    String guardTargetName = in.readLine();
                    targetPlayer = game.getPlayerByName(guardTargetName);
                    out.println("Guess a card (1.Guard 2.Priest 3.Baron 4.Handmaid 5.Prince 6.King 7.Countess 8.Princess):");
                    guessedCard = in.readLine();
                    break;

                case "Prince":
                    out.println("Choose a player to target with Prince effect:");
                    String princeTargetName = in.readLine();
                    targetPlayer = game.getPlayerByName(princeTargetName);
                    break;

                case "King":
                    out.println("Choose a player to target with King effect:");
                    String kingTargetName = in.readLine();
                    targetPlayer = game.getPlayerByName(kingTargetName);
                    break;

                case "Priest":
                    out.println("Choose a player to target with Priest effect:");
                    String priestTargetName = in.readLine();
                    targetPlayer = game.getPlayerByName(priestTargetName);
                    break;

                case "Baron":
                    out.println("Choose a player to target with Baron effect:");
                    String baronTargetName = in.readLine();
                    targetPlayer = game.getPlayerByName(baronTargetName);
                    break;

                case "Handmaid":
                case "Countess":
                case "Princess":
                    break;
            }

            playedCard.applyEffect(game, Server.this, game.getCurrentPlayer(), targetPlayer, guessedCard);

            if (game.isRoundOver()) {
                Player roundWinner = game.getCurrentPlayer(); 
                endRound(roundWinner); // End the round and award tokens
            } else {
                game.nextTurn();
                broadcast("It's now " + game.getCurrentPlayer().getName() + "'s turn.", null);
            }
        }
    } catch (NumberFormatException e) {
        out.println("Invalid card index.");
    } catch (IOException e) {
        out.println("Error processing additional input.");
    }
}

        private void startGame() {
            game.startGame();
            System.out.println("The game has started!");
            System.out.println("It's "+ game.getCurrentPlayer().getName() + "'s turn.");

        }
    public void guardEffect(Game game, Player currentPlayer, Player targetPlayer, String guessedCard) {
        if (targetPlayer == null || targetPlayer.isProtected()){
            System.out.println("Player is protected or invalid");
            return;
        }

        Card targetCard = targetPlayer.getHand().get(0);
        if(targetCard.getName().equalsIgnoreCase(guessedCard)){
            System.out.println("Your Guess is correct!" + targetPlayer + "is eliminated");
            game.removePlayer(targetPlayer);
        } else {
            System.out.println("Wrong Guess!");
        }
    }

    public void priestEffect(Player targetPlayer) {
        if (targetPlayer == null || targetPlayer.isProtected()){
            System.out.println("Player is Protected!");
            return;
        }else {
            System.out.println(targetPlayer + "'s Hand :" + targetPlayer.getHand().get(0));
        }

    }

    public void baronEffect(Game game, Player currentPlayer, Player targetPlayer) {
        if(targetPlayer == null || targetPlayer.isProtected()){
            System.out.println("Player is Protected!");
            return;
        }

        Card currentPlayersCard = currentPlayer.getHand().get(0);
        Card targetPlayersCard  = targetPlayer.getHand().get(0);

        if(currentPlayersCard.getValue() > targetPlayersCard.getValue()){
            System.out.println(targetPlayer + "lost!");
            game.removePlayer(targetPlayer);
        }else if(currentPlayersCard.getValue() < targetPlayersCard.getValue()){
            System.out.println(currentPlayer + "lost!");
            game.removePlayer(currentPlayer);
        }else{
            System.out.println("Tie!");
        }
    }

    public void princeEffect(Game game, Player targetPlayer) {
    if (targetPlayer == null || targetPlayer.isProtected()) {
        System.out.println("Player is protected or invalid.");
        return;
    }

    Card discardedCard = targetPlayer.getHand().remove(0);
    System.out.println(targetPlayer.getName() + " discarded " + discardedCard.getName());

    if (discardedCard.getName().equals("Princess")) {
        System.out.println(targetPlayer.getName() + " is eliminated for discarding the Princess.");
        game.removePlayer(targetPlayer);
    } else {
        targetPlayer.drawCard(game.getDeck());
    }
}


    public void kingEffect(Player currentPlayer, Player targetPlayer) {
        if(targetPlayer == null || targetPlayer.isProtected()){
            System.out.println("Player is Protected!");
            return;
        }

        Card currentPlCard = currentPlayer.getHand().get(0);
        Card targetPlCard  = targetPlayer.getHand().get(0);

        currentPlayer.getHand().add(targetPlCard);
        targetPlayer.getHand().add(currentPlCard);

        System.out.println(currentPlayer + "swapped hands with " + targetPlayer);
    }

    }

public static void main(String[] args){
        Server server = new Server(); 
        server.run();
    }

}