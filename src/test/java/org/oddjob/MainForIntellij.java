package org.oddjob;

/**
 * Eclipse puts test classes on the classpath, Intellij doesn't. The allows us to run Oddjob with the test
 * classpath from intellij.
 * <p>
 * When running loading oj-assembly into Intellij and using this main, remember to change the working directory.
 */
public class MainForIntellij {

    public static void main(String... args) throws Exception {
        org.oddjob.Main.main(args);
    }
}
