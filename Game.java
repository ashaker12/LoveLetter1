
import java.util.List;

public class Game{
    private List<Players> Player;
    private Deck deck;
    private int currentPlayerIndex;
    private boolean gameStarted;

    public game(){
        this.players = new ArrayList<>();
        this.Deck = new Deck();
        this.currentPlayerIndex = 0;
        this.gameStarted = false;
    }

    public void startGame() {
        
    }

    public Deck getDeck() {
        return deck;
    }
}