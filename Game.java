
import java.util.ArrayList;
import java.util.List;

public class Game{
    private List<Player> players;
    private Deck deck;
    private int currentPlayerIndex;
    private boolean gameStarted;
    private int tokensToWin;

    public Game(){
        this.players = new ArrayList<>();
        this.deck = new Deck();
        this.currentPlayerIndex = 0;
        this.gameStarted = false;
    }

    public void startGame() {
        gameStarted = true;
        deck.Shuffle();
        int playerCount = players.size();
        if (playerCount == 2) tokensToWin = 7;
        else if (playerCount == 3) tokensToWin = 5;
        else if (playerCount == 4) tokensToWin = 4;

        //card dealing
        for (Player player : players){
            player.drawCard(deck);
        }

        System.out.println("Game started! It's " + getCurrentPlayer().getName() + "'s turn.");
    }

    public Player getCurrentPlayer(){
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1 )% players.size();
        System.out.println("It's now " + getCurrentPlayer().getName() + "'s turn.");
    }

    public boolean isDeckEmpty() {
        return deck.draw() == null;
    }

    public void determineWinner() {
        if (players.size() == 1) {
            Player winner = players.get(0);
            System.out.println("The game is over! " + winner.getName() + " wins!");
            gameStarted = false; // Reset game status
        }
    }

    public void addPlayer(Player player) {
            players.add(player);
    }

    void removePlayer(Player currentPlayer) {
        players.remove(currentPlayer);
        System.out.println(currentPlayer.getName() + " has been knocked out.");
    }

    public void Winner(){
        if (players.size()==1){
            Player winner = players.get(0);
            System.out.println(winner + " wins!");
            gameStarted = false;
        } else if(isDeckEmpty()){
            Player winner = null;
            int lowestVal = 0;
            for(Player player : players){
                int CardVal = player.getHand().get(0).getValue();
                if (CardVal >= lowestVal){
                    lowestVal = CardVal;
                    winner = player;
                }
            }
            System.out.println("Winner is : " + winner);
        }
    }

    public void determineRoundWinner(Player winner) {
        if (winner != null) {
            winner.addToken();
            System.out.println(winner.getName() + " won the round and now has " + winner.getTokens() + " tokens!");
            if (winner.getTokens() >= tokensToWin) {
                System.out.println(winner.getName() + " wins the game with " + winner.getTokens() + " tokens!");
                gameStarted = false;
            }
        }
    }

    public Deck getDeck(){
        return deck;
    }

    public boolean isGameStarted(){
        return gameStarted;
    }

    public boolean isRoundOver() {
        return players.size() == 1;
    }

    public Player getRoundWinner() {
        return players.size() == 1 ? players.get(0) : null;
    }

    public Player getPlayerByName(String name) {
    for (Player player : players) {
        if (player.getName().equalsIgnoreCase(name)) {
            return player;
        }
    }
    return null;
}

}