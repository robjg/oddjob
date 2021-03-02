package org.oddjob.util;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.Mockito;
import org.oddjob.OurDirs;
import org.oddjob.Structural;
import org.oddjob.oddballs.BuildOddballs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClassLoaderSorterTest {

    private static final Logger logger = LoggerFactory.getLogger(ClassLoaderSorterTest.class);

	@Rule
	public TestName name = new TestName();

	public String getName() {
		return name.getMethodName();
	}

    @Before
    public void setUp() throws Exception {

        logger.info("----------------------  " + getName() + "  -------------------------");
    }

    @Test
    public void testUnderstandingOfCreatingProxyWithWrongClassLoader() throws MalformedURLException, ClassNotFoundException, SecurityException, IllegalArgumentException {

        logger.info("System class loader is: " + ClassLoader.getSystemClassLoader());

        new BuildOddballs().run();

        OurDirs dirs = new OurDirs();

        URLClassLoader specialLoader = new URLClassLoader(
                new URL[]{new File(dirs.base() +
                        "/test/oddballs/apple/classes").toURI().toURL()},
                getClass().getClassLoader());

        Class<?> fruitClass = Class.forName("fruit.Fruit", true, specialLoader);

        Class<?>[] interfaces = {fruitClass, Structural.class};

        try {
            Proxy.newProxyInstance(Structural.class.getClassLoader(),
                    interfaces,
                    Mockito.mock(InvocationHandler.class));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), Matchers.containsString("not visible from class loader"));
        }

        Object proxy = Proxy.newProxyInstance(specialLoader,
                interfaces,
                Mockito.mock(InvocationHandler.class));

        assertThat(fruitClass.isInstance(proxy), is(true));

    }

    @Test
    public void testWhenTwoClassesUsedThenHighestClassLoaderIsUsed() throws MalformedURLException, ClassNotFoundException, SecurityException, IllegalArgumentException {

        new BuildOddballs().run();

        OurDirs dirs = new OurDirs();

        URLClassLoader specialLoader = new URLClassLoader(
                new URL[]{new File(dirs.base() +
                        "/test/oddballs/apple/classes").toURI().toURL()},
                getClass().getClassLoader());

        Class<?> fruitClass = Class.forName("fruit.Fruit", true, specialLoader);

        Class<?>[] interfaces = {List.class, fruitClass, Structural.class};

        ClassLoader test = new ClassLoaderSorter().getTopLoader(
                interfaces);

        Class<?> result = Class.forName("fruit.Fruit", true, test);

        assertThat(result.getClassLoader(), is(specialLoader));

        Object proxy = Proxy.newProxyInstance(test,
                interfaces,
                Mockito.mock(InvocationHandler.class));

        assertThat(fruitClass.isInstance(proxy), is(true));
    }

    @Test
    public void testWhenSingleStructuralClassProxyRequiredCorrectClassLoaderIsGiven() {

        ClassLoader test = new ClassLoaderSorter().getTopLoader(
                new Class<?>[]{Structural.class});

        Object proxy = Proxy.newProxyInstance(test,
                new Class<?>[]{Structural.class},
                Mockito.mock(InvocationHandler.class));

        assertThat(proxy instanceof Structural, is(true));
    }
}
