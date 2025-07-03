package main.data;

public class MovableObject {
    private Type type;
    public enum Type{
        smallUpright,
        fullUpright,
        babyGrand,
        parlorGrand,
        semiConcertGrand,
        concertGrand,
        smallSafe,
        medSafe,
        lrgSafe,
        xlSafe
    }

    public MovableObject(Type type){
        this.type = type;
    }
}
