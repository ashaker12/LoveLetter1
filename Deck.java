
import java.util.Collections;
import java.util.LinkedList;

public class Deck{
    private LinkedList<Card> cards = new LinkedList<>();
    
    //create Deck with cards
    public Deck(){
    for( int i=0; i < 5; i++){
        cards.add(new Card("Guard", 1, "choose a player and name a number (other than 1). If that player has that number in their hand, that player is knocked out of the round. If all other players still in the round cannot be chosen (eg. due to Handmaid or Sycophant), this card is discarded without effect."));
    }
    for(int i = 0; i < 2; i++){
        cards.add(new Card("Priest", 2, "You can look at another player’s hand. Do not reveal the hand to any other players. "));
        cards.add(new card("Baron", 3, "Choose another player still in the round. You and that player secretly compare your hands. The player with the lower number is knocked out of the round. In case of a tie, nothing happens."));
        cards.add(new card("Handmaid", 4, "you are immune to the effects of other players’ cards until the start of your next turn. If all players other than the player whose turn it is are protected by the Handmaid, the player must choose him or herself for a card’s effects, if possible."));
        cards.add(new card("Prince", 5, " choose one player still in the round (including yourself). That player discards his or her hand (but doesn’t apply its effect, unless it is the Princess, see page 8) and draws a new one. If the deck is empty and the player cannot draw a card, that player draws the card that was removed at the start of the round. If all other players are protected by the Handmaid, you must choose yourself."));
    }
    cards.add(new Card("King", 6,"trade the card in your hand with the card held by another player of your choice. You cannot trade with a player who is out of the round."));
    cards.add(new Card("Countess", 7,"If you ever have the Countess and either the King or Prince in your hand, you must discard the Countess. You do not have to reveal the other card in your hand. Of course, you can also discard the Countess even if you do not have a royal family member in your hand. The Countess likes to play mind games...."));
    cards.add(new Card("Princess", 8," You are immediately knocked out of the round. If the Princess was discarded by a card effect, any remaining effects of that card do not apply (you do not draw a card from the Prince, for example)."));
    
    Shuffle();
    }
    
    public void Shuffle(){
        Collections.shuffle(cards);
    }

    public Card draw(){
        if (cards.isEmpty){
            return null;
        } else {
            return cards.pop();
        }
    }
}