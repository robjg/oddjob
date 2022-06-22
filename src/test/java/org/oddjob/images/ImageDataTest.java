package org.oddjob.images;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ImageDataTest {

    @Test
    public void testCreateFromUrl() throws IOException {

        URL url = Objects.requireNonNull(
                IconHelper.class.getResource("triangle_green.gif"));

        ImageData imageData = ImageData.fromUrl(url, "Executing");

        assertThat(imageData.getBytes().length, is(84));
        assertThat(imageData.getMimeType(), is("image/gif"));
        assertThat(imageData.getDescription(), is("Executing"));
    }
}