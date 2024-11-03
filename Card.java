public class Card {
    private String name;
    private int value;
    private String effect;

    public Card(String name, int value, String effect) {
        this.name = name;
        this.value = value;
        this.effect = effect;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public String effect() {
        return effect;
    }

    public String toString() {
        return name + " " + value + " " + effect;
    }

    public void applyEffect(Game game, Server server, Player currentPlayer, Player targetPlayer, String guessedCard) {
        switch (name) {
            case "Guard":
                guardEffect(game, server, currentPlayer, targetPlayer, guessedCard);
                break;
            case "Priest":
                priestEffect(server, targetPlayer);
                break;
            case "Baron":
                baronEffect(game, server, currentPlayer, targetPlayer);
                break;
            case "Handmaid":
                currentPlayer.setProtected(true);
                server.broadcast(currentPlayer.getName() + " is protected until the next turn.", null);
                break;
            case "Prince":
                princeEffect(game, server, targetPlayer);
                break;
            case "King":
                kingEffect(server, currentPlayer, targetPlayer);
                break;
            case "Countess":
                server.broadcast("Countess was discarded without effect!", null);
                break;
            case "Princess":
                server.broadcast(currentPlayer.getName() + " discarded the Princess and is eliminated!", null);
                game.removePlayer(currentPlayer); 
                break;
        }
    }

    private void guardEffect(Game game, Server server, Player currentPlayer, Player targetPlayer, String guessedCard) {
        if (targetPlayer == null || targetPlayer.isProtected()) {
            server.broadcast("Player is protected or invalid for Guard effect", null);
            return;
        }

        Card targetCard = targetPlayer.getHand().get(0);
        if (targetCard.getName().equalsIgnoreCase(guessedCard)) {
            server.broadcast("Correct Guess! " + targetPlayer.getName() + " is eliminated.", null);
            game.removePlayer(targetPlayer);
        } else {
            server.broadcast("Wrong Guess!", null);
        }
    }

    private void priestEffect(Server server, Player targetPlayer) {
        if (targetPlayer == null || targetPlayer.isProtected()) {
            server.broadcast("Player is protected, Priest effect has no effect.", null);
            return;
        }
        server.broadcast(targetPlayer.getName() + "'s hand is: " + targetPlayer.showHand(), null);
    }

    private void baronEffect(Game game, Server server, Player currentPlayer, Player targetPlayer) {
        if (targetPlayer == null || targetPlayer.isProtected()) {
            server.broadcast("Player is protected, Baron effect has no effect.", null);
            return;
        }

        Card currentPlayersCard = currentPlayer.getHand().get(0);
        Card targetPlayersCard = targetPlayer.getHand().get(0);

        if (currentPlayersCard.getValue() > targetPlayersCard.getValue()) {
            server.broadcast(targetPlayer.getName() + " lost and is eliminated by Baron.", null);
            game.removePlayer(targetPlayer);
        } else if (currentPlayersCard.getValue() < targetPlayersCard.getValue()) {
            server.broadcast(currentPlayer.getName() + " lost and is eliminated by Baron.", null);
            game.removePlayer(currentPlayer);
        } else {
            server.broadcast("It's a tie! No one is eliminated.", null);
        }
    }

    private void princeEffect(Game game, Server server, Player targetPlayer) {
        if (targetPlayer == null || targetPlayer.isProtected()) {
            server.broadcast("Player is protected or invalid, Prince effect has no effect.", null);
            return;
        }

        Card discardedCard = targetPlayer.getHand().remove(0);
        server.broadcast(targetPlayer.getName() + " discarded " + discardedCard.getName(), null);

        if (discardedCard.getName().equals("Princess")) {
            server.broadcast(targetPlayer.getName() + " is eliminated for discarding the Princess.", null);
            game.removePlayer(targetPlayer);
        } else {
            targetPlayer.drawCard(game.getDeck());
        }
    }

    private void kingEffect(Server server, Player currentPlayer, Player targetPlayer) {
        if (targetPlayer == null || targetPlayer.isProtected()) {
            server.broadcast("Player is protected or invalid, King effect has no effect.", null);
            return;
        }

        Card currentPlCard = currentPlayer.getHand().get(0);
        Card targetPlCard = targetPlayer.getHand().get(0);

        currentPlayer.getHand().add(targetPlCard);
        targetPlayer.getHand().add(currentPlCard);

        server.broadcast(currentPlayer.getName() + " swapped hands with " + targetPlayer.getName(), null);
    }
}
