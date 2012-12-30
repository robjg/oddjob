package org.oddjob.images;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Provide stable serialization for ImageIcon. Required because 
 * serialization changed in Java 6 U26, thus causing Oddjob clients
 * and server on version either side of this to fail.
 * 
 * @author rob
 *
 */
public class ImageIconStable extends ImageIcon {

	private static final long serialVersionUID = 2012122400L;
	
	public ImageIconStable(URL location, String description) {
		super(location, description);
	}
	
	public ImageIconStable(Image image, String description) {
		super(image, description);
	}
	
	private void readObject(ObjectInputStream in) {
		throw new IllegalStateException(
				"This should be deserialized by ImageIconData.");
	}
	
	private Object writeReplace() throws IOException {
		
		int w = getIconWidth();
		int h = getIconHeight();
		
		Image image = getImage();
		
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
		
		return new ImageIconData(w, h, pixels, getDescription());
	}
	
	public static class ImageIconData implements Serializable {
		
		private static final long serialVersionUID = 2012122400L;
		
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
		
		private Object readResolve() {
			Toolkit tk = Toolkit.getDefaultToolkit();
			ColorModel cm = ColorModel.getRGBdefault();
			Image image = tk.createImage(new MemoryImageSource(
					width, height, cm, pixels, 0, width));
			return new ImageIconStable(image, description);
		}
	}
}
