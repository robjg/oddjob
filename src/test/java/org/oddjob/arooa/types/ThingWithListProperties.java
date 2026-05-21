package org.oddjob.arooa.types;

import java.util.List;

public class ThingWithListProperties implements Runnable{

    private List<Integer> ints;

    @Override
    public void run() {
        System.out.println(ints);
    }

    public List<Integer> getInts() {
        return ints;
    }

    public void setInts(List<Integer> ints) {
        this.ints = ints;
    }
}
