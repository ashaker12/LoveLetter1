
import java.util.List;

public class Game{
    private List<Player> players;
    private Deck deck;
    private int currentPlayerIndex;
    private boolean gameStarted;

    public Game(){
        this.players = new ArrayList<>();
        this.Deck = new Deck();
        this.currentPlayerIndex = 0;
        this.gameStarted = false;
    }

    public void startGame() {
        gameStarted = true;
        deck.shuffle();
        for (Player player : players){
            player.drawCard(deck);
        }
    }

    public Player getCurrentPlayer(){
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        currentPlayerIndex = currentPlayerIndex + 1;
    }
}