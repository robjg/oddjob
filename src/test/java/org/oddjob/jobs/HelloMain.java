package org.oddjob.jobs;

import java.util.Arrays;
import java.util.stream.Collectors;

public class HelloMain {

    public static void main(String[] args) {

        String greeting = System.getProperty("our.greeting");

        String people = Arrays.stream(args).collect(Collectors.joining(" and "));

        System.out.println(greeting + " " + people);
    }


}
