package main.data;

public record  StorageUpdater(int unit, Type type, MovableObject object) {
    enum Type {
        add,
        remove,
        move
    }
}
