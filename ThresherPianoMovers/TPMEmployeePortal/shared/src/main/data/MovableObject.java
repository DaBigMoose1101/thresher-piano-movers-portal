package main.data;

public class MovableObject {
    private Type type;
    public enum Type{
        smallUpright,
        fullUpright,
        babyGrand,
        parlorGrand,
        semiConcertGrand,
        concertGrand
    }

    public MovableObject(Type type){
        this.type = type;
    }
}
