
import java.util.ArrayList;
import java.util.List;

public class Player{
    private String name;
    private List<Card> hand;
    private boolean isProtected;

    public Player(String name){
        this.name = name;
        this.hand = new ArrayList<>();
        this.isProtected = false; 
    }

    public String getName(){
        return name;
    }

    public List<Card> getHand(){
        return hand;
    }

    public void drawCard(Deck deck){
        Card drawnCard = deck.draw();
        if (drawnCard != null){
            hand.add(drawnCard);
        }
    }

    public Card playCard(int Index){
        if (Index >= 0 && Index < hand.size()){
            return hand.remove(Index);
        }else {
            System.out.println("invalid Index");
            return null;
        }
    }

    public String showHand(){
        StringBuilder handDetails = new StringBuilder();
        for(int i =0; i < hand.size(); i++){
            Card card = hand.get(i);

            handDetails.append(i);

            handDetails.append(card.toString());
        }
        return handDetails.toString();
    }

    void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public boolean isProtected(){
        return this.isProtected;
    }
}