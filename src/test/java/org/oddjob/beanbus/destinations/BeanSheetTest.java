package org.oddjob.beanbus.destinations;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.BeanView;
import org.oddjob.arooa.reflect.BeanViews;
import org.oddjob.arooa.standard.StandardArooaSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BeanSheetTest extends OjTestCase {

    public static class Fruit {

        private String type;

        private String variety;

        private String colour;

        private double size;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getVariety() {
            return variety;
        }

        public void setVariety(String variety) {
            this.variety = variety;
        }

        public String getColour() {
            return colour;
        }

        public void setColour(String colour) {
            this.colour = colour;
        }

        public double getSize() {
            return size;
        }

        public void setSize(double size) {
            this.size = size;
        }
    }

    private static class OurViews implements BeanViews {

        @Override
        public BeanView beanViewFor(ArooaClass arooaClass) {
            return new BeanView() {
                @Override
                public String titleFor(String property) {
                    if ("colour".equals(property)) {
                        return "The Colour";
                    }
                    return property;
                }

                @Override
                public String[] getProperties() {
                    return new String[]{
                            "colour", "size", "type", "variety"
                    };
                }
            };
        }
    }


    String EOL = System.getProperty("line.separator");

    private Fruit[] createFruit() {

        Fruit fruit1 = new Fruit();
        fruit1.setType("Apple");
        fruit1.setVariety("Cox");
        fruit1.setColour("Red and Green");
        fruit1.setSize(7.6);

        Fruit fruit2 = new Fruit();
        fruit2.setType("Orange");
        fruit2.setVariety("Jaffa");
        fruit2.setColour("Orange");
        fruit2.setSize(9.245);

        return new Fruit[]{fruit1, fruit2};
    }

    @Test
    public void testFruitReport() throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Fruit[] values = createFruit();

        BeanSheet test = new BeanSheet();
        test.setOutput(out);
        test.setArooaSession(new StandardArooaSession());
        test.setBeanViews(new OurViews());

        test.run();

        for (Fruit bean : values) {
            test.accept(bean);
        }

        test.flush();

        for (Fruit bean : values) {
            test.accept(bean);
        }

        test.flush();
        test.close();

        String expected =
                "The Colour     size   type    variety" + EOL +
                        "-------------  -----  ------  -------" + EOL +
                        "Red and Green  7.6    Apple   Cox" + EOL +
                        "Orange         9.245  Orange  Jaffa" + EOL +
                        "The Colour     size   type    variety" + EOL +
                        "-------------  -----  ------  -------" + EOL +
                        "Red and Green  7.6    Apple   Cox" + EOL +
                        "Orange         9.245  Orange  Jaffa" + EOL;

        assertEquals(expected, out.toString());
    }

    @Test
    public void testNoHeaders() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Object[] values = createFruit();

        BeanSheet test = new BeanSheet();
        test.setOutput(out);
        test.setNoHeaders(true);
        test.setArooaSession(new StandardArooaSession());
        test.setBeanViews(new OurViews());

        test.writeBeans(Arrays.asList(values));

        String expected =
                "Red and Green  7.6    Apple   Cox" + EOL +
                        "Orange         9.245  Orange  Jaffa" + EOL;

        assertEquals(expected, out.toString());
    }

}
