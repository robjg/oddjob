package org.oddjob.beanbus.example;

import java.util.function.UnaryOperator;

public class DoublePrice implements UnaryOperator<Fruit> {

    @Override
    public Fruit apply(Fruit fruit) {
        fruit.setPrice(fruit.getPrice() * 2);
        return fruit;
    }
}
