/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.framework.adapt.beanutil;

import org.apache.commons.beanutils.DynaProperty;
import org.junit.jupiter.api.Test;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class WrapDynaClassTest {

    public static class MyBean {
        public String getSimple() {
            return null;
        }

        public String getMapped(String foo) {
            return null;
        }

        public String getIndexed(int i) {
            return null;
        }

        public boolean isOk() {
            return true;
        }
    }

    @Test
    public void testProperties() {

        WrapDynaClass test = WrapDynaClass.createDynaClass(MyBean.class, new BeanUtilsPropertyAccessor());

        DynaProperty result;

        result = test.getDynaProperty("simple");
        assertThat("simple", result, notNullValue());
        assertThat("simple class", result.getType(), is(String.class));
        assertThat("is indexed", result.isIndexed(), is(false));
        assertThat("is mapped", result.isMapped(), is(false));

        result = test.getDynaProperty("indexed");
        assertThat("indexed", result, notNullValue());
        assertThat("is indexed", result.isIndexed(), is(true));
        assertThat("is mapped", result.isMapped(), is(false));
        assertThat("class", result.getContentType(), is(String.class));

        result = test.getDynaProperty("mapped");
        assertThat("mapped", result, notNullValue());
        assertThat("is indexed", result.isIndexed(), is(false));
        assertThat("is mapped", result.isMapped(), is(true));
        assertThat("class", result.getContentType(), is(String.class));

        result = test.getDynaProperty("ok");
        assertThat("boolean", result, notNullValue());
        assertThat("class", result.getType(), is(boolean.class));

    }

    public static class MixedTypes {
        public String getStuff(String key) {
            return "Stuff";
        }

        public void setStuff(String key) {
        }
    }

    @Test
    public void testMixedTypes() {
         WrapDynaClass test = WrapDynaClass.createDynaClass(MixedTypes.class, new BeanUtilsPropertyAccessor());

        DynaProperty result;

        result = test.getDynaProperty("stuff");
        assertThat(result, notNullValue());
        assertThat(result.isIndexed(), is(false));
        assertThat(result.isMapped(), is(true));
        assertThat(result.getType(), is(Map.class));
        assertThat(result.getContentType(), is(String.class));
    }

    @Test
    public void testSerialize() throws Exception {
        WrapDynaClass test = WrapDynaClass.createDynaClass(MyBean.class, new BeanUtilsPropertyAccessor());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bytes);
        oos.writeObject(test);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()));

        WrapDynaClass clone = (WrapDynaClass) ois.readObject();

        Map<String, Class<?>> props =
                new HashMap<>();


        DynaProperty[] dps = clone.getDynaProperties();
        for (DynaProperty dp : dps) {
            props.put(dp.getName(), dp.getType());
        }

        assertEquals(test.getDynaProperties().length, clone.getDynaProperties().length);
        assertThat(props.get("simple"), is(String.class));
    }
}
