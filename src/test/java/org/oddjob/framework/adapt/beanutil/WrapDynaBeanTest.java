/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.framework.adapt.beanutil;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.jupiter.api.Test;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.reflect.BeanOverview;
import org.oddjob.arooa.reflect.PropertyAccessor;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test WrapDynaBean.
 */
public class WrapDynaBeanTest {

    /**
     * Simple Bean fixture.
     */
    public static class SimpleBean {
        private String simple;

        public void setSimple(String simple) {
            this.simple = simple;
        }

        public String getSimple() {
            return simple;
        }
    }

    @Test
    public void testSimple() throws Exception {
        SimpleBean bean = new SimpleBean();

        PropertyAccessor propertyAccessor = new BeanUtilsPropertyAccessor();

        WrapDynaBean wrap = new WrapDynaBean(bean, propertyAccessor);

        BeanOverview beanOverview = propertyAccessor.getClassName(wrap)
                .getBeanOverview(propertyAccessor);

        assertThat(beanOverview.getProperties(), arrayContainingInAnyOrder("class", "simple"));
        assertThat(beanOverview.hasReadableProperty("class"), is(true));
        assertThat(beanOverview.hasReadableProperty("simple"), is(true));
        assertThat(beanOverview.hasWriteableProperty("simple"), is(true));
        assertThat(beanOverview.hasWriteableProperty("foo"), is(false));

        wrap.set("simple", "test");

        assertEquals("test", bean.getSimple());
        assertEquals("test", wrap.get("simple"));

        assertThat(wrap.get("class"), is(SimpleBean.class));

        bean.setSimple(null);

        propertyAccessor.setProperty(wrap, "simple", "test");
        assertEquals("test", propertyAccessor.getProperty(wrap, "simple"));
    }

    public static class MappedBean {
        private final Map<String, Object> map = new HashMap<>();

        public void setMapped(String name, Object value) {
            map.put(name, value);
        }

        public Object getMapped(String name) {
            return map.get(name);
        }
    }

    @Test
    public void testMapped() throws Exception {

        PropertyAccessor propertyAccessor = new BeanUtilsPropertyAccessor();

        MappedBean bean = new MappedBean();

        WrapDynaBean wrap = new WrapDynaBean(bean, propertyAccessor);

        wrap.set("mapped", "simple", "test");

        assertEquals("test", bean.getMapped("simple"));
        assertEquals("test", wrap.get("mapped", "simple"));

        propertyAccessor.setProperty(wrap, "mapped(simple)", "test");

        assertEquals("test", propertyAccessor.getProperty(wrap, "mapped(simple)"));
        assertEquals("test", propertyAccessor.getMappedProperty(wrap, "mapped", "simple"));
    }

    public static class IndexedBean {
        private String[] array = new String[1];

        public void setIndexed(String[] array) {
            this.array = array;
        }

        //		public void setIndexed(int index, String value) {
//			this.array[index] = value;
//		}
        public String[] getIndexed() {
            return array;
        }
//		public String getIndexed(int index) {
//			return array[index];
//		}
    }

    @Test
    public void testIndexed() throws Exception {

        PropertyAccessor propertyAccessor = new BeanUtilsPropertyAccessor();

        IndexedBean bean = new IndexedBean();

        WrapDynaBean wrap = new WrapDynaBean(bean, propertyAccessor);

        wrap.set("indexed", 0, "test");

        assertEquals("test", bean.getIndexed()[0]);
        assertEquals("test", wrap.get("indexed", 0));

        propertyAccessor.setProperty(wrap, "indexed[0]", "test");

        assertEquals("test", propertyAccessor.getProperty(wrap, "indexed[0]"));
        assertEquals("test", propertyAccessor.getIndexedProperty(wrap, "indexed", 0));
    }

    /**
     * InAccessable Mapped Bean fixture.
     */
    public static class InAccessableBean {
        private String simple;

        public void setSimple(String simple) {
            this.simple = simple;
        }

        String getSimple() {
            return simple;
        }
    }

    @Test
    public void testInAccessable() throws Exception {
        InAccessableBean bean = new InAccessableBean();

        WrapDynaBean wrap = new WrapDynaBean(bean, new BeanUtilsPropertyAccessor());

        wrap.set("simple", "test");

        assertEquals("test", bean.getSimple());

        // should behave like no read method and return null.
        assertEquals(null, wrap.get("simple"));

        bean.setSimple(null);

        PropertyUtils.setProperty(wrap, "simple", "test");

        // should behave like no read method and return null.
        assertEquals(null, PropertyUtils.getProperty(wrap, "simple"));
    }

    /**
     * InAccessable Bean fixture.
     */
    public static class InAccessableMappedBean {
        private final Map<String, Object> map = new HashMap<>();

        public void setMapped(String name, Object value) {
            map.put(name, value);
        }

        Object getMapped(String name) {
            return map.get(name);
        }
    }

    @Test
    public void testInAccessableMapped() throws Exception {
        InAccessableMappedBean bean = new InAccessableMappedBean();

        WrapDynaBean wrap = new WrapDynaBean(bean, new BeanUtilsPropertyAccessor());

        wrap.set("mapped", "simple", "test");

        assertEquals("test", bean.getMapped("simple"));

        // should be null as read method not public
        assertEquals(null, wrap.get("mapped", "simple"));

        PropertyUtils.setProperty(wrap, "mapped(simple)", "test");

        // should be null as read method not public
        assertEquals(null, PropertyUtils.getProperty(wrap, "mapped(simple)"));
    }
}
