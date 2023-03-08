package org.oddjob.framework.util;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.OurDirs;
import org.oddjob.util.Restore;
import org.oddjob.util.URLClassLoaderTypeTest;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

public class ContextClassLoadersTest extends OjTestCase {

    @Test
    public void testSimple() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, NoSuchMethodException, InvocationTargetException {

        OurDirs dirs = new OurDirs();

        ClassLoader existing = Thread.currentThread().getContextClassLoader();

        File check = dirs.relative("test/classloader/AJob.class");
        if (!check.exists()) {
            URLClassLoaderTypeTest.compileSample(dirs.base());
        }

        URLClassLoader classLoader = new URLClassLoader(new URL[]{
                dirs.relative("test/classloader").toURI().toURL()},
                this.getClass().getClassLoader());

        Object comp = Class.forName("AJob", true, classLoader).getConstructor().newInstance();

        try (Restore ignored = ContextClassloaders.push(comp)) {

            assertEquals(classLoader,
                    Thread.currentThread().getContextClassLoader());

        }
        assertEquals(existing, Thread.currentThread().getContextClassLoader());
    }
}
