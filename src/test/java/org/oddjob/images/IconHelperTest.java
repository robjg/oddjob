package org.oddjob.images;

import org.junit.Test;
import org.oddjob.Iconic;
import org.oddjob.OjTestCase;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;

public class IconHelperTest extends OjTestCase {

    static class OurListener implements IconListener {

        List<IconEvent> events = new ArrayList<>();

        @Override
        public void iconEvent(IconEvent e) {
            events.add(e);
        }
    }

    static class OurIconic implements Iconic {
        @Override
        public void addIconListener(IconListener listener) {
        }

        @Override
        public void removeIconListener(IconListener listener) {
        }

        @Override
        public ImageData iconForId(String id) {
            throw new RuntimeException();
        }
    }

    @Test
    public void testSameIdNotFired() {

        IconHelper test = new IconHelper(new OurIconic(), IconHelper.READY);

        OurListener listener = new OurListener();

        test.addIconListener(listener);

        assertEquals("ready", listener.events.get(0).getIconId());
        assertEquals(1, listener.events.size());

        test.changeIcon("executing");

        assertEquals("executing", listener.events.get(1).getIconId());
        assertEquals(2, listener.events.size());

        test.changeIcon("executing");

        assertEquals("executing", listener.events.get(1).getIconId());
        assertEquals(2, listener.events.size());

    }

    public void testImageIconFromImage() throws IOException {

		URL url = IconHelper.class.getResource("triangle_green.gif");

		ImageData imageData = ImageData.fromUrl(url, "Executing");

		ImageIcon icon = IconHelper.imageIconFrom(imageData);

        ImageIcon imageIcon = new ImageIconStable(
                IconHelper.class.getResource("triangle_green.gif"),
                "Executing");

		assertThat(icon.getImage(), is(imageIcon.getImage()));
	}
}
