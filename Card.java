
import java.util.logging.Handler;

public class Card{
    private String name;
    private int value;
    private String effect;

    public Card(String name, int value, String effect){
        this.name = name;
        this.value = value;
        this.effect =  effect;
    }

    public String getName(){
        return name;
    }

    public int getValue(){
        return value;
    }

    public String effect(){
        return effect;
    }

    public String toString(){
        return name + " " + value + " " + effect;
    }

    public void applyEffect(Game game, Player currentPlayer, Player targetPlayer, String guessedCard) {
        switch (name) {
            case "Guard":
                guardEffect(game, currentPlayer, targetPlayer, guessedCard);
                break;
            case "Priest":
                priestEffect(targetPlayer);
                break;
            case "Baron":
                baronEffect(game, currentPlayer, targetPlayer);
                break;
            case "Handmaid":
                currentPlayer.setProtected(true);
                System.out.println(currentPlayer + " is protected until the next turn.");
                break;
            case "Prince":
                princeEffect(game, targetPlayer);
                break;
            case "King":
                kingEffect(currentPlayer, targetPlayer);
                break;
            case "Countess":
                System.out.println("Countess was discarded!");
                break;
            case "Princess":
                System.out.println(currentPlayer + "is eliminated");
                game.removePlayer(currentPlayer); 
                break;
        }
    }

    private void guardEffect(Game game, Player currentPlayer, Player targetPlayer, String guessedCard) {
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

    private void priestEffect(Player targetPlayer) {
        if (targetPlayer == null || targetPlayer.isProtected()){
            System.out.println("Player is Protected!");
            return;
        }else {
            System.out.println(targetPlayer + "'s Hand :" + targetPlayer.getHand().get(0));
        }

    }

    private void baronEffect(Game game, Player currentPlayer, Player targetPlayer) {
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

    private void princeEffect(Game game, Player targetPlayer) {
        if(targetPlayer == null || targetPlayer.isProtected()){
            System.out.println("Player is Protected!");
            return;
        }

        Card discardedCard = targetPlayer.getHand().remove(0);

        if (discardedCard.getName().equals("Princess")){
            System.out.println(targetPlayer + " is removed due to discarding the Princess.");
            game.removePlayer(targetPlayer);
        }else { 
            System.out.println(discardedCard + "was removed from: " + targetPlayer);
            targetPlayer.drawCard(game.getDeck());
        }
    }

    private void kingEffect(Player currentPlayer, Player targetPlayer) {
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