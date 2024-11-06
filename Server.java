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
    private ExecutorService threadpool;
    private ArrayList<String> clientNames = new ArrayList<>();
    private final int MIN_PLAYERS = 2;
    private final int MAX_PLAYERS = 4;
    private boolean chatStarted = false;
    private Game game = new Game();

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            threadpool = Executors.newCachedThreadPool();

            while (!done) {
                Socket client = server.accept();

                if (connections.size() >= MAX_PLAYERS) {
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    out.println("Chat is full, please try again later.");
                    client.close();
                    continue;
                }

                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                threadpool.execute(handler);
            }

        } catch (IOException e) {
            quit();
        }
    }

    private void endRound(Player roundWinner) {
        broadcast(roundWinner.getName() + " has won this round and earned a Token of Affection!", null);

        if (!game.isGameStarted()) {
            broadcast("Game Over! " + roundWinner.getName() + " is the overall winner!", null);
            quit();
        } else {
            game.startGame();
            broadcast("A new round has started! It's " + game.getCurrentPlayer().getName() + "'s turn.", null);
        }
    }

    public void sendMessage(String message, ConnectionHandler receiver) {
        receiver.out.println(message);
    }

    public void broadcast(String message, ConnectionHandler sender) {
        for (ConnectionHandler ch : connections) {
            if (ch != null && ch != sender) {
                ch.out.println(message);
            }
        }
    }

    public void quit() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.quit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;
        private Player player;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                System.out.println("Server Started...");
                out.println("Enter your Name:");

                while (true) {
                    name = in.readLine();

                    if (name == null || name.isEmpty()) {
                        out.println("Invalid name. Please choose another.");
                    } else if (clientNames.contains(name)) {
                        out.println("Name already in use. Please choose another.");
                    } else {
                        clientNames.add(name);
                        break;
                    }
                }

                player = new Player(name);
                game.addPlayer(player);
                out.println("Welcome " + name + "!");
                broadcast(name + " joined the room", this);

                synchronized (connections) {
                    if (chatStarted) {
                        out.println("Chat already started!");
                    } else if (connections.size() < MIN_PLAYERS) {
                        out.println("Waiting for more players to join...");
                        connections.wait();
                    }
                    if (!Server.this.chatStarted && connections.size() >= MIN_PLAYERS) {
                        broadcast("You can type 'Start' to begin the chat.", this);
                        out.println("You can type 'Start' to begin the chat.");
                    }
                }

                handleChatStart();
                handleMessages();

            } catch (Exception e) {
                quit();
            }
        }

        public void quit() {
            try {
                in.close();
                out.close();
                synchronized (connections) {
                    clientNames.remove(name);
                    connections.remove(this);
                    connections.notifyAll();
                }
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void handleChatStart() throws IOException, InterruptedException {
            String startMessage = in.readLine();
            if (startMessage != null && startMessage.equalsIgnoreCase("Start")) {
                synchronized (connections) {
                    if (!Server.this.chatStarted) {
                        startGame();
                        Server.this.chatStarted = true;
                        broadcast("The chat has started!", this);
                        connections.notifyAll();
                    }
                }
            }
        }

        public void handleMessages() throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("bye")) {
                    broadcast(name + " left the Chat!", this);
                    quit();
                } else if (message.startsWith("/dm")) {
                    handleDirectMessage(message);
                } else if (message.startsWith("/draw")) {
                    handleDrawCommand();
                } else if (message.startsWith("/play")) {
                    handlePlayCommand(message);
        
             } else {
                    broadcast(name + ": " + message, this);
                }
            }
        }

        public ConnectionHandler connectionByName(String name) {
            for (ConnectionHandler ch : connections) {
                if (ch != null && ch.name.equals(name)) {
                    return ch;
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
            ConnectionHandler recipientHandler = connectionByName(recipientNameString);

            if (recipientHandler != null) {
                recipientHandler.out.println("[DM from " + name + "]: " + dmMessage);
            } else {
                out.println("User " + recipientNameString + " not found.");
            }
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
                int cardInd = Integer.parseInt(parts[1]);
                Card playedCard = player.playCard(cardInd);

                if (playedCard != null) {
                    broadcast(player.getName() + " played " + playedCard.getName(), this);

                    // Handle specific card effects here
                    // ...

                    if (game.isRoundOver()) {
                        Player roundWinner = game.getCurrentPlayer();
                        endRound(roundWinner);
                    } else {
                        game.nextTurn();
                        broadcast("It's now " + game.getCurrentPlayer().getName() + "'s turn.", null);
                    }
                }
            } catch (NumberFormatException e) {
                out.println("Invalid card index.");
            }
        }

        private void startGame() {
            game.startGame();
            broadcast("The game has started!", this);
            broadcast("It's " + game.getCurrentPlayer().getName() + "'s turn.", null);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
