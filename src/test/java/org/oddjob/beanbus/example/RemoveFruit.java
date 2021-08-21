package org.oddjob.beanbus.example;

import java.util.function.Predicate;

public class RemoveFruit implements Predicate<Fruit> {

    private String remove;

    @Override
    public boolean test(Fruit fruit) {
        return !remove.equals(fruit.getType());
    }

    public String getRemove() {
        return remove;
    }

    public void setRemove(String remove) {
        this.remove = remove;
    }
}
