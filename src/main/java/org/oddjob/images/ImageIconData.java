package org.oddjob.images;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.Serializable;

/**
 * Used for serialisation for of Icons between client and server. Replaces serialisation in
 * {@link ImageIconStable} stable because this is easier to serialise via Json for
 * the web.
 */
public class ImageIconData implements Serializable {

    private static final long serialVersionUID = 2020071300L;

    private final int width;

    private final int height;

    private final int[] pixels;

    private final String description;

    public ImageIconData(int width, int height, int[] pixels,
                         String description) {
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.description = description;
    }

    public static ImageIconData fromImageIcon(ImageIcon imageIcon) throws IOException {

        int w = imageIcon.getIconWidth();
        int h = imageIcon.getIconHeight();

        Image image = imageIcon.getImage();

        if (image == null) {
            throw new IllegalStateException(
                    "Image should be loaded already!");
        }

        int[] pixels = new int[w * h];

        try {
            PixelGrabber pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);
            pg.grabPixels();
            if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
                throw new IOException("failed to load image contents.");
            }
        }
        catch (InterruptedException e) {
            throw new IOException("Image load interrupted.");
        }

        String description = imageIcon.getDescription();

        return new ImageIconData(w, h, pixels, description);
    }

    public ImageIcon toImageIcon() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        ColorModel cm = ColorModel.getRGBdefault();
        Image image = tk.createImage(new MemoryImageSource(
                width, height, cm, pixels, 0, width));
        return new ImageIconStable(image, description);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPixels() {
        return pixels;
    }

    public String getDescription() {
        return description;
    }
}
