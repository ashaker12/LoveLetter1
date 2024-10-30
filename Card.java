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

}