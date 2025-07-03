package main.records;

import main.data.MovableObject;

public record  StorageUpdater(int unit, Type type, MovableObject object) {
    enum Type {
        add,
        remove,
        move
    }
}
