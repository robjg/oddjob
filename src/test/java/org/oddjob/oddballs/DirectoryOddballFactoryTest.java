package org.oddjob.oddballs;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.oddjob.OurDirs;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ElementMappings;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;

import java.io.File;
import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class DirectoryOddballFactoryTest {

    @BeforeAll
    public static void setUp() throws Exception {

        new BuildOddballs().run();
    }

    @Test
    void createFromDirectory() throws Exception {

        OurDirs dirs = new OurDirs();

        Oddball result = DirectoryOddballFactory
                .from(new File(dirs.base(), "test/oddballs/apple"))
                .createFrom(getClass().getClassLoader());

        ArooaDescriptor descriptor = result.getArooaDescriptor();

        assertThat(descriptor, notNullValue());

        InstantiationContext instantiationContext =
                new InstantiationContext(ArooaType.COMPONENT, null);

        ElementMappings mappings = descriptor.getElementMappings();
        assertThat(mappings, notNullValue());

        ArooaClass appleClass =
                mappings.mappingFor(
                        new ArooaElement(new URI("http://rgordon.co.uk/fruit"),
                                "apple"), instantiationContext);

        assertThat((appleClass).forClass().getName(), is("fruit.Apple"));

        ClassLoader loader = result.getClassLoader();

        assertThat(loader.loadClass("fruit.Apple"), notNullValue());
    }

    @Test
    void createFromFiles() throws Exception {

        OurDirs dirs = new OurDirs();

        Oddball result = DirectoryOddballFactory
                .from(new File(dirs.base(), "test/oddballs/apple"))
                .createFrom(getClass().getClassLoader());

        ArooaDescriptor descriptor = result.getArooaDescriptor();

        assertThat(descriptor, notNullValue());

        InstantiationContext instantiationContext =
                new InstantiationContext(ArooaType.COMPONENT, null);

        ElementMappings mappings = descriptor.getElementMappings();
        assertThat(mappings, notNullValue());

        ArooaClass appleClass =
                mappings.mappingFor(
                        new ArooaElement(new URI("http://rgordon.co.uk/fruit"),
                                "apple"), instantiationContext);

        assertThat((appleClass).forClass().getName(), is("fruit.Apple"));

        ClassLoader loader = result.getClassLoader();

        assertThat(loader.loadClass("fruit.Apple"), notNullValue());
    }
}
