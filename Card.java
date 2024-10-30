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

    public void applyEffect(Game game, Player currentPlayer, Player targetPlayer) {
        switch (name) {
            case "Guard":
                // Logic for Guard effect
                break;
            case "Priest":
                // Logic for Priest effect
                break;
            case "Baron":
                // Logic for Baron effect
                break;
            case "Handmaid":
                currentPlayer.setProtected(true);
                break;
            case "Prince":
                // Logic for Prince effect
                break;
            case "King":
                // Logic for King effect
                break;
            case "Countess":
                // No direct effect but used as part of game logic
                break;
            case "Princess":
                game.removePlayer(currentPlayer); 
                break;
        }

}
}