package org.oddjob.beanbus.pipeline;

public class Folds {

    private Folds() {}

    public static  Section<Integer, Integer> maxInt() {

        return Pipes.fold(Integer.MIN_VALUE, (a, x) -> Math.max(a, x));
    }

    public static  Section<Long, Long> maxLong() {

        return Pipes.fold(Long.MIN_VALUE, (a, x) -> Math.max(a, x));
    }

    public static  Section<Double, Double> maxDouble() {

        return Pipes.fold(Double.MIN_VALUE, (a, x) -> Math.max(a, x));
    }


}
