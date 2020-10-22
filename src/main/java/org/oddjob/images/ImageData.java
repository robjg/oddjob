package org.oddjob.images;

import org.oddjob.arooa.utils.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;

/**
 * Used for serialisation for of Icons between client and server. Replaces serialisation in
 * {@link ImageIconStable} stable because this is easier to serialise via Json for
 * the web.
 */
public class ImageData implements Serializable {

    private static final long serialVersionUID = 2020102100L;

    private final byte[] bytes;

    private final String mimeType;

    private final String description;

    public ImageData(byte[] bytes, String mimeType, String description) {
        this.bytes = bytes;
        this.mimeType = mimeType;
        this.description = description;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getDescription() {
        return description;
    }

    public static ImageData fromUrl(URL url, String description) throws IOException {

        URLConnection connection = url.openConnection();
        String mimeType = connection.getContentType();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IoUtils.copy(connection.getInputStream(), outputStream);
        return new ImageData(outputStream.toByteArray(), mimeType, description);
    }
}
